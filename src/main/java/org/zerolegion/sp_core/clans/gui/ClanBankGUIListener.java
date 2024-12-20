package org.zerolegion.sp_core.clans.gui;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.zerolegion.sp_core.SP_CORE;
import org.zerolegion.sp_core.clans.ClanManager;
import org.zerolegion.sp_core.clans.SpaceClan;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClanBankGUIListener implements Listener {
    private final SP_CORE plugin;
    private final ClanManager clanManager;
    private final ClanBankGUI bankGUI;
    private final ClanMainGUI mainGUI;
    private final Map<UUID, TransactionState> playerStates;

    private enum TransactionState {
        WAITING_DEPOSIT,
        WAITING_WITHDRAW
    }

    public ClanBankGUIListener(SP_CORE plugin, ClanManager clanManager, ClanBankGUI bankGUI, ClanMainGUI mainGUI) {
        this.plugin = plugin;
        this.clanManager = clanManager;
        this.bankGUI = bankGUI;
        this.mainGUI = mainGUI;
        this.playerStates = new HashMap<>();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        String title = event.getView().getTitle();
        if (!title.contains("✧ Banco do Clã ✧") && 
            !title.contains("✧ Sacar do Banco ✧") && 
            !title.contains("✧ Depositar no Banco ✧")) return;

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null) return;

        SpaceClan clan = clanManager.getPlayerClan(player.getUniqueId());
        if (clan == null) return;

        if (title.equals(ChatColor.DARK_PURPLE + "✧ Banco do Clã ✧")) {
            switch (clicked.getType().name()) {
                case "GOLD_INGOT":
                    if (clan.getSetting("bankDeposit") || clan.isLeader(player.getUniqueId()) || clan.isOfficer(player.getUniqueId())) {
                        bankGUI.openGUI(player, false);
                    } else {
                        player.sendMessage(ChatColor.RED + "Você não tem permissão para depositar!");
                    }
                    break;
                case "EMERALD":
                    if (clan.getSetting("bankWithdraw") || clan.isLeader(player.getUniqueId()) || clan.isOfficer(player.getUniqueId())) {
                        bankGUI.openGUI(player, true);
                    } else {
                        player.sendMessage(ChatColor.RED + "Você não tem permissão para sacar!");
                    }
                    break;
                case "ARROW":
                    mainGUI.openGUI(player);
                    break;
            }
        } else {
            boolean isWithdraw = title.contains("Sacar");
            
            if (clicked.getType().name().equals("ARROW")) {
                bankGUI.openGUI(player, isWithdraw);
                return;
            }

            if (clicked.getType().name().equals("NAME_TAG")) {
                if (isWithdraw) {
                    startWithdraw(player);
                } else {
                    startDeposit(player);
                }
                return;
            }

            if (clicked.getType().name().equals("GOLD_INGOT")) {
                String amountStr = ChatColor.stripColor(clicked.getItemMeta().getDisplayName())
                    .replace(" ⭐", "")
                    .replace(",", "");
                try {
                    double amount = Double.parseDouble(amountStr);
                    if (isWithdraw) {
                        handleWithdraw(player, clan, amount);
                    } else {
                        handleDeposit(player, clan, amount);
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
    }

    private void startDeposit(Player player) {
        player.closeInventory();
        playerStates.put(player.getUniqueId(), TransactionState.WAITING_DEPOSIT);
        player.sendMessage("");
        player.sendMessage(ChatColor.LIGHT_PURPLE + "✧ " + ChatColor.WHITE + "Digite o valor que deseja depositar");
        player.sendMessage(ChatColor.GRAY + "Digite 'cancelar' para cancelar");
        player.sendMessage("");
    }

    private void startWithdraw(Player player) {
        player.closeInventory();
        playerStates.put(player.getUniqueId(), TransactionState.WAITING_WITHDRAW);
        player.sendMessage("");
        player.sendMessage(ChatColor.LIGHT_PURPLE + "✧ " + ChatColor.WHITE + "Digite o valor que deseja sacar");
        player.sendMessage(ChatColor.GRAY + "Digite 'cancelar' para cancelar");
        player.sendMessage("");
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        TransactionState state = playerStates.get(player.getUniqueId());
        if (state == null) return;

        event.setCancelled(true);
        String message = event.getMessage();

        if (message.equalsIgnoreCase("cancelar")) {
            playerStates.remove(player.getUniqueId());
            bankGUI.openGUI(player, state == TransactionState.WAITING_WITHDRAW);
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(message);
            if (amount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Por favor, digite um valor válido!");
            return;
        }

        SpaceClan clan = clanManager.getPlayerClan(player.getUniqueId());
        if (clan == null) return;

        switch (state) {
            case WAITING_DEPOSIT:
                handleDeposit(player, clan, amount);
                break;
            case WAITING_WITHDRAW:
                handleWithdraw(player, clan, amount);
                break;
        }

        playerStates.remove(player.getUniqueId());
        bankGUI.openGUI(player, state == TransactionState.WAITING_WITHDRAW);
    }

    private void handleDeposit(Player player, SpaceClan clan, double amount) {
        if (plugin.getStellarEconomyManager().getBalance(player.getUniqueId()) < amount) {
            player.sendMessage(ChatColor.RED + "Você não tem créditos suficientes!");
            return;
        }

        plugin.getStellarEconomyManager().removeBalance(player.getUniqueId(), amount);
        clan.deposit(amount);
        clanManager.saveClan(clan);

        player.sendMessage(ChatColor.GREEN + "Você depositou " + amount + " créditos no banco do clã!");
        
        // Notificar membros online
        for (UUID memberId : clan.getMembers().keySet()) {
            Player member = plugin.getServer().getPlayer(memberId);
            if (member != null && member.isOnline() && !member.equals(player)) {
                member.sendMessage(ChatColor.GREEN + player.getName() + " depositou " + amount + " créditos no banco do clã!");
            }
        }
    }

    private void handleWithdraw(Player player, SpaceClan clan, double amount) {
        if (!clan.withdraw(amount)) {
            player.sendMessage(ChatColor.RED + "O banco do clã não tem créditos suficientes!");
            return;
        }

        plugin.getStellarEconomyManager().addBalance(player.getUniqueId(), amount);
        clanManager.saveClan(clan);

        player.sendMessage(ChatColor.GREEN + "Você sacou " + amount + " créditos do banco do clã!");
        
        // Notificar membros online
        for (UUID memberId : clan.getMembers().keySet()) {
            Player member = plugin.getServer().getPlayer(memberId);
            if (member != null && member.isOnline() && !member.equals(player)) {
                member.sendMessage(ChatColor.RED + player.getName() + " sacou " + amount + " créditos do banco do clã!");
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        String title = event.getView().getTitle();
        if (!title.contains("✧ Banco do Clã ✧") && 
            !title.contains("✧ Sacar do Banco ✧") && 
            !title.contains("✧ Depositar no Banco ✧")) return;

        Player player = (Player) event.getPlayer();
        if (!playerStates.containsKey(player.getUniqueId())) {
            playerStates.remove(player.getUniqueId());
        }
    }
} 