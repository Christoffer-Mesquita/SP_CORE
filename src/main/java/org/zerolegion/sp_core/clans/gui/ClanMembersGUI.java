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
import org.zerolegion.sp_core.clans.ClanRole;

import java.util.*;

public class ClanMembersGUI {
    private final ClanManager clanManager;
    private final int size = 54;

    public ClanMembersGUI(ClanManager clanManager) {
        this.clanManager = clanManager;
    }

    public void openGUI(Player player) {
        SpaceClan clan = clanManager.getPlayerClan(player.getUniqueId());
        if (clan == null) return;

        Inventory inv = Bukkit.createInventory(null, size, ChatColor.DARK_PURPLE + "✧ Membros do Clã ✧");

        // Cabeçalho com informações
        ItemStack info = new ItemStack(Material.BEACON);
        ItemMeta meta = info.getItemMeta();
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + clan.getName());
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Total de membros: " + ChatColor.WHITE + clan.getMembers().size());
        lore.add("");
        lore.add(ChatColor.GRAY + "Cargos:");
        int leaders = 0, officers = 0, members = 0, recruits = 0;
        for (ClanRole role : clan.getMembers().values()) {
            switch (role) {
                case LEADER: leaders++; break;
                case OFFICER: officers++; break;
                case MEMBER: members++; break;
                case RECRUIT: recruits++; break;
            }
        }
        lore.add(ChatColor.DARK_RED + "• " + ChatColor.RED + "Líder: " + ChatColor.WHITE + leaders);
        lore.add(ChatColor.RED + "• " + ChatColor.RED + "Oficiais: " + ChatColor.WHITE + officers);
        lore.add(ChatColor.GREEN + "• " + ChatColor.GREEN + "Membros: " + ChatColor.WHITE + members);
        lore.add(ChatColor.GRAY + "• " + ChatColor.GRAY + "Recrutas: " + ChatColor.WHITE + recruits);
        meta.setLore(lore);
        info.setItemMeta(meta);
        inv.setItem(4, info);

        // Lista de membros
        int slot = 19;
        boolean isLeaderOrOfficer = clan.isLeader(player.getUniqueId()) || clan.isOfficer(player.getUniqueId());

        // Primeiro o líder
        for (Map.Entry<UUID, ClanRole> entry : clan.getMembers().entrySet()) {
            if (entry.getValue() == ClanRole.LEADER) {
                addMemberHead(inv, entry.getKey(), entry.getValue(), slot++, isLeaderOrOfficer);
                break;
            }
        }

        // Depois os oficiais
        for (Map.Entry<UUID, ClanRole> entry : clan.getMembers().entrySet()) {
            if (entry.getValue() == ClanRole.OFFICER) {
                if (slot > 43) break; // Evita overflow
                addMemberHead(inv, entry.getKey(), entry.getValue(), slot++, isLeaderOrOfficer);
            }
        }

        // Membros normais
        for (Map.Entry<UUID, ClanRole> entry : clan.getMembers().entrySet()) {
            if (entry.getValue() == ClanRole.MEMBER) {
                if (slot > 43) break;
                addMemberHead(inv, entry.getKey(), entry.getValue(), slot++, isLeaderOrOfficer);
            }
        }

        // Por fim os recrutas
        for (Map.Entry<UUID, ClanRole> entry : clan.getMembers().entrySet()) {
            if (entry.getValue() == ClanRole.RECRUIT) {
                if (slot > 43) break;
                addMemberHead(inv, entry.getKey(), entry.getValue(), slot++, isLeaderOrOfficer);
            }
        }

        // Botão de convidar (apenas para líder e oficiais)
        if (isLeaderOrOfficer) {
            ItemStack invite = new ItemStack(Material.EMERALD);
            meta = invite.getItemMeta();
            meta.setDisplayName(ChatColor.GREEN + "Convidar Jogador");
            lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Clique para convidar");
            lore.add(ChatColor.GRAY + "um novo jogador");
            meta.setLore(lore);
            invite.setItemMeta(meta);
            inv.setItem(49, invite);
        }

        // Botão voltar
        ItemStack back = new ItemStack(Material.ARROW);
        meta = back.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "Voltar");
        back.setItemMeta(meta);
        inv.setItem(45, back);

        player.openInventory(inv);
    }

    private void addMemberHead(Inventory inv, UUID uuid, ClanRole role, int slot, boolean canManage) {
        ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        
        String name = Bukkit.getOfflinePlayer(uuid).getName();
        meta.setDisplayName(role.getDisplay() + " " + name);
        
        List<String> lore = new ArrayList<>();
        Player target = Bukkit.getPlayer(uuid);
        if (target != null && target.isOnline()) {
            lore.add(ChatColor.GREEN + "● Online");
        } else {
            lore.add(ChatColor.RED + "● Offline");
        }
        
        if (canManage) {
            lore.add("");
            lore.add(ChatColor.YELLOW + "Clique para gerenciar");
            lore.add(ChatColor.YELLOW + "este membro");
        }
        
        meta.setLore(lore);
        meta.setOwner(String.valueOf(Bukkit.getOfflinePlayer(uuid)));
        head.setItemMeta(meta);
        
        inv.setItem(slot, head);
    }
} 