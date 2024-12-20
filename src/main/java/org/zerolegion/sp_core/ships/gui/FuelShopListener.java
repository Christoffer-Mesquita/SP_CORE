package org.zerolegion.sp_core.ships.gui;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.zerolegion.sp_core.ships.PlayerHangar;
import org.zerolegion.sp_core.ships.PlayerShip;
import org.zerolegion.sp_core.ships.SpaceshipManager;

public class FuelShopListener implements Listener {
    private final SpaceshipManager spaceshipManager;
    private final FuelShopGUI shopGUI;

    public FuelShopListener(SpaceshipManager spaceshipManager, FuelShopGUI shopGUI) {
        this.spaceshipManager = spaceshipManager;
        this.shopGUI = shopGUI;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!event.getView().getTitle().equals(ChatColor.DARK_PURPLE + "✧ Posto de Combustível ✧")) return;

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        // Botão voltar
        if (clickedItem.getType() == Material.BARRIER) {
            player.closeInventory();
            return;
        }

        PlayerHangar hangar = spaceshipManager.getPlayerHangar(player.getUniqueId());
        if (hangar == null || hangar.getShips().isEmpty()) {
            player.sendMessage(ChatColor.RED + "Você não possui nenhuma nave!");
            player.closeInventory();
            return;
        }

        PlayerShip ship = hangar.getShip(0); // Por enquanto, usa a primeira nave
        if (ship == null) {
            player.sendMessage(ChatColor.RED + "Erro ao carregar sua nave!");
            player.closeInventory();
            return;
        }

        String fuelType = null;
        switch (clickedItem.getType()) {
            case SULPHUR:
                fuelType = "basic";
                break;
            case BLAZE_POWDER:
                fuelType = "premium";
                break;
            case GLOWSTONE_DUST:
                fuelType = "experimental";
                break;
        }

        if (fuelType != null) {
            double price = spaceshipManager.getFuelPrice(fuelType);
            double currentFuel = ship.getFuel();
            double amount = event.isShiftClick() ? 100 - currentFuel : 10.0;

            if (amount <= 0) {
                player.sendMessage(ChatColor.RED + "Sua nave já está com o tanque cheio!");
                return;
            }

            // Calcular preço total
            double totalPrice = (price * amount) / 10.0; // price é por 10%

            // Verificar se o jogador tem créditos suficientes
            if (!spaceshipManager.getEconomyManager().removeBalance(player.getUniqueId(), totalPrice)) {
                player.sendMessage(ChatColor.RED + "Você não tem créditos suficientes!");
                return;
            }

            // Adicionar combustível
            ship.addFuel(amount);
            spaceshipManager.savePlayerHangar(player.getUniqueId());

            // Atualizar interface
            shopGUI.openFuelShop(player, ship);

            player.sendMessage(ChatColor.GREEN + "Você abasteceu " + String.format("%.1f%%", amount) + 
                " de combustível por " + spaceshipManager.getEconomyManager().formatValue(totalPrice) + " ⭐");
        }
    }
} 