package org.zerolegion.sp_core.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.zerolegion.sp_core.SP_CORE;
import org.bukkit.ChatColor;

public class GlobalChatCommand implements CommandExecutor {
    private final SP_CORE plugin;

    public GlobalChatCommand(SP_CORE plugin) {
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
            // Toggle do chat global
            plugin.getChatManager().toggleGlobalChat(player);
            return true;
        }

        // Se tiver argumentos, envia mensagem direta no global
        String message = String.join(" ", args);
        plugin.getChatManager().sendGlobalMessage(player, message);
        return true;
    }
} 