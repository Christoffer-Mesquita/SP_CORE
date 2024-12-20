package org.zerolegion.sp_core.clans.gui;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.zerolegion.sp_core.SP_CORE;
import org.zerolegion.sp_core.clans.ClanManager;
import org.zerolegion.sp_core.clans.SpaceClan;

public class ClanEventsGUIListener implements Listener {
    private final SP_CORE plugin;
    private final ClanManager clanManager;
    private final ClanEventsGUI eventsGUI;
    private final ClanMainGUI mainGUI;

    public ClanEventsGUIListener(SP_CORE plugin, ClanManager clanManager, ClanEventsGUI eventsGUI, ClanMainGUI mainGUI) {
        this.plugin = plugin;
        this.clanManager = clanManager;
        this.eventsGUI = eventsGUI;
        this.mainGUI = mainGUI;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!event.getView().getTitle().equals(ChatColor.DARK_PURPLE + "✧ Eventos do Clã ✧")) return;

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null) return;

        SpaceClan clan = clanManager.getPlayerClan(player.getUniqueId());
        if (clan == null) return;

        if (clicked.getType().name().equals("ARROW")) {
            mainGUI.openGUI(player);
            return;
        }

        // Aqui você pode adicionar a lógica para participar de eventos
        // Por exemplo, verificar o tipo de evento clicado e registrar a participação do clã
        player.sendMessage(ChatColor.YELLOW + "Sistema de eventos em desenvolvimento!");
    }
} 