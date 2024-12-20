package org.zerolegion.sp_core.ships.planets;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.zerolegion.sp_core.SP_CORE;

public class PlanetCommandListener implements Listener {
    private final PlanetManager planetManager;

    public PlanetCommandListener(PlanetManager planetManager) {
        this.planetManager = planetManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String command = event.getMessage().toLowerCase();

        // Verifica se o jogador está em um planeta
        Planet playerPlanet = planetManager.getPlayerPlanet(player.getUniqueId());
        if (playerPlanet == null) return; // Não está em um planeta, permite todos os comandos

        // Lista de comandos permitidos
        if (command.startsWith("/nave voltar") || 
            command.equals("/nave") || 
            player.hasPermission("sp_core.planet.bypass")) {
            return; // Permite estes comandos
        }

        // Bloqueia todos os outros comandos
        event.setCancelled(true);
        player.sendMessage(ChatColor.RED + "Você não pode usar comandos enquanto estiver em um planeta!");
        player.sendMessage(ChatColor.YELLOW + "Use /nave voltar para retornar ao spawn.");
    }
} 