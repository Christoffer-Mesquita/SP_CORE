package org.zerolegion.sp_core.ships;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.zerolegion.sp_core.SP_CORE;
import org.zerolegion.sp_core.economy.StellarEconomyManager;
import org.zerolegion.sp_core.ships.effects.ShipEffectManager;
import org.zerolegion.sp_core.ships.gui.FuelShopGUI;
import org.zerolegion.sp_core.ships.gui.FuelShopListener;
import org.zerolegion.sp_core.ships.gui.ShipShopGUI;
import org.zerolegion.sp_core.ships.gui.ShipShopListener;
import org.zerolegion.sp_core.ships.listeners.ShipEffectListener;

import java.io.File;
import java.util.*;

public class SpaceshipManager {
    private final SP_CORE plugin;
    private final MongoCollection<Document> shipsCollection;
    private final StellarEconomyManager economyManager;
    private final Map<String, ShipTemplate> shipTemplates;
    private final Map<UUID, PlayerHangar> playerHangars;
    private final Map<UUID, PlayerShip> activeShips;
    private final ShipEffectManager shipEffectManager;
    private FileConfiguration shipsConfig;
    private ShipShopGUI shopGUI;
    private FuelShopGUI fuelShopGUI;

    public SpaceshipManager(SP_CORE plugin, StellarEconomyManager economyManager) {
        this.plugin = plugin;
        this.economyManager = economyManager;
        this.shipsCollection = plugin.getDatabase().getCollection("player_ships");
        this.shipTemplates = new HashMap<>();
        this.playerHangars = new HashMap<>();
        this.activeShips = new HashMap<>();
        this.shipEffectManager = new ShipEffectManager(plugin);
        
        loadConfiguration();
        loadShipTemplates();
        
        // Inicializar GUIs
        this.shopGUI = new ShipShopGUI(this);
        this.fuelShopGUI = new FuelShopGUI(this);
        loadAllHangars();
    }

    private void loadAllHangars() {
        plugin.getLogger().info("[DEBUG] Carregando hangares de todos os jogadores online...");
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            loadPlayerHangar(player.getUniqueId());
            plugin.getLogger().info("[DEBUG] Hangar carregado para " + player.getName());
        }
    }

    private void loadConfiguration() {
        File configFile = new File(plugin.getDataFolder(), "ships.yml");
        if (!configFile.exists()) {
            plugin.saveResource("ships.yml", false);
        }
        shipsConfig = YamlConfiguration.loadConfiguration(configFile);
    }

    private void loadShipTemplates() {
        ConfigurationSection shipsSection = shipsConfig.getConfigurationSection("ships");
        if (shipsSection == null) return;

        for (String shipId : shipsSection.getKeys(false)) {
            ConfigurationSection shipSection = shipsSection.getConfigurationSection(shipId);
            if (shipSection == null) continue;

            ShipTemplate template = new ShipTemplate(
                shipId,
                shipSection.getString("name"),
                ShipType.valueOf(shipSection.getString("type")),
                shipSection.getBoolean("purchasable"),
                shipSection.getDouble("price"),
                loadShipStats(shipSection.getConfigurationSection("stats")),
                shipSection.getStringList("description")
            );

            shipTemplates.put(shipId, template);
        }
    }

    private Map<String, Double> loadShipStats(ConfigurationSection statsSection) {
        Map<String, Double> stats = new HashMap<>();
        if (statsSection == null) return stats;

        for (String statKey : statsSection.getKeys(false)) {
            if (statsSection.isDouble(statKey) || statsSection.isInt(statKey)) {
                stats.put(statKey, statsSection.getDouble(statKey));
            }
        }

        return stats;
    }

    public void loadPlayerHangar(UUID playerId) {
        plugin.getLogger().info("[DEBUG] Carregando hangar do jogador " + playerId);
        Document doc = shipsCollection.find(Filters.eq("uuid", playerId.toString())).first();
        
        if (doc == null) {
            // Criar novo hangar para o jogador
            plugin.getLogger().info("[DEBUG] Criando novo hangar para o jogador " + playerId);
            PlayerHangar hangar = new PlayerHangar(playerId, 1);
            playerHangars.put(playerId, hangar);
            savePlayerHangar(playerId);
        } else {
            // Carregar hangar existente
            plugin.getLogger().info("[DEBUG] Carregando hangar existente para o jogador " + playerId);
            int hangarLevel = doc.getInteger("hangar_level", 1);
            PlayerHangar hangar = new PlayerHangar(playerId, hangarLevel);
            
            // Carregar naves do jogador
            List<Document> shipDocs = (List<Document>) doc.get("ships");
            if (shipDocs != null) {
                for (Document shipDoc : shipDocs) {
                    PlayerShip ship = new PlayerShip(
                        shipDoc.getString("template_id"),
                        shipDoc.getString("name"),
                        shipDoc.getDouble("fuel"),
                        loadShipUpgrades(shipDoc)
                    );
                    hangar.addShip(ship);
                    plugin.getLogger().info("[DEBUG] Carregada nave " + ship.getName() + " para o jogador " + playerId);
                }
            }
            
            playerHangars.put(playerId, hangar);
        }
    }

    private Map<String, Integer> loadShipUpgrades(Document shipDoc) {
        Map<String, Integer> upgrades = new HashMap<>();
        Document upgradesDoc = (Document) shipDoc.get("upgrades");
        if (upgradesDoc != null) {
            for (String key : upgradesDoc.keySet()) {
                upgrades.put(key, upgradesDoc.getInteger(key));
            }
        }
        return upgrades;
    }

    public void savePlayerHangar(UUID playerId) {
        plugin.getLogger().info("[DEBUG] Salvando hangar do jogador " + playerId);
        PlayerHangar hangar = playerHangars.get(playerId);
        if (hangar == null) return;

        List<Document> shipDocs = new ArrayList<>();
        for (PlayerShip ship : hangar.getShips()) {
            Document shipDoc = new Document()
                .append("template_id", ship.getTemplateId())
                .append("name", ship.getName())
                .append("fuel", ship.getFuel())
                .append("upgrades", new Document(ship.getUpgrades()));
            shipDocs.add(shipDoc);
            plugin.getLogger().info("[DEBUG] Salvando nave " + ship.getName() + " do jogador " + playerId);
        }

        Document hangarDoc = new Document()
            .append("uuid", playerId.toString())
            .append("hangar_level", hangar.getLevel())
            .append("ships", shipDocs);

        try {
            shipsCollection.updateOne(
                Filters.eq("uuid", playerId.toString()),
                new Document("$set", hangarDoc),
                new UpdateOptions().upsert(true)
            );
            plugin.getLogger().info("[DEBUG] Hangar salvo com sucesso para o jogador " + playerId);
        } catch (Exception e) {
            plugin.getLogger().severe("[ERROR] Erro ao salvar hangar do jogador " + playerId + ": " + e.getMessage());
        }
    }

    public boolean purchaseShip(Player player, String shipId) {
        plugin.getLogger().info("[DEBUG] Iniciando processo de compra - Jogador: " + player.getName() + ", Nave: " + shipId);
        
        ShipTemplate template = shipTemplates.get(shipId);
        if (template == null || !template.isPurchasable()) {
            player.sendMessage("§c✘ Esta nave não está disponível para compra!");
            return false;
        }

        PlayerHangar hangar = playerHangars.get(player.getUniqueId());
        if (hangar == null) {
            plugin.getLogger().info("[DEBUG] Criando novo hangar para " + player.getName());
            hangar = new PlayerHangar(player.getUniqueId(), 1);
            playerHangars.put(player.getUniqueId(), hangar);
            savePlayerHangar(player.getUniqueId());
        }

        // Verificar se o jogador já possui esta nave
        if (hangar.getShips().stream().anyMatch(ship -> ship.getTemplateId().equals(shipId))) {
            player.sendMessage("");
            player.sendMessage("§c✘ Você já possui esta nave!");
            player.sendMessage("§cVocê não pode ter duas naves do mesmo modelo.");
            player.sendMessage("");
            player.playSound(player.getLocation(), "NOTE_BASS", 1.0f, 0.5f);
            return false;
        }

        if (hangar.getShips().size() >= getMaxShips(hangar.getLevel())) {
            player.sendMessage("");
            player.sendMessage("§c✘ Seu hangar está cheio!");
            player.sendMessage("§cFaça um upgrade para ter mais espaço.");
            player.sendMessage("");
            player.playSound(player.getLocation(), "NOTE_BASS", 1.0f, 0.5f);
            return false;
        }

        double price = template.getPrice();
        if (!economyManager.removeBalance(player.getUniqueId(), price)) {
            player.sendMessage("");
            player.sendMessage("§c✘ Você não tem créditos suficientes para comprar esta nave!");
            player.sendMessage("§cPreço: " + economyManager.formatValue(price) + " ⭐");
            player.sendMessage("");
            player.playSound(player.getLocation(), "NOTE_BASS", 1.0f, 0.5f);
            return false;
        }

        try {
            // Criar e adicionar a nova nave
            PlayerShip newShip = new PlayerShip(shipId, template.getName(), 100.0, new HashMap<>());
            hangar.addShip(newShip);
            
            // Salvar no MongoDB
            savePlayerHangar(player.getUniqueId());
            plugin.getLogger().info("[DEBUG] Nave comprada e salva com sucesso - Jogador: " + player.getName() + ", Nave: " + template.getName());

            // Enviar mensagem de sucesso
            player.sendMessage("");
            player.sendMessage("§a✔ Você comprou uma " + template.getName());
            player.sendMessage("§7por §f" + economyManager.formatValue(price) + " ⭐");
            player.sendMessage("");
            player.playSound(player.getLocation(), "LEVEL_UP", 1.0f, 1.0f);
            return true;
        } catch (Exception e) {
            // Em caso de erro, devolver os créditos
            plugin.getLogger().severe("[ERROR] Erro ao salvar nave comprada - Jogador: " + player.getName() + ", Erro: " + e.getMessage());
            economyManager.addBalance(player.getUniqueId(), price);
            
            player.sendMessage("");
            player.sendMessage("§c✘ Ocorreu um erro ao processar sua compra!");
            player.sendMessage("§cSeus créditos foram devolvidos.");
            player.sendMessage("§cTente novamente mais tarde.");
            player.sendMessage("");
            player.playSound(player.getLocation(), "NOTE_BASS", 1.0f, 0.5f);
            return false;
        }
    }

    public int getMaxShips(int hangarLevel) {
        ConfigurationSection hangarSection = shipsConfig.getConfigurationSection("hangar.levels." + hangarLevel);
        return hangarSection != null ? hangarSection.getInt("max_ships", 2) : 2;
    }

    public Collection<ShipTemplate> getAvailableShips() {
        return shipTemplates.values();
    }

    public PlayerHangar getPlayerHangar(UUID playerId) {
        return playerHangars.get(playerId);
    }

    public void unloadPlayerHangar(UUID playerId) {
        savePlayerHangar(playerId);
        playerHangars.remove(playerId);
    }

    public SP_CORE getPlugin() {
        return plugin;
    }

    public StellarEconomyManager getEconomyManager() {
        return economyManager;
    }

    public Map<UUID, PlayerHangar> getPlayerHangars() {
        return playerHangars;
    }

    public double getFuelPrice(String fuelType) {
        return shipsConfig.getDouble("fuel." + fuelType + ".price", 100.0);
    }

    public double getFuelEfficiency(String fuelType) {
        return shipsConfig.getDouble("fuel." + fuelType + ".efficiency", 1.0);
    }

    public String getFuelName(String fuelType) {
        return shipsConfig.getString("fuel." + fuelType + ".name", 
            ChatColor.GRAY + "Combustível " + fuelType);
    }

    public boolean useFuel(PlayerShip ship, double amount) {
        if (ship.getFuel() >= amount) {
            ship.useFuel(amount);
            return true;
        }
        return false;
    }

    public void handlePlayerJoin(Player player) {
        loadPlayerHangar(player.getUniqueId());
    }

    public void handlePlayerQuit(Player player) {
        UUID playerId = player.getUniqueId();
        savePlayerHangar(playerId);
        playerHangars.remove(playerId);
    }

    public void registerListeners() {
        plugin.getServer().getPluginManager().registerEvents(
            new ShipShopListener(this, shopGUI), plugin);
        plugin.getServer().getPluginManager().registerEvents(
            new FuelShopListener(this, fuelShopGUI), plugin);
        plugin.getServer().getPluginManager().registerEvents(new ShipEffectListener(plugin), plugin);
    }

    public ShipShopGUI getShopGUI() {
        return shopGUI;
    }

    public FuelShopGUI getFuelShopGUI() {
        return fuelShopGUI;
    }

    public ShipEffectManager getShipEffectManager() {
        return shipEffectManager;
    }

    public void setActiveShip(UUID playerId, PlayerShip ship) {
        activeShips.put(playerId, ship);
        Player player = plugin.getServer().getPlayer(playerId);
        if (player != null && player.isOnline()) {
            shipEffectManager.createEffect(player, ship.getName());
        }
    }

    public PlayerShip getActiveShip(UUID playerId) {
        return activeShips.get(playerId);
    }

    public void removeActiveShip(UUID playerId) {
        activeShips.remove(playerId);
        Player player = plugin.getServer().getPlayer(playerId);
        if (player != null) {
            shipEffectManager.removeEffect(player);
        }
    }

    public void onDisable() {
        // Remove todos os efeitos ao desligar
        shipEffectManager.onDisable();
        
        // Salva todos os hangares
        for (UUID playerId : playerHangars.keySet()) {
            savePlayerHangar(playerId);
        }
    }
} 