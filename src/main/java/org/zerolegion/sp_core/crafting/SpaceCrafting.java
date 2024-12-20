package org.zerolegion.sp_core.crafting;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SpaceCrafting {
    private static final Random random = new Random();

    public enum CraftingQuality {
        COMUM(ChatColor.GRAY + "✦ Comum", 0),
        RARO(ChatColor.BLUE + "✧ Raro", 1),
        ÉPICO(ChatColor.DARK_PURPLE + "✯ Épico", 2),
        LENDÁRIO(ChatColor.GOLD + "✰ Lendário", 3);

        private final String display;
        private final int level;

        CraftingQuality(String display, int level) {
            this.display = display;
            this.level = level;
        }

        public String getDisplay() {
            return display;
        }

        public int getLevel() {
            return level;
        }

        public static CraftingQuality getRandomQuality() {
            double chance = random.nextDouble();
            if (chance < 0.05) return LENDÁRIO;      // 5%
            if (chance < 0.15) return ÉPICO;         // 10%
            if (chance < 0.35) return RARO;          // 20%
            return COMUM;                            // 65%
        }
    }

    public static ItemStack createSpaceItem(Material material, String name, List<String> baseLore, CraftingQuality quality) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        // Nome com qualidade
        meta.setDisplayName(quality.getDisplay() + " " + ChatColor.WHITE + name);
        
        // Lore com estatísticas baseadas na qualidade
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.addAll(baseLore);
        lore.add("");
        lore.add(quality.getDisplay());
        
        // Adicionar bônus baseado na qualidade
        if (quality.getLevel() > 0) {
            lore.add(ChatColor.GRAY + "Bônus de Qualidade:");
            for (int i = 0; i < quality.getLevel(); i++) {
                lore.add(ChatColor.GRAY + "• " + getRandomBonus());
            }
        }
        
        meta.setLore(lore);
        
        // Adicionar brilho para itens não comuns
        if (quality != CraftingQuality.COMUM) {
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
        }
        
        item.setItemMeta(meta);
        return item;
    }

    private static String getRandomBonus() {
        String[] bonuses = {
            "+5% de Velocidade de Mineração",
            "+3% de Chance de Drop Duplo",
            "+2% de XP Extra",
            "+4% de Resistência no Espaço",
            "+3% de Economia de Oxigênio",
            "+5% de Velocidade de Movimento",
            "+4% de Proteção Contra Radiação",
            "+3% de Eficiência Energética"
        };
        return bonuses[random.nextInt(bonuses.length)];
    }

    public static boolean hasQuality(ItemStack item) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore()) return false;
        List<String> lore = item.getItemMeta().getLore();
        for (CraftingQuality quality : CraftingQuality.values()) {
            if (lore.contains(quality.getDisplay())) return true;
        }
        return false;
    }

    public static CraftingQuality getItemQuality(ItemStack item) {
        if (!hasQuality(item)) return null;
        List<String> lore = item.getItemMeta().getLore();
        for (CraftingQuality quality : CraftingQuality.values()) {
            if (lore.contains(quality.getDisplay())) return quality;
        }
        return null;
    }
} 