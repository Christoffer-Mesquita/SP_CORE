package org.zerolegion.sp_core.ships.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.zerolegion.sp_core.ships.PlayerHangar;
import org.zerolegion.sp_core.ships.ShipTemplate;
import org.zerolegion.sp_core.ships.SpaceshipManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class ShipShopGUI {
    private final SpaceshipManager spaceshipManager;
    private final String inventoryTitle = "§8» §dLoja de Naves";
    private final Map<Integer, String> slotToShipId = new HashMap<>();

    public ShipShopGUI(SpaceshipManager spaceshipManager) {
        this.spaceshipManager = spaceshipManager;
    }

    public void openShopMenu(Player player) {
        spaceshipManager.getPlugin().getLogger().info("[DEBUG] Abrindo menu da loja de naves para " + player.getName());
        Inventory inv = Bukkit.createInventory(null, 45, inventoryTitle);
        slotToShipId.clear();

        // Decoração do fundo
        ItemStack borderItem = createItem(Material.STAINED_GLASS_PANE, 1, (byte) 15, "§r");
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, borderItem);
        }

        // Adicionar naves disponíveis
        int[] slots = {11, 13, 15, 29, 31, 33};
        int slotIndex = 0;

        for (ShipTemplate template : spaceshipManager.getAvailableShips()) {
            if (template.isPurchasable() && slotIndex < slots.length) {
                int slot = slots[slotIndex];
                ItemStack shipItem = createShipItem(template, player);
                inv.setItem(slot, shipItem);
                slotToShipId.put(slot, template.getId());
                spaceshipManager.getPlugin().getLogger().info("[DEBUG] Adicionada nave " + template.getId() + " no slot " + slot + " com nome " + template.getName());
                slotIndex++;
            }
        }

        // Debug: Imprimir todos os slots mapeados
        for (Map.Entry<Integer, String> entry : slotToShipId.entrySet()) {
            spaceshipManager.getPlugin().getLogger().info("[DEBUG] Slot " + entry.getKey() + " -> Nave: " + entry.getValue());
        }

        player.openInventory(inv);
    }

    private ItemStack createShipItem(ShipTemplate template, Player player) {
        Material material;

        switch (template.getType()) {
            case MINING:
                material = Material.DIAMOND_PICKAXE;
                break;
            case COMBAT:
                material = Material.DIAMOND_SWORD;
                break;
            case TRANSPORT:
                material = Material.CHEST;
                break;
            default:
                material = Material.NETHER_STAR;
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        // Nome da nave
        meta.setDisplayName("§b✧ " + template.getName() + " §b✧");

        // Criar lore
        List<String> lore = new ArrayList<>();
        lore.addAll(template.getDescription());
        lore.add("");
        
        // Adicionar estatísticas
        lore.add("§6✧ Estatísticas:");
        for (String stat : getStatsDescription(template)) {
            lore.add(stat);
        }
        lore.add("");

        // Verificar se o jogador já possui esta nave
        PlayerHangar hangar = spaceshipManager.getPlayerHangar(player.getUniqueId());
        boolean hasShip = hangar != null && hangar.getShips().stream()
            .anyMatch(ship -> ship.getTemplateId().equals(template.getId()));

        if (hasShip) {
            lore.add("§c✘ Você já possui esta nave!");
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        } else {
            // Adicionar informações de preço
            double price = template.getPrice();
            double balance = spaceshipManager.getEconomyManager().getBalance(player.getUniqueId());
            
            lore.add("§6✧ Preço: §f" + spaceshipManager.getEconomyManager().formatValue(price) + " ⭐");
            lore.add("§6✧ Seu saldo: §f" + spaceshipManager.getEconomyManager().formatValue(balance) + " ⭐");
            lore.add("");

            if (balance >= price) {
                lore.add("§a✔ Clique para comprar!");
            } else {
                lore.add("§c✘ Créditos insuficientes!");
                lore.add("§cFaltam: " + spaceshipManager.getEconomyManager().formatValue(price - balance) + " ⭐");
            }
        }

        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private List<String> getStatsDescription(ShipTemplate template) {
        List<String> stats = new ArrayList<>();
        for (String key : template.getStats().keySet()) {
            String statName = key.replace("_", " ");
            statName = statName.substring(0, 1).toUpperCase() + statName.substring(1).toLowerCase();
            stats.add("§f• " + statName + ": " + template.getStats().get(key));
        }
        return stats;
    }

    private ItemStack createItem(Material material, int amount, byte data, String name, String... lore) {
        ItemStack item = new ItemStack(material, amount, data);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        if (lore.length > 0) {
            meta.setLore(Arrays.asList(lore));
        }
        item.setItemMeta(meta);
        return item;
    }

    public String getInventoryTitle() {
        return inventoryTitle;
    }

    public boolean handleClick(Player player, Inventory inventory, int slot) {
        spaceshipManager.getPlugin().getLogger().info("[DEBUG] Clique detectado - Jogador: " + player.getName() + ", Slot: " + slot);
        spaceshipManager.getPlugin().getLogger().info("[DEBUG] Título do inventário: '" + inventory.getTitle() + "' vs '" + inventoryTitle + "'");

        if (!inventory.getTitle().equals(inventoryTitle)) {
            spaceshipManager.getPlugin().getLogger().info("[DEBUG] Título do inventário não corresponde");
            return false;
        }

        // Debug: Imprimir todos os slots mapeados no momento do clique
        spaceshipManager.getPlugin().getLogger().info("[DEBUG] Slots mapeados no momento do clique:");
        for (Map.Entry<Integer, String> entry : slotToShipId.entrySet()) {
            spaceshipManager.getPlugin().getLogger().info("[DEBUG] Slot " + entry.getKey() + " -> Nave: " + entry.getValue());
        }

        String shipId = slotToShipId.get(slot);
        spaceshipManager.getPlugin().getLogger().info("[DEBUG] ID da nave no slot " + slot + ": " + shipId);
        
        if (shipId != null) {
            ShipTemplate template = spaceshipManager.getAvailableShips().stream()
                .filter(t -> t.getId().equals(shipId))
                .findFirst()
                .orElse(null);
            
            spaceshipManager.getPlugin().getLogger().info("[DEBUG] Template encontrado: " + (template != null ? template.getName() : "null"));

            if (template != null && template.isPurchasable()) {
                handleShipPurchase(player, template);
                openShopMenu(player);
                return true;
            }
        }

        return false;
    }

    private void handleShipPurchase(Player player, ShipTemplate template) {
        spaceshipManager.getPlugin().getLogger().info("[DEBUG] Iniciando compra - Jogador: " + player.getName() + ", Nave: " + template.getName());
        
        PlayerHangar hangar = spaceshipManager.getPlayerHangar(player.getUniqueId());
        if (hangar != null && hangar.getShips().stream().anyMatch(ship -> ship.getTemplateId().equals(template.getId()))) {
            player.sendMessage("");
            player.sendMessage("§c✘ Você já possui esta nave!");
            player.sendMessage("§cVocê não pode ter duas naves do mesmo modelo.");
            player.sendMessage("");
            player.playSound(player.getLocation(), "NOTE_BASS", 1.0f, 0.5f);
            return;
        }

        spaceshipManager.purchaseShip(player, template.getId());
    }
} 