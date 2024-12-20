package org.zerolegion.sp_core.ships.effects;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.zerolegion.sp_core.SP_CORE;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ShipEffectManager {
    private final SP_CORE plugin;
    private final Map<UUID, ShipEffect> playerEffects;
    private BukkitTask updateTask;

    public ShipEffectManager(SP_CORE plugin) {
        this.plugin = plugin;
        this.playerEffects = new HashMap<>();
        startUpdateTask();
    }

    private void startUpdateTask() {
        updateTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (Map.Entry<UUID, ShipEffect> entry : playerEffects.entrySet()) {
                Player player = plugin.getServer().getPlayer(entry.getKey());
                if (player != null && player.isOnline()) {
                    updateEffectPosition(player, entry.getValue());
                }
            }
        }, 1L, 1L);
    }

    public void createEffect(Player player, String shipName) {
        removeEffect(player); // Remove efeito existente se houver

        Location loc = player.getLocation();
        
        // Cria o Blaze
        Blaze blaze = (Blaze) player.getWorld().spawnEntity(loc, EntityType.BLAZE);
        blaze.setAI(false);
        blaze.setInvulnerable(true);
        blaze.setSilent(true);
        blaze.setMetadata("ship_effect", new FixedMetadataValue(plugin, player.getUniqueId().toString()));
        
        // Cria o holograma com o nome da nave
        ArmorStand hologram = (ArmorStand) player.getWorld().spawnEntity(loc.add(0, 2.5, 0), EntityType.ARMOR_STAND);
        hologram.setVisible(false);
        hologram.setGravity(false);
        hologram.setCustomNameVisible(true);
        hologram.setCustomName(ChatColor.LIGHT_PURPLE + "✧ " + ChatColor.WHITE + shipName + ChatColor.LIGHT_PURPLE + " ✧");
        hologram.setMetadata("ship_hologram", new FixedMetadataValue(plugin, player.getUniqueId().toString()));
        
        // Salva as entidades
        playerEffects.put(player.getUniqueId(), new ShipEffect(blaze, hologram));
        
        plugin.getLogger().info("[DEBUG] Efeito de nave criado para " + player.getName());
    }

    public void removeEffect(Player player) {
        ShipEffect effect = playerEffects.remove(player.getUniqueId());
        if (effect != null) {
            effect.getBlaze().remove();
            effect.getHologram().remove();
            plugin.getLogger().info("[DEBUG] Efeito de nave removido para " + player.getName());
        }
    }

    private void updateEffectPosition(Player player, ShipEffect effect) {
        if (effect == null || player == null) return;

        Location playerLoc = player.getLocation();
        Vector direction = playerLoc.getDirection().multiply(-1); // Atrás do jogador
        
        // Posição do Blaze
        Location blazeLoc = playerLoc.clone().add(direction.getX(), 0, direction.getZ());
        effect.getBlaze().teleport(blazeLoc);
        
        // Posição do holograma
        Location holoLoc = blazeLoc.clone().add(0, 2.5, 0);
        effect.getHologram().teleport(holoLoc);
    }

    public void onDisable() {
        if (updateTask != null) {
            updateTask.cancel();
        }
        
        // Remove todos os efeitos
        for (ShipEffect effect : playerEffects.values()) {
            effect.getBlaze().remove();
            effect.getHologram().remove();
        }
        playerEffects.clear();
    }

    private static class ShipEffect {
        private final Blaze blaze;
        private final ArmorStand hologram;

        public ShipEffect(Blaze blaze, ArmorStand hologram) {
            this.blaze = blaze;
            this.hologram = hologram;
        }

        public Blaze getBlaze() {
            return blaze;
        }

        public ArmorStand getHologram() {
            return hologram;
        }
    }
} 