package org.zerolegion.sp_core.economy.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.zerolegion.sp_core.economy.StellarEconomyManager;

public class TransferBookGUI {
    private final StellarEconomyManager economyManager;

    public TransferBookGUI(StellarEconomyManager economyManager) {
        this.economyManager = economyManager;
    }

    public void openTransferBook(Player player) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();

        meta.setTitle(ChatColor.LIGHT_PURPLE + "Transferência de Créditos");
        meta.setAuthor("Sistema");

        StringBuilder page = new StringBuilder();
        page.append(ChatColor.DARK_PURPLE + "✧ Transferência ✧\n\n");
        page.append(ChatColor.GRAY + "Seu saldo atual: \n");
        page.append(ChatColor.YELLOW + economyManager.getFormattedBalance(player) + "\n\n");
        page.append(ChatColor.GRAY + "Para transferir créditos, use:\n");
        page.append(ChatColor.WHITE + "/creditos enviar <jogador> <valor>\n\n");
        page.append(ChatColor.GRAY + "Exemplo:\n");
        page.append(ChatColor.WHITE + "/creditos enviar Steve 1000");

        meta.addPage(page.toString());
        book.setItemMeta(meta);

        // Guarda o item atual do slot
        ItemStack currentItem = player.getItemInHand();
        
        // Coloca o livro na mão do jogador
        player.setItemInHand(book);
        
        // Agenda a tarefa para restaurar o item original após 2 ticks
        Bukkit.getScheduler().scheduleSyncDelayedTask(economyManager.getPlugin(), () -> {
            player.setItemInHand(currentItem);
            player.sendMessage(ChatColor.YELLOW + "✧ Use: /creditos enviar <jogador> <valor>");
        }, 2L);
    }
} 