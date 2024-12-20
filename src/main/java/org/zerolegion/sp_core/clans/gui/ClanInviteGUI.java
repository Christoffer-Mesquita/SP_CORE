package org.zerolegion.sp_core.clans.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.zerolegion.sp_core.clans.ClanManager;
import org.zerolegion.sp_core.clans.SpaceClan;

import java.util.ArrayList;
import java.util.List;

public class ClanInviteGUI {
    private final ClanManager clanManager;
    private final int size = 54;

    public ClanInviteGUI(ClanManager clanManager) {
        this.clanManager = clanManager;
    }

    public void openGUI(Player player) {
        SpaceClan clan = clanManager.getPlayerClan(player.getUniqueId());
        if (clan == null) return;

        Inventory inv = Bukkit.createInventory(null, size, ChatColor.DARK_PURPLE + "✧ Convidar Jogadores ✧");

        // Cabeçalho com informações
        ItemStack info = new ItemStack(Material.BOOK);
        ItemMeta meta = info.getItemMeta();
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Informações");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Clique em um jogador");
        lore.add(ChatColor.GRAY + "para convidá-lo para o clã");
        lore.add("");
        lore.add(ChatColor.YELLOW + "Membros atuais: " + clan.getMembers().size());
        meta.setLore(lore);
        info.setItemMeta(meta);
        inv.setItem(4, info);

        // Lista de jogadores online que não estão em nenhum clã
        int slot = 9;
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.equals(player)) continue;
            if (clanManager.getPlayerClan(onlinePlayer.getUniqueId()) != null) continue;

            ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
            SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
            skullMeta.setDisplayName(ChatColor.GREEN + onlinePlayer.getName());
            List<String> playerLore = new ArrayList<>();
            playerLore.add(ChatColor.GRAY + "Clique para convidar");
            playerLore.add(ChatColor.GRAY + "este jogador para o clã");
            skullMeta.setLore(playerLore);
            skullMeta.setOwner(onlinePlayer.getName());
            head.setItemMeta(skullMeta);

            if (slot < size - 9) {
                inv.setItem(slot++, head);
            }
        }

        // Botão voltar
        ItemStack back = new ItemStack(Material.ARROW);
        meta = back.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "Voltar");
        back.setItemMeta(meta);
        inv.setItem(size - 9, back);

        player.openInventory(inv);
    }
} 