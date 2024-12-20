package org.zerolegion.sp_core.clans.gui;

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

import java.util.UUID;

public class ClanMembersGUIListener implements Listener {
    private final SP_CORE plugin;
    private final ClanManager clanManager;
    private final ClanMainGUI mainGUI;
    private final ClanMemberManageGUI manageGUI;
    private final ClanInviteGUI inviteGUI;

    public ClanMembersGUIListener(SP_CORE plugin, ClanManager clanManager, ClanMainGUI mainGUI, ClanInviteGUI inviteGUI) {
        this.plugin = plugin;
        this.clanManager = clanManager;
        this.mainGUI = mainGUI;
        this.manageGUI = new ClanMemberManageGUI(clanManager);
        this.inviteGUI = inviteGUI;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!event.getView().getTitle().equals(ChatColor.DARK_PURPLE + "✧ Membros do Clã ✧")) return;

        event.setCancelled(true);
        
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        
        if (clicked == null) return;

        SpaceClan clan = clanManager.getPlayerClan(player.getUniqueId());
        if (clan == null) return;

        if (clicked.getType().name().equals("SKULL_ITEM") && clicked.getDurability() == 3) {
            handleMemberClick(player, clicked, clan);
        } else if (clicked.getType().name().equals("EMERALD")) {
            inviteGUI.openGUI(player);
        } else if (clicked.getType().name().equals("ARROW")) {
            mainGUI.openGUI(player);
        }
    }

    private void handleMemberClick(Player player, ItemStack clicked, SpaceClan clan) {
        if (!(clicked.getItemMeta() instanceof SkullMeta)) return;
        
        SkullMeta meta = (SkullMeta) clicked.getItemMeta();
        if (meta.getOwner() == null) return;

        UUID targetId = plugin.getServer().getOfflinePlayer(meta.getOwner()).getUniqueId();
        
        // Verifica se o jogador pode gerenciar membros
        if (clan.isLeader(player.getUniqueId()) || clan.isOfficer(player.getUniqueId())) {
            // Não pode gerenciar o líder
            if (!clan.isLeader(targetId)) {
                manageGUI.openGUI(player, targetId);
            }
        }
    }
} 