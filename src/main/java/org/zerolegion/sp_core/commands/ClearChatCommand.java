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

public class ClearChatCommand implements CommandExecutor, TabCompleter {
    private final SP_CORE plugin;

    public ClearChatCommand(SP_CORE plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("sensitive.clearchat")) {
            sender.sendMessage(ChatColor.RED + "⚠ Você não possui autorização para executar este comando!");
            return true;
        }

        // Se não especificar argumento, limpa para todos
        String mode = args.length > 0 ? args[0].toLowerCase() : "all";

        switch (mode) {
            case "all":
                clearGlobalChat(sender);
                break;
            case "self":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "⚠ Este comando só pode ser usado por jogadores!");
                    return true;
                }
                clearSelfChat((Player) sender);
                break;
            default:
                sender.sendMessage(ChatColor.RED + "⚠ Uso: /clearchat [all/self]");
                break;
        }

        return true;
    }

    private void clearGlobalChat(CommandSender sender) {
        // Envia 100 linhas em branco para todos os jogadores
        for (Player player : Bukkit.getOnlinePlayers()) {
            for (int i = 0; i < 100; i++) {
                player.sendMessage("");
            }
        }

        // Envia mensagem decorada de limpeza
        String clearMessage = "\n" +
            ChatColor.AQUA + "❈ " + ChatColor.DARK_AQUA + "Chat Limpo " + ChatColor.AQUA + "❈\n" +
            ChatColor.GRAY + "O chat foi limpo por " + ChatColor.YELLOW + sender.getName() + "\n" +
            ChatColor.AQUA + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬\n";

        Bukkit.broadcastMessage(clearMessage);
    }

    private void clearSelfChat(Player player) {
        // Envia 100 linhas em branco apenas para o jogador
        for (int i = 0; i < 100; i++) {
            player.sendMessage("");
        }

        // Envia mensagem decorada de limpeza
        String clearMessage = "\n" +
            ChatColor.AQUA + "❈ " + ChatColor.DARK_AQUA + "Chat Limpo " + ChatColor.AQUA + "❈\n" +
            ChatColor.GRAY + "Você limpou seu chat\n" +
            ChatColor.AQUA + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬\n";

        player.sendMessage(clearMessage);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("all", "self");
        }
        return new ArrayList<>();
    }
} 