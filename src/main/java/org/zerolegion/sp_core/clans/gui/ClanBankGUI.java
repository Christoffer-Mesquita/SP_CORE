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
import org.zerolegion.sp_core.clans.ClanPermission;

import java.util.ArrayList;
import java.util.List;
import java.text.DecimalFormat;

public class ClanBankGUI {
    private final ClanManager clanManager;
    private final int size = 36;
    private final DecimalFormat formatter;

    public ClanBankGUI(ClanManager clanManager) {
        this.clanManager = clanManager;
        this.formatter = new DecimalFormat("#,###");
    }

    public void openGUI(Player player, boolean isWithdraw) {
        SpaceClan clan = clanManager.getPlayerClan(player.getUniqueId());
        if (clan == null) return;

        // Verifica permissão
        if (isWithdraw && !clan.hasPermission(player.getUniqueId(), ClanPermission.BANK_WITHDRAW)) {
            player.sendMessage(ChatColor.RED + "Você não tem permissão para sacar do banco do clã!");
            return;
        }
        if (!isWithdraw && !clan.hasPermission(player.getUniqueId(), ClanPermission.BANK_DEPOSIT)) {
            player.sendMessage(ChatColor.RED + "Você não tem permissão para depositar no banco do clã!");
            return;
        }

        String title = isWithdraw ? "Sacar do Banco" : "Depositar no Banco";
        Inventory inv = Bukkit.createInventory(null, size, 
            ChatColor.DARK_PURPLE + "✧ " + title + " ✧");

        // Informações do Banco
        ItemStack info = new ItemStack(Material.GOLD_BLOCK);
        ItemMeta meta = info.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "Banco do Clã");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Saldo atual: " + ChatColor.GOLD + 
            formatter.format(clan.getBank()) + " ⭐");
        meta.setLore(lore);
        info.setItemMeta(meta);
        inv.setItem(4, info);

        // Opções de valores
        addValueOption(inv, 10, 1000);
        addValueOption(inv, 11, 5000);
        addValueOption(inv, 12, 10000);
        addValueOption(inv, 14, 50000);
        addValueOption(inv, 15, 100000);
        addValueOption(inv, 16, 500000);

        // Valor personalizado
        ItemStack custom = new ItemStack(Material.NAME_TAG);
        meta = custom.getItemMeta();
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Valor Personalizado");
        lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Clique para digitar");
        lore.add(ChatColor.GRAY + "um valor personalizado");
        meta.setLore(lore);
        custom.setItemMeta(meta);
        inv.setItem(22, custom);

        // Botão voltar
        ItemStack back = new ItemStack(Material.ARROW);
        meta = back.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "Voltar");
        back.setItemMeta(meta);
        inv.setItem(31, back);

        player.openInventory(inv);
    }

    private void addValueOption(Inventory inv, int slot, int value) {
        ItemStack item = new ItemStack(Material.GOLD_INGOT);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + formatter.format(value) + " ⭐");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Clique para selecionar");
        lore.add(ChatColor.GRAY + "este valor");
        meta.setLore(lore);
        item.setItemMeta(meta);
        inv.setItem(slot, item);
    }
} 