package org.zerolegion.sp_core.crafting.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import org.zerolegion.sp_core.SP_CORE;
import org.zerolegion.sp_core.crafting.gui.SpaceCraftingGUI;

public class SpaceCraftingCommand implements CommandExecutor {
    private final SP_CORE plugin;
    private final SpaceCraftingGUI gui;

    public SpaceCraftingCommand(SP_CORE plugin, SpaceCraftingGUI gui) {
        this.plugin = plugin;
        this.gui = gui;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Este comando só pode ser executado por jogadores!");
            return true;
        }

        Player player = (Player) sender;

        // Verificar permissão
        if (!player.hasPermission("sp_core.crafting")) {
            player.sendMessage(ChatColor.RED + "Você não tem permissão para usar este comando!");
            return true;
        }

        // Abrir o GUI
        gui.openMainGUI(player);
        return true;
    }
} 