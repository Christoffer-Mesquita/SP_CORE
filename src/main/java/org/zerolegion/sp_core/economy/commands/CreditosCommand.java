package org.zerolegion.sp_core.economy.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.zerolegion.sp_core.economy.StellarEconomyManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class CreditosCommand implements CommandExecutor, TabCompleter {
    private final StellarEconomyManager economyManager;

    public CreditosCommand(StellarEconomyManager economyManager) {
        this.economyManager = economyManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Este comando só pode ser executado por jogadores!");
            return true;
        }

        Player player = (Player) sender;

        // Comando base /creditos - mostra o saldo
        if (args.length == 0) {
            economyManager.openGUI(player);
            return true;
        }

        // Comando /creditos enviar <jogador> <quantidade>
        if (args[0].equalsIgnoreCase("enviar") && args.length == 3) {
            // Verificar se o jogador alvo existe
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                player.sendMessage(ChatColor.RED + "⚠ Jogador não encontrado!");
                return true;
            }

            // Verificar se o jogador está tentando enviar para si mesmo
            if (player.equals(target)) {
                player.sendMessage(ChatColor.RED + "⚠ Você não pode transferir créditos para si mesmo!");
                return true;
            }

            try {
                // Verificar se o valor é um número válido
                if (!args[2].matches("^\\d+(\\.\\d{1,2})?$")) {
                    player.sendMessage(ChatColor.RED + "⚠ Formato inválido! Use: /creditos enviar <jogador> <valor>");
                    return true;
                }

                double amount = Double.parseDouble(args[2]);

                // Verificar valor mínimo
                if (amount < 1) {
                    player.sendMessage(ChatColor.RED + "⚠ O valor mínimo para transferência é 1 ⭐!");
                    return true;
                }

                // Verificar se o jogador tem saldo suficiente
                double currentBalance = economyManager.getBalance(player.getUniqueId());
                if (currentBalance < amount) {
                    player.sendMessage("");
                    player.sendMessage(ChatColor.RED + "⚠ Créditos Estelares insuficientes!");
                    player.sendMessage(ChatColor.GRAY + "• Saldo atual: " + ChatColor.WHITE + 
                        economyManager.formatValue(currentBalance) + " ⭐");
                    player.sendMessage(ChatColor.GRAY + "• Tentando enviar: " + ChatColor.WHITE + 
                        economyManager.formatValue(amount) + " ⭐");
                    player.sendMessage(ChatColor.GRAY + "• Faltam: " + ChatColor.WHITE + 
                        economyManager.formatValue(amount - currentBalance) + " ⭐");
                    player.sendMessage("");
                    return true;
                }

                economyManager.transfer(player, target, amount);
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "⚠ Valor inválido! Use apenas números.");
            }
            return true;
        }

        // Comando /creditos top [quantidade]
        if (args[0].equalsIgnoreCase("top")) {
            int limit = 10;
            if (args.length > 1) {
                try {
                    limit = Integer.parseInt(args[1]);
                    limit = Math.min(100, Math.max(1, limit));
                } catch (NumberFormatException ignored) {}
            }
            economyManager.getTopBalance(player, limit);
            return true;
        }

        // Comandos administrativos
        if (player.hasPermission("sp.creditos.admin")) {
            if (args.length >= 3) {
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    player.sendMessage(ChatColor.RED + "⚠ Jogador não encontrado!");
                    return true;
                }

                try {
                    // Verificar se o valor é um número válido
                    if (!args[2].matches("^\\d+(\\.\\d{1,2})?$")) {
                        player.sendMessage(ChatColor.RED + "⚠ Formato inválido! Use apenas números com até 2 casas decimais.");
                        return true;
                    }

                    double amount = Double.parseDouble(args[2]);
                    
                    switch (args[0].toLowerCase()) {
                        case "add":
                            adminAddBalance(target, amount, player);
                            break;
                        case "remove":
                            adminRemoveBalance(target, amount, player);
                            break;
                        case "set":
                            economyManager.setBalance(target.getUniqueId(), amount);
                            player.sendMessage(ChatColor.YELLOW + "✧ Saldo de " + target.getName() + 
                                             " definido para " + economyManager.formatValue(amount) + " ⭐");
                            break;
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "⚠ Valor inválido! Use apenas números.");
                }
                return true;
            }
        }

        // Mensagem de ajuda
        sendHelpMessage(player);
        return true;
    }

    private void sendHelpMessage(Player player) {
        player.sendMessage("");
        player.sendMessage(ChatColor.YELLOW + "✧ Comandos de Créditos Estelares:");
        player.sendMessage(ChatColor.GRAY + "• /creditos " + ChatColor.WHITE + "- Mostra seu saldo");
        player.sendMessage(ChatColor.GRAY + "• /creditos enviar <jogador> <valor> " + ChatColor.WHITE + "- Envia créditos");
        player.sendMessage(ChatColor.GRAY + "• /creditos top [quantidade] " + ChatColor.WHITE + "- Mostra o ranking");
        
        if (player.hasPermission("sp.creditos.admin")) {
            player.sendMessage("");
            player.sendMessage(ChatColor.YELLOW + "✧ Comandos Administrativos:");
            player.sendMessage(ChatColor.GRAY + "• /creditos add <jogador> <valor>");
            player.sendMessage(ChatColor.GRAY + "• /creditos remove <jogador> <valor>");
            player.sendMessage(ChatColor.GRAY + "• /creditos set <jogador> <valor>");
        }
        player.sendMessage("");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("enviar");
            completions.add("top");
            
            if (sender.hasPermission("sp.creditos.admin")) {
                completions.add("add");
                completions.add("remove");
                completions.add("set");
            }
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("enviar") || 
                  (sender.hasPermission("sp.creditos.admin") && (args[0].equalsIgnoreCase("add") || 
                   args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("set"))))) {
            completions.addAll(Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .collect(Collectors.toList()));
        }

        return completions.stream()
                .filter(completion -> completion.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }

    private void adminAddBalance(Player target, double amount, Player admin) {
        economyManager.addBalance(target.getUniqueId(), amount);
        admin.sendMessage(ChatColor.YELLOW + "✧ Adicionado " + 
            economyManager.formatValue(amount) + " ⭐ ao saldo de " + target.getName());
        target.sendMessage(ChatColor.YELLOW + "✧ Você recebeu " + 
            economyManager.formatValue(amount) + " ⭐ de um administrador");
    }

    private void adminRemoveBalance(Player target, double amount, Player admin) {
        if (economyManager.removeBalance(target.getUniqueId(), amount)) {
            admin.sendMessage(ChatColor.YELLOW + "✧ Removido " + 
                economyManager.formatValue(amount) + " ⭐ do saldo de " + target.getName());
            target.sendMessage(ChatColor.YELLOW + "✧ Um administrador removeu " + 
                economyManager.formatValue(amount) + " ⭐ do seu saldo");
        } else {
            admin.sendMessage(ChatColor.RED + "⚠ O jogador não possui saldo suficiente!");
        }
    }

    private void transfer(Player from, Player to, double amount) {
        UUID fromId = from.getUniqueId();
        UUID toId = to.getUniqueId();
        
        if (economyManager.removeBalance(fromId, amount)) {
            economyManager.addBalance(toId, amount);
            
            from.sendMessage(ChatColor.YELLOW + "✧ Você enviou " + 
                economyManager.formatValue(amount) + " ⭐ para " + to.getName());
            to.sendMessage(ChatColor.YELLOW + "✧ Você recebeu " + 
                economyManager.formatValue(amount) + " ⭐ de " + from.getName());
        }
    }
} 