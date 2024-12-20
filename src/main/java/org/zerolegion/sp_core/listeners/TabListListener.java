package org.zerolegion.sp_core.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.zerolegion.sp_core.SP_CORE;
import org.bukkit.entity.Player;

public class TabListListener implements Listener {
    private final SP_CORE plugin;

    public TabListListener(SP_CORE plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getTabListManager().updateTabList(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getTabListManager().removePlayer(player);
    }
} 