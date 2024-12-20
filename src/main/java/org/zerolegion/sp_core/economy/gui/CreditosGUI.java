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
import org.zerolegion.sp_core.economy.TransactionHistory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class CreditosGUI {
    private final StellarEconomyManager economyManager;
    private final TransactionHistory transactionHistory;
    private final SimpleDateFormat dateFormat;

    public CreditosGUI(StellarEconomyManager economyManager, TransactionHistory transactionHistory) {
        this.economyManager = economyManager;
        this.transactionHistory = transactionHistory;
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    }

    public void openMainMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.DARK_PURPLE + "✧ Créditos Estelares ✧");

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

        // Cabeça do jogador
        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        skullMeta.setOwner(player.getName());
        skullMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "✧ " + ChatColor.WHITE + player.getName());
        List<String> skullLore = new ArrayList<>();
        skullLore.add(ChatColor.GRAY + "Saldo atual: " + ChatColor.YELLOW + economyManager.getFormattedBalance(player));
        skullMeta.setLore(skullLore);
        skull.setItemMeta(skullMeta);
        inv.setItem(13, skull);

        // Botão de transferência
        ItemStack transfer = createItem(
            Material.PAPER,
            ChatColor.LIGHT_PURPLE + "✧ " + ChatColor.WHITE + "Transferir Créditos",
            ChatColor.GRAY + "Clique para transferir créditos",
            ChatColor.GRAY + "para outro jogador"
        );
        inv.setItem(29, transfer);

        // Botão de histórico
        ItemStack history = createItem(
            Material.BOOK,
            ChatColor.LIGHT_PURPLE + "✧ " + ChatColor.WHITE + "Histórico de Transações",
            ChatColor.GRAY + "Clique para ver seu histórico",
            ChatColor.GRAY + "de transações recentes"
        );
        inv.setItem(31, history);

        // Botão de ranking
        ItemStack ranking = createItem(
            Material.NETHER_STAR,
            ChatColor.LIGHT_PURPLE + "✧ " + ChatColor.WHITE + "Ranking Global",
            ChatColor.GRAY + "Clique para ver o ranking",
            ChatColor.GRAY + "dos jogadores mais ricos"
        );
        inv.setItem(33, ranking);

        // Histórico recente
        List<Document> recentTransactions = transactionHistory.getPlayerTransactions(player.getUniqueId(), 5);
        int slot = 45;
        for (Document transaction : recentTransactions) {
            String type = transaction.getString("type");
            double amount = transaction.getDouble("amount");
            Date timestamp = transaction.getDate("timestamp");
            String fromUuid = transaction.getString("from_uuid");
            String toUuid = transaction.getString("to_uuid");
            
            Material material;
            byte data = 0;
            String title;
            ChatColor color;
            
            // Verifica se o jogador é o remetente ou destinatário
            boolean isSender = fromUuid.equals(player.getUniqueId().toString());
            
            if (type.equals("TRANSFER")) {
                if (isSender) {
                    material = Material.INK_SACK;
                    data = 1; // Rosa vermelha
                    Player receiver = Bukkit.getPlayer(UUID.fromString(toUuid));
                    title = "Enviado para " + (receiver != null ? receiver.getName() : "Jogador Offline");
                    color = ChatColor.RED;
                } else {
                    material = Material.INK_SACK;
                    data = 10; // Lima verde
                    Player sender = Bukkit.getPlayer(UUID.fromString(fromUuid));
                    title = "Recebido de " + (sender != null ? sender.getName() : "Jogador Offline");
                    color = ChatColor.GREEN;
                }
            } else if (type.equals("ADMIN_ADD")) {
                material = Material.INK_SACK;
                data = 11; // Amarelo
                title = "Créditos Adicionados";
                color = ChatColor.YELLOW;
            } else if (type.equals("ADMIN_REMOVE")) {
                material = Material.INK_SACK;
                data = 14; // Vermelho
                title = "Créditos Removidos";
                color = ChatColor.RED;
            } else {
                material = Material.INK_SACK;
                data = 8; // Cinza
                title = "Transação do Sistema";
                color = ChatColor.GRAY;
            }

            ItemStack transactionItem = createItem(
                material,
                data,
                color + "✧ " + title,
                ChatColor.GRAY + "Valor: " + color + economyManager.formatValue(amount) + " ⭐",
                ChatColor.GRAY + "Data: " + ChatColor.WHITE + dateFormat.format(timestamp)
            );
            
            if (slot < 54) {
                inv.setItem(slot++, transactionItem);
            }
        }

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