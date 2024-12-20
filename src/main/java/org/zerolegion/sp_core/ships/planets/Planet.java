package org.zerolegion.sp_core.ships.planets;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.zerolegion.sp_core.SP_CORE;
import org.zerolegion.sp_core.ships.PlayerShip;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class Planet {
    private static final int MIN_DISTANCE_BETWEEN_PLANETS = 1000; // Distância mínima entre planetas
    private static final int MAX_COORDINATE = 10000; // Coordenada máxima para geração
    private static final Random random = new Random();
    
    private final Location center;
    private final int radius;
    private final Material mainMaterial;
    private final List<Block> blocks;
    private final Player owner;
    private final PlayerShip ship;
    private boolean isGenerated;
    private boolean isGenerating;
    private GameMode previousGameMode;

    public Planet(Location baseLocation, int radius, Material mainMaterial, Player owner, PlayerShip ship) {
        // Gerar coordenadas aleatórias distantes
        int x = (random.nextBoolean() ? 1 : -1) * (random.nextInt(MAX_COORDINATE) + MIN_DISTANCE_BETWEEN_PLANETS);
        int z = (random.nextBoolean() ? 1 : -1) * (random.nextInt(MAX_COORDINATE) + MIN_DISTANCE_BETWEEN_PLANETS);
        
        World planetWorld = SP_CORE.getInstance().getPlanetManager().getPlanetWorld();
        this.center = new Location(planetWorld, x, 200, z); // Y fixo em 200
        this.radius = radius;
        this.mainMaterial = mainMaterial;
        this.owner = owner;
        this.ship = ship;
        this.blocks = new ArrayList<>();
        this.isGenerated = false;
        this.isGenerating = false;
    }

    public void generate() {
        if (isGenerated || isGenerating) return;
        isGenerating = true;

        previousGameMode = owner.getGameMode();
        owner.setGameMode(GameMode.SPECTATOR);
        owner.setAllowFlight(true);
        owner.setFlying(true);
        
        // Posicionar o jogador para visualizar a geração
        Location viewLocation = center.clone().add(radius * 2, radius, 0);
        viewLocation.setDirection(center.clone().subtract(viewLocation).toVector());
        owner.teleport(viewLocation);

        owner.sendMessage(ChatColor.YELLOW + "Procurando um planeta adequado para você...");
        owner.sendMessage(ChatColor.GRAY + "Coordenadas do planeta: X: " + center.getBlockX() + ", Z: " + center.getBlockZ());
        
        // Lista para armazenar os blocos do topo
        List<Block> topBlocks = new ArrayList<>();
        int highestY = center.getBlockY() - radius; // Inicializa com o Y mais baixo do planeta

        List<Vector> positions = new ArrayList<>();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Vector position = new Vector(x, y, z);
                    if (position.length() <= radius) {
                        positions.add(position);
                        
                        // Verifica se é um bloco do topo
                        Location blockLoc = center.clone().add(x, y, z);
                        if (blockLoc.getBlockY() > highestY) {
                            highestY = blockLoc.getBlockY();
                            topBlocks.clear(); // Limpa a lista anterior se encontrou um Y mais alto
                        }
                        if (blockLoc.getBlockY() == highestY) {
                            topBlocks.add(blockLoc.getBlock());
                        }
                    }
                }
            }
        }

        final List<Block> finalTopBlocks = topBlocks;
        final int totalBlocks = positions.size();
        final int blocksPerTick = 100;
        AtomicInteger progress = new AtomicInteger(0);

        new BukkitRunnable() {
            int index = 0;

            @Override
            public void run() {
                for (int i = 0; i < blocksPerTick && index < positions.size(); i++) {
                    Vector pos = positions.get(index++);
                    Location blockLoc = center.clone().add(pos);
                    Block block = blockLoc.getBlock();
                    block.setType(mainMaterial);
                    blocks.add(block);

                    int currentProgress = (index * 100) / totalBlocks;
                    if (currentProgress % 20 == 0 && progress.get() != currentProgress) {
                        progress.set(currentProgress);
                        owner.sendMessage(ChatColor.GREEN + "Gerando planeta: " + currentProgress + "%");
                    }
                }

                if (index >= positions.size()) {
                    addOres();
                    isGenerated = true;
                    isGenerating = false;
                    
                    owner.setGameMode(previousGameMode);
                    owner.setWalkSpeed(0.2f);
                    owner.setFlySpeed(0.1f);
                    owner.setAllowFlight(previousGameMode == GameMode.CREATIVE || previousGameMode == GameMode.SPECTATOR);
                    
                    // Encontrar um bloco seguro no topo para teleportar
                    if (!finalTopBlocks.isEmpty()) {
                        Block spawnBlock = finalTopBlocks.get(random.nextInt(finalTopBlocks.size()));
                        Location spawnLoc = spawnBlock.getLocation().add(0.5, 1, 0.5); // Centro do bloco e 1 bloco acima
                        spawnLoc.setPitch(0); // Olhando para frente
                        owner.teleport(spawnLoc);
                    }
                    
                    owner.sendMessage(ChatColor.GREEN + "Planeta gerado com sucesso!");
                    owner.sendMessage(ChatColor.YELLOW + "Lembre-se: Apenas o comando /nave voltar está disponível aqui.");
                    owner.sendMessage(ChatColor.YELLOW + "Use /nave voltar para retornar ao spawn quando quiser.");
                    cancel();
                }
            }
        }.runTaskTimer(SP_CORE.getInstance(), 1L, 1L);
    }

    // Método para verificar se um local está muito próximo de outro planeta
    public boolean isTooCloseToOtherPlanet(Location location) {
        return center.distance(location) < MIN_DISTANCE_BETWEEN_PLANETS;
    }

    private void addOres() {
        Random random = new Random();
        int oreCount = (int) (blocks.size() * 0.1);
        Material[] ores = {Material.DIAMOND_ORE, Material.EMERALD_ORE, Material.GOLD_ORE, Material.IRON_ORE};

        for (int i = 0; i < oreCount; i++) {
            Block block = blocks.get(random.nextInt(blocks.size()));
            block.setType(ores[random.nextInt(ores.length)]);
        }
    }

    public void remove() {
        // Desativar modo de mineração
        PlanetManager planetManager = SP_CORE.getInstance().getPlanetManager();
        if (planetManager != null && planetManager.getPlanetMiningListener() != null) {
            planetManager.getPlanetMiningListener().deactivateMiningMode(owner);
        }

        // Remover blocos
        for (Block block : blocks) {
            block.setType(Material.AIR);
        }
        blocks.clear();
        isGenerated = false;
    }

    public Location getSpawnLocation() {
        // Encontrar o bloco mais alto do planeta
        int highestY = center.getBlockY() - radius;
        Block spawnBlock = null;

        for (Block block : blocks) {
            if (block.getLocation().getBlockY() > highestY) {
                highestY = block.getLocation().getBlockY();
                spawnBlock = block;
            }
        }

        if (spawnBlock != null) {
            return spawnBlock.getLocation().add(0.5, 1, 0.5); // Centro do bloco e 1 bloco acima
        }

        // Fallback para o spawn padrão se algo der errado
        return center.clone().add(0, radius + 1, 0);
    }

    public double getMiningBonus() {
        switch (ship.getTemplateId()) {
            case "miner_advanced":
                return 2.0;
            case "miner_basic":
                return 1.5;
            default:
                return 1.0;
        }
    }

    public Player getOwner() {
        return owner;
    }

    public PlayerShip getShip() {
        return ship;
    }

    public boolean isGenerated() {
        return isGenerated;
    }

    public Location getCenter() {
        return center;
    }

    public int getRadius() {
        return radius;
    }

    public boolean isGenerating() {
        return isGenerating;
    }

    public List<Block> getBlocks() {
        return blocks;
    }
} 