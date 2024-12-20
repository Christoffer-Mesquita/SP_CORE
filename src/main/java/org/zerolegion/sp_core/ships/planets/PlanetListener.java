package org.zerolegion.sp_core.ships.planets;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.zerolegion.sp_core.SP_CORE;

public class PlanetListener implements Listener {
    private final SP_CORE plugin;
    private final int MAX_DISTANCE = 100; // Distância máxima em blocos

    public PlanetListener(SP_CORE plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Planet planet = plugin.getPlanetManager().getPlayerPlanet(player.getUniqueId());

        if (planet != null) {
            Location planetCenter = planet.getCenter();
            Location playerLoc = player.getLocation();

            // Verificar se o jogador está muito longe do planeta
            if (planetCenter.distance(playerLoc) > MAX_DISTANCE) {
                // Teleportar jogador de volta ao spawn
                player.teleport(player.getWorld().getSpawnLocation());
                
                // Remover planeta e efeitos
                plugin.getPlanetManager().removePlayerPlanet(player);
                
                // Mensagens
                player.sendMessage("");
                player.sendMessage("§c✘ Você se afastou muito do seu planeta!");
                player.sendMessage("§7Você foi teleportado de volta ao spawn.");
                player.sendMessage("");
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        // Remover planeta quando o jogador deslogar
        plugin.getPlanetManager().removePlayerPlanet(player);
    }
} 