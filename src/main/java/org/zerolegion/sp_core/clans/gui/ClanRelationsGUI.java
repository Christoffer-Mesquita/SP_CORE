package org.zerolegion.sp_core.clans.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.zerolegion.sp_core.clans.ClanManager;
import org.zerolegion.sp_core.clans.SpaceClan;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ClanRelationsGUI {
    private final ClanManager clanManager;
    private final int size = 54;

    public ClanRelationsGUI(ClanManager clanManager) {
        this.clanManager = clanManager;
    }

    public void openGUI(Player player) {
        SpaceClan clan = clanManager.getPlayerClan(player.getUniqueId());
        if (clan == null) return;

        Inventory inv = Bukkit.createInventory(null, size, ChatColor.DARK_PURPLE + "✧ Alianças e Guerras ✧");

        // Informações do Clã
        ItemStack info = new ItemStack(Material.BEACON);
        ItemMeta meta = info.getItemMeta();
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + clan.getName());
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Alianças: " + ChatColor.GREEN + clan.getAllies().size());
        lore.add(ChatColor.GRAY + "Guerras: " + ChatColor.RED + clan.getEnemies().size());
        meta.setLore(lore);
        info.setItemMeta(meta);
        inv.setItem(4, info);

        // Lista de Aliados
        int allySlot = 19;
        Set<String> allies = clan.getAllies();
        for (String allyId : allies) {
            SpaceClan ally = clanManager.getClan(allyId);
            if (ally != null && allySlot < 35) {
                ItemStack allyItem = new ItemStack(Material.EMERALD);
                meta = allyItem.getItemMeta();
                meta.setDisplayName(ChatColor.GREEN + ally.getName());
                lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "Tag: " + ChatColor.WHITE + ally.getTag());
                lore.add(ChatColor.GRAY + "Poder: " + ChatColor.RED + ally.getPower());
                lore.add("");
                lore.add(ChatColor.YELLOW + "Clique para gerenciar");
                lore.add(ChatColor.YELLOW + "esta aliança");
                meta.setLore(lore);
                allyItem.setItemMeta(meta);
                inv.setItem(allySlot++, allyItem);
            }
        }

        // Lista de Inimigos
        int enemySlot = 37;
        Set<String> enemies = clan.getEnemies();
        for (String enemyId : enemies) {
            SpaceClan enemy = clanManager.getClan(enemyId);
            if (enemy != null && enemySlot < 53) {
                ItemStack enemyItem = new ItemStack(Material.REDSTONE);
                meta = enemyItem.getItemMeta();
                meta.setDisplayName(ChatColor.RED + enemy.getName());
                lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "Tag: " + ChatColor.WHITE + enemy.getTag());
                lore.add(ChatColor.GRAY + "Poder: " + ChatColor.RED + enemy.getPower());
                lore.add("");
                lore.add(ChatColor.YELLOW + "Clique para gerenciar");
                lore.add(ChatColor.YELLOW + "esta guerra");
                meta.setLore(lore);
                enemyItem.setItemMeta(meta);
                inv.setItem(enemySlot++, enemyItem);
            }
        }

        // Botão para procurar clãs
        if (clan.isLeader(player.getUniqueId()) || clan.isOfficer(player.getUniqueId())) {
            ItemStack search = new ItemStack(Material.COMPASS);
            meta = search.getItemMeta();
            meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Procurar Clãs");
            lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Clique para procurar");
            lore.add(ChatColor.GRAY + "outros clãs");
            meta.setLore(lore);
            search.setItemMeta(meta);
            inv.setItem(49, search);
        }

        // Botão voltar
        ItemStack back = new ItemStack(Material.ARROW);
        meta = back.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "Voltar");
        back.setItemMeta(meta);
        inv.setItem(45, back);

        player.openInventory(inv);
    }
} 