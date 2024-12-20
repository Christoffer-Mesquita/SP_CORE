package org.zerolegion.sp_core.clans.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.zerolegion.sp_core.clans.ClanManager;
import org.zerolegion.sp_core.clans.SpaceClan;

import java.util.ArrayList;
import java.util.List;

public class ClanSettingsGUI {
    private final ClanManager clanManager;
    private final int size = 36;

    public ClanSettingsGUI(ClanManager clanManager) {
        this.clanManager = clanManager;
    }

    public void openGUI(Player player) {
        SpaceClan clan = clanManager.getPlayerClan(player.getUniqueId());
        if (clan == null || !clan.isLeader(player.getUniqueId())) return;

        Inventory inv = Bukkit.createInventory(null, size, ChatColor.DARK_PURPLE + "✧ Configurações do Clã ✧");

        // Configuração de Entrada Livre
        addToggleSetting(inv, clan, "openJoin", Material.IRON_DOOR,
            "Entrada Livre", 10,
            "Permite que jogadores entrem", "no clã sem convite");

        // Configuração de Fogo Amigo
        addToggleSetting(inv, clan, "friendlyFire", Material.DIAMOND_SWORD,
            "Fogo Amigo", 12,
            "Permite que membros do clã", "causem dano entre si");

        // Configuração de Base Pública
        addToggleSetting(inv, clan, "publicBase", Material.ENDER_PORTAL_FRAME,
            "Base Pública", 14,
            "Permite que qualquer jogador", "visite a base do clã");

        // Configuração de Teleporte de Aliados
        addToggleSetting(inv, clan, "allyTeleport", Material.ENDER_PEARL,
            "Teleporte de Aliados", 16,
            "Permite que aliados se", "teleportem para a base");

        // Configuração de Depósito no Banco
        addToggleSetting(inv, clan, "bankDeposit", Material.GOLD_INGOT,
            "Depósito no Banco", 20,
            "Permite que membros", "depositem no banco do clã");

        // Configuração de Saque do Banco
        addToggleSetting(inv, clan, "bankWithdraw", Material.GOLD_BLOCK,
            "Saque do Banco", 24,
            "Permite que membros", "saquem do banco do clã");

        // Botão voltar
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta meta = back.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "Voltar");
        back.setItemMeta(meta);
        inv.setItem(31, back);

        player.openInventory(inv);
    }

    private void addToggleSetting(Inventory inv, SpaceClan clan, String setting,
                                Material material, String name, int slot,
                                String... description) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName((clan.getSetting(setting) ? ChatColor.GREEN : ChatColor.RED) + name);
        
        List<String> lore = new ArrayList<>();
        for (String line : description) {
            lore.add(ChatColor.GRAY + line);
        }
        lore.add("");
        lore.add(clan.getSetting(setting) 
            ? ChatColor.GREEN + "✔ Ativado" 
            : ChatColor.RED + "✘ Desativado");
        lore.add("");
        lore.add(ChatColor.YELLOW + "Clique para alterar");
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        inv.setItem(slot, item);
    }
} 