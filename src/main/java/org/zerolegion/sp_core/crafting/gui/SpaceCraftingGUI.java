package org.zerolegion.sp_core.crafting.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.zerolegion.sp_core.crafting.SpaceCrafting;
import org.zerolegion.sp_core.crafting.SpaceRecipe;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpaceCraftingGUI {
    private final int size = 54; // 6 linhas
    private final Map<String, SpaceRecipe> recipes;
    private final Map<Player, SpaceRecipe> selectedRecipes;
    private final String guiTitle = ChatColor.DARK_PURPLE + "✧ Bancada Espacial ✧";

    public SpaceCraftingGUI() {
        this.recipes = new HashMap<>();
        this.selectedRecipes = new HashMap<>();
    }

    public void registerRecipe(SpaceRecipe recipe) {
        recipes.put(recipe.getId(), recipe);
    }

    public Map<String, SpaceRecipe> getRecipes() {
        return recipes;
    }

    public void openMainGUI(Player player) {
        Inventory inv = Bukkit.createInventory(null, size, guiTitle);

        // Fundo preto
        for (int i = 0; i < size; i++) {
            ItemStack background = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 15);
            ItemMeta meta = background.getItemMeta();
            meta.setDisplayName(" ");
            background.setItemMeta(meta);
            inv.setItem(i, background);
        }

        // Borda superior e inferior com vidro roxo
        ItemStack purpleGlass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 2);
        ItemMeta purpleMeta = purpleGlass.getItemMeta();
        purpleMeta.setDisplayName(" ");
        purpleGlass.setItemMeta(purpleMeta);

        // Superior
        for (int i = 0; i < 9; i++) {
            inv.setItem(i, purpleGlass);
        }
        // Inferior
        for (int i = 45; i < 54; i++) {
            inv.setItem(i, purpleGlass);
        }

        // Slots de crafting (3x3)
        ItemStack craftSlot = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 0);
        ItemMeta craftMeta = craftSlot.getItemMeta();
        craftMeta.setDisplayName(ChatColor.GRAY + "Slot de Crafting");
        List<String> craftLore = new ArrayList<>();
        craftLore.add(ChatColor.GRAY + "Coloque os ingredientes aqui");
        craftMeta.setLore(craftLore);
        craftSlot.setItemMeta(craftMeta);

        int[] craftingSlots = {11, 12, 13, 20, 21, 22, 29, 30, 31};
        for (int slot : craftingSlots) {
            inv.setItem(slot, craftSlot);
        }

        // Resultado
        ItemStack resultSlot = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 5);
        ItemMeta resultMeta = resultSlot.getItemMeta();
        resultMeta.setDisplayName(ChatColor.GREEN + "Resultado");
        List<String> resultLore = new ArrayList<>();
        resultLore.add(ChatColor.GRAY + "O item criado aparecerá aqui");
        resultMeta.setLore(resultLore);
        resultSlot.setItemMeta(resultMeta);
        inv.setItem(24, resultSlot);

        // Botão de crafting
        ItemStack craftButton = new ItemStack(Material.ANVIL);
        ItemMeta buttonMeta = craftButton.getItemMeta();
        buttonMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "✧ " + ChatColor.WHITE + "Criar Item" + ChatColor.LIGHT_PURPLE + " ✧");
        List<String> buttonLore = new ArrayList<>();
        buttonLore.add("");
        buttonLore.add(ChatColor.GRAY + "Clique para tentar criar o item");
        buttonLore.add(ChatColor.GRAY + "com os ingredientes fornecidos");
        buttonLore.add("");
        buttonLore.add(ChatColor.YELLOW + "Taxa de Sucesso: " + ChatColor.WHITE + "???");
        buttonMeta.setLore(buttonLore);
        craftButton.setItemMeta(buttonMeta);
        inv.setItem(33, craftButton);

        // Botão de receitas
        ItemStack recipeBook = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta bookMeta = recipeBook.getItemMeta();
        bookMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "✧ " + ChatColor.WHITE + "Receitas" + ChatColor.LIGHT_PURPLE + " ✧");
        List<String> bookLore = new ArrayList<>();
        bookLore.add("");
        bookLore.add(ChatColor.GRAY + "Clique para ver todas as");
        bookLore.add(ChatColor.GRAY + "receitas disponíveis");
        bookMeta.setLore(bookLore);
        recipeBook.setItemMeta(bookMeta);
        inv.setItem(15, recipeBook);

        // Decoração com vidros azuis
        ItemStack blueGlass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 3);
        ItemMeta blueMeta = blueGlass.getItemMeta();
        blueMeta.setDisplayName(" ");
        blueGlass.setItemMeta(blueMeta);

        int[] decorSlots = {1, 7, 9, 17, 36, 44, 46, 52};
        for (int slot : decorSlots) {
            inv.setItem(slot, blueGlass);
        }

        player.openInventory(inv);
    }

    public void openRecipeBookGUI(Player player) {
        Inventory inv = Bukkit.createInventory(null, size, ChatColor.DARK_PURPLE + "✧ Receitas Espaciais ✧");

        // Fundo preto e decoração similar ao menu principal
        // ... (código similar ao openMainGUI para decoração)

        // Listar todas as receitas disponíveis
        int slot = 10;
        for (SpaceRecipe recipe : recipes.values()) {
            ItemStack icon = recipe.getResult().clone();
            ItemMeta meta = icon.getItemMeta();
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.addAll(recipe.getDescription());
            lore.add("");
            lore.add(ChatColor.GRAY + "Nível Requerido: " + ChatColor.YELLOW + recipe.getRequiredLevel());
            lore.add(ChatColor.GRAY + "Taxa Base de Sucesso: " + ChatColor.YELLOW + (recipe.getBaseSuccessRate() * 100) + "%");
            lore.add("");
            lore.add(ChatColor.YELLOW + "Clique para ver a receita!");
            meta.setLore(lore);
            icon.setItemMeta(meta);
            
            inv.setItem(slot++, icon);
            
            // Organizar em 4 linhas de 7 slots
            if ((slot - 9) % 9 == 8) slot += 2;
            if (slot > 43) break;
        }

        // Botão voltar
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName(ChatColor.RED + "Voltar");
        back.setItemMeta(backMeta);
        inv.setItem(49, back);

        player.openInventory(inv);
    }

    public void showRecipeDetails(Player player, SpaceRecipe recipe) {
        Inventory inv = Bukkit.createInventory(null, size, ChatColor.DARK_PURPLE + "✧ Detalhes da Receita ✧");

        // Fundo e decoração
        // ... (código similar para decoração)

        // Mostrar resultado
        inv.setItem(24, recipe.getResult());

        // Mostrar ingredientes nas posições corretas
        for (Map.Entry<Integer, ItemStack> entry : recipe.getIngredients().entrySet()) {
            inv.setItem(entry.getKey(), entry.getValue());
        }

        // Informações adicionais
        ItemStack info = new ItemStack(Material.PAPER);
        ItemMeta infoMeta = info.getItemMeta();
        infoMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "✧ " + ChatColor.WHITE + "Informações" + ChatColor.LIGHT_PURPLE + " ✧");
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.addAll(recipe.getDescription());
        lore.add("");
        lore.add(ChatColor.GRAY + "Nível Requerido: " + ChatColor.YELLOW + recipe.getRequiredLevel());
        lore.add(ChatColor.GRAY + "Taxa Base de Sucesso: " + ChatColor.YELLOW + (recipe.getBaseSuccessRate() * 100) + "%");
        infoMeta.setLore(lore);
        info.setItemMeta(infoMeta);
        inv.setItem(15, info);

        // Botão para usar esta receita
        ItemStack use = new ItemStack(Material.WORKBENCH);
        ItemMeta useMeta = use.getItemMeta();
        useMeta.setDisplayName(ChatColor.GREEN + "Usar Esta Receita");
        List<String> useLore = new ArrayList<>();
        useLore.add(ChatColor.GRAY + "Clique para usar esta receita");
        useLore.add(ChatColor.GRAY + "na bancada de trabalho");
        useMeta.setLore(useLore);
        use.setItemMeta(useMeta);
        inv.setItem(33, use);

        // Botão voltar
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName(ChatColor.RED + "Voltar para Receitas");
        back.setItemMeta(backMeta);
        inv.setItem(49, back);

        player.openInventory(inv);
    }

    public Map<Integer, ItemStack> getProvidedIngredients(Inventory inv) {
        Map<Integer, ItemStack> ingredients = new HashMap<>();
        int[] slots = {11, 12, 13, 20, 21, 22, 29, 30, 31};
        
        for (int slot : slots) {
            ItemStack item = inv.getItem(slot);
            if (item != null && item.getType() != Material.STAINED_GLASS_PANE) {
                ingredients.put(slot, item.clone());
            }
        }
        
        return ingredients;
    }

    public void setSelectedRecipe(Player player, SpaceRecipe recipe) {
        selectedRecipes.put(player, recipe);
    }

    public SpaceRecipe getSelectedRecipe(Player player) {
        return selectedRecipes.get(player);
    }

    public void removeSelectedRecipe(Player player) {
        selectedRecipes.remove(player);
    }
} 