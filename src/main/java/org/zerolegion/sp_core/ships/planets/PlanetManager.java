package org.zerolegion.sp_core.ships.planets;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.zerolegion.sp_core.SP_CORE;
import org.zerolegion.sp_core.ships.PlayerHangar;
import org.zerolegion.sp_core.ships.PlayerShip;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class PlanetManager {
    private final SP_CORE plugin;
    private final World planetWorld;
    private final Map<UUID, Planet> activePlanets;
    private final Random random;
    private Location spawnLocation;
    private final PlanetMiningListener planetMiningListener;
    private static final String PLANET_WORLD_NAME = "planetas";
    private File spawnFile;

    public PlanetManager(SP_CORE plugin) {
        this.plugin = plugin;
        this.planetWorld = createPlanetWorld();
        this.activePlanets = new HashMap<>();
        this.random = new Random();
        this.planetMiningListener = new PlanetMiningListener(this);
        
        // Carregar spawn location
        loadSpawnLocation();
        
        // Registrar listeners
        plugin.getServer().getPluginManager().registerEvents(planetMiningListener, plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlanetCommandListener(this), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlanetDamageListener(), plugin);
    }

    private World createPlanetWorld() {
        // Verificar se o mundo já existe
        World world = plugin.getServer().getWorld(PLANET_WORLD_NAME);
        
        if (world == null) {
            plugin.getLogger().info("Criando mundo vazio para planetas...");
            
            // Criar configurações do mundo
            WorldCreator creator = new WorldCreator(PLANET_WORLD_NAME);
            creator.generator(new EmptyWorldGenerator());
            creator.environment(World.Environment.NORMAL);
            creator.generateStructures(false);
            creator.type(WorldType.FLAT);
            
            // Criar o mundo
            world = creator.createWorld();
            
            if (world != null) {
                // Configurar o mundo
                world.setGameRuleValue("doDaylightCycle", "false");
                world.setGameRuleValue("doWeatherCycle", "false");
                world.setGameRuleValue("doMobSpawning", "false");
                world.setTime(6000); // Meio-dia
                world.setDifficulty(Difficulty.PEACEFUL);
                world.setSpawnLocation(0, 500, 0);
                
                plugin.getLogger().info("Mundo de planetas criado com sucesso!");
            } else {
                plugin.getLogger().severe("Falha ao criar mundo de planetas!");
            }
        }
        
        return world;
    }

    private void loadSpawnLocation() {
        spawnFile = new File(plugin.getDataFolder(), "spawn.yml");
        if (!spawnFile.exists()) {
            spawnLocation = plugin.getServer().getWorlds().get(0).getSpawnLocation();
            saveSpawnLocation();
        } else {
            FileConfiguration config = YamlConfiguration.loadConfiguration(spawnFile);
            World world = plugin.getServer().getWorld(config.getString("world"));
            if (world != null) {
                spawnLocation = new Location(
                    world,
                    config.getDouble("x"),
                    config.getDouble("y"),
                    config.getDouble("z"),
                    (float) config.getDouble("yaw"),
                    (float) config.getDouble("pitch")
                );
            } else {
                spawnLocation = plugin.getServer().getWorlds().get(0).getSpawnLocation();
            }
        }
    }

    private void saveSpawnLocation() {
        FileConfiguration config = new YamlConfiguration();
        config.set("world", spawnLocation.getWorld().getName());
        config.set("x", spawnLocation.getX());
        config.set("y", spawnLocation.getY());
        config.set("z", spawnLocation.getZ());
        config.set("yaw", spawnLocation.getYaw());
        config.set("pitch", spawnLocation.getPitch());
        
        try {
            config.save(spawnFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Erro ao salvar localização do spawn: " + e.getMessage());
        }
    }

    public void setSpawnLocation(Location location) {
        this.spawnLocation = location.clone();
        saveSpawnLocation();
    }

    public Location getSpawnLocation() {
        return spawnLocation.clone();
    }

    // Gerador de mundo vazio
    private static class EmptyWorldGenerator extends ChunkGenerator {
        @Override
        public ChunkData generateChunkData(World world, Random random, int x, int z, BiomeGrid biome) {
            return createChunkData(world);
        }
    }

    public void teleportPlayerToPlanet(Player player) {
        PlayerHangar hangar = plugin.getSpaceshipManager().getPlayerHangar(player.getUniqueId());
        if (hangar == null || hangar.getShips().isEmpty()) {
            player.sendMessage("§c✘ Você precisa de uma nave para viajar!");
            return;
        }

        PlayerShip ship = hangar.getShip(0);
        if (ship == null) {
            player.sendMessage("§c✘ Erro ao carregar sua nave!");
            return;
        }

        if (ship.getFuel() < 10) {
            player.sendMessage("§c✘ Sua nave precisa de pelo menos 10% de combustível para viajar!");
            return;
        }

        if (activePlanets.containsKey(player.getUniqueId())) {
            player.sendMessage("§c✘ Você já tem um planeta ativo!");
            player.sendMessage("§7Use /nave voltar para retornar ao spawn primeiro.");
            return;
        }

        try {
            // Criar novo planeta
            Planet planet = new Planet(planetWorld.getSpawnLocation(), 15, getMaterialForShip(ship), player, ship);
            planet.generate();
            activePlanets.put(player.getUniqueId(), planet);

            // Consumir combustível
            ship.useFuel(10);
            plugin.getSpaceshipManager().savePlayerHangar(player.getUniqueId());

            // Mensagens
            player.sendMessage("");
            player.sendMessage("§a✔ Preparando viagem para seu planeta pessoal!");
            player.sendMessage("§7Bônus de mineração: §f" + String.format("%.1f%%", (planet.getMiningBonus() - 1.0) * 100));
            player.sendMessage("§7Combustível restante: §f" + String.format("%.1f%%", ship.getFuel()));
            player.sendMessage("");

        } catch (Exception e) {
            plugin.getLogger().severe("[ERROR] Erro ao gerar planeta para " + player.getName() + ": " + e.getMessage());
            player.sendMessage("§c✘ Ocorreu um erro ao gerar seu planeta!");
            
            Planet planet = activePlanets.get(player.getUniqueId());
            if (planet != null) {
                planet.remove();
                activePlanets.remove(player.getUniqueId());
            }
        }
    }

    private Material getMaterialForShip(PlayerShip ship) {
        switch (ship.getTemplateId()) {
            case "miner_advanced":
                return Material.STONE;
            case "miner_basic":
                return Material.STONE;
            default:
                return Material.STONE;
        }
    }

    public World getPlanetWorld() {
        return planetWorld;
    }

    public void removePlayerPlanet(Player player) {
        Planet planet = activePlanets.get(player.getUniqueId());
        if (planet != null) {
            // Remover planeta
            plugin.getLogger().info("[DEBUG] Removendo planeta do jogador " + player.getName());
            planet.remove();
            activePlanets.remove(player.getUniqueId());

            // Remover efeitos
            player.removePotionEffect(PotionEffectType.FAST_DIGGING);
            player.removePotionEffect(PotionEffectType.NIGHT_VISION);

            // Teleportar para o spawn configurado
            player.teleport(spawnLocation);

            // Salvar dados da nave (combustível)
            PlayerHangar hangar = plugin.getSpaceshipManager().getPlayerHangar(player.getUniqueId());
            if (hangar != null) {
                plugin.getSpaceshipManager().savePlayerHangar(player.getUniqueId());
            }
        }
    }

    public Planet getPlayerPlanet(UUID playerId) {
        return activePlanets.get(playerId);
    }

    public void onDisable() {
        plugin.getLogger().info("[DEBUG] Removendo todos os planetas ativos...");
        // Remover todos os planetas ao desligar o servidor
        for (Map.Entry<UUID, Planet> entry : activePlanets.entrySet()) {
            plugin.getLogger().info("[DEBUG] Removendo planeta do jogador " + entry.getKey());
            entry.getValue().remove();
        }
        activePlanets.clear();
        plugin.getLogger().info("[DEBUG] Todos os planetas foram removidos!");
    }

    public PlanetMiningListener getPlanetMiningListener() {
        return planetMiningListener;
    }

    public SP_CORE getPlugin() {
        return plugin;
    }

    // Classe interna para lidar com dano nos planetas
    private class PlanetDamageListener implements Listener {
        @EventHandler(priority = EventPriority.HIGHEST)
        public void onEntityDamage(EntityDamageEvent event) {
            if (!(event.getEntity() instanceof Player)) return;
            
            // Verifica se está no mundo dos planetas
            if (!event.getEntity().getWorld().getName().equals(PLANET_WORLD_NAME)) return;
            
            // Cancela qualquer tipo de dano, exceto o do sistema de oxigênio
            if (event.getCause() != EntityDamageEvent.DamageCause.CUSTOM) {
                event.setCancelled(true);
            }
        }
    }
} 