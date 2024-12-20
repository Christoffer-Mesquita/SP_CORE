package org.zerolegion.sp_core.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import org.zerolegion.sp_core.SP_CORE;
import org.zerolegion.sp_core.crafting.gui.SpaceCraftingGUI;

public class SpaceCraftCommand implements CommandExecutor {
    private final SP_CORE plugin;
    private final SpaceCraftingGUI craftingGUI;

    public SpaceCraftCommand(SP_CORE plugin, SpaceCraftingGUI craftingGUI) {
        this.plugin = plugin;
        this.craftingGUI = craftingGUI;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Este comando só pode ser usado por jogadores!");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("sensitive.craft")) {
            player.sendMessage(ChatColor.RED + "Você não tem permissão para usar este comando!");
            return true;
        }

        craftingGUI.openMainGUI(player);
        return true;
    }
} 