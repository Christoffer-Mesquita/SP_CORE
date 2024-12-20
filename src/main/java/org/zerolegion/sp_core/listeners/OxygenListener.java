package org.zerolegion.sp_core.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.zerolegion.sp_core.SP_CORE;

public class OxygenListener implements Listener {
    private final SP_CORE plugin;

    public OxygenListener(SP_CORE plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.getOxygenManager().checkOxygenTank(event.getPlayer());
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        // Agenda para dar o tanque após o respawn
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            plugin.getOxygenManager().giveOxygenTank(event.getPlayer());
        }, 1L);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        // Remove o tanque de oxigênio dos drops
        event.getDrops().removeIf(item -> plugin.getOxygenManager().isOxygenTank(item));
        plugin.getOxygenManager().handlePlayerDeath(event.getEntity());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        ItemStack item = event.getItemDrop().getItemStack();
        if (plugin.getOxygenManager().isOxygenTank(item)) {
            event.setCancelled(plugin.getOxygenManager().handleTankDrop(event.getPlayer()));
        }
    }

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        ItemStack item = event.getItem().getItemStack();
        if (plugin.getOxygenManager().isOxygenTank(item)) {
            plugin.getOxygenManager().handleTankPickup(event.getPlayer(), item);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        // Previne mover o tanque de oxigênio
        if (event.getCurrentItem() != null && plugin.getOxygenManager().isOxygenTank(event.getCurrentItem())) {
            event.setCancelled(true);
            ((Player) event.getWhoClicked()).updateInventory();
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        if (plugin.getOxygenManager().isOxygenTank(event.getItem())) {
            event.setCancelled(true);
            event.getPlayer().updateInventory();
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item != null && plugin.getOxygenManager().isOxygenTank(item)) {
            event.setCancelled(true);
            event.getPlayer().updateInventory();
        }
    }
} 