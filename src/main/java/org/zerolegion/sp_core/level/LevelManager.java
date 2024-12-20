package org.zerolegion.sp_core.level;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wither;
import org.bukkit.entity.EntityType;
import org.zerolegion.sp_core.SP_CORE;
import org.zerolegion.sp_core.level.placeholders.LevelPlaceholders;
import java.util.*;
import java.text.DecimalFormat;

public class LevelManager {
    private final SP_CORE plugin;
    private final MongoCollection<Document> levelsCollection;
    private final Map<UUID, Integer> playerLevels;
    private final Map<UUID, Integer> bossBarEntities;
    private final DecimalFormat formatter;
    private LevelPlaceholders placeholders;

    public LevelManager(SP_CORE plugin) {
        this.plugin = plugin;
        this.levelsCollection = plugin.getDatabase().getCollection("player_levels");
        this.playerLevels = new HashMap<>();
        this.bossBarEntities = new HashMap<>();
        this.formatter = new DecimalFormat("#,###");
        
        // Inicia a task de atualização da barra
        startUpdateTask();

        // Registrar placeholders se o PlaceholderAPI estiver presente
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            this.placeholders = new LevelPlaceholders(this);
            this.placeholders.register();
            plugin.getLogger().info("Placeholders do sistema de level registrados com sucesso!");
        }
    }

    private void startUpdateTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                updateBossBar(player);
            }
        }, 20L, 20L); // Atualiza a cada 1 segundo
    }

    public void loadPlayer(Player player) {
        UUID playerId = player.getUniqueId();
        Document doc = levelsCollection.find(new Document("uuid", playerId.toString())).first();
        
        int level;
        if (doc == null) {
            // Jogador novo, cria dados iniciais
            level = 1;
            levelsCollection.insertOne(new Document("uuid", playerId.toString())
                .append("level", 1));
        } else {
            level = doc.getInteger("level", 1);
        }
        
        playerLevels.put(playerId, level);
        updateBossBar(player);
    }

    public void unloadPlayer(Player player) {
        UUID playerId = player.getUniqueId();
        Integer level = playerLevels.remove(playerId);
        
        if (level != null) {
            // Remove a barra de boss
            removeBossBar(player);
            
            // Salva os dados no MongoDB
            levelsCollection.updateOne(
                new Document("uuid", playerId.toString()),
                Updates.set("level", level),
                new UpdateOptions().upsert(true)
            );
        }
    }

    private void removeBossBar(Player player) {
        Integer entityId = bossBarEntities.remove(player.getUniqueId());
        if (entityId != null) {
            for (org.bukkit.entity.Entity entity : player.getWorld().getEntities()) {
                if (entity.getEntityId() == entityId && entity instanceof Wither) {
                    entity.remove();
                    break;
                }
            }
        }
    }

    public void setLevel(Player player, int newLevel) {
        if (newLevel < 1) newLevel = 1;
        
        UUID playerId = player.getUniqueId();
        int oldLevel = playerLevels.getOrDefault(playerId, 1);
        
        playerLevels.put(playerId, newLevel);
        
        // Se o nível mudou
        if (newLevel != oldLevel) {
            String levelMessage = "\n" +
                ChatColor.AQUA + "❈ " + ChatColor.DARK_AQUA + "Level " + 
                (newLevel > oldLevel ? "Up!" : "Down!") + ChatColor.AQUA + " ❈\n" +
                ChatColor.GRAY + "Nível " + ChatColor.YELLOW + oldLevel + 
                ChatColor.GRAY + " ➜ " + ChatColor.YELLOW + newLevel + "\n" +
                ChatColor.AQUA + "▬▬▬▬▬▬▬▬▬▬▬��▬▬▬▬▬▬▬▬\n";
            
            player.sendMessage(levelMessage);

            
            // Atualiza a barra e salva no MongoDB
            updateBossBar(player);
            saveLevelData(playerId, newLevel);
        }
    }

    private void saveLevelData(UUID playerId, int level) {
        levelsCollection.updateOne(
            new Document("uuid", playerId.toString()),
            Updates.set("level", level),
            new UpdateOptions().upsert(true)
        );
    }

    private void updateBossBar(Player player) {
        Integer level = playerLevels.get(player.getUniqueId());
        if (level == null) return;

        String levelText = ChatColor.AQUA + "❈ " + ChatColor.DARK_AQUA + "Level " + 
            ChatColor.YELLOW + formatter.format(level) + ChatColor.AQUA + " ❈";

        // Remove a barra antiga se existir
        removeBossBar(player);

        // Cria uma nova barra
        Location loc = player.getLocation().clone();
        loc.setY(-500); // Esconde o Wither bem longe
        
        Wither wither = (Wither) player.getWorld().spawnEntity(loc, EntityType.WITHER);
        wither.setCustomName(levelText);
        wither.setCustomNameVisible(true);
        wither.setHealth(wither.getMaxHealth()); // Barra cheia
        
        // Guarda o ID da entidade para remover depois
        bossBarEntities.put(player.getUniqueId(), wither.getEntityId());
    }

    public int getPlayerLevel(UUID playerId) {
        return playerLevels.getOrDefault(playerId, 1);
    }
} 