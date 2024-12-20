package org.zerolegion.sp_core.clans.gui;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.zerolegion.sp_core.SP_CORE;
import org.zerolegion.sp_core.clans.ClanManager;
import org.zerolegion.sp_core.clans.SpaceClan;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClanCreateGUIListener implements Listener {
    private final SP_CORE plugin;
    private final ClanManager clanManager;
    private final ClanCreateGUI createGUI;
    private final ClanMainGUI mainGUI;
    private final Map<UUID, CreationState> playerStates;
    private final Map<UUID, String> clanNames;
    private final Map<UUID, String> clanTags;

    private enum CreationState {
        WAITING_NAME,
        WAITING_TAG,
        WAITING_DESCRIPTION
    }

    public ClanCreateGUIListener(SP_CORE plugin, ClanManager clanManager, ClanCreateGUI createGUI, ClanMainGUI mainGUI) {
        this.plugin = plugin;
        this.clanManager = clanManager;
        this.createGUI = createGUI;
        this.mainGUI = mainGUI;
        this.playerStates = new HashMap<>();
        this.clanNames = new HashMap<>();
        this.clanTags = new HashMap<>();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!event.getView().getTitle().equals(ChatColor.DARK_PURPLE + "✧ Criar Clã ✧")) return;

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null) return;

        switch (clicked.getType().name()) {
            case "NAME_TAG":
                startNameInput(player);
                break;
            case "PAPER":
                startTagInput(player);
                break;
            case "BOOK_AND_QUILL":
                startDescriptionInput(player);
                break;
            case "ARROW":
                mainGUI.openGUI(player);
                break;
        }
    }

    private void startNameInput(Player player) {
        player.closeInventory();
        playerStates.put(player.getUniqueId(), CreationState.WAITING_NAME);
        player.sendMessage("");
        player.sendMessage(ChatColor.LIGHT_PURPLE + "✧ " + ChatColor.WHITE + "Digite o nome do clã no chat");
        player.sendMessage(ChatColor.GRAY + "Digite 'cancelar' para cancelar");
        player.sendMessage("");
    }

    private void startTagInput(Player player) {
        if (!clanNames.containsKey(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Por favor, defina o nome do clã primeiro!");
            createGUI.openGUI(player);
            return;
        }

        player.closeInventory();
        playerStates.put(player.getUniqueId(), CreationState.WAITING_TAG);
        player.sendMessage("");
        player.sendMessage(ChatColor.LIGHT_PURPLE + "✧ " + ChatColor.WHITE + "Digite a tag do clã no chat (3-4 caracteres)");
        player.sendMessage(ChatColor.GRAY + "Digite 'cancelar' para cancelar");
        player.sendMessage("");
    }

    private void startDescriptionInput(Player player) {
        player.closeInventory();
        playerStates.put(player.getUniqueId(), CreationState.WAITING_DESCRIPTION);
        player.sendMessage("");
        player.sendMessage(ChatColor.LIGHT_PURPLE + "✧ " + ChatColor.WHITE + "Digite a descrição do clã no chat");
        player.sendMessage(ChatColor.GRAY + "Digite 'cancelar' para cancelar");
        player.sendMessage("");
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        CreationState state = playerStates.get(player.getUniqueId());
        if (state == null) return;

        event.setCancelled(true);
        String message = event.getMessage();

        if (message.equalsIgnoreCase("cancelar")) {
            playerStates.remove(player.getUniqueId());
            clanNames.remove(player.getUniqueId());
            clanTags.remove(player.getUniqueId());
            createGUI.openGUI(player);
            return;
        }

        switch (state) {
            case WAITING_NAME:
                handleNameInput(player, message);
                break;
            case WAITING_TAG:
                handleTagInput(player, message);
                break;
            case WAITING_DESCRIPTION:
                handleDescriptionInput(player, message);
                break;
        }

        playerStates.remove(player.getUniqueId());
        createGUI.openGUI(player);
    }

    private void handleNameInput(Player player, String name) {
        if (name.length() < 3 || name.length() > 16) {
            player.sendMessage(ChatColor.RED + "O nome do clã deve ter entre 3 e 16 caracteres!");
            return;
        }

        // Verificar se o nome já existe
        for (SpaceClan clan : clanManager.getAllClans()) {
            if (clan.getName().equalsIgnoreCase(name)) {
                player.sendMessage(ChatColor.RED + "Este nome de clã já está em uso!");
                return;
            }
        }

        clanNames.put(player.getUniqueId(), name);
        player.sendMessage(ChatColor.GREEN + "Nome do clã definido! Agora defina a tag.");
    }

    private void handleTagInput(Player player, String tag) {
        if (tag.length() < 3 || tag.length() > 4) {
            player.sendMessage(ChatColor.RED + "A tag do clã deve ter entre 3 e 4 caracteres!");
            return;
        }

        // Verificar se a tag já existe
        for (SpaceClan clan : clanManager.getAllClans()) {
            if (clan.getTag().equalsIgnoreCase(tag)) {
                player.sendMessage(ChatColor.RED + "Esta tag já está em uso!");
                return;
            }
        }

        // Verificar nível mínimo
        if (plugin.getLevelManager().getPlayerLevel(player.getUniqueId()) < createGUI.getMinLevel()) {
            player.sendMessage(ChatColor.RED + "Você precisa ser nível " + createGUI.getMinLevel() + "+ para criar um clã!");
            return;
        }

        // Verificar créditos
        if (plugin.getStellarEconomyManager().getBalance(player.getUniqueId()) < createGUI.getCreationCost()) {
            player.sendMessage(ChatColor.RED + "Você precisa de " + createGUI.getCreationCost() + " créditos para criar um clã!");
            return;
        }

        // Verificar se já está em um clã
        if (clanManager.getPlayerClan(player.getUniqueId()) != null) {
            player.sendMessage(ChatColor.RED + "Você já está em um clã!");
            return;
        }

        clanTags.put(player.getUniqueId(), tag);
        
        // Criar o clã com o nome e tag armazenados
        String name = clanNames.get(player.getUniqueId());
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            player.performCommand("clan criar " + name + " " + tag);
            // Limpar os dados armazenados
            clanNames.remove(player.getUniqueId());
            clanTags.remove(player.getUniqueId());
            // Abrir o menu principal do clã
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                mainGUI.openGUI(player);
            }, 2L); // Delay de 2 ticks para garantir que o clã foi criado
        });

        player.sendMessage(ChatColor.GREEN + "Clã criado com sucesso!");
    }

    private void handleDescriptionInput(Player player, String description) {
        if (description.length() > 100) {
            player.sendMessage(ChatColor.RED + "A descrição deve ter no máximo 100 caracteres!");
            return;
        }

        SpaceClan clan = clanManager.getPlayerClan(player.getUniqueId());
        if (clan != null) {
            clan.setDescription(description);
            clanManager.saveClan(clan);
            player.sendMessage(ChatColor.GREEN + "Descrição do clã atualizada!");
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        if (!event.getView().getTitle().equals(ChatColor.DARK_PURPLE + "✧ Criar Clã ✧")) return;

        Player player = (Player) event.getPlayer();
        if (!playerStates.containsKey(player.getUniqueId())) {
            playerStates.remove(player.getUniqueId());
        }
    }
} 