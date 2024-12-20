package org.zerolegion.sp_core.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.zerolegion.sp_core.SP_CORE;
import java.util.*;
import org.zerolegion.sp_core.permissions.Group;

public class PermissionCommand implements CommandExecutor, TabCompleter {
    private final SP_CORE plugin;

    public PermissionCommand(SP_CORE plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("sensitive.permissions")) {
            sender.sendMessage(ChatColor.RED + "Você não tem permissão para usar este comando!");
            return true;
        }

        if (args.length < 1) {
            sendHelp(sender);
            return true;
        }

        String action = args[0].toLowerCase();

        switch (action) {
            case "group":
                handleGroupCommand(sender, args);
                break;
            case "player":
                handlePlayerCommand(sender, args);
                break;
            case "list":
                handleListCommand(sender, args);
                break;
            default:
                sendHelp(sender);
                break;
        }

        return true;
    }

    private void handleGroupCommand(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "⚠ Uso correto: /permissoes group <nome> <create/delete/addperm/delperm/setprefix/setweight>");
            return;
        }

        String groupName = args[1];
        String subAction = args[2].toLowerCase();

        if (sender instanceof Player && !subAction.equals("create")) {
            Player player = (Player) sender;
            if (!plugin.getPermissionManager().canManageGroup(player, groupName)) {
                sender.sendMessage(ChatColor.RED + "⚠ Você não tem permissão para gerenciar este grupo!");
                return;
            }
        }

        if (!subAction.equals("create") && !plugin.getPermissionManager().getAllGroups().contains(groupName)) {
            sender.sendMessage(ChatColor.RED + "⚠ O grupo '" + ChatColor.YELLOW + groupName + ChatColor.RED + "' não existe!");
            return;
        }

        switch (subAction) {
            case "create":
                if (plugin.getPermissionManager().getAllGroups().contains(groupName)) {
                    sender.sendMessage(ChatColor.RED + "⚠ O grupo '" + ChatColor.YELLOW + groupName + ChatColor.RED + "' já existe!");
                    return;
                }
                if (args.length < 4) {
                    sender.sendMessage(ChatColor.RED + "⚠ Uso: /permissoes group <nome> create <prefix>");
                    return;
                }
                plugin.getPermissionManager().createGroup(groupName, args[3]);
                sender.sendMessage(ChatColor.GREEN + "✔ Grupo '" + ChatColor.YELLOW + groupName + ChatColor.GREEN + "' criado com sucesso!");
                break;

            case "setweight":
                if (args.length < 4) {
                    sender.sendMessage(ChatColor.RED + "⚠ Uso: /permissoes group <nome> setweight <peso>");
                    return;
                }
                try {
                    int weight = Integer.parseInt(args[3]);
                    plugin.getPermissionManager().setGroupWeight(groupName, weight);
                    sender.sendMessage(ChatColor.GREEN + "✔ Peso do grupo '" + ChatColor.YELLOW + groupName + ChatColor.GREEN + "' definido para " + weight);
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "⚠ O peso deve ser um número!");
                }
                break;

            case "delete":
                plugin.getPermissionManager().deleteGroup(groupName);
                sender.sendMessage(ChatColor.GREEN + "✔ Grupo '" + ChatColor.YELLOW + groupName + ChatColor.GREEN + "' deletado com sucesso!");
                break;

            case "addperm":
                if (args.length < 4) {
                    sender.sendMessage(ChatColor.RED + "⚠ Uso: /permissoes group <nome> addperm <permissão>");
                    return;
                }
                plugin.getPermissionManager().addGroupPermission(groupName, args[3]);
                sender.sendMessage(ChatColor.GREEN + "✔ Permissão '" + ChatColor.YELLOW + args[3] + ChatColor.GREEN + "' adicionada ao grupo '" + ChatColor.YELLOW + groupName + ChatColor.GREEN + "'!");
                break;

            case "delperm":
                if (args.length < 4) {
                    sender.sendMessage(ChatColor.RED + "⚠ Uso: /permissoes group <nome> delperm <permissão>");
                    return;
                }
                plugin.getPermissionManager().removeGroupPermission(groupName, args[3]);
                sender.sendMessage(ChatColor.GREEN + "✔ Permissão '" + ChatColor.YELLOW + args[3] + ChatColor.GREEN + "' removida do grupo '" + ChatColor.YELLOW + groupName + ChatColor.GREEN + "'!");
                break;

            case "setprefix":
                if (args.length < 4) {
                    sender.sendMessage(ChatColor.RED + "⚠ Uso: /permissoes group <nome> setprefix <prefix>");
                    return;
                }
                plugin.getPermissionManager().setGroupPrefix(groupName, args[3]);
                sender.sendMessage(ChatColor.GREEN + "✔ Prefix do grupo '" + ChatColor.YELLOW + groupName + ChatColor.GREEN + "' atualizado!");
                break;

            default:
                sender.sendMessage(ChatColor.RED + "⚠ Ação desconhecida. Use create/delete/addperm/delperm/setprefix/setweight");
                break;
        }
    }

    private void handlePlayerCommand(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage(ChatColor.RED + "⚠ Uso: /permissoes player <player> <addgroup/removegroup/addperm/delperm>");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "⚠ Jogador não encontrado!");
            return;
        }

        String subAction = args[2].toLowerCase();
        UUID playerId = target.getUniqueId();

        switch (subAction) {
            case "addgroup":
                String groupName = args[3];
                if (!plugin.getPermissionManager().getAllGroups().contains(groupName)) {
                    sender.sendMessage(ChatColor.RED + "⚠ O grupo '" + ChatColor.YELLOW + groupName + ChatColor.RED + "' não existe!");
                    return;
                }

                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    if (!plugin.getPermissionManager().canManageGroup(player, groupName)) {
                        sender.sendMessage(ChatColor.RED + "⚠ Você não tem permissão para adicionar jogadores a este grupo!");
                        return;
                    }
                }

                boolean alreadyInGroup = plugin.getPermissionManager().getPlayerGroups(playerId).stream()
                    .anyMatch(group -> group.getName().equalsIgnoreCase(groupName));
                
                if (alreadyInGroup) {
                    sender.sendMessage(ChatColor.RED + "⚠ O jogador já está no grupo '" + ChatColor.YELLOW + groupName + ChatColor.RED + "'!");
                    return;
                }
                plugin.getPermissionManager().addPlayerToGroup(playerId, groupName);
                sender.sendMessage(ChatColor.GREEN + "✔ Jogador adicionado ao grupo '" + ChatColor.YELLOW + groupName + ChatColor.GREEN + "'!");
                break;

            case "removegroup":
                groupName = args[3];
                if (!plugin.getPermissionManager().getAllGroups().contains(groupName)) {
                    sender.sendMessage(ChatColor.RED + "⚠ O grupo '" + ChatColor.YELLOW + groupName + ChatColor.RED + "' não existe!");
                    return;
                }

                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    if (!plugin.getPermissionManager().canManageGroup(player, groupName)) {
                        sender.sendMessage(ChatColor.RED + "⚠ Você não tem permissão para remover jogadores deste grupo!");
                        return;
                    }
                }

                if (!plugin.getPermissionManager().getPlayerGroups(playerId).contains(groupName)) {
                    sender.sendMessage(ChatColor.RED + "⚠ O jogador não está no grupo '" + ChatColor.YELLOW + groupName + ChatColor.RED + "'!");
                    return;
                }
                plugin.getPermissionManager().removePlayerFromGroup(playerId, groupName);
                sender.sendMessage(ChatColor.GREEN + "✔ Jogador removido do grupo '" + ChatColor.YELLOW + groupName + ChatColor.GREEN + "'!");
                break;

            case "addperm":
                plugin.getPermissionManager().addPlayerPermission(playerId, args[3]);
                sender.sendMessage(ChatColor.GREEN + "✔ Permissão '" + ChatColor.YELLOW + args[3] + ChatColor.GREEN + "' adicionada ao jogador!");
                break;

            case "delperm":
                plugin.getPermissionManager().removePlayerPermission(playerId, args[3]);
                sender.sendMessage(ChatColor.GREEN + "✔ Permissão '" + ChatColor.YELLOW + args[3] + ChatColor.GREEN + "' removida do jogador!");
                break;

            default:
                sender.sendMessage(ChatColor.RED + "⚠ Ação desconhecida. Use addgroup/removegroup/addperm/delperm");
                break;
        }
    }

    private void handleListCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "⚠ Uso: /permissoes list <groups/player <nome>/group <nome>>");
            return;
        }

        String subAction = args[1].toLowerCase();

        switch (subAction) {
            case "groups":
                Set<String> groups = plugin.getPermissionManager().getAllGroups();
                sender.sendMessage(ChatColor.AQUA + "❈ " + ChatColor.DARK_AQUA + "Grupos Disponíveis " + ChatColor.AQUA + "❈");
                groups.stream()
                    .sorted((g1, g2) -> Integer.compare(
                        plugin.getPermissionManager().getGroupWeight(g2),
                        plugin.getPermissionManager().getGroupWeight(g1)))
                    .forEach(group -> {
                        String prefix = plugin.getPermissionManager().getGroupPrefix(group);
                        int weight = plugin.getPermissionManager().getGroupWeight(group);
                        sender.sendMessage(ChatColor.AQUA + "► " + ChatColor.WHITE + group + 
                            ChatColor.YELLOW + " (Prefix: " + prefix + ChatColor.YELLOW + ", Peso: " + weight + ")");
                    });
                break;

            case "player":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "⚠ Uso: /permissoes list player <nome>");
                    return;
                }
                Player target = Bukkit.getPlayer(args[2]);
                if (target == null) {
                    sender.sendMessage(ChatColor.RED + "⚠ Jogador não encontrado!");
                    return;
                }

                UUID playerId = target.getUniqueId();
                List<Group> playerGroups = plugin.getPermissionManager().getPlayerGroups(playerId);
                Set<String> playerPerms = plugin.getPermissionManager().getPlayerPermissions(playerId);

                sender.sendMessage(ChatColor.AQUA + "❈ " + ChatColor.DARK_AQUA + "Informações do Jogador " + ChatColor.YELLOW + target.getName() + ChatColor.AQUA + " ❈");
                sender.sendMessage(ChatColor.AQUA + "► " + ChatColor.DARK_AQUA + "Grupos:");
                for (Group group : playerGroups) {
                    sender.sendMessage(ChatColor.AQUA + "  ● " + ChatColor.WHITE + group.getName() + 
                        ChatColor.GRAY + " (Peso: " + group.getWeight() + ", Prefix: " + group.getPrefix() + ")");
                }
                sender.sendMessage(ChatColor.AQUA + "► " + ChatColor.DARK_AQUA + "Permissões Individuais:");
                for (String perm : playerPerms) {
                    sender.sendMessage(ChatColor.AQUA + "  ● " + ChatColor.WHITE + perm);
                }
                break;

            case "group":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "⚠ Uso: /permissoes list group <nome>");
                    return;
                }
                String groupName = args[2];
                Set<String> groupPerms = plugin.getPermissionManager().getGroupPermissions(groupName);
                String prefix = plugin.getPermissionManager().getGroupPrefix(groupName);

                sender.sendMessage(ChatColor.AQUA + "❈ " + ChatColor.DARK_AQUA + "Informações do Grupo " + ChatColor.YELLOW + groupName + ChatColor.AQUA + " ❈");
                sender.sendMessage(ChatColor.AQUA + "► " + ChatColor.DARK_AQUA + "Prefix: " + ChatColor.WHITE + prefix);
                sender.sendMessage(ChatColor.AQUA + "► " + ChatColor.DARK_AQUA + "Permissões:");
                for (String perm : groupPerms) {
                    sender.sendMessage(ChatColor.AQUA + "  ● " + ChatColor.WHITE + perm);
                }
                break;

            default:
                sender.sendMessage(ChatColor.RED + "⚠ Uso: /permissoes list <groups/player <nome>/group <nome>>");
                break;
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.AQUA + "❈ " + ChatColor.DARK_AQUA + "Sistema de Permissões " + ChatColor.AQUA + "❈");
        sender.sendMessage(ChatColor.AQUA + "► " + ChatColor.WHITE + "/permissoes group <nome> create <prefix> " + ChatColor.GRAY + "- Criar grupo");
        sender.sendMessage(ChatColor.AQUA + "► " + ChatColor.WHITE + "/permissoes group <nome> delete " + ChatColor.GRAY + "- Deletar grupo");
        sender.sendMessage(ChatColor.AQUA + "► " + ChatColor.WHITE + "/permissoes group <nome> setweight <peso> " + ChatColor.GRAY + "- Definir peso do grupo");
        sender.sendMessage(ChatColor.AQUA + "► " + ChatColor.WHITE + "/permissoes group <nome> addperm <perm> " + ChatColor.GRAY + "- Adicionar permissão");
        sender.sendMessage(ChatColor.AQUA + "► " + ChatColor.WHITE + "/permissoes group <nome> delperm <perm> " + ChatColor.GRAY + "- Remover permissão");
        sender.sendMessage(ChatColor.AQUA + "► " + ChatColor.WHITE + "/permissoes group <nome> setprefix <prefix> " + ChatColor.GRAY + "- Definir prefix");
        sender.sendMessage(ChatColor.AQUA + "► " + ChatColor.WHITE + "/permissoes player <player> addgroup <grupo> " + ChatColor.GRAY + "- Add ao grupo");
        sender.sendMessage(ChatColor.AQUA + "► " + ChatColor.WHITE + "/permissoes player <player> removegroup <grupo> " + ChatColor.GRAY + "- Remover do grupo");
        sender.sendMessage(ChatColor.AQUA + "► " + ChatColor.WHITE + "/permissoes player <player> addperm <perm> " + ChatColor.GRAY + "- Add permissão");
        sender.sendMessage(ChatColor.AQUA + "► " + ChatColor.WHITE + "/permissoes player <player> delperm <perm> " + ChatColor.GRAY + "- Remover permissão");
        sender.sendMessage(ChatColor.AQUA + "► " + ChatColor.WHITE + "/permissoes list groups " + ChatColor.GRAY + "- Listar grupos");
        sender.sendMessage(ChatColor.AQUA + "► " + ChatColor.WHITE + "/permissoes list player <nome> " + ChatColor.GRAY + "- Info do jogador");
        sender.sendMessage(ChatColor.AQUA + "► " + ChatColor.WHITE + "/permissoes list group <nome> " + ChatColor.GRAY + "- Info do grupo");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("sensitive.permissions")) {
            return new ArrayList<>();
        }

        if (args.length == 1) {
            return Arrays.asList("group", "player", "list");
        }

        if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "group":
                    List<String> groups = new ArrayList<>(plugin.getPermissionManager().getAllGroups());
                    groups.add("new"); // Para criar novo grupo
                    return groups;
                case "player":
                    return null; // Retorna null para mostrar lista de jogadores
                case "list":
                    return Arrays.asList("groups", "player", "group");
            }
        }

        if (args.length == 3) {
            switch (args[0].toLowerCase()) {
                case "group":
                    return Arrays.asList("create", "delete", "addperm", "delperm", "setprefix", "setweight");
                case "player":
                    return Arrays.asList("addgroup", "removegroup", "addperm", "delperm");
            }
        }

        if (args.length == 4) {
            if (args[0].toLowerCase().equals("player") && 
                (args[2].toLowerCase().equals("addgroup") || args[2].toLowerCase().equals("removegroup"))) {
                return new ArrayList<>(plugin.getPermissionManager().getAllGroups());
            }
        }

        return new ArrayList<>();
    }
} 