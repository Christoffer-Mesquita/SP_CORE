package org.zerolegion.sp_core.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.zerolegion.sp_core.SP_CORE;

public class PlayerListener implements Listener {
    private final SP_CORE plugin;

    public PlayerListener(SP_CORE plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getStellarEconomyManager().loadPlayer(player.getUniqueId());
        plugin.getLogger().info("[ECONOMY] Carregando dados econômicos para " + player.getName());
        
        plugin.getSpaceshipManager().handlePlayerJoin(player);
        plugin.getLogger().info("[SHIPS] Carregando dados das naves para " + player.getName());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getStellarEconomyManager().unloadPlayer(player.getUniqueId());
        plugin.getLogger().info("[ECONOMY] Salvando e descarregando dados econômicos de " + player.getName());
        
        plugin.getSpaceshipManager().handlePlayerQuit(player);
        plugin.getLogger().info("[SHIPS] Salvando e descarregando dados das naves de " + player.getName());
    }
} 