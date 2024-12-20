package org.zerolegion.sp_core.ships.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.zerolegion.sp_core.ships.PlayerHangar;
import org.zerolegion.sp_core.ships.PlayerShip;
import org.zerolegion.sp_core.ships.SpaceshipManager;
import org.zerolegion.sp_core.ships.planets.Planet;

public class ShipCommand implements CommandExecutor {
    private final SpaceshipManager spaceshipManager;

    public ShipCommand(SpaceshipManager spaceshipManager) {
        this.spaceshipManager = spaceshipManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Este comando só pode ser usado por jogadores!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            sendHelpMessage(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "loja":
                spaceshipManager.getShopGUI().openShopMenu(player);
                break;

            case "info":
                showShipInfo(player);
                break;

            case "lista":
                listPlayerShips(player);
                break;

            case "viajar":
                PlayerHangar hangarViagem = spaceshipManager.getPlayerHangar(player.getUniqueId());
                if (hangarViagem == null || hangarViagem.getShips().isEmpty()) {
                    player.sendMessage(ChatColor.RED + "Você precisa de uma nave para viajar!");
                    return true;
                }
                PlayerShip shipViagem = hangarViagem.getShip(0);
                if (shipViagem.getFuel() < 10) {
                    player.sendMessage(ChatColor.RED + "Sua nave precisa de pelo menos 10% de combustível para viajar!");
                    return true;
                }
                player.sendMessage(ChatColor.YELLOW + "Preparando sua viagem...");
                spaceshipManager.getPlugin().getPlanetManager().teleportPlayerToPlanet(player);
                break;

            case "voltar":
                Planet playerPlanet = spaceshipManager.getPlugin().getPlanetManager().getPlayerPlanet(player.getUniqueId());
                if (playerPlanet != null && playerPlanet.isGenerating()) {
                    player.sendMessage(ChatColor.RED + "Aguarde seu planeta terminar de ser gerado!");
                    return true;
                }
                if (playerPlanet == null) {
                    player.sendMessage(ChatColor.RED + "Você não está em nenhum planeta!");
                    return true;
                }
                
                // Mostrar relatório antes de teleportar
                spaceshipManager.getPlugin().getPlanetManager().getPlanetMiningListener().showMissionReport(player, playerPlanet);
                
                // Remover o planeta e teleportar o jogador
                spaceshipManager.getPlugin().getPlanetManager().removePlayerPlanet(player);
                player.setFallDistance(0); // Evitar dano de queda
                player.teleport(player.getWorld().getSpawnLocation());
                player.sendMessage("§a✔ Você retornou ao spawn!");
                break;

            case "combustivel":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Use: /nave combustivel <comprar/info>");
                    return true;
                }
                handleFuelCommand(player, args[1]);
                break;

            case "hangar":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Use: /nave hangar <info/upgrade>");
                    return true;
                }
                handleHangarCommand(player, args[1]);
                break;

            case "setspawn":
                if (!player.hasPermission("sp_core.admin")) {
                    player.sendMessage(ChatColor.RED + "Você não tem permissão para usar este comando!");
                    return true;
                }
                spaceshipManager.getPlugin().getPlanetManager().setSpawnLocation(player.getLocation());
                player.sendMessage(ChatColor.GREEN + "✔ Spawn configurado com sucesso!");
                player.sendMessage(ChatColor.GRAY + "Os jogadores retornarão para este local ao sair dos planetas.");
                break;

            default:
                sendHelpMessage(player);
                break;
        }

        return true;
    }

    private void sendHelpMessage(Player player) {
        player.sendMessage("");
        player.sendMessage("§e✧ Comandos de Nave ✧");
        player.sendMessage("");
        player.sendMessage("§f• §7/nave loja §f- Abrir loja de naves");
        player.sendMessage("§f• §7/nave info §f- Ver informações da sua nave");
        player.sendMessage("§f• §7/nave lista §f- Listar suas naves");
        player.sendMessage("§f• §7/nave viajar §f- Viajar para um planeta minerável");
        player.sendMessage("§f• §7/nave voltar §f- Retornar ao spawn");
        player.sendMessage("§f• §7/nave combustivel <comprar/info> §f- Gerenciar combustível");
        player.sendMessage("§f• §7/nave hangar <info/upgrade> §f- Gerenciar seu hangar");
        if (player.hasPermission("sp_core.admin")) {
            player.sendMessage("");
            player.sendMessage("§c✧ Comandos Administrativos ✧");
            player.sendMessage("§f• §7/nave setspawn §f- Definir local de retorno dos planetas");
        }
        player.sendMessage("");
    }

    private void showShipInfo(Player player) {
        PlayerHangar hangar = spaceshipManager.getPlayerHangar(player.getUniqueId());
        if (hangar == null || hangar.getShips().isEmpty()) {
            player.sendMessage(ChatColor.RED + "Você não possui nenhuma nave!");
            return;
        }

        PlayerShip ship = hangar.getShip(0); // Por enquanto, mostra apenas a primeira nave
        if (ship == null) {
            player.sendMessage(ChatColor.RED + "Erro ao carregar informações da nave!");
            return;
        }

        player.sendMessage("");
        player.sendMessage(ChatColor.YELLOW + "✧ Informações da Nave ✧");
        player.sendMessage("");
        player.sendMessage(ChatColor.GRAY + "Nome: " + ChatColor.WHITE + ship.getName());
        player.sendMessage(ChatColor.GRAY + "Combustível: " + ChatColor.WHITE + 
            String.format("%.1f%%", ship.getFuel()));
        
        // Mostrar upgrades
        if (!ship.getUpgrades().isEmpty()) {
            player.sendMessage("");
            player.sendMessage(ChatColor.YELLOW + "Melhorias:");
            ship.getUpgrades().forEach((upgrade, level) -> 
                player.sendMessage(ChatColor.GRAY + "• " + upgrade + ": " + 
                    ChatColor.WHITE + "Nível " + level)
            );
        }
        
        player.sendMessage("");
    }

    private void listPlayerShips(Player player) {
        PlayerHangar hangar = spaceshipManager.getPlayerHangar(player.getUniqueId());
        if (hangar == null || hangar.getShips().isEmpty()) {
            player.sendMessage(ChatColor.RED + "Você não possui nenhuma nave!");
            return;
        }

        player.sendMessage("");
        player.sendMessage(ChatColor.YELLOW + "✧ Suas Naves ✧");
        player.sendMessage("");

        int index = 1;
        for (PlayerShip ship : hangar.getShips()) {
            player.sendMessage(ChatColor.GRAY + String.valueOf(index++) + ". " + 
                ChatColor.WHITE + ship.getName() + ChatColor.GRAY + " - " + 
                String.format("%.1f%% de combustível", ship.getFuel()));
        }

        player.sendMessage("");
    }

    private void handleFuelCommand(Player player, String subCommand) {
        PlayerHangar hangar = spaceshipManager.getPlayerHangar(player.getUniqueId());
        if (hangar == null || hangar.getShips().isEmpty()) {
            player.sendMessage(ChatColor.RED + "Você não possui nenhuma nave!");
            return;
        }

        PlayerShip ship = hangar.getShip(0); // Por enquanto, usa a primeira nave
        if (ship == null) {
            player.sendMessage(ChatColor.RED + "Erro ao carregar sua nave!");
            return;
        }

        switch (subCommand.toLowerCase()) {
            case "info":
                player.sendMessage("");
                player.sendMessage(ChatColor.YELLOW + "✧ Informações de Combustível ✧");
                player.sendMessage("");
                player.sendMessage(ChatColor.GRAY + "Nave: " + ChatColor.WHITE + ship.getName());
                player.sendMessage(ChatColor.GRAY + "Combustível: " + ChatColor.WHITE + 
                    String.format("%.1f%%", ship.getFuel()));
                player.sendMessage("");
                break;

            case "comprar":
                spaceshipManager.getFuelShopGUI().openFuelShop(player, ship);
                break;

            default:
                player.sendMessage(ChatColor.RED + "Use: /nave combustivel <comprar/info>");
                break;
        }
    }

    private void handleHangarCommand(Player player, String subCommand) {
        PlayerHangar hangar = spaceshipManager.getPlayerHangar(player.getUniqueId());
        if (hangar == null) {
            player.sendMessage(ChatColor.RED + "Você não possui um hangar!");
            return;
        }

        switch (subCommand.toLowerCase()) {
            case "info":
                player.sendMessage("");
                player.sendMessage(ChatColor.YELLOW + "✧ Informações do Hangar ✧");
                player.sendMessage("");
                player.sendMessage(ChatColor.GRAY + "Nível: " + ChatColor.WHITE + hangar.getLevel());
                player.sendMessage(ChatColor.GRAY + "Naves: " + ChatColor.WHITE + 
                    hangar.getShips().size() + "/" + spaceshipManager.getMaxShips(hangar.getLevel()));
                player.sendMessage("");
                break;

            case "upgrade":
                player.sendMessage(ChatColor.RED + "Sistema de upgrade em desenvolvimento!");
                break;

            default:
                player.sendMessage(ChatColor.RED + "Use: /nave hangar <info/upgrade>");
                break;
        }
    }
} 