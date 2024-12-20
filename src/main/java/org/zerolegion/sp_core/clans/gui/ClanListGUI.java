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

public class ClanListGUI {
    private final ClanManager clanManager;
    private final int size = 54;
    private final int CLANS_PER_PAGE = 28;
    private int currentPage = 0;

    public ClanListGUI(ClanManager clanManager) {
        this.clanManager = clanManager;
    }

    public void openGUI(Player player) {
        openGUI(player, 0);
    }

    public void openGUI(Player player, int page) {
        this.currentPage = page;
        Inventory inv = Bukkit.createInventory(null, size, 
            ChatColor.DARK_PURPLE + "✧ Lista de Clãs ✧");

        // Informações
        ItemStack info = new ItemStack(Material.BOOK);
        ItemMeta meta = info.getItemMeta();
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Lista de Clãs");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Total de clãs: " + 
            ChatColor.WHITE + clanManager.getAllClans().size());
        lore.add(ChatColor.GRAY + "Página: " + ChatColor.WHITE + 
            (currentPage + 1));
        meta.setLore(lore);
        info.setItemMeta(meta);
        inv.setItem(4, info);

        // Lista os clãs
        List<SpaceClan> clans = clanManager.getTopClans(Integer.MAX_VALUE);
        int startIndex = currentPage * CLANS_PER_PAGE;
        int endIndex = Math.min(startIndex + CLANS_PER_PAGE, clans.size());
        
        int slot = 9;
        for (int i = startIndex; i < endIndex; i++) {
            SpaceClan clan = clans.get(i);
            ItemStack clanItem = new ItemStack(Material.BEACON);
            meta = clanItem.getItemMeta();
            meta.setDisplayName(ChatColor.LIGHT_PURPLE + clan.getName());
            
            lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Tag: " + ChatColor.WHITE + clan.getTag());
            lore.add(ChatColor.GRAY + "Líder: " + ChatColor.YELLOW + 
                Bukkit.getOfflinePlayer(clan.getLeader()).getName());
            lore.add(ChatColor.GRAY + "Membros: " + ChatColor.WHITE + 
                clan.getMembers().size());
            lore.add(ChatColor.GRAY + "Poder: " + ChatColor.RED + clan.getPower());
            
            if (clan.getDescription() != null && !clan.getDescription().isEmpty()) {
                lore.add("");
                lore.add(ChatColor.GRAY + clan.getDescription());
            }
            
            lore.add("");
            if (clan.getSetting("openJoin")) {
                lore.add(ChatColor.GREEN + "✔ Entrada Livre");
                lore.add(ChatColor.YELLOW + "Clique para entrar");
            } else {
                lore.add(ChatColor.RED + "✘ Entrada Restrita");
                lore.add(ChatColor.YELLOW + "Clique para solicitar entrada");
            }
            
            meta.setLore(lore);
            clanItem.setItemMeta(meta);
            inv.setItem(slot++, clanItem);
        }

        // Botões de navegação
        if (currentPage > 0) {
            ItemStack prev = new ItemStack(Material.ARROW);
            meta = prev.getItemMeta();
            meta.setDisplayName(ChatColor.YELLOW + "Página Anterior");
            prev.setItemMeta(meta);
            inv.setItem(45, prev);
        }

        if (endIndex < clans.size()) {
            ItemStack next = new ItemStack(Material.ARROW);
            meta = next.getItemMeta();
            meta.setDisplayName(ChatColor.YELLOW + "Próxima Página");
            next.setItemMeta(meta);
            inv.setItem(53, next);
        }

        // Botão voltar
        ItemStack back = new ItemStack(Material.BARRIER);
        meta = back.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "Voltar");
        back.setItemMeta(meta);
        inv.setItem(49, back);

        player.openInventory(inv);
    }

    public int getCurrentPage() {
        return currentPage;
    }
} 