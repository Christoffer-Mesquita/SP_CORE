package org.zerolegion.sp_core.clans.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.zerolegion.sp_core.clans.ClanManager;

import java.util.ArrayList;
import java.util.List;

public class ClanCreateGUI {
    private final ClanManager clanManager;
    private final int size = 27;
    private final double CREATION_COST = 100000.0;
    private final int MIN_LEVEL = 10;

    public ClanCreateGUI(ClanManager clanManager) {
        this.clanManager = clanManager;
    }

    public void openGUI(Player player) {
        Inventory inv = Bukkit.createInventory(null, size, 
            ChatColor.DARK_PURPLE + "✧ Criar Clã ✧");

        // Informações
        ItemStack info = new ItemStack(Material.BOOK);
        ItemMeta meta = info.getItemMeta();
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Criar um Novo Clã");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Crie seu próprio clã e");
        lore.add(ChatColor.GRAY + "torne-se um líder!");
        lore.add("");
        lore.add(ChatColor.YELLOW + "Requisitos:");
        lore.add(ChatColor.GRAY + "• " + CREATION_COST + " Créditos");
        lore.add(ChatColor.GRAY + "• Nível " + MIN_LEVEL + "+");
        meta.setLore(lore);
        info.setItemMeta(meta);
        inv.setItem(4, info);

        // Nome do Clã
        ItemStack name = new ItemStack(Material.NAME_TAG);
        meta = name.getItemMeta();
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Nome do Clã");
        lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Defina o nome do seu clã");
        lore.add("");
        lore.add(ChatColor.YELLOW + "Clique para digitar");
        meta.setLore(lore);
        name.setItemMeta(meta);
        inv.setItem(11, name);

        // Tag do Clã
        ItemStack tag = new ItemStack(Material.PAPER);
        meta = tag.getItemMeta();
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Tag do Clã");
        lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Defina a tag do seu clã");
        lore.add(ChatColor.GRAY + "(3-4 caracteres)");
        lore.add("");
        lore.add(ChatColor.YELLOW + "Clique para digitar");
        meta.setLore(lore);
        tag.setItemMeta(meta);
        inv.setItem(13, tag);

        // Descrição do Clã
        ItemStack desc = new ItemStack(Material.BOOK_AND_QUILL);
        meta = desc.getItemMeta();
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Descrição");
        lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Adicione uma descrição");
        lore.add(ChatColor.GRAY + "para seu clã");
        lore.add("");
        lore.add(ChatColor.YELLOW + "Clique para digitar");
        meta.setLore(lore);
        desc.setItemMeta(meta);
        inv.setItem(15, desc);

        // Botão de Criar
        ItemStack create = new ItemStack(Material.NETHER_STAR);
        meta = create.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "Criar Clã");
        lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Clique para criar seu clã");
        lore.add(ChatColor.GRAY + "com as configurações");
        lore.add(ChatColor.GRAY + "definidas");
        meta.setLore(lore);
        create.setItemMeta(meta);
        inv.setItem(22, create);

        // Botão voltar
        ItemStack back = new ItemStack(Material.ARROW);
        meta = back.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "Voltar");
        back.setItemMeta(meta);
        inv.setItem(18, back);

        player.openInventory(inv);
    }

    public double getCreationCost() {
        return CREATION_COST;
    }

    public int getMinLevel() {
        return MIN_LEVEL;
    }
} 