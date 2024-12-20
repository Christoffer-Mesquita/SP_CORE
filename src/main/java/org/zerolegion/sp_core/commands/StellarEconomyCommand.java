package org.zerolegion.sp_core.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.zerolegion.sp_core.SP_CORE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class StellarEconomyCommand implements CommandExecutor, TabCompleter {
    private final SP_CORE plugin;

    public StellarEconomyCommand(SP_CORE plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Este comando só pode ser usado por jogadores!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            // /credits - Ver saldo
            showBalance(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "enviar":
            case "pay":
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Use: /credits enviar <jogador> <quantia>");
                    return true;
                }
                handleTransfer(player, args[1], args[2]);
                break;

            case "top":
                plugin.getStellarEconomyManager().getTopBalance(player, 10);
                break;

            case "ajuda":
            case "help":
                showHelp(player);
                break;

            default:
                showHelp(player);
                break;
        }

        return true;
    }

    private void showBalance(Player player) {
        player.sendMessage("");
        player.sendMessage(ChatColor.YELLOW + "✧ Seus Créditos Estelares:");
        player.sendMessage(ChatColor.WHITE + plugin.getStellarEconomyManager().getFormattedBalance(player));
        player.sendMessage("");
    }

    private void handleTransfer(Player from, String targetName, String amountStr) {
        Player to = Bukkit.getPlayer(targetName);
        if (to == null) {
            from.sendMessage(ChatColor.RED + "⚠ Jogador não encontrado!");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            from.sendMessage(ChatColor.RED + "⚠ Quantia inválida!");
            return;
        }

        plugin.getStellarEconomyManager().transfer(from, to, amount);
    }

    private void showHelp(Player player) {
        player.sendMessage("");
        player.sendMessage(ChatColor.YELLOW + "✧ COMANDOS DE CRÉDITOS ESTELARES ✧");
        player.sendMessage("");
        player.sendMessage(ChatColor.GRAY + "• " + ChatColor.WHITE + "/credits " + ChatColor.GRAY + "- Ver seu saldo");
        player.sendMessage(ChatColor.GRAY + "• " + ChatColor.WHITE + "/credits enviar <jogador> <quantia>" + ChatColor.GRAY + " - Transferir créditos");
        player.sendMessage(ChatColor.GRAY + "• " + ChatColor.WHITE + "/credits top " + ChatColor.GRAY + "- Ver top 10 mais ricos");
        player.sendMessage(ChatColor.GRAY + "• " + ChatColor.WHITE + "/credits ajuda " + ChatColor.GRAY + "- Ver esta mensagem");
        player.sendMessage("");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("enviar", "top", "ajuda")
                    .stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("enviar")) {
            return Bukkit.getOnlinePlayers()
                    .stream()
                    .map(Player::getName)
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
} 