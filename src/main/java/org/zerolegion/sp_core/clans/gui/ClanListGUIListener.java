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

public class ClanListGUIListener implements Listener {
    private final SP_CORE plugin;
    private final ClanManager clanManager;
    private final ClanListGUI listGUI;
    private final ClanCreateGUI createGUI;
    private final ClanMainGUI mainGUI;

    public ClanListGUIListener(SP_CORE plugin, ClanManager clanManager, ClanListGUI listGUI, ClanCreateGUI createGUI, ClanMainGUI mainGUI) {
        this.plugin = plugin;
        this.clanManager = clanManager;
        this.listGUI = listGUI;
        this.createGUI = createGUI;
        this.mainGUI = mainGUI;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!event.getView().getTitle().equals(ChatColor.DARK_PURPLE + "✧ Lista de Clãs ✧")) return;

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null) return;

        switch (clicked.getType().name()) {
            case "NETHER_STAR":
                createGUI.openGUI(player);
                break;
            case "SKULL_ITEM":
                if (clicked.getDurability() == 3) {
                    handleClanClick(player, clicked);
                }
                break;
            case "ARROW":
                mainGUI.openGUI(player);
                break;
        }
    }

    private void handleClanClick(Player player, ItemStack clicked) {
        String clanName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
        SpaceClan clan = null;

        for (SpaceClan c : clanManager.getAllClans()) {
            if (c.getName().equals(clanName)) {
                clan = c;
                break;
            }
        }

        if (clan == null) return;

        player.performCommand("clan info " + clan.getName());
    }
} 