package org.zerolegion.sp_core.clans.gui;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.zerolegion.sp_core.SP_CORE;
import org.zerolegion.sp_core.clans.ClanManager;
import org.zerolegion.sp_core.clans.SpaceClan;

import java.util.UUID;

public class ClanRelationsGUIListener implements Listener {
    private final SP_CORE plugin;
    private final ClanManager clanManager;
    private final ClanRelationsGUI relationsGUI;
    private final ClanMainGUI mainGUI;

    public ClanRelationsGUIListener(SP_CORE plugin, ClanManager clanManager, ClanRelationsGUI relationsGUI, ClanMainGUI mainGUI) {
        this.plugin = plugin;
        this.clanManager = clanManager;
        this.relationsGUI = relationsGUI;
        this.mainGUI = mainGUI;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!event.getView().getTitle().equals(ChatColor.DARK_PURPLE + "✧ Relações do Clã ✧")) return;

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null) return;

        SpaceClan clan = clanManager.getPlayerClan(player.getUniqueId());
        if (clan == null) return;

        if (!clan.isLeader(player.getUniqueId()) && !clan.isOfficer(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Apenas líderes e oficiais podem gerenciar relações!");
            return;
        }

        if (clicked.getType().name().equals("ARROW")) {
            mainGUI.openGUI(player);
            return;
        }

        // Verificar se é uma cabeça de jogador (representando um clã)
        if (clicked.getType().name().equals("SKULL_ITEM") && clicked.getDurability() == 3) {
            handleClanClick(player, clicked, clan, event.isRightClick());
        }
    }

    private void handleClanClick(Player player, ItemStack clicked, SpaceClan clan, boolean isRightClick) {
        String clanName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
        SpaceClan targetClan = null;

        for (SpaceClan c : clanManager.getAllClans()) {
            if (c.getName().equals(clanName)) {
                targetClan = c;
                break;
            }
        }

        if (targetClan == null || targetClan.equals(clan)) return;

        if (isRightClick) {
            // Declarar guerra
            if (clan.getAllies().contains(targetClan.getId())) {
                clan.removeAlly(targetClan.getId());
                targetClan.removeAlly(clan.getId());
            }
            clan.addEnemy(targetClan.getId());
            targetClan.addEnemy(clan.getId());
            
            player.sendMessage(ChatColor.RED + "Guerra declarada contra o clã " + targetClan.getName() + "!");
            
            // Notificar membros online do clã alvo
            for (UUID memberId : targetClan.getMembers().keySet()) {
                Player member = plugin.getServer().getPlayer(memberId);
                if (member != null && member.isOnline()) {
                    member.sendMessage("");
                    member.sendMessage(ChatColor.RED + "⚔ O clã " + clan.getName() + " declarou guerra contra seu clã!");
                    member.sendMessage("");
                }
            }
        } else {
            // Propor aliança
            if (clan.getEnemies().contains(targetClan.getId())) {
                clan.removeEnemy(targetClan.getId());
                targetClan.removeEnemy(clan.getId());
            }
            clan.addAlly(targetClan.getId());
            targetClan.addAlly(clan.getId());
            
            player.sendMessage(ChatColor.GREEN + "Aliança formada com o clã " + targetClan.getName() + "!");
            
            // Notificar membros online do clã alvo
            for (UUID memberId : targetClan.getMembers().keySet()) {
                Player member = plugin.getServer().getPlayer(memberId);
                if (member != null && member.isOnline()) {
                    member.sendMessage("");
                    member.sendMessage(ChatColor.GREEN + "✦ O clã " + clan.getName() + " formou uma aliança com seu clã!");
                    member.sendMessage("");
                }
            }
        }

        clanManager.saveClan(clan);
        clanManager.saveClan(targetClan);
        relationsGUI.openGUI(player);
    }
} 