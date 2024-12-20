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

public class LevelCommand implements CommandExecutor, TabCompleter {
    private final SP_CORE plugin;

    public LevelCommand(SP_CORE plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("sensitive.level")) {
            sender.sendMessage(ChatColor.RED + "⚠ Você não possui autorização para executar este comando!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "⚠ Uso: /level <jogador> <nível>");
            return true;
        }

        // Pega o jogador alvo
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "⚠ Jogador não encontrado!");
            return true;
        }

        // Pega o novo nível
        int newLevel;
        try {
            newLevel = Integer.parseInt(args[1]);
            if (newLevel < 1) {
                sender.sendMessage(ChatColor.RED + "⚠ O nível deve ser maior que 0!");
                return true;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "⚠ O nível deve ser um número!");
            return true;
        }

        // Altera o nível
        plugin.getLevelManager().setLevel(target, newLevel);

        // Envia mensagem de confirmação
        String message = "\n" +
            ChatColor.AQUA + "❈ " + ChatColor.DARK_AQUA + "Level Alterado " + ChatColor.AQUA + "❈\n" +
            ChatColor.GRAY + "Jogador: " + ChatColor.YELLOW + target.getName() + "\n" +
            ChatColor.GRAY + "Novo nível: " + ChatColor.YELLOW + newLevel + "\n" +
            ChatColor.AQUA + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬\n";

        sender.sendMessage(message);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("sensitive.level")) {
            return new ArrayList<>();
        }

        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
} 