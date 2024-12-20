package org.zerolegion.sp_core.ships.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.zerolegion.sp_core.SP_CORE;
import org.zerolegion.sp_core.ships.SpaceshipManager;
import org.zerolegion.sp_core.ships.PlayerShip;

public class ShipEffectListener implements Listener {
    private final SP_CORE plugin;
    private final SpaceshipManager spaceshipManager;

    public ShipEffectListener(SP_CORE plugin) {
        this.plugin = plugin;
        this.spaceshipManager = plugin.getSpaceshipManager();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            PlayerShip ship = spaceshipManager.getActiveShip(player.getUniqueId());
            if (ship != null) {
                spaceshipManager.getShipEffectManager().createEffect(player, ship.getName());
                plugin.getLogger().info("[DEBUG] Criando efeito de nave para " + player.getName() + " ao entrar");
            }
        }, 20L); // Delay de 1 segundo para garantir que tudo carregou
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        spaceshipManager.getShipEffectManager().removeEffect(player);
        plugin.getLogger().info("[DEBUG] Removendo efeito de nave para " + player.getName() + " ao sair");
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        PlayerShip ship = spaceshipManager.getActiveShip(player.getUniqueId());
        
        if (ship != null) {
            // Remove o efeito antigo e cria um novo na nova localização
            spaceshipManager.getShipEffectManager().removeEffect(player);
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) {
                    spaceshipManager.getShipEffectManager().createEffect(player, ship.getName());
                    plugin.getLogger().info("[DEBUG] Recriando efeito de nave para " + player.getName() + " após teleporte");
                }
            }, 5L);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        spaceshipManager.getShipEffectManager().removeEffect(player);
        plugin.getLogger().info("[DEBUG] Removendo efeito de nave para " + player.getName() + " ao morrer");
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                PlayerShip ship = spaceshipManager.getActiveShip(player.getUniqueId());
                if (ship != null) {
                    spaceshipManager.getShipEffectManager().createEffect(player, ship.getName());
                    plugin.getLogger().info("[DEBUG] Recriando efeito de nave para " + player.getName() + " ao renascer");
                }
            }
        }, 20L);
    }
} 