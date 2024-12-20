package org.zerolegion.sp_core.classes;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClassSelector {
    private final ClassManager classManager;
    private final String inventoryTitle = "§8» §dEscolha sua Classe";

    public ClassSelector(ClassManager classManager) {
        this.classManager = classManager;
    }

    public void openSelector(Player player) {
        Inventory inv = Bukkit.createInventory(null, 45, inventoryTitle);

        // Decoração do inventário
        ItemStack borderItem = createItem(Material.STAINED_GLASS_PANE, 1, (byte) 0, "§r");
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, borderItem);
        }

        // Guerreiro Espacial
        inv.setItem(11, createClassItem(Material.DIAMOND_SWORD,
                ClassManager.PlayerClass.GUERREIRO_ESPACIAL,
                "§7O Guerreiro Espacial é um combatente",
                "§7especializado em combate corpo a corpo",
                "§7e resistência superior.",
                "",
                "§6✧ Bônus:",
                "§f• +20% de Dano Físico",
                "§f• +30% de Resistência",
                "§f• Habilidade especial: Fúria Estelar",
                "",
                "§eClique para selecionar!"));

        // Mago Cósmico
        inv.setItem(13, createClassItem(Material.BLAZE_ROD,
                ClassManager.PlayerClass.MAGO_COSMICO,
                "§7O Mago Cósmico manipula as energias",
                "§7do universo para causar destruição",
                "§7em área e controle de campo.",
                "",
                "§6✧ Bônus:",
                "§f• +30% de Dano Mágico",
                "§f• +20% de Regeneração de Mana",
                "§f• Habilidade especial: Nova Cósmica",
                "",
                "§eClique para selecionar!"));

        // Caçador Intergaláctico
        inv.setItem(15, createClassItem(Material.BOW,
                ClassManager.PlayerClass.CACADOR_INTERGALACTICO,
                "§7O Caçador Intergaláctico é um mestre",
                "§7em combate à distância e mobilidade",
                "§7superior em qualquer terreno.",
                "",
                "§6✧ Bônus:",
                "§f• +25% de Dano à Distância",
                "§f• +20% de Velocidade de Movimento",
                "§f• Habilidade especial: Tiro Orbital",
                "",
                "§eClique para selecionar!"));

        // Engenheiro Espacial
        inv.setItem(29, createClassItem(Material.REDSTONE,
                ClassManager.PlayerClass.ENGENHEIRO_ESPACIAL,
                "§7O Engenheiro Espacial é um gênio",
                "§7da tecnologia que utiliza gadgets",
                "§7e automações para vencer.",
                "",
                "§6✧ Bônus:",
                "§f• +30% de Eficiência em Crafting",
                "§f• +20% de Chance de Drop Extra",
                "§f• Habilidade especial: Drone de Suporte",
                "",
                "§eClique para selecionar!"));

        // Necromante Void
        inv.setItem(33, createClassItem(Material.SKULL_ITEM,
                ClassManager.PlayerClass.NECROMANTE_VOID,
                "§7O Necromante Void controla as forças",
                "§7sombrias do vácuo espacial para",
                "§7drenar a vida de seus inimigos.",
                "",
                "§6✧ Bônus:",
                "§f• +25% de Dano Sombrio",
                "§f• +20% de Roubo de Vida",
                "§f• Habilidade especial: Void Walker",
                "",
                "§eClique para selecionar!"));

        player.openInventory(inv);
    }

    private ItemStack createClassItem(Material material, ClassManager.PlayerClass playerClass, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        meta.setDisplayName(playerClass.getCor() + "✧ " + playerClass.getNome() + " ✧");
        meta.setLore(Arrays.asList(lore));
        
        // Adiciona efeito brilhante e esconde atributos
        meta.addEnchant(Enchantment.DURABILITY, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createItem(Material material, int amount, byte data, String name, String... lore) {
        ItemStack item = new ItemStack(material, amount, data);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        if (lore.length > 0) {
            meta.setLore(Arrays.asList(lore));
        }
        item.setItemMeta(meta);
        return item;
    }

    public boolean handleClick(Player player, Inventory inventory, int slot) {
        if (!inventory.getTitle().equals(inventoryTitle)) {
            return false;
        }

        ClassManager.PlayerClass selectedClass = null;

        switch (slot) {
            case 11:
                selectedClass = ClassManager.PlayerClass.GUERREIRO_ESPACIAL;
                break;
            case 13:
                selectedClass = ClassManager.PlayerClass.MAGO_COSMICO;
                break;
            case 15:
                selectedClass = ClassManager.PlayerClass.CACADOR_INTERGALACTICO;
                break;
            case 29:
                selectedClass = ClassManager.PlayerClass.ENGENHEIRO_ESPACIAL;
                break;
            case 33:
                selectedClass = ClassManager.PlayerClass.NECROMANTE_VOID;
                break;
        }

        if (selectedClass != null) {
            classManager.setPlayerClass(player, selectedClass);
            player.closeInventory();
            
            // Efeitos de seleção
            player.sendTitle(
                selectedClass.getCor() + "✧ " + selectedClass.getNome() + " ✧",
                "§fClasse selecionada com sucesso!"
            );
            
            return true;
        }

        return false;
    }
} 