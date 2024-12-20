package org.zerolegion.sp_core.oxygen;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.zerolegion.sp_core.SP_CORE;

import java.util.*;

public class OxygenManager {
    private final SP_CORE plugin;
    private final Set<UUID> playersWithoutOxygen;
    private final Set<UUID> warnedPlayers;
    private final int OXYGEN_SLOT = 8; // Slot 9 (0-based index)
    private final int DAMAGE_INTERVAL = 40; // 2 segundos (40 ticks)
    private final int DAMAGE_AMOUNT = 2; // 1 coração

    public OxygenManager(SP_CORE plugin) {
        this.plugin = plugin;
        this.playersWithoutOxygen = new HashSet<>();
        this.warnedPlayers = new HashSet<>();
        startDamageTask();
    }

    private void startDamageTask() {
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (UUID uuid : playersWithoutOxygen) {
                Player player = plugin.getServer().getPlayer(uuid);
                if (player != null && player.isOnline()) {
                    player.damage(DAMAGE_AMOUNT);
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BASS, 1.0f, 1.0f);
                    player.sendMessage(ChatColor.RED + "☠ Você está sem oxigênio! Encontre um Tanque de Oxigênio rapidamente!");
                }
            }
        }, DAMAGE_INTERVAL, DAMAGE_INTERVAL);
    }

    public ItemStack createOxygenTank() {
        ItemStack tank = new ItemStack(Material.POTION, 1, (short) 0);
        PotionMeta meta = (PotionMeta) tank.getItemMeta();

        meta.setDisplayName(ChatColor.AQUA + "✧ Tanque de Oxigênio ✧");
        List<String> lore = Arrays.asList(
            ChatColor.GRAY + "Um equipamento vital para sua",
            ChatColor.GRAY + "sobrevivência no espaço.",
            "",
            ChatColor.YELLOW + "Funções:",
            ChatColor.WHITE + "• Fornece oxigênio para respirar",
            ChatColor.WHITE + "• Proteção contra o vácuo espacial",
            ChatColor.WHITE + "• Sistema de reciclagem automática",
            "",
            ChatColor.RED + "⚠ AVISO:",
            ChatColor.RED + "Não remova este item do slot 9!",
            ChatColor.RED + "Você precisa dele para sobreviver.",
            "",
            ChatColor.DARK_GRAY + "Item Inquebrável"
        );
        meta.setLore(lore);

        // Adiciona efeitos visuais
        meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        // Na 1.8, não existe setUnbreakable, então usamos spigot.yml para esconder durabilidade
        tank.setItemMeta(meta);
        return tank;
    }

    public void giveOxygenTank(Player player) {
        // Remove qualquer tanque existente no slot
        if (isOxygenTank(player.getInventory().getItem(OXYGEN_SLOT))) {
            player.getInventory().setItem(OXYGEN_SLOT, null);
        }
        
        // Cria e coloca o novo tanque
        ItemStack tank = createOxygenTank();
        player.getInventory().setItem(OXYGEN_SLOT, tank);
        player.updateInventory();
        
        // Remove dos sets de controle
        playersWithoutOxygen.remove(player.getUniqueId());
        warnedPlayers.remove(player.getUniqueId());
    }

    public void checkOxygenTank(Player player) {
        ItemStack item = player.getInventory().getItem(OXYGEN_SLOT);
        if (item == null || !isOxygenTank(item)) {
            giveOxygenTank(player);
        }
    }

    public boolean isOxygenTank(ItemStack item) {
        if (item == null || item.getType() != Material.POTION) return false;
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.hasDisplayName() && 
               meta.getDisplayName().contains("Tanque de Oxigênio");
    }

    public boolean handleTankDrop(Player player) {
        if (!warnedPlayers.contains(player.getUniqueId())) {
            // Primeira tentativa: aviso
            warnedPlayers.add(player.getUniqueId());
            player.sendMessage("");
            player.sendMessage(ChatColor.RED + "⚠ AVISO DE SEGURANÇA ⚠");
            player.sendMessage(ChatColor.GRAY + "Você está prestes a remover seu Tanque de Oxigênio!");
            player.sendMessage(ChatColor.GRAY + "Sem ele, você começará a sofrer dano por falta de ar.");
            player.sendMessage(ChatColor.YELLOW + "Para dropar, tente novamente nos próximos 5 segundos.");
            player.sendMessage("");
            
            // Remove o aviso após 5 segundos
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                warnedPlayers.remove(player.getUniqueId());
            }, 100L); // 5 segundos
            
            return true; // Cancela o drop
        } else {
            // Segunda tentativa: permite o drop e inicia o dano
            warnedPlayers.remove(player.getUniqueId());
            playersWithoutOxygen.add(player.getUniqueId());
            
            player.sendMessage("");
            player.sendMessage(ChatColor.RED + "☠ ALERTA CRÍTICO ☠");
            player.sendMessage(ChatColor.GRAY + "Tanque de Oxigênio removido! Iniciando dano por falta de ar...");
            player.sendMessage("");
            
            // Efeitos sonoros e visuais
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BASS, 1.0f, 0.5f);
            
            return false; // Permite o drop
        }
    }

    public void handlePlayerDeath(Player player) {
        // Remove dos sets
        playersWithoutOxygen.remove(player.getUniqueId());
        warnedPlayers.remove(player.getUniqueId());
        
        // Agenda para dar o tanque após o respawn
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            giveOxygenTank(player);
        }, 1L);
    }

    public void handleTankPickup(Player player, ItemStack item) {
        if (isOxygenTank(item)) {
            // Remove o item do chão
            item.setAmount(0);
            
            // Remove qualquer tanque existente no inventário
            for (int i = 0; i < player.getInventory().getSize(); i++) {
                ItemStack invItem = player.getInventory().getItem(i);
                if (isOxygenTank(invItem)) {
                    player.getInventory().setItem(i, null);
                }
            }
            
            // Coloca o novo tanque no slot correto
            giveOxygenTank(player);
            player.updateInventory();
            
            // Verifica novamente após 1 segundo para garantir que não há duplicatas
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                checkForDuplicateTanks(player);
            }, 20L); // 20 ticks = 1 segundo
            
            player.sendMessage(ChatColor.GREEN + "✔ Tanque de Oxigênio reconectado! Sistema respiratório normalizado.");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BASS, 1.0f, 1.0f);
        }
    }

    private void checkForDuplicateTanks(Player player) {
        int tankCount = 0;
        
        // Conta quantos tanques existem no inventário
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (isOxygenTank(item)) {
                tankCount++;
                if (i != OXYGEN_SLOT) { // Se não for o slot 9, remove
                    player.getInventory().setItem(i, null);
                }
            }
        }
        
        // Se não tiver tanque no slot 9, mas tiver em outro lugar, move para o slot 9
        if (tankCount > 0 && !isOxygenTank(player.getInventory().getItem(OXYGEN_SLOT))) {
            giveOxygenTank(player);
        }
        
        // Se tiver mais de um tanque, garante que só fique o do slot 9
        if (tankCount > 1) {
            // Remove todos os tanques
            for (int i = 0; i < player.getInventory().getSize(); i++) {
                if (i != OXYGEN_SLOT && isOxygenTank(player.getInventory().getItem(i))) {
                    player.getInventory().setItem(i, null);
                }
            }
            // Garante que tem um tanque no slot 9
            if (!isOxygenTank(player.getInventory().getItem(OXYGEN_SLOT))) {
                giveOxygenTank(player);
            }
        }
        
        player.updateInventory();
    }
} 