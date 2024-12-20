package org.zerolegion.sp_core.crafting.gui;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.zerolegion.sp_core.SP_CORE;
import org.zerolegion.sp_core.crafting.SpaceCrafting;
import org.zerolegion.sp_core.crafting.SpaceRecipe;
import java.util.Map;
import java.util.Random;

public class SpaceCraftingGUIListener implements Listener {
    private final SP_CORE plugin;
    private final SpaceCraftingGUI gui;
    private final Random random = new Random();

    public SpaceCraftingGUIListener(SP_CORE plugin, SpaceCraftingGUI gui) {
        this.plugin = plugin;
        this.gui = gui;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        if (title.equals(ChatColor.DARK_PURPLE + "✧ Bancada Espacial ✧")) {
            handleMainGUIClick(event, player);
        } else if (title.equals(ChatColor.DARK_PURPLE + "✧ Receitas Espaciais ✧")) {
            handleRecipeBookClick(event, player);
        } else if (title.equals(ChatColor.DARK_PURPLE + "✧ Detalhes da Receita ✧")) {
            handleRecipeDetailsClick(event, player);
        }
    }

    private void handleMainGUIClick(InventoryClickEvent event, Player player) {
        int slot = event.getRawSlot();
        
        // Permitir colocar itens apenas nos slots de crafting
        int[] craftingSlots = {11, 12, 13, 20, 21, 22, 29, 30, 31};
        boolean isCraftingSlot = false;
        for (int craftSlot : craftingSlots) {
            if (slot == craftSlot) {
                isCraftingSlot = true;
                break;
            }
        }

        if (!isCraftingSlot && slot < 54) {
            event.setCancelled(true);

            // Botão de receitas
            if (slot == 15) {
                gui.openRecipeBookGUI(player);
            }
            // Botão de crafting
            else if (slot == 33) {
                attemptCrafting(player, event.getInventory());
            }
        }
    }

    private void handleRecipeBookClick(InventoryClickEvent event, Player player) {
        event.setCancelled(true);
        int slot = event.getRawSlot();

        // Botão voltar
        if (slot == 49) {
            gui.openMainGUI(player);
            return;
        }

        // Clique em uma receita
        ItemStack clicked = event.getCurrentItem();
        if (clicked != null && clicked.getType() != Material.AIR && clicked.getType() != Material.STAINED_GLASS_PANE) {
            for (SpaceRecipe recipe : gui.getRecipes().values()) {
                if (recipe.getResult().isSimilar(clicked)) {
                    gui.showRecipeDetails(player, recipe);
                    break;
                }
            }
        }
    }

    private void handleRecipeDetailsClick(InventoryClickEvent event, Player player) {
        event.setCancelled(true);
        int slot = event.getRawSlot();

        // Botão voltar
        if (slot == 49) {
            gui.openRecipeBookGUI(player);
        }
        // Botão usar receita
        else if (slot == 33) {
            SpaceRecipe recipe = gui.getSelectedRecipe(player);
            if (recipe != null) {
                gui.openMainGUI(player);
            }
        }
    }

    private void attemptCrafting(Player player, org.bukkit.inventory.Inventory inv) {
        Map<Integer, ItemStack> providedIngredients = gui.getProvidedIngredients(inv);
        if (providedIngredients.isEmpty()) {
            player.sendMessage(ChatColor.RED + "Coloque os ingredientes nos slots de crafting!");
            return;
        }

        SpaceRecipe selectedRecipe = gui.getSelectedRecipe(player);
        if (selectedRecipe == null) {
            player.sendMessage(ChatColor.RED + "Selecione uma receita primeiro!");
            return;
        }

        // Verificar nível
        int playerLevel = plugin.getLevelManager().getPlayerLevel(player.getUniqueId());
        if (playerLevel < selectedRecipe.getRequiredLevel()) {
            player.sendMessage(ChatColor.RED + "Você precisa ser nível " + selectedRecipe.getRequiredLevel() + "+ para criar este item!");
            return;
        }

        // Verificar ingredientes
        if (!selectedRecipe.matchesIngredients(providedIngredients)) {
            player.sendMessage(ChatColor.RED + "Ingredientes incorretos para esta receita!");
            return;
        }

        // Calcular chance de sucesso
        double successRate = selectedRecipe.calculateSuccessRate(playerLevel, providedIngredients);
        boolean success = random.nextDouble() < successRate;

        // Consumir ingredientes
        for (ItemStack ingredient : providedIngredients.values()) {
            ingredient.setAmount(ingredient.getAmount() - 1);
        }

        if (success) {
            // Criar item com qualidade aleatória
            ItemStack result = selectedRecipe.getResult().clone();
            SpaceCrafting.CraftingQuality quality = SpaceCrafting.CraftingQuality.getRandomQuality();
            
            // Adicionar o item ao inventário do jogador
            player.getInventory().addItem(result);
            
            // Efeitos de sucesso
            player.sendMessage(ChatColor.GREEN + "✧ Item criado com sucesso! ✧");
            player.sendMessage(ChatColor.GRAY + "Qualidade: " + quality.getDisplay());
        } else {
            // Efeitos de falha
            player.sendMessage(ChatColor.RED + "✖ Falha ao criar o item!");
        }

        // Atualizar a GUI
        gui.openMainGUI(player);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();
        String title = event.getView().getTitle();

        if (title.equals(ChatColor.DARK_PURPLE + "✧ Bancada Espacial ✧")) {
            // Retornar itens dos slots de crafting ao jogador
            Map<Integer, ItemStack> ingredients = gui.getProvidedIngredients(event.getInventory());
            for (ItemStack item : ingredients.values()) {
                player.getInventory().addItem(item);
            }
            
            // Limpar receita selecionada
            gui.removeSelectedRecipe(player);
        }
    }
} 