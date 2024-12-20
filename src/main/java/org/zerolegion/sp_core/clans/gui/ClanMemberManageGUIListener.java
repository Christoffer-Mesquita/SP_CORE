package org.zerolegion.sp_core.clans.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.zerolegion.sp_core.SP_CORE;
import org.zerolegion.sp_core.clans.ClanManager;
import org.zerolegion.sp_core.clans.SpaceClan;
import org.zerolegion.sp_core.clans.ClanRole;

import java.util.UUID;

public class ClanMemberManageGUIListener implements Listener {
    private final SP_CORE plugin;
    private final ClanManager clanManager;
    private final ClanMembersGUI membersGUI;

    public ClanMemberManageGUIListener(SP_CORE plugin, ClanManager clanManager, ClanMembersGUI membersGUI) {
        this.plugin = plugin;
        this.clanManager = clanManager;
        this.membersGUI = membersGUI;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        String title = event.getView().getTitle();
        if (!title.startsWith(ChatColor.DARK_PURPLE + "✧ Gerenciar: ")) return;

        event.setCancelled(true);
        
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        
        if (clicked == null) return;

        SpaceClan clan = clanManager.getPlayerClan(player.getUniqueId());
        if (clan == null) return;

        // Extrai o nome do jogador do título
        String targetName = title.substring((ChatColor.DARK_PURPLE + "✧ Gerenciar: ").length(), 
            title.length() - (ChatColor.DARK_PURPLE + " ✧").length());
        Player target = Bukkit.getPlayer(targetName);
        
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Jogador não encontrado!");
            player.closeInventory();
            return;
        }

        UUID targetId = target.getUniqueId();

        switch (clicked.getType()) {
            case GOLD_BLOCK:
                handleRoleChange(player, targetId, clan, ClanRole.OFFICER);
                break;
            case IRON_BLOCK:
                handleRoleChange(player, targetId, clan, ClanRole.MEMBER);
                break;
            case STONE:
                handleRoleChange(player, targetId, clan, ClanRole.RECRUIT);
                break;
            case BARRIER:
                handleKick(player, targetId, clan);
                break;
            case ARROW:
                membersGUI.openGUI(player);
                break;
        }
    }

    private void handleRoleChange(Player player, UUID targetId, SpaceClan clan, ClanRole newRole) {
        if (!clan.isLeader(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Apenas o líder pode alterar cargos!");
            return;
        }

        if (clan.isLeader(targetId)) {
            player.sendMessage(ChatColor.RED + "Você não pode alterar o cargo do líder!");
            return;
        }

        ClanRole currentRole = clan.getMembers().get(targetId);
        if (currentRole == newRole) {
            player.sendMessage(ChatColor.RED + "Este membro já possui este cargo!");
            return;
        }

        clan.getMembers().put(targetId, newRole);
        clanManager.saveClan(clan);

        Player target = Bukkit.getPlayer(targetId);
        if (target != null) {
            target.sendMessage("");
            target.sendMessage(ChatColor.GREEN + "Seu cargo no clã foi alterado para " + newRole.getDisplay());
            target.sendMessage("");
        }

        player.sendMessage(ChatColor.GREEN + "Cargo alterado com sucesso!");
        player.closeInventory();
        membersGUI.openGUI(player);
    }

    private void handleKick(Player player, UUID targetId, SpaceClan clan) {
        if (!clan.isLeader(player.getUniqueId()) && !clan.isOfficer(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Você não tem permissão para expulsar membros!");
            return;
        }

        if (clan.isLeader(targetId)) {
            player.sendMessage(ChatColor.RED + "Você não pode expulsar o líder!");
            return;
        }

        // Oficiais não podem expulsar outros oficiais
        if (clan.isOfficer(player.getUniqueId()) && clan.isOfficer(targetId)) {
            player.sendMessage(ChatColor.RED + "Oficiais não podem expulsar outros oficiais!");
            return;
        }

        clan.removeMember(targetId);
        clanManager.saveClan(clan);

        Player target = Bukkit.getPlayer(targetId);
        if (target != null) {
            target.sendMessage("");
            target.sendMessage(ChatColor.RED + "Você foi expulso do clã " + clan.getName());
            target.sendMessage("");
        }

        player.sendMessage(ChatColor.GREEN + "Membro expulso com sucesso!");
        player.closeInventory();
        membersGUI.openGUI(player);
    }
} 