package org.zerolegion.sp_core.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.entity.Player;
import org.zerolegion.sp_core.SP_CORE;

public class PermissionListener implements Listener {
    private final SP_CORE plugin;

    public PermissionListener(SP_CORE plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.getPermissionManager().loadPlayer(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getPermissionManager().unloadPlayer(event.getPlayer());
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        // Carrega as permissões assim que o jogador tenta logar
        plugin.getPermissionManager().loadPlayer(event.getPlayer());
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        String command = event.getMessage().split(" ")[0].toLowerCase().substring(1); // Remove o /

        // Verifica se o jogador tem permissão para usar o comando
        if (!plugin.getPermissionManager().hasPermission(player, "command." + command)) {
            // Permite comandos básicos mesmo sem permissão
            if (!command.equals("help") && !command.equals("spawn") && !command.equals("lobby")) {
                event.setCancelled(true);
                player.sendMessage("§c⚠ Você não tem permissão para usar este comando!");
            }
        }
    }

    @EventHandler
    public void onPluginEnable(PluginEnableEvent event) {
        // Recarrega as permissões de todos os jogadores quando um plugin é ativado
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            plugin.getPermissionManager().loadPlayer(player);
        }
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent event) {
        // Se não for nosso plugin, recarrega as permissões
        if (!event.getPlugin().equals(plugin)) {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                plugin.getPermissionManager().loadPlayer(player);
            }
        }
    }
} 