package org.zerolegion.sp_core.ships.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.zerolegion.sp_core.ships.PlayerShip;
import org.zerolegion.sp_core.ships.SpaceshipManager;

import java.util.ArrayList;
import java.util.List;

public class FuelShopGUI {
    private final SpaceshipManager spaceshipManager;

    public FuelShopGUI(SpaceshipManager spaceshipManager) {
        this.spaceshipManager = spaceshipManager;
    }

    public void openFuelShop(Player player, PlayerShip ship) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.DARK_PURPLE + "✧ Posto de Combustível ✧");

        // Decoração do fundo
        ItemStack background = createItem(Material.STAINED_GLASS_PANE, (byte) 15, " ");
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, background);
        }

        // Informações da nave
        List<String> shipInfo = new ArrayList<>();
        shipInfo.add(ChatColor.GRAY + "Combustível atual: " + ChatColor.YELLOW + 
            String.format("%.1f%%", ship.getFuel()));
        shipInfo.add("");
        shipInfo.add(ChatColor.GRAY + "Clique nos itens abaixo");
        shipInfo.add(ChatColor.GRAY + "para comprar combustível");

        ItemStack shipItem = createItem(Material.MINECART, 
            ChatColor.AQUA + "✧ " + ship.getName() + " ✧", 
            shipInfo);
        inv.setItem(4, shipItem);

        // Opções de combustível
        addFuelOption(inv, 11, "basic", ship);
        addFuelOption(inv, 13, "premium", ship);
        addFuelOption(inv, 15, "experimental", ship);

        // Botão voltar
        ItemStack back = createItem(Material.BARRIER, ChatColor.RED + "Voltar");
        inv.setItem(22, back);

        player.openInventory(inv);
    }

    private void addFuelOption(Inventory inv, int slot, String fuelType, PlayerShip ship) {
        double price = spaceshipManager.getFuelPrice(fuelType);
        double efficiency = spaceshipManager.getFuelEfficiency(fuelType);
        String name = spaceshipManager.getFuelName(fuelType);
        
        Material material;
        byte data = 0;
        
        switch (fuelType) {
            case "premium":
                material = Material.BLAZE_POWDER;
                break;
            case "experimental":
                material = Material.GLOWSTONE_DUST;
                break;
            default:
                material = Material.SULPHUR;
                break;
        }

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Eficiência: " + ChatColor.YELLOW + efficiency + "x");
        lore.add(ChatColor.GRAY + "Preço: " + ChatColor.YELLOW + 
            spaceshipManager.getEconomyManager().formatValue(price) + " ⭐" + 
            ChatColor.GRAY + " por 10%");
        lore.add("");
        lore.add(ChatColor.GRAY + "Clique para comprar 10%");
        lore.add(ChatColor.GRAY + "Shift + Clique para encher");

        ItemStack item = createItem(material, data, name, lore);
        inv.setItem(slot, item);
    }

    private ItemStack createItem(Material material, String name, List<String> lore) {
        return createItem(material, (byte) 0, name, lore);
    }

    private ItemStack createItem(Material material, byte data, String name, List<String> lore) {
        ItemStack item = new ItemStack(material, 1, data);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        
        if (lore != null && !lore.isEmpty()) {
            meta.setLore(lore);
        }
        
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createItem(Material material, String name) {
        return createItem(material, (byte) 0, name, null);
    }

    private ItemStack createItem(Material material, byte data, String name) {
        return createItem(material, data, name, null);
    }
} 