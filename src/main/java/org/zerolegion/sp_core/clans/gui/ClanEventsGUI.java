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

public class ClanEventsGUI {
    private final ClanManager clanManager;
    private final int size = 54;

    public ClanEventsGUI(ClanManager clanManager) {
        this.clanManager = clanManager;
    }

    public void openGUI(Player player) {
        SpaceClan clan = clanManager.getPlayerClan(player.getUniqueId());
        if (clan == null) return;

        Inventory inv = Bukkit.createInventory(null, size, ChatColor.DARK_PURPLE + "✧ Eventos do Clã ✧");

        // Informações do Clã
        ItemStack info = new ItemStack(Material.BEACON);
        ItemMeta meta = info.getItemMeta();
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + clan.getName());
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Membros: " + ChatColor.WHITE + clan.getMembers().size());
        lore.add(ChatColor.GRAY + "Poder: " + ChatColor.RED + clan.getPower());
        meta.setLore(lore);
        info.setItemMeta(meta);
        inv.setItem(4, info);

        // Eventos disponíveis
        addEventOption(inv, 19, Material.DRAGON_EGG, "Raid Espacial",
            "Organize uma raid em conjunto", "com seu clã para conquistar",
            "recursos espaciais valiosos");

        addEventOption(inv, 21, Material.BEACON, "Conquista de Base",
            "Lidere seu clã em uma missão", "para conquistar uma nova base",
            "espacial para expansão");

        addEventOption(inv, 23, Material.DIAMOND_SWORD, "Guerra Espacial",
            "Declare guerra contra outro clã", "e dispute territórios em uma",
            "batalha épica no espaço");

        addEventOption(inv, 25, Material.GOLD_BLOCK, "Missão de Mineração",
            "Organize uma expedição de", "mineração em grupo para",
            "coletar recursos raros");

        // Eventos ativos (se houver)
        // TODO: Implementar sistema de eventos ativos

        // Botão para criar evento (apenas líder/oficial)
        if (clan.isLeader(player.getUniqueId()) || clan.isOfficer(player.getUniqueId())) {
            ItemStack create = new ItemStack(Material.NETHER_STAR);
            meta = create.getItemMeta();
            meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Criar Novo Evento");
            lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Clique para criar um");
            lore.add(ChatColor.GRAY + "novo evento para o clã");
            meta.setLore(lore);
            create.setItemMeta(meta);
            inv.setItem(49, create);
        }

        // Botão voltar
        ItemStack back = new ItemStack(Material.ARROW);
        meta = back.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "Voltar");
        back.setItemMeta(meta);
        inv.setItem(45, back);

        player.openInventory(inv);
    }

    private void addEventOption(Inventory inv, int slot, Material material, 
                              String name, String... description) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + name);
        
        List<String> lore = new ArrayList<>();
        for (String line : description) {
            lore.add(ChatColor.GRAY + line);
        }
        lore.add("");
        lore.add(ChatColor.YELLOW + "Clique para mais informações");
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        inv.setItem(slot, item);
    }
} 