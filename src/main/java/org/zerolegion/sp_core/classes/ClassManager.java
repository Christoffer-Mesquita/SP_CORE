package org.zerolegion.sp_core.classes;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;
import org.bukkit.entity.Player;
import org.zerolegion.sp_core.SP_CORE;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClassManager {
    private final SP_CORE plugin;
    private final MongoCollection<Document> classCollection;
    private final Map<UUID, PlayerClass> playerClasses;
    private final ClassSelector classSelector;

    public enum PlayerClass {
        GUERREIRO_ESPACIAL("Guerreiro Espacial", "§c"),
        MAGO_COSMICO("Mago Cósmico", "§b"),
        CACADOR_INTERGALACTICO("Caçador Intergaláctico", "§a"),
        ENGENHEIRO_ESPACIAL("Engenheiro Espacial", "§e"),
        NECROMANTE_VOID("Necromante Void", "§5");

        private final String nome;
        private final String cor;

        PlayerClass(String nome, String cor) {
            this.nome = nome;
            this.cor = cor;
        }

        public String getNome() {
            return nome;
        }

        public String getCor() {
            return cor;
        }
    }

    public ClassManager(SP_CORE plugin) {
        this.plugin = plugin;
        this.classCollection = plugin.getDatabase().getCollection("player_classes");
        this.playerClasses = new HashMap<>();
        this.classSelector = new ClassSelector(this);
    }

    public void loadPlayerClass(Player player) {
        Document doc = classCollection.find(Filters.eq("uuid", player.getUniqueId().toString())).first();
        
        if (doc == null) {
            // Jogador não tem classe, mostrar GUI de seleção
            classSelector.openSelector(player);
        } else {
            // Carregar classe do jogador
            PlayerClass playerClass = PlayerClass.valueOf(doc.getString("class"));
            playerClasses.put(player.getUniqueId(), playerClass);
        }
    }

    public void setPlayerClass(Player player, PlayerClass playerClass) {
        // Salvar no cache
        playerClasses.put(player.getUniqueId(), playerClass);

        // Salvar no MongoDB
        Document doc = new Document("uuid", player.getUniqueId().toString())
                .append("name", player.getName())
                .append("class", playerClass.name());

        classCollection.replaceOne(
                Filters.eq("uuid", player.getUniqueId().toString()),
                doc,
                new ReplaceOptions().upsert(true)
        );
    }

    public PlayerClass getPlayerClass(Player player) {
        return playerClasses.get(player.getUniqueId());
    }

    public boolean hasClass(Player player) {
        return playerClasses.containsKey(player.getUniqueId());
    }

    public SP_CORE getPlugin() {
        return plugin;
    }

    public ClassSelector getClassSelector() {
        return classSelector;
    }
} 