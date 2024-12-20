package org.zerolegion.sp_core.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.zerolegion.sp_core.SP_CORE;
import org.zerolegion.sp_core.clans.ClanManager;
import org.zerolegion.sp_core.clans.SpaceClan;
import org.zerolegion.sp_core.clans.ClanRole;
import org.zerolegion.sp_core.clans.gui.ClanMainGUI;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.UUID;

public class ClanCommand implements CommandExecutor, TabCompleter {
    private final SP_CORE plugin;
    private final ClanManager clanManager;
    private final ClanMainGUI mainGUI;

    public ClanCommand(SP_CORE plugin) {
        this.plugin = plugin;
        this.clanManager = plugin.getClanManager();
        this.mainGUI = new ClanMainGUI(clanManager);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Este comando só pode ser usado por jogadores!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            mainGUI.openGUI(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "criar":
                handleCreate(player, args);
                break;
            case "aceitar":
                handleAccept(player, args);
                break;
            case "recusar":
                handleDecline(player, args);
                break;
            case "sair":
                handleLeave(player);
                break;
            case "info":
                handleInfo(player, args);
                break;
            case "deletar":
                handleDelete(player);
                break;
            case "ajuda":
                sendHelp(player);
                break;
            default:
                player.sendMessage(ChatColor.RED + "Comando desconhecido. Use /clan ajuda para ver os comandos disponíveis.");
                break;
        }

        return true;
    }

    private void handleCreate(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Uso correto: /clan criar <nome> <tag>");
            return;
        }

        String name = args[1];
        String tag = args[2];

        if (clanManager.getPlayerClan(player.getUniqueId()) != null) {
            player.sendMessage(ChatColor.RED + "Você já está em um clã!");
            return;
        }

        SpaceClan clan = clanManager.createClan(name, tag, player);
        if (clan == null) {
            player.sendMessage(ChatColor.RED + "Não foi possível criar o clã. O nome ou tag já existem!");
            return;
        }

        player.sendMessage("");
        player.sendMessage(ChatColor.GREEN + "✔ Clã criado com sucesso!");
        player.sendMessage(ChatColor.GRAY + "Use /clan para abrir o menu do clã.");
        player.sendMessage("");
    }

    private void handleAccept(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Uso correto: /clan aceitar <nome_do_clã>");
            return;
        }

        String clanName = args[1];
        SpaceClan clan = null;

        for (SpaceClan c : clanManager.getAllClans()) {
            if (c.getName().equalsIgnoreCase(clanName)) {
                clan = c;
                break;
            }
        }

        if (clan == null) {
            player.sendMessage(ChatColor.RED + "Clã não encontrado!");
            return;
        }

        if (clanManager.getPlayerClan(player.getUniqueId()) != null) {
            player.sendMessage(ChatColor.RED + "Você já está em um clã!");
            return;
        }

        clanManager.addMember(clan.getId(), player.getUniqueId(), ClanRole.RECRUIT);
        player.sendMessage(ChatColor.GREEN + "Você entrou no clã " + clan.getName() + "!");
    }

    private void handleDecline(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Uso correto: /clan recusar <nome_do_clã>");
            return;
        }

        player.sendMessage(ChatColor.GREEN + "Convite recusado!");
    }

    private void handleLeave(Player player) {
        SpaceClan clan = clanManager.getPlayerClan(player.getUniqueId());
        if (clan == null) {
            player.sendMessage(ChatColor.RED + "Você não está em nenhum clã!");
            return;
        }

        if (clan.isLeader(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "O líder não pode sair do clã! Use /clan deletar para deletar o clã.");
            return;
        }

        clanManager.removeMember(clan.getId(), player.getUniqueId());
        player.sendMessage(ChatColor.GREEN + "Você saiu do clã " + clan.getName() + "!");
    }

    private void handleInfo(Player player, String[] args) {
        SpaceClan clan;
        if (args.length > 1) {
            String clanName = args[1];
            clan = clanManager.getAllClans().stream()
                    .filter(c -> c.getName().equalsIgnoreCase(clanName))
                    .findFirst()
                    .orElse(null);
        } else {
            clan = clanManager.getPlayerClan(player.getUniqueId());
        }

        if (clan == null) {
            player.sendMessage(ChatColor.RED + "Clã não encontrado!");
            return;
        }

        player.sendMessage("");
        player.sendMessage(ChatColor.LIGHT_PURPLE + "✧ " + ChatColor.WHITE + "Informações do Clã " + 
                         ChatColor.LIGHT_PURPLE + clan.getName() + ChatColor.WHITE + " ✧");
        player.sendMessage(ChatColor.GRAY + "Tag: " + ChatColor.WHITE + clan.getTag());
        player.sendMessage(ChatColor.GRAY + "Líder: " + ChatColor.WHITE + 
                         plugin.getServer().getOfflinePlayer(clan.getLeader()).getName());
        player.sendMessage(ChatColor.GRAY + "Membros: " + ChatColor.WHITE + clan.getMembers().size());
        player.sendMessage(ChatColor.GRAY + "Poder: " + ChatColor.WHITE + clan.getPower());
        if (clan.getDescription() != null && !clan.getDescription().isEmpty()) {
            player.sendMessage(ChatColor.GRAY + "Descrição: " + ChatColor.WHITE + clan.getDescription());
        }
        player.sendMessage("");
    }

    private void handleDelete(Player player) {
        SpaceClan clan = clanManager.getPlayerClan(player.getUniqueId());
        if (clan == null) {
            player.sendMessage(ChatColor.RED + "Você não está em nenhum clã!");
            return;
        }

        if (!clan.isLeader(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Apenas o líder pode deletar o clã!");
            return;
        }

        // Notificar membros online antes de deletar
        for (UUID memberId : clan.getMembers().keySet()) {
            Player member = plugin.getServer().getPlayer(memberId);
            if (member != null && member.isOnline()) {
                member.sendMessage("");
                member.sendMessage(ChatColor.RED + "⚠ O clã " + clan.getName() + " foi deletado pelo líder!");
                member.sendMessage("");
            }
        }

        clanManager.disbandClan(clan.getId());
        player.sendMessage(ChatColor.GREEN + "Clã deletado com sucesso!");
    }

    private void sendHelp(Player player) {
        player.sendMessage("");
        player.sendMessage(ChatColor.LIGHT_PURPLE + "✧ " + ChatColor.WHITE + "Comandos do Clã " + ChatColor.LIGHT_PURPLE + "✧");
        player.sendMessage(ChatColor.YELLOW + "/clan " + ChatColor.GRAY + "- Abre o menu do clã");
        player.sendMessage(ChatColor.YELLOW + "/clan criar <nome> <tag> " + ChatColor.GRAY + "- Cria um novo clã");
        player.sendMessage(ChatColor.YELLOW + "/clan aceitar <clã> " + ChatColor.GRAY + "- Aceita um convite de clã");
        player.sendMessage(ChatColor.YELLOW + "/clan recusar <clã> " + ChatColor.GRAY + "- Recusa um convite de clã");
        player.sendMessage(ChatColor.YELLOW + "/clan sair " + ChatColor.GRAY + "- Sai do seu clã atual");
        player.sendMessage(ChatColor.YELLOW + "/clan info [clã] " + ChatColor.GRAY + "- Mostra informações do clã");
        player.sendMessage(ChatColor.YELLOW + "/clan deletar " + ChatColor.GRAY + "- Deleta seu clã (apenas líder)");
        player.sendMessage(ChatColor.YELLOW + "/clan ajuda " + ChatColor.GRAY + "- Mostra esta mensagem");
        player.sendMessage("");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("criar");
            completions.add("aceitar");
            completions.add("recusar");
            completions.add("sair");
            completions.add("info");
            completions.add("deletar");
            completions.add("ajuda");
            return filterCompletions(completions, args[0]);
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("info") || 
                args[0].equalsIgnoreCase("aceitar") || 
                args[0].equalsIgnoreCase("recusar")) {
                return clanManager.getAllClans().stream()
                        .map(SpaceClan::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }

        return completions;
    }

    private List<String> filterCompletions(List<String> completions, String input) {
        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(input.toLowerCase()))
                .collect(Collectors.toList());
    }
} 