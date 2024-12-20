package org.zerolegion.sp_core.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
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

public class GamemodeCommand implements CommandExecutor, TabCompleter {
    private final SP_CORE plugin;

    public GamemodeCommand(SP_CORE plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("sensitive.gamemode")) {
            sender.sendMessage(ChatColor.RED + "⚠ Você não possui autorização para executar este comando!");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "⚠ Uso: /gm <0/1/2/3> [jogador]");
            return true;
        }

        // Pega o modo de jogo
        GameMode gameMode = getGameMode(args[0]);
        if (gameMode == null) {
            sender.sendMessage(ChatColor.RED + "⚠ Modo de jogo inválido! Use 0, 1, 2 ou 3");
            return true;
        }

        // Pega o jogador alvo
        Player target;
        if (args.length > 1) {
            target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "⚠ Jogador não encontrado!");
                return true;
            }
        } else {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "⚠ Você precisa especificar um jogador!");
                return true;
            }
            target = (Player) sender;
        }

        // Altera o modo de jogo
        target.setGameMode(gameMode);

        // Envia mensagens decoradas
        String gameModeMessage = "\n" +
            ChatColor.AQUA + "❈ " + ChatColor.DARK_AQUA + "Modo de Jogo " + ChatColor.AQUA + "❈\n" +
            ChatColor.GRAY + "Alterado para " + formatGameMode(gameMode) + "\n" +
            ChatColor.AQUA + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬\n";

        target.sendMessage(gameModeMessage);

        // Se o sender for diferente do target, notifica o sender
        if (sender != target) {
            String adminMessage = "\n" +
                ChatColor.AQUA + "❈ " + ChatColor.DARK_AQUA + "Modo de Jogo " + ChatColor.AQUA + "❈\n" +
                ChatColor.GRAY + "Alterado para " + formatGameMode(gameMode) + "\n" +
                ChatColor.GRAY + "Jogador: " + ChatColor.YELLOW + target.getName() + "\n" +
                ChatColor.AQUA + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬\n";

            sender.sendMessage(adminMessage);
        }

        return true;
    }

    private GameMode getGameMode(String input) {
        switch (input.toLowerCase()) {
            case "0":
            case "s":
            case "survival":
                return GameMode.SURVIVAL;
            case "1":
            case "c":
            case "creative":
                return GameMode.CREATIVE;
            case "2":
            case "a":
            case "adventure":
                return GameMode.ADVENTURE;
            case "3":
            case "sp":
            case "spectator":
                return GameMode.SPECTATOR;
            default:
                return null;
        }
    }

    private String formatGameMode(GameMode gameMode) {
        switch (gameMode) {
            case SURVIVAL:
                return ChatColor.GREEN + "Sobrevivência";
            case CREATIVE:
                return ChatColor.GOLD + "Criativo";
            case ADVENTURE:
                return ChatColor.RED + "Aventura";
            case SPECTATOR:
                return ChatColor.GRAY + "Espectador";
            default:
                return ChatColor.WHITE + gameMode.name();
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("sensitive.gamemode")) {
            return new ArrayList<>();
        }

        if (args.length == 1) {
            return Arrays.asList("0", "1", "2", "3");
        }

        if (args.length == 2) {
            return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
} 