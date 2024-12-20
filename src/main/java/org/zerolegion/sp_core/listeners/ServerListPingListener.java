package org.zerolegion.sp_core.listeners;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;
import org.zerolegion.sp_core.SP_CORE;

public class ServerListPingListener implements Listener {
    private final SP_CORE plugin;
    private String line1;
    private String line2;
    private boolean maintenanceMode;
    private String maintenanceLine1;
    private String maintenanceLine2;

    public ServerListPingListener(SP_CORE plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        ConfigurationSection motd = plugin.getConfig().getConfigurationSection("motd");
        if (motd == null) return;

        // Carregar configurações normais
        line1 = colorize(motd.getConfigurationSection("line1").getString("text"));
        line2 = colorize(motd.getConfigurationSection("line2").getString("text"));

        // Carregar configurações de manutenção
        ConfigurationSection maintenance = motd.getConfigurationSection("maintenance");
        maintenanceMode = maintenance.getBoolean("enabled");
        maintenanceLine1 = colorize(maintenance.getString("line1"));
        maintenanceLine2 = colorize(maintenance.getString("line2"));
    }

    @EventHandler
    public void onServerPing(ServerListPingEvent event) {
        if (maintenanceMode) {
            event.setMotd(maintenanceLine1 + "\n" + maintenanceLine2);
            return;
        }

        String finalLine1 = replacePlaceholders(line1, event);
        String finalLine2 = replacePlaceholders(line2, event);
        event.setMotd(finalLine1 + "\n" + finalLine2);
    }

    private String replacePlaceholders(String text, ServerListPingEvent event) {
        return text
            .replace("%online%", String.valueOf(plugin.getServer().getOnlinePlayers().size()))
            .replace("%max%", String.valueOf(plugin.getServer().getMaxPlayers()));
    }

    private String colorize(String text) {
        if (text == null) return "";
        return ChatColor.translateAlternateColorCodes('&', text);
    }
} 