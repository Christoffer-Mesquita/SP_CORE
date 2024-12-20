package org.zerolegion.sp_core.economy.gui;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.zerolegion.sp_core.economy.StellarEconomyManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CreditosGUIListener implements Listener {
    private final StellarEconomyManager economyManager;
    private final CreditosGUI creditosGUI;
    private final HistoricoGUI historicoGUI;
    private final RankingGUI rankingGUI;
    private final TransferBookGUI transferBookGUI;
    private final Map<UUID, Integer> playerPages;

    public CreditosGUIListener(StellarEconomyManager economyManager, CreditosGUI creditosGUI) {
        this.economyManager = economyManager;
        this.creditosGUI = creditosGUI;
        this.historicoGUI = new HistoricoGUI(economyManager, economyManager.getTransactionHistory());
        this.rankingGUI = new RankingGUI(economyManager);
        this.transferBookGUI = new TransferBookGUI(economyManager);
        this.playerPages = new HashMap<>();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        
        if (title.equals(ChatColor.DARK_PURPLE + "✧ Créditos Estelares ✧")) {
            event.setCancelled(true);
            handleMainMenu(player, event.getCurrentItem());
        }
        else if (title.equals(ChatColor.DARK_PURPLE + "✧ Histórico de Transações ✧")) {
            event.setCancelled(true);
            handleHistoricoMenu(player, event.getCurrentItem());
        }
        else if (title.equals(ChatColor.DARK_PURPLE + "✧ Ranking Global ✧")) {
            event.setCancelled(true);
            handleRankingMenu(player, event.getCurrentItem());
        }
    }

    private void handleMainMenu(Player player, ItemStack clickedItem) {
        if (clickedItem == null || !clickedItem.hasItemMeta()) return;
        
        String displayName = clickedItem.getItemMeta().getDisplayName();
        
        if (displayName.contains("Transferir Créditos")) {
            transferBookGUI.openTransferBook(player);
        }
        else if (displayName.contains("Histórico de Transações")) {
            playerPages.put(player.getUniqueId(), 0);
            historicoGUI.openHistorico(player, 0);
        }
        else if (displayName.contains("Ranking Global")) {
            playerPages.put(player.getUniqueId(), 0);
            rankingGUI.openRanking(player, 0);
        }
    }

    private void handleHistoricoMenu(Player player, ItemStack clickedItem) {
        if (clickedItem == null || !clickedItem.hasItemMeta()) return;
        
        String displayName = clickedItem.getItemMeta().getDisplayName();
        int currentPage = playerPages.getOrDefault(player.getUniqueId(), 0);
        
        if (displayName.contains("Página Anterior")) {
            if (currentPage > 0) {
                playerPages.put(player.getUniqueId(), currentPage - 1);
                historicoGUI.openHistorico(player, currentPage - 1);
            }
        }
        else if (displayName.contains("Próxima Página")) {
            playerPages.put(player.getUniqueId(), currentPage + 1);
            historicoGUI.openHistorico(player, currentPage + 1);
        }
        else if (displayName.equals(ChatColor.RED + "Voltar")) {
            creditosGUI.openMainMenu(player);
        }
    }

    private void handleRankingMenu(Player player, ItemStack clickedItem) {
        if (clickedItem == null || !clickedItem.hasItemMeta()) return;
        
        String displayName = clickedItem.getItemMeta().getDisplayName();
        int currentPage = playerPages.getOrDefault(player.getUniqueId(), 0);
        
        if (displayName.contains("Página Anterior")) {
            if (currentPage > 0) {
                playerPages.put(player.getUniqueId(), currentPage - 1);
                rankingGUI.openRanking(player, currentPage - 1);
            }
        }
        else if (displayName.contains("Próxima Página")) {
            playerPages.put(player.getUniqueId(), currentPage + 1);
            rankingGUI.openRanking(player, currentPage + 1);
        }
        else if (displayName.equals(ChatColor.RED + "Voltar")) {
            creditosGUI.openMainMenu(player);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        
        // Limpar dados de página quando o jogador fecha o inventário
        if (event.getView().getTitle().contains("✧")) {
            playerPages.remove(event.getPlayer().getUniqueId());
        }
    }
} 