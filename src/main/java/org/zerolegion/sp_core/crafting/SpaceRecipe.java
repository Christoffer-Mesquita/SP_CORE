package org.zerolegion.sp_core.crafting;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class SpaceRecipe {
    private final String id;
    private final ItemStack result;
    private final Map<Integer, ItemStack> ingredients;
    private final int requiredLevel;
    private final double baseSuccessRate;
    private final List<String> description;

    public SpaceRecipe(String id, ItemStack result, Map<Integer, ItemStack> ingredients, int requiredLevel, double baseSuccessRate, List<String> description) {
        this.id = id;
        this.result = result;
        this.ingredients = ingredients;
        this.requiredLevel = requiredLevel;
        this.baseSuccessRate = baseSuccessRate;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public ItemStack getResult() {
        return result.clone();
    }

    public Map<Integer, ItemStack> getIngredients() {
        return new HashMap<>(ingredients);
    }

    public int getRequiredLevel() {
        return requiredLevel;
    }

    public double getBaseSuccessRate() {
        return baseSuccessRate;
    }

    public List<String> getDescription() {
        return new ArrayList<>(description);
    }

    public boolean matchesIngredients(Map<Integer, ItemStack> providedIngredients) {
        for (Map.Entry<Integer, ItemStack> entry : ingredients.entrySet()) {
            ItemStack required = entry.getValue();
            ItemStack provided = providedIngredients.get(entry.getKey());
            
            if (provided == null || provided.getType() != required.getType() || 
                provided.getAmount() < required.getAmount()) {
                return false;
            }

            // Verificar qualidade se for um item espacial
            if (SpaceCrafting.hasQuality(required)) {
                SpaceCrafting.CraftingQuality requiredQuality = SpaceCrafting.getItemQuality(required);
                SpaceCrafting.CraftingQuality providedQuality = SpaceCrafting.getItemQuality(provided);
                
                if (providedQuality == null || providedQuality.getLevel() < requiredQuality.getLevel()) {
                    return false;
                }
            }
        }
        return true;
    }

    public double calculateSuccessRate(int playerLevel, Map<Integer, ItemStack> providedIngredients) {
        double rate = baseSuccessRate;
        
        // Bônus por nível
        int levelDifference = playerLevel - requiredLevel;
        if (levelDifference > 0) {
            rate += Math.min(levelDifference * 0.02, 0.2); // Máximo de 20% de bônus por nível
        }
        
        // Bônus por qualidade dos ingredientes
        for (ItemStack ingredient : providedIngredients.values()) {
            if (SpaceCrafting.hasQuality(ingredient)) {
                SpaceCrafting.CraftingQuality quality = SpaceCrafting.getItemQuality(ingredient);
                rate += quality.getLevel() * 0.05; // 5% por nível de qualidade
            }
        }
        
        return Math.min(rate, 1.0); // Máximo de 100% de chance
    }

    public static class Builder {
        private String id;
        private ItemStack result;
        private Map<Integer, ItemStack> ingredients = new HashMap<>();
        private int requiredLevel = 1;
        private double baseSuccessRate = 0.7;
        private List<String> description = new ArrayList<>();

        public Builder(String id, ItemStack result) {
            this.id = id;
            this.result = result;
        }

        public Builder addIngredient(int slot, ItemStack ingredient) {
            ingredients.put(slot, ingredient);
            return this;
        }

        public Builder setRequiredLevel(int level) {
            this.requiredLevel = level;
            return this;
        }

        public Builder setBaseSuccessRate(double rate) {
            this.baseSuccessRate = rate;
            return this;
        }

        public Builder addDescription(String line) {
            description.add(line);
            return this;
        }

        public SpaceRecipe build() {
            return new SpaceRecipe(id, result, ingredients, requiredLevel, baseSuccessRate, description);
        }
    }
} 