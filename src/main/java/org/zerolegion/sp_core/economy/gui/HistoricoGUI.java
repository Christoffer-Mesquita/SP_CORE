package org.zerolegion.sp_core.economy.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bson.Document;
import org.zerolegion.sp_core.economy.StellarEconomyManager;
import org.zerolegion.sp_core.economy.TransactionHistory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class HistoricoGUI {
    private final StellarEconomyManager economyManager;
    private final TransactionHistory transactionHistory;
    private final SimpleDateFormat dateFormat;
    private final int ITEMS_PER_PAGE = 45;

    public HistoricoGUI(StellarEconomyManager economyManager, TransactionHistory transactionHistory) {
        this.economyManager = economyManager;
        this.transactionHistory = transactionHistory;
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    }

    public void openHistorico(Player player, int page) {
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.DARK_PURPLE + "✧ Histórico de Transações ✧");

        // Decoração do fundo
        ItemStack background = createItem(Material.STAINED_GLASS_PANE, (byte) 15, " ");
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, background);
        }

        // Carregar transações
        List<Document> transactions = transactionHistory.getPlayerTransactions(player.getUniqueId(), ITEMS_PER_PAGE * (page + 1));
        int startIndex = page * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, transactions.size());

        // Mostrar transações
        int slot = 0;
        for (int i = startIndex; i < endIndex; i++) {
            Document transaction = transactions.get(i);
            String type = transaction.getString("type");
            double amount = transaction.getDouble("amount");
            Date timestamp = transaction.getDate("timestamp");
            String fromUuid = transaction.getString("from_uuid");
            String toUuid = transaction.getString("to_uuid");
            
            Material material;
            byte data;
            String title;
            ChatColor color;
            List<String> lore = new ArrayList<>();
            
            boolean isSender = fromUuid.equals(player.getUniqueId().toString());
            
            switch (type) {
                case "TRANSFER":
                    if (isSender) {
                        material = Material.INK_SACK;
                        data = 1;
                        Player receiver = Bukkit.getPlayer(UUID.fromString(toUuid));
                        title = "Enviado para " + (receiver != null ? receiver.getName() : "Jogador Offline");
                        color = ChatColor.RED;
                        lore.add(ChatColor.GRAY + "Enviado para: " + ChatColor.WHITE + 
                            (receiver != null ? receiver.getName() : "Jogador Offline"));
                    } else {
                        material = Material.INK_SACK;
                        data = 10;
                        Player sender = Bukkit.getPlayer(UUID.fromString(fromUuid));
                        title = "Recebido de " + (sender != null ? sender.getName() : "Jogador Offline");
                        color = ChatColor.GREEN;
                        lore.add(ChatColor.GRAY + "Recebido de: " + ChatColor.WHITE + 
                            (sender != null ? sender.getName() : "Jogador Offline"));
                    }
                    break;
                case "ADMIN_ADD":
                    material = Material.INK_SACK;
                    data = 11;
                    title = "Créditos Adicionados";
                    color = ChatColor.YELLOW;
                    Player admin = Bukkit.getPlayer(UUID.fromString(fromUuid));
                    lore.add(ChatColor.GRAY + "Adicionado por: " + ChatColor.WHITE + 
                        (admin != null ? admin.getName() : "Administrador"));
                    break;
                case "ADMIN_REMOVE":
                    material = Material.INK_SACK;
                    data = 14;
                    title = "Créditos Removidos";
                    color = ChatColor.RED;
                    admin = Bukkit.getPlayer(UUID.fromString(fromUuid));
                    lore.add(ChatColor.GRAY + "Removido por: " + ChatColor.WHITE + 
                        (admin != null ? admin.getName() : "Administrador"));
                    break;
                default:
                    material = Material.INK_SACK;
                    data = 8;
                    title = "Transação do Sistema";
                    color = ChatColor.GRAY;
                    break;
            }
            
            lore.add("");
            lore.add(ChatColor.GRAY + "Valor: " + color + economyManager.formatValue(amount) + " ⭐");
            lore.add(ChatColor.GRAY + "Data: " + ChatColor.WHITE + dateFormat.format(timestamp));
            
            ItemStack item = createItem(material, data, color + "✧ " + title, lore.toArray(new String[0]));
            inv.setItem(slot++, item);
        }

        // Botões de navegação
        if (page > 0) {
            ItemStack previousPage = createItem(Material.ARROW, ChatColor.YELLOW + "← Página Anterior");
            inv.setItem(45, previousPage);
        }
        
        if (endIndex < transactions.size()) {
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