package org.zerolegion.sp_core.ships.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.zerolegion.sp_core.ships.SpaceshipManager;

public class ShipShopListener implements Listener {
    private final SpaceshipManager spaceshipManager;
    private final ShipShopGUI shopGUI;

    public ShipShopListener(SpaceshipManager spaceshipManager, ShipShopGUI shopGUI) {
        this.spaceshipManager = spaceshipManager;
        this.shopGUI = shopGUI;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        
        spaceshipManager.getPlugin().getLogger().info("[DEBUG] Clique em inventário detectado - Jogador: " + player.getName());
        spaceshipManager.getPlugin().getLogger().info("[DEBUG] Slot clicado: " + event.getSlot());
        spaceshipManager.getPlugin().getLogger().info("[DEBUG] Inventário: " + event.getView().getTitle());
        
        if (event.getView().getTitle().equals(shopGUI.getInventoryTitle())) {
            event.setCancelled(true);
            spaceshipManager.getPlugin().getLogger().info("[DEBUG] Evento cancelado - é nosso inventário");
            
            if (event.getCurrentItem() != null) {
                shopGUI.handleClick(player, event.getInventory(), event.getSlot());
            }
        }
    }
} 