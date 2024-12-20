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

public class ClanSettingsGUIListener implements Listener {
    private final SP_CORE plugin;
    private final ClanManager clanManager;
    private final ClanSettingsGUI settingsGUI;
    private final ClanMainGUI mainGUI;

    public ClanSettingsGUIListener(SP_CORE plugin, ClanManager clanManager, ClanSettingsGUI settingsGUI, ClanMainGUI mainGUI) {
        this.plugin = plugin;
        this.clanManager = clanManager;
        this.settingsGUI = settingsGUI;
        this.mainGUI = mainGUI;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!event.getView().getTitle().equals(ChatColor.DARK_PURPLE + "✧ Configurações do Clã ✧")) return;

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null) return;

        SpaceClan clan = clanManager.getPlayerClan(player.getUniqueId());
        if (clan == null) return;

        if (!clan.isLeader(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Apenas o líder pode alterar as configurações do clã!");
            return;
        }

        String setting = null;
        switch (clicked.getType().name()) {
            case "IRON_DOOR":
                setting = "openJoin";
                break;
            case "DIAMOND_SWORD":
                setting = "friendlyFire";
                break;
            case "ENDER_PEARL":
                setting = "publicBase";
                break;
            case "COMPASS":
                setting = "allyTeleport";
                break;
            case "GOLD_INGOT":
                setting = "bankDeposit";
                break;
            case "EMERALD":
                setting = "bankWithdraw";
                break;
            case "ARROW":
                mainGUI.openGUI(player);
                return;
        }

        if (setting != null) {
            boolean currentValue = clan.getSetting(setting);
            clan.setSetting(setting, !currentValue);
            clanManager.saveClan(clan);
            settingsGUI.openGUI(player);
        }
    }
} 