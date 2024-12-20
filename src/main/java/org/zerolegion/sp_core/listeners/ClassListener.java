package org.zerolegion.sp_core.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.zerolegion.sp_core.SP_CORE;

public class ClassListener implements Listener {
    private final SP_CORE plugin;

    public ClassListener(SP_CORE plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        
        // Verifica se o jogador realmente se moveu (não apenas girou a cabeça)
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
            event.getFrom().getBlockY() == event.getTo().getBlockY() &&
            event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        // Verifica se o jogador está no chão
        if (player.isOnGround() && !plugin.getClassManager().hasClass(player)) {
            // Agenda para executar no próximo tick para garantir que tudo está carregado
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                plugin.getClassManager().loadPlayerClass(player);
            }, 1L);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        
        // Tenta processar o clique no seletor de classes
        if (plugin.getClassManager().getClassSelector().handleClick(player, event.getInventory(), event.getSlot())) {
            event.setCancelled(true);
        }
    }
} 