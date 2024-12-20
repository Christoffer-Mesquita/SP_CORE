package org.zerolegion.sp_core.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.zerolegion.sp_core.SP_CORE;

public class ChatListener implements Listener {
    private final SP_CORE plugin;

    public ChatListener(SP_CORE plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (event.isCancelled()) return;
        event.setCancelled(true);
        
        // Verifica se o jogador está no chat global
        if (plugin.getChatManager().isInGlobalChat(event.getPlayer())) {
            plugin.getChatManager().sendGlobalMessage(event.getPlayer(), event.getMessage());
        } else {
            plugin.getChatManager().sendLocalMessage(event.getPlayer(), event.getMessage());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getChatManager().clearPlayerCache(event.getPlayer().getUniqueId());
    }
} 