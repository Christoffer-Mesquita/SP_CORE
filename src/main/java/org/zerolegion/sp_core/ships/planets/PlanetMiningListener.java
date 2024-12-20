package org.zerolegion.sp_core.ships.planets;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.zerolegion.sp_core.ships.PlayerShip;
import org.zerolegion.sp_core.economy.StellarEconomyManager;
import org.zerolegion.sp_core.SP_CORE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class PlanetMiningListener implements Listener {
    private final PlanetManager planetManager;
    private final HashMap<UUID, Long> lastMiningMessage = new HashMap<>();
    private final HashMap<UUID, Long> lastFuelWarning = new HashMap<>();
    private final HashMap<UUID, Boolean> miningMode = new HashMap<>();
    private static final long MESSAGE_COOLDOWN = 5000; // 5 segundos de cooldown entre mensagens
    private static final long POWER_COOLDOWN = 10000; // 10 segundos entre cada poder
    private static final long FUEL_WARNING_COOLDOWN = 30000; // 30 segundos entre avisos de combustível
    private static final double POWER_FUEL_COST = 2.0; // 2% de combustível por uso
    private final HashMap<UUID, Long> lastPowerUse = new HashMap<>();

    // Tracking de mineração
    private final HashMap<UUID, Integer> blocksBroken = new HashMap<>();
    private final HashMap<UUID, Double> creditsEarned = new HashMap<>();
    private final HashMap<UUID, Long> missionStartTime = new HashMap<>();

    // Lista de materiais que devem ser removidos
    private static final Set<Material> PLANET_MATERIALS = new HashSet<Material>() {{
        // Blocos básicos
        add(Material.STONE);
        add(Material.COBBLESTONE);
        
        // Minérios
        add(Material.DIAMOND_ORE);
        add(Material.EMERALD_ORE);
        add(Material.GOLD_ORE);
        add(Material.IRON_ORE);
        add(Material.COAL_ORE);
        add(Material.REDSTONE_ORE);
        add(Material.LAPIS_ORE);
        
        // Itens processados
        add(Material.DIAMOND);
        add(Material.EMERALD);
        add(Material.GOLD_INGOT);
        add(Material.IRON_INGOT);
        add(Material.COAL);
        add(Material.REDSTONE);
    }};

    // Valores base de créditos por minério
    private static final HashMap<Material, Double> ORE_VALUES = new HashMap<Material, Double>() {{
        // Minérios
        put(Material.DIAMOND_ORE, 50.0);
        put(Material.EMERALD_ORE, 45.0);
        put(Material.GOLD_ORE, 30.0);
        put(Material.IRON_ORE, 20.0);
        put(Material.COAL_ORE, 10.0);
        put(Material.REDSTONE_ORE, 15.0);
        put(Material.LAPIS_ORE, 25.0);
        
        // Itens processados
        put(Material.DIAMOND, 50.0);
        put(Material.EMERALD, 45.0);
        put(Material.GOLD_INGOT, 30.0);
        put(Material.IRON_INGOT, 20.0);
        put(Material.COAL, 10.0);
        put(Material.REDSTONE, 15.0);

        // Blocos básicos
        put(Material.STONE, 1.0);
        put(Material.COBBLESTONE, 1.0);
    }};

    public PlanetMiningListener(PlanetManager planetManager) {
        this.planetManager = planetManager;
        
        // Inicia o verificador de inventário
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : planetManager.getPlanetWorld().getPlayers()) {
                    if (player.getWorld().getName().equals("planetas")) {
                        // Remove apenas materiais do planeta do inventário
                        for (int i = 0; i < player.getInventory().getSize(); i++) {
                            if (player.getInventory().getItem(i) != null) {
                                Material type = player.getInventory().getItem(i).getType();
                                if (PLANET_MATERIALS.contains(type)) {
                                    player.getInventory().setItem(i, null);
                                }
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(SP_CORE.getInstance(), 20L, 20L); // Verifica a cada segundo
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        
        // Verifica se está no mundo dos planetas
        if (!event.getBlock().getWorld().getName().equals("planetas")) {
            return;
        }

        Planet planet = planetManager.getPlayerPlanet(player.getUniqueId());
        if (planet == null) {
            return;
        }

        // Verifica se o bloco quebrado pertence ao planeta
        if (!planet.getBlocks().contains(event.getBlock())) {
            return;
        }

        // Ativa o modo de mineração se ainda não estiver ativo
        if (!miningMode.getOrDefault(player.getUniqueId(), false)) {
            activateMiningMode(player, planet);
        }

        // Processa a mineração e dá os créditos
        processMining(player, planet, event.getBlock().getType());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemPickup(PlayerPickupItemEvent event) {
        // Verifica se está no mundo dos planetas
        if (event.getPlayer().getWorld().getName().equals("planetas")) {
            Material type = event.getItem().getItemStack().getType();
            // Cancela apenas se for material do planeta
            if (PLANET_MATERIALS.contains(type)) {
                event.setCancelled(true);
                event.getItem().remove();
                
                try {
                } catch (Exception e) {
                    // Ignora se o som não existir
                }
            }
        }
    }

    private void activateMiningMode(Player player, Planet planet) {
        miningMode.put(player.getUniqueId(), true);
        lastPowerUse.put(player.getUniqueId(), System.currentTimeMillis());
        
        // Iniciar tracking da missão
        blocksBroken.put(player.getUniqueId(), 0);
        creditsEarned.put(player.getUniqueId(), 0.0);
        missionStartTime.put(player.getUniqueId(), System.currentTimeMillis());
        
        // Iniciar verificador de poder especial
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!miningMode.getOrDefault(player.getUniqueId(), false)) {
                    cancel();
                    return;
                }

                tryUsePower(player, planet);
            }
        }.runTaskTimer(SP_CORE.getInstance(), 20L, 20L);
        
        player.sendMessage("");
        player.sendMessage(ChatColor.YELLOW + "⚡ Modo de Mineração Ativado!");
        player.sendMessage(ChatColor.GRAY + "Sua nave está fornecendo suporte para mineração.");
        
        PlayerShip ship = planet.getShip();
        if (ship != null) {
            String shipType = ship.getTemplateId();
            switch (shipType) {
                case "miner_advanced":
                    player.sendMessage(ChatColor.GREEN + "Bônus de Nave Avançada: +100% de créditos");
                    player.sendMessage(ChatColor.AQUA + "Poder Especial: Quebra 5 blocos a cada 10 segundos!");
                    player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, Integer.MAX_VALUE, 1));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0));
                    break;
                case "miner_basic":
                    player.sendMessage(ChatColor.GREEN + "Bônus de Nave Básica: +50% de créditos");
                    player.sendMessage(ChatColor.AQUA + "Poder Especial: Quebra 3 blocos a cada 15 segundos!");
                    player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, Integer.MAX_VALUE, 0));
                    break;
            }
        }
        
        player.sendMessage("");
    }

    private void tryUsePower(Player player, Planet planet) {
        PlayerShip ship = planet.getShip();
        if (ship == null) return;

        // Verificar se o jogador ainda está no mundo dos planetas
        if (!player.getWorld().getName().equals("planetas")) {
            deactivateMiningMode(player);
            return;
        }

        // Verificar cooldown
        long currentTime = System.currentTimeMillis();
        long lastUse = lastPowerUse.getOrDefault(player.getUniqueId(), 0L);
        long cooldown = ship.getTemplateId().equals("miner_advanced") ? POWER_COOLDOWN : POWER_COOLDOWN + 5000;

        if (currentTime - lastUse < cooldown) return;

        // Verificar combustível
        if (ship.getFuel() < POWER_FUEL_COST) {
            // Verificar cooldown da mensagem de combustível
            Long lastWarning = lastFuelWarning.get(player.getUniqueId());
            if (lastWarning == null || currentTime - lastWarning >= FUEL_WARNING_COOLDOWN) {
                player.sendMessage(ChatColor.RED + "⚠ Combustível insuficiente para usar o poder especial!");
                lastFuelWarning.put(player.getUniqueId(), currentTime);
            }
            return;
        }

        // Usar combustível
        ship.useFuel(POWER_FUEL_COST);

        // Pegar blocos próximos ao jogador
        Location playerLoc = player.getLocation();
        List<Block> nearbyBlocks = new ArrayList<>();
        int radius = 2;
        int maxBlocks = ship.getTemplateId().equals("miner_advanced") ? 5 : 3;

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block block = playerLoc.clone().add(x, y, z).getBlock();
                    if (planet.getBlocks().contains(block) && block.getType() != Material.AIR) {
                        nearbyBlocks.add(block);
                        if (nearbyBlocks.size() >= maxBlocks) break;
                    }
                }
                if (nearbyBlocks.size() >= maxBlocks) break;
            }
            if (nearbyBlocks.size() >= maxBlocks) break;
        }

        // Quebrar blocos e dar recompensas
        if (!nearbyBlocks.isEmpty()) {
            player.spawnParticle(Particle.EXPLOSION_LARGE, player.getLocation(), 1);

            for (Block block : nearbyBlocks) {
                Material type = block.getType();
                processMining(player, planet, type);
                block.setType(Material.AIR);
            }

            player.sendMessage(ChatColor.AQUA + "⚡ Poder especial ativado! " + 
                ChatColor.GRAY + nearbyBlocks.size() + " blocos quebrados! " +
                ChatColor.YELLOW + String.format("(-%.1f%% combustível)", POWER_FUEL_COST));
        }

        lastPowerUse.put(player.getUniqueId(), currentTime);
    }

    private void processMining(Player player, Planet planet, Material minedMaterial) {
        // Verificar se o jogador ainda está no mundo dos planetas
        if (!player.getWorld().getName().equals("planetas")) {
            deactivateMiningMode(player);
            return;
        }

        // Incrementar contador de blocos
        blocksBroken.merge(player.getUniqueId(), 1, Integer::sum);
        
        double baseValue = ORE_VALUES.getOrDefault(minedMaterial, 1.0);
        PlayerShip ship = planet.getShip();
        double bonus = 1.0;
        
        if (ship != null) {
            switch (ship.getTemplateId()) {
                case "miner_advanced":
                    bonus = 2.0;
                    break;
                case "miner_basic":
                    bonus = 1.5;
                    break;
            }
        }

        double finalValue = baseValue * bonus;
        
        // Adicionar ao total de créditos ganhos
        creditsEarned.merge(player.getUniqueId(), finalValue, Double::sum);

        // Adicionar créditos ao jogador
        StellarEconomyManager economyManager = SP_CORE.getInstance().getStellarEconomyManager();
        economyManager.addBalance(player.getUniqueId(), finalValue);

        // Efeitos visuais e sonoros

        // Enviar mensagem (com cooldown)
        long currentTime = System.currentTimeMillis();
        Long lastMessage = lastMiningMessage.get(player.getUniqueId());
        
        if (lastMessage == null || currentTime - lastMessage >= MESSAGE_COOLDOWN) {
            String materialName = formatMaterialName(minedMaterial);
            player.sendMessage(ChatColor.GREEN + "⛏ Você minerou " + materialName + 
                ChatColor.GREEN + " e ganhou " + ChatColor.YELLOW + 
                economyManager.formatValue(finalValue) + " ⭐");
            lastMiningMessage.put(player.getUniqueId(), currentTime);
        }
    }

    private String formatMaterialName(Material material) {
        String name = material.name().replace("_ORE", "").replace("_", " ");
        return ChatColor.YELLOW + name.substring(0, 1) + name.substring(1).toLowerCase();
    }

    public void showMissionReport(Player player, Planet planet) {
        UUID playerId = player.getUniqueId();
        if (!blocksBroken.containsKey(playerId)) return;

        int totalBlocks = blocksBroken.get(playerId);
        double totalCredits = creditsEarned.get(playerId);
        long missionTime = System.currentTimeMillis() - missionStartTime.get(playerId);
        long minutesSpent = missionTime / 60000;

        PlayerShip ship = planet.getShip();
        double fuelLeft = ship != null ? ship.getFuel() : 0;
        double fuelUsed = ship != null ? (100 - ship.getFuel()) : 0;

        player.sendMessage("");
        player.sendMessage(ChatColor.YELLOW + "═══════ " + ChatColor.WHITE + "Relatório da Missão" + ChatColor.YELLOW + " ═══════");
        player.sendMessage(ChatColor.GRAY + "Tempo de mineração: " + ChatColor.WHITE + minutesSpent + " minutos");
        player.sendMessage(ChatColor.GRAY + "Blocos minerados: " + ChatColor.WHITE + totalBlocks);
        player.sendMessage(ChatColor.GRAY + "Créditos ganhos: " + ChatColor.YELLOW + SP_CORE.getInstance().getStellarEconomyManager().formatValue(totalCredits) + " ⭐");
        player.sendMessage(ChatColor.GRAY + "Combustível gasto: " + ChatColor.RED + String.format("%.1f%%", fuelUsed));
        player.sendMessage(ChatColor.GRAY + "Combustível restante: " + ChatColor.GREEN + String.format("%.1f%%", fuelLeft));
        player.sendMessage(ChatColor.YELLOW + "═══════════════════════════");
        player.sendMessage("");

        // Limpar dados da missão
        blocksBroken.remove(playerId);
        creditsEarned.remove(playerId);
        missionStartTime.remove(playerId);
    }

    public void deactivateMiningMode(Player player) {
        miningMode.remove(player.getUniqueId());
        lastPowerUse.remove(player.getUniqueId());
        lastFuelWarning.remove(player.getUniqueId());
        player.removePotionEffect(PotionEffectType.FAST_DIGGING);
        player.removePotionEffect(PotionEffectType.NIGHT_VISION);
        player.setFallDistance(0); // Evitar dano de queda ao teleportar
    }
} 