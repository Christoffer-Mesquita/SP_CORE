package org.zerolegion.sp_core.clans.gui;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.zerolegion.sp_core.clans.ClanManager;
import org.zerolegion.sp_core.clans.SpaceClan;
import org.bukkit.Bukkit;

public class ClanInviteGUIListener implements Listener {
    private final ClanManager clanManager;
    private final ClanMembersGUI membersGUI;

    public ClanInviteGUIListener(ClanManager clanManager, ClanMembersGUI membersGUI) {
        this.clanManager = clanManager;
        this.membersGUI = membersGUI;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!event.getView().getTitle().equals(ChatColor.DARK_PURPLE + "✧ Convidar Jogadores ✧")) return;

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null) return;

        SpaceClan clan = clanManager.getPlayerClan(player.getUniqueId());
        if (clan == null) return;

        // Botão voltar
        if (clicked.getType().name().equals("ARROW")) {
            membersGUI.openGUI(player);
            return;
        }

        // Convidar jogador
        if (clicked.getType().name().equals("SKULL_ITEM") && clicked.getDurability() == 3) {
            SkullMeta meta = (SkullMeta) clicked.getItemMeta();
            if (meta == null || meta.getOwner() == null) return;

            Player target = Bukkit.getPlayer(meta.getOwner());
            if (target == null) {
                player.sendMessage(ChatColor.RED + "Jogador não está mais online!");
                return;
            }

            if (clanManager.getPlayerClan(target.getUniqueId()) != null) {
                player.sendMessage(ChatColor.RED + "Este jogador já está em um clã!");
                return;
            }

            // Enviar convite
            player.sendMessage(ChatColor.GREEN + "Convite enviado para " + target.getName() + "!");
            target.sendMessage("");
            target.sendMessage(ChatColor.LIGHT_PURPLE + "✧ " + ChatColor.WHITE + "Você recebeu um convite para entrar no clã " + 
                             ChatColor.LIGHT_PURPLE + clan.getName() + ChatColor.WHITE + "!");
            target.sendMessage(ChatColor.GRAY + "Use " + ChatColor.YELLOW + "/clan aceitar " + clan.getName() + 
                             ChatColor.GRAY + " para aceitar o convite.");
            target.sendMessage("");

            player.closeInventory();
        }
    }
} 