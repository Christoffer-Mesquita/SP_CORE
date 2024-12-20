package org.zerolegion.sp_core.economy.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bson.Document;
import org.zerolegion.sp_core.economy.StellarEconomyManager;

import java.util.ArrayList;
import java.util.List;

public class RankingGUI {
    private final StellarEconomyManager economyManager;
    private final int ITEMS_PER_PAGE = 21;

    public RankingGUI(StellarEconomyManager economyManager) {
        this.economyManager = economyManager;
    }

    public void openRanking(Player player, int page) {
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.DARK_PURPLE + "✧ Ranking Global ✧");

        // Decoração do fundo
        ItemStack background = createItem(Material.STAINED_GLASS_PANE, (byte) 15, " ");
        ItemStack border = createItem(Material.STAINED_GLASS_PANE, (byte) 10, " ");

        // Preenche o fundo
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, background);
        }

        // Borda superior e inferior
        for (int i = 0; i < 9; i++) {
            inv.setItem(i, border);
            inv.setItem(inv.getSize() - 9 + i, border);
        }

        // Carregar top jogadores
        List<Document> topPlayers = economyManager.getTopPlayersData(ITEMS_PER_PAGE * (page + 1));
        int startIndex = page * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, topPlayers.size());

        // Mostrar jogadores
        int slot = 10;
        int position = startIndex + 1;
        for (int i = startIndex; i < endIndex; i++) {
            Document doc = topPlayers.get(i);
            String name = doc.getString("name");
            double balance = doc.getDouble("balance");

            // Criar cabeça do jogador
            ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
            SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
            skullMeta.setOwner(name);

            ChatColor color;
            String prefix;
            if (position == 1) {
                color = ChatColor.GOLD;
                prefix = "1º Lugar";
            } else if (position == 2) {
                color = ChatColor.GRAY;
                prefix = "2º Lugar";
            } else if (position == 3) {
                color = ChatColor.RED;
                prefix = "3º Lugar";
            } else {
                color = ChatColor.WHITE;
                prefix = position + "º Lugar";
            }

            skullMeta.setDisplayName(color + "✧ " + prefix + " ✧");
            
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(ChatColor.GRAY + "Jogador: " + ChatColor.WHITE + name);
            lore.add(ChatColor.GRAY + "Saldo: " + ChatColor.YELLOW + economyManager.formatValue(balance) + " ⭐");
            lore.add("");
            skullMeta.setLore(lore);
            
            skull.setItemMeta(skullMeta);
            
            if ((position - 1) % 7 == 0 && position > 1) {
                slot += 2;
            }
            inv.setItem(slot++, skull);
            position++;
        }

        // Botões de navegação
        if (page > 0) {
            ItemStack previousPage = createItem(Material.ARROW, ChatColor.YELLOW + "← Página Anterior");
            inv.setItem(45, previousPage);
        }
        
        if (endIndex < topPlayers.size()) {
            ItemStack nextPage = createItem(Material.ARROW, ChatColor.YELLOW + "Próxima Página →");
            inv.setItem(53, nextPage);
        }

        // Botão voltar
        ItemStack back = createItem(Material.BARRIER, ChatColor.RED + "Voltar");
        inv.setItem(49, back);

        player.openInventory(inv);
    }

    private ItemStack createItem(Material material, String name, String... lore) {
        return createItem(material, (byte) 0, name, lore);
    }

    private ItemStack createItem(Material material, byte data, String name, String... lore) {
        ItemStack item = new ItemStack(material, 1, data);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        
        if (lore.length > 0) {
            List<String> loreList = new ArrayList<>();
            for (String line : lore) {
                loreList.add(line);
            }
            meta.setLore(loreList);
        }
        
        item.setItemMeta(meta);
        return item;
    }
} 