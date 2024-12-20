package org.zerolegion.sp_core.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.zerolegion.sp_core.SP_CORE;

public class LevelListener implements Listener {
    private final SP_CORE plugin;

    public LevelListener(SP_CORE plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.getLevelManager().loadPlayer(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getLevelManager().unloadPlayer(event.getPlayer());
    }
} 