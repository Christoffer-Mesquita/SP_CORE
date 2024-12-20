package org.zerolegion.sp_core.clans.gui;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.zerolegion.sp_core.SP_CORE;
import org.zerolegion.sp_core.clans.ClanManager;
import org.zerolegion.sp_core.clans.ClanPermission;
import org.zerolegion.sp_core.clans.SpaceClan;

public class ClanMainGUIListener implements Listener {
    private final SP_CORE plugin;
    private final ClanManager clanManager;
    private final ClanMembersGUI membersGUI;
    private final ClanSettingsGUI settingsGUI;
    private final ClanRelationsGUI relationsGUI;
    private final ClanBankGUI bankGUI;
    private final ClanEventsGUI eventsGUI;
    private final ClanListGUI listGUI;
    private final ClanCreateGUI createGUI;

    public ClanMainGUIListener(SP_CORE plugin, ClanManager clanManager) {
        this.plugin = plugin;
        this.clanManager = clanManager;
        this.membersGUI = new ClanMembersGUI(clanManager);
        this.settingsGUI = new ClanSettingsGUI(clanManager);
        this.relationsGUI = new ClanRelationsGUI(clanManager);
        this.bankGUI = new ClanBankGUI(clanManager);
        this.eventsGUI = new ClanEventsGUI(clanManager);
        this.listGUI = new ClanListGUI(clanManager);
        this.createGUI = new ClanCreateGUI(clanManager);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!event.getView().getTitle().equals(ChatColor.DARK_PURPLE + "✧ Clã Espacial ✧")) return;

        event.setCancelled(true);
        
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        
        if (clicked == null) return;

        SpaceClan clan = clanManager.getPlayerClan(player.getUniqueId());

        // Jogador sem clã
        if (clan == null) {
            handleNoClanClick(player, clicked);
            return;
        }

        // Jogador com clã
        switch (clicked.getType()) {
            case ENDER_PORTAL_FRAME: // Base
                handleBaseClick(player, clan, event.isShiftClick());
                break;
            case SKULL_ITEM: // Membros
                membersGUI.openGUI(player);
                break;
            case DIAMOND_SWORD: // Alianças e Guerras
                relationsGUI.openGUI(player);
                break;
            case GOLD_BLOCK: // Banco
                handleBankClick(player, event.isShiftClick());
                break;
            case REDSTONE_COMPARATOR: // Configurações
                if (clan.isLeader(player.getUniqueId())) {
                    settingsGUI.openGUI(player);
                }
                break;
            case DRAGON_EGG: // Eventos
                eventsGUI.openGUI(player);
                break;
            case BARRIER: // Sair do Clã
                handleLeaveClick(player);
                break;
        }
    }

    private void handleNoClanClick(Player player, ItemStack clicked) {
        switch (clicked.getType()) {
            case NETHER_STAR: // Criar Clã
                createGUI.openGUI(player);
                break;
            case BOOK: // Lista de Clãs
                listGUI.openGUI(player);
                break;
        }
    }

    private void handleBaseClick(Player player, SpaceClan clan, boolean isShiftClick) {
        if (isShiftClick && (clan.isLeader(player.getUniqueId()) || clan.isOfficer(player.getUniqueId()))) {
            clan.setBase(null);
            player.sendMessage(ChatColor.GREEN + "Base do clã removida!");
            return;
        }

        if (clan.getBase() == null && clan.isLeader(player.getUniqueId())) {
            clan.setBase(player.getLocation());
            player.sendMessage(ChatColor.GREEN + "Base do clã definida em sua localização!");
            return;
        }

        if (clan.getBase() != null && clan.hasPermission(player.getUniqueId(), ClanPermission.TELEPORT_BASE)) {
            player.teleport(clan.getBase());
            player.sendMessage(ChatColor.GREEN + "Teleportado para a base do clã!");
        }
    }

    private void handleBankClick(Player player, boolean isShiftClick) {
        bankGUI.openGUI(player, isShiftClick);
    }

    private void handleLeaveClick(Player player) {
        SpaceClan clan = clanManager.getPlayerClan(player.getUniqueId());
        if (clan == null || clan.isLeader(player.getUniqueId())) return;

        player.closeInventory();
        player.sendMessage("");
        player.sendMessage(ChatColor.RED + "⚠ Você tem certeza que deseja sair do clã?");
        player.sendMessage(ChatColor.RED + "Digite /clan leave para confirmar");
        player.sendMessage("");
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        // Implementar se necessário
    }
} 