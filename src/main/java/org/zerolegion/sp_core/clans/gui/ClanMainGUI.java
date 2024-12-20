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
import org.zerolegion.sp_core.clans.ClanRole;
import org.zerolegion.sp_core.clans.ClanPermission;

import java.util.ArrayList;
import java.util.List;

public class ClanMainGUI {
    private final ClanManager clanManager;
    private final int size = 54; // 6 linhas

    public ClanMainGUI(ClanManager clanManager) {
        this.clanManager = clanManager;
    }

    public void openGUI(Player player) {
        SpaceClan clan = clanManager.getPlayerClan(player.getUniqueId());
        Inventory inv = Bukkit.createInventory(null, size, ChatColor.DARK_PURPLE + "✧ Clã Espacial ✧");

        // Borda superior
        for (int i = 0; i < 9; i++) {
            ItemStack border = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 2);
            ItemMeta meta = border.getItemMeta();
            meta.setDisplayName(" ");
            border.setItemMeta(meta);
            inv.setItem(i, border);
        }

        // Borda inferior
        for (int i = 45; i < 54; i++) {
            ItemStack border = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 2);
            ItemMeta meta = border.getItemMeta();
            meta.setDisplayName(" ");
            border.setItemMeta(meta);
            inv.setItem(i, border);
        }

        // Bordas laterais
        for (int i = 0; i < 6; i++) {
            ItemStack border = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 2);
            ItemMeta meta = border.getItemMeta();
            meta.setDisplayName(" ");
            border.setItemMeta(meta);
            inv.setItem(i * 9, border);
            inv.setItem(i * 9 + 8, border);
        }

        if (clan == null) {
            createNoClanGUI(inv);
        } else {
            createClanGUI(inv, clan, player);
        }

        player.openInventory(inv);
    }

    private void createNoClanGUI(Inventory inv) {
        // Preenchendo todo o inventário com vidro preto
        for (int i = 0; i < size; i++) {
            ItemStack background = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 15);
            ItemMeta meta = background.getItemMeta();
            meta.setDisplayName(" ");
            background.setItemMeta(meta);
            inv.setItem(i, background);
        }

        // Borda decorativa superior e inferior com vidro roxo
        ItemStack purpleGlass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 2);
        ItemMeta purpleMeta = purpleGlass.getItemMeta();
        purpleMeta.setDisplayName(" ");
        purpleGlass.setItemMeta(purpleMeta);

        // Superior
        for (int i = 0; i < 9; i++) {
            inv.setItem(i, purpleGlass);
        }
        // Inferior
        for (int i = 45; i < 54; i++) {
            inv.setItem(i, purpleGlass);
        }

        // Item central para criar clã
        ItemStack createClan = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = createClan.getItemMeta();
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "✧ " + ChatColor.WHITE + "Criar Clã" + ChatColor.LIGHT_PURPLE + " ✧");
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(ChatColor.GRAY + "Crie seu próprio clã espacial e");
        lore.add(ChatColor.GRAY + "comece sua jornada nas estrelas!");
        lore.add("");
        lore.add(ChatColor.LIGHT_PURPLE + "Requisitos:");
        lore.add(ChatColor.GRAY + "• " + ChatColor.GOLD + "100.000 Créditos");
        lore.add(ChatColor.GRAY + "• " + ChatColor.AQUA + "Nível 10+");
        lore.add("");
        lore.add(ChatColor.YELLOW + "Clique para criar!");
        meta.setLore(lore);
        createClan.setItemMeta(meta);
        
        inv.setItem(22, createClan);

        // Item para listar clãs
        ItemStack listClans = new ItemStack(Material.ENCHANTED_BOOK);
        meta = listClans.getItemMeta();
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "✧ " + ChatColor.WHITE + "Lista de Clãs" + ChatColor.LIGHT_PURPLE + " ✧");
        lore = new ArrayList<>();
        lore.add("");
        lore.add(ChatColor.GRAY + "Explore os clãs existentes e");
        lore.add(ChatColor.GRAY + "encontre sua nova família espacial!");
        lore.add("");
        lore.add(ChatColor.YELLOW + "Clique para explorar!");
        meta.setLore(lore);
        listClans.setItemMeta(meta);
        
        inv.setItem(31, listClans);

        // Decoração com vidros coloridos
        ItemStack lightBlueGlass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 3);
        ItemMeta glassMeta = lightBlueGlass.getItemMeta();
        glassMeta.setDisplayName(" ");
        lightBlueGlass.setItemMeta(glassMeta);

        // Posições dos vidros decorativos em forma de moldura
        int[] decorSlots = {
            // Cantos superiores
            1, 7,
            // Laterais
            9, 17,
            // Centro
            21, 23,
            // Laterais inferiores
            27, 35,
            // Cantos inferiores
            37, 43
        };

        for (int slot : decorSlots) {
            inv.setItem(slot, lightBlueGlass);
        }
    }

    private void createClanGUI(Inventory inv, SpaceClan clan, Player player) {
        boolean isLeader = clan.isLeader(player.getUniqueId());
        boolean isOfficer = clan.isOfficer(player.getUniqueId());

        // Preenchendo todo o inventário com vidro preto
        for (int i = 0; i < size; i++) {
            ItemStack background = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 15);
            ItemMeta meta = background.getItemMeta();
            meta.setDisplayName(" ");
            background.setItemMeta(meta);
            inv.setItem(i, background);
        }

        // Borda decorativa superior e inferior com vidro roxo
        ItemStack purpleGlass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 2);
        ItemMeta purpleMeta = purpleGlass.getItemMeta();
        purpleMeta.setDisplayName(" ");
        purpleGlass.setItemMeta(purpleMeta);

        // Superior
        for (int i = 0; i < 9; i++) {
            inv.setItem(i, purpleGlass);
        }
        // Inferior
        for (int i = 45; i < 54; i++) {
            inv.setItem(i, purpleGlass);
        }

        // Informações do Clã (Centro Superior)
        ItemStack info = new ItemStack(Material.BEACON);
        ItemMeta meta = info.getItemMeta();
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "✧ " + ChatColor.WHITE + clan.getName() + ChatColor.LIGHT_PURPLE + " ✧");
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(ChatColor.GRAY + "Tag: " + ChatColor.AQUA + "[" + clan.getTag() + "]");
        lore.add(ChatColor.GRAY + "Líder: " + ChatColor.GOLD + Bukkit.getOfflinePlayer(clan.getLeader()).getName());
        lore.add(ChatColor.GRAY + "Membros: " + ChatColor.WHITE + clan.getMembers().size());
        lore.add(ChatColor.GRAY + "Poder: " + ChatColor.RED + "⚔ " + clan.getPower());
        lore.add("");
        if (clan.getAnnouncement() != null && !clan.getAnnouncement().isEmpty()) {
            lore.add(ChatColor.YELLOW + "» Anúncio:");
            lore.add(ChatColor.WHITE + clan.getAnnouncement());
            lore.add("");
        }
        meta.setLore(lore);
        info.setItemMeta(meta);
        inv.setItem(4, info);

        // Base do Clã
        ItemStack base = new ItemStack(Material.BEACON);
        meta = base.getItemMeta();
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "✧ " + ChatColor.WHITE + "Base do Clã" + ChatColor.LIGHT_PURPLE + " ✧");
        lore = new ArrayList<>();
        lore.add("");
        if (clan.getBase() != null) {
            lore.add(ChatColor.GRAY + "» Clique para " + ChatColor.GREEN + "teleportar");
            if (isLeader || isOfficer) {
                lore.add(ChatColor.GRAY + "» Shift + Clique para " + ChatColor.RED + "redefinir");
            }
        } else if (isLeader) {
            lore.add(ChatColor.GRAY + "» Clique para definir a base");
            lore.add(ChatColor.GRAY + "na sua localização atual");
        } else {
            lore.add(ChatColor.RED + "✖ Base não definida");
        }
        meta.setLore(lore);
        base.setItemMeta(meta);
        inv.setItem(20, base);

        // Membros
        ItemStack members = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        meta = members.getItemMeta();
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "✧ " + ChatColor.WHITE + "Membros" + ChatColor.LIGHT_PURPLE + " ✧");
        lore = new ArrayList<>();
        lore.add("");
        lore.add(ChatColor.GRAY + "Gerencie os membros do seu clã");
        lore.add(ChatColor.GRAY + "e suas permissões.");
        if (isLeader || isOfficer) {
            lore.add("");
            lore.add(ChatColor.YELLOW + "» Gerencie membros");
            lore.add(ChatColor.YELLOW + "» Defina cargos");
            lore.add(ChatColor.YELLOW + "» Convide jogadores");
        }
        meta.setLore(lore);
        members.setItemMeta(meta);
        inv.setItem(22, members);

        // Alianças e Guerras
        ItemStack relations = new ItemStack(Material.DIAMOND_SWORD);
        meta = relations.getItemMeta();
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "✧ " + ChatColor.WHITE + "Diplomacia" + ChatColor.LIGHT_PURPLE + " ✧");
        lore = new ArrayList<>();
        lore.add("");
        lore.add(ChatColor.GREEN + "» Aliados: " + clan.getAllies().size());
        lore.add(ChatColor.RED + "» Inimigos: " + clan.getEnemies().size());
        if (isLeader || isOfficer) {
            lore.add("");
            lore.add(ChatColor.YELLOW + "Clique para gerenciar");
            lore.add(ChatColor.YELLOW + "alianças e guerras");
        }
        meta.setLore(lore);
        relations.setItemMeta(meta);
        inv.setItem(24, relations);

        // Banco do Clã
        ItemStack bank = new ItemStack(Material.GOLD_BLOCK);
        meta = bank.getItemMeta();
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "✧ " + ChatColor.WHITE + "Banco do Clã" + ChatColor.LIGHT_PURPLE + " ✧");
        lore = new ArrayList<>();
        lore.add("");
        lore.add(ChatColor.GRAY + "Saldo: " + ChatColor.GOLD + clan.getBank() + " ⭐");
        if (clan.hasPermission(player.getUniqueId(), ClanPermission.BANK_DEPOSIT)) {
            lore.add("");
            lore.add(ChatColor.YELLOW + "» Clique para depositar");
        }
        if (clan.hasPermission(player.getUniqueId(), ClanPermission.BANK_WITHDRAW)) {
            lore.add(ChatColor.YELLOW + "» Shift + Clique para sacar");
        }
        meta.setLore(lore);
        bank.setItemMeta(meta);
        inv.setItem(30, bank);

        // Eventos do Clã
        ItemStack events = new ItemStack(Material.DRAGON_EGG);
        meta = events.getItemMeta();
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "✧ " + ChatColor.WHITE + "Eventos" + ChatColor.LIGHT_PURPLE + " ✧");
        lore = new ArrayList<>();
        lore.add("");
        lore.add(ChatColor.GRAY + "Participe de eventos espaciais");
        lore.add(ChatColor.GRAY + "exclusivos para clãs!");
        if (isLeader || isOfficer) {
            lore.add("");
            lore.add(ChatColor.YELLOW + "» Crie eventos");
            lore.add(ChatColor.YELLOW + "» Gerencie eventos ativos");
        }
        meta.setLore(lore);
        events.setItemMeta(meta);
        inv.setItem(32, events);

        // Configurações (apenas para líder)
        if (isLeader) {
            ItemStack settings = new ItemStack(Material.COMMAND);
            meta = settings.getItemMeta();
            meta.setDisplayName(ChatColor.LIGHT_PURPLE + "✧ " + ChatColor.WHITE + "Configurações" + ChatColor.LIGHT_PURPLE + " ✧");
            lore = new ArrayList<>();
            lore.add("");
            lore.add(ChatColor.GRAY + "Configure as opções do seu clã:");
            lore.add("");
            lore.add(ChatColor.YELLOW + "» Permissões");
            lore.add(ChatColor.YELLOW + "» Recrutamento");
            lore.add(ChatColor.YELLOW + "» Fogo amigo");
            lore.add(ChatColor.YELLOW + "» Acesso à base");
            meta.setLore(lore);
            settings.setItemMeta(meta);
            inv.setItem(40, settings);
        }

        // Sair do Clã
        if (!isLeader) {
            ItemStack leave = new ItemStack(Material.BARRIER);
            meta = leave.getItemMeta();
            meta.setDisplayName(ChatColor.RED + "Sair do Clã");
            lore = new ArrayList<>();
            lore.add("");
            lore.add(ChatColor.GRAY + "Tem certeza que deseja");
            lore.add(ChatColor.GRAY + "abandonar seu clã?");
            lore.add("");
            lore.add(ChatColor.RED + "» Esta ação não pode ser desfeita!");
            meta.setLore(lore);
            leave.setItemMeta(meta);
            inv.setItem(49, leave);
        }

        // Decoração com vidros coloridos
        ItemStack lightBlueGlass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 3);
        ItemMeta glassMeta = lightBlueGlass.getItemMeta();
        glassMeta.setDisplayName(" ");
        lightBlueGlass.setItemMeta(glassMeta);

        // Posições dos vidros decorativos em forma de moldura
        int[] decorSlots = {
            // Cantos superiores
            1, 7,
            // Laterais
            9, 17,
            // Centro
            21, 23,
            // Laterais inferiores
            27, 35,
            // Cantos inferiores
            37, 43
        };

        for (int slot : decorSlots) {
            inv.setItem(slot, lightBlueGlass);
        }
    }
} 