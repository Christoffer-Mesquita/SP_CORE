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
import org.zerolegion.sp_core.clans.ClanPermission;
import org.zerolegion.sp_core.clans.SpaceClan;
import org.zerolegion.sp_core.clans.ClanRole;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ClanMemberManageGUI {
    private final ClanManager clanManager;
    private final int size = 36;

    public ClanMemberManageGUI(ClanManager clanManager) {
        this.clanManager = clanManager;
    }

    public void openGUI(Player player, UUID targetId) {
        SpaceClan clan = clanManager.getPlayerClan(player.getUniqueId());
        if (clan == null) return;

        ClanRole targetRole = clan.getMembers().get(targetId);
        if (targetRole == null) return;

        String targetName = Bukkit.getOfflinePlayer(targetId).getName();
        Inventory inv = Bukkit.createInventory(null, size, 
            ChatColor.DARK_PURPLE + "✧ Gerenciar: " + targetName + " ✧");

        // Cabeçalho com informações do membro
        ItemStack head = new ItemStack(Material.SKULL_ITEM);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setDisplayName(targetRole.getDisplay() + " " + targetName);
        List<String> lore = new ArrayList<>();
        Player target = Bukkit.getPlayer(targetId);
        if (target != null && target.isOnline()) {
            lore.add(ChatColor.GREEN + "● Online");
        } else {
            lore.add(ChatColor.RED + "● Offline");
        }
        meta.setLore(lore);
        meta.setOwner(Bukkit.getOfflinePlayer(targetId).getName());
        head.setItemMeta(meta);
        inv.setItem(4, head);

        // Opções de cargo (apenas para líder)
        if (clan.isLeader(player.getUniqueId())) {
            addRoleOption(inv, ClanRole.OFFICER, 19, targetRole);
            addRoleOption(inv, ClanRole.MEMBER, 20, targetRole);
            addRoleOption(inv, ClanRole.RECRUIT, 21, targetRole);
        }

        // Opção de expulsar
        ItemStack kick = new ItemStack(Material.BARRIER);
        meta = (SkullMeta) kick.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "Expulsar Membro");
        lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Clique para expulsar");
        lore.add(ChatColor.GRAY + "este membro do clã");
        lore.add("");
        lore.add(ChatColor.RED + "Atenção: Esta ação não");
        lore.add(ChatColor.RED + "pode ser desfeita!");
        meta.setLore(lore);
        kick.setItemMeta(meta);
        inv.setItem(31, kick);

        // Botão voltar
        ItemStack back = new ItemStack(Material.ARROW);
        meta = (SkullMeta) back.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "Voltar");
        back.setItemMeta(meta);
        inv.setItem(27, back);

        player.openInventory(inv);
    }

    private void addRoleOption(Inventory inv, ClanRole role, int slot, ClanRole currentRole) {
        ItemStack item = new ItemStack(getRoleMaterial(role));
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(role.getDisplay());
        
        List<String> lore = new ArrayList<>();
        if (role == currentRole) {
            lore.add(ChatColor.GREEN + "✔ Cargo atual");
        } else {
            lore.add(ChatColor.GRAY + "Clique para definir");
            lore.add(ChatColor.GRAY + "este cargo");
        }
        
        lore.add("");
        lore.add(ChatColor.YELLOW + "Permissões:");
        for (ClanPermission perm : getPermissionsForRole(role)) {
            lore.add(ChatColor.GRAY + "• " + perm.getDescription());
        }
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        inv.setItem(slot, item);
    }

    private Material getRoleMaterial(ClanRole role) {
        switch (role) {
            case LEADER: return Material.DIAMOND_BLOCK;
            case OFFICER: return Material.GOLD_BLOCK;
            case MEMBER: return Material.IRON_BLOCK;
            case RECRUIT: return Material.STONE;
            default: return Material.BARRIER;
        }
    }

    private List<ClanPermission> getPermissionsForRole(ClanRole role) {
        List<ClanPermission> perms = new ArrayList<>();
        for (ClanPermission perm : ClanPermission.values()) {
            if (role.hasPermission(perm)) {
                perms.add(perm);
            }
        }
        return perms;
    }
} 