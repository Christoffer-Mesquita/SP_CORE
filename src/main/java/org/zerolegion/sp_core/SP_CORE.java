package org.zerolegion.sp_core;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Listener;
import org.bukkit.configuration.file.FileConfiguration;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.zerolegion.sp_core.chat.ChatManager;
import org.zerolegion.sp_core.commands.*;
import org.zerolegion.sp_core.listeners.*;
import org.zerolegion.sp_core.ships.SpaceshipManager;
import org.zerolegion.sp_core.ships.commands.ShipCommand;
import org.zerolegion.sp_core.ships.planets.PlanetListener;
import org.zerolegion.sp_core.ships.planets.PlanetManager;
import org.zerolegion.sp_core.tablist.TabListManager;
import org.zerolegion.sp_core.permissions.PermissionManager;
import org.zerolegion.sp_core.level.LevelManager;
import org.zerolegion.sp_core.classes.ClassManager;
import org.zerolegion.sp_core.oxygen.OxygenManager;
import org.zerolegion.sp_core.economy.StellarEconomyManager;
import org.zerolegion.sp_core.economy.commands.CreditosCommand;
import org.zerolegion.sp_core.economy.gui.CreditosGUIListener;
import org.zerolegion.sp_core.clans.ClanManager;
import org.zerolegion.sp_core.clans.gui.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.util.UUID;
import java.util.ArrayList;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.zerolegion.sp_core.crafting.SpaceCrafting;
import org.zerolegion.sp_core.crafting.SpaceRecipe;
import org.zerolegion.sp_core.crafting.gui.SpaceCraftingGUI;
import org.zerolegion.sp_core.crafting.commands.SpaceCraftingCommand;
import org.zerolegion.sp_core.crafting.gui.SpaceCraftingGUIListener;
import java.util.List;

public final class SP_CORE extends JavaPlugin {
    private static SP_CORE instance;
    private MongoClient mongoClient;
    private MongoDatabase database;
    private ChatManager chatManager;
    private TabListManager tabListManager;
    private PermissionManager permissionManager;
    private LevelManager levelManager;
    private ClassManager classManager;
    private OxygenManager oxygenManager;
    private StellarEconomyManager stellarEconomyManager;
    private SpaceshipManager spaceshipManager;
    private PlanetManager planetManager;
    private ClanManager clanManager;
    private SpaceCraftingGUI craftingGUI;
    private FileConfiguration config;

    @Override
    public void onEnable() {
        instance = this;
        
        // Salvar config padrão
        saveDefaultConfig();
        config = getConfig();
        
        // Inicializar sistemas
        initializeSystems();

        getLogger().info("SP_CORE iniciado com sucesso!");
    }

    private void initializeSystems() {
        // Conectar ao MongoDB
        String mongoUri = config.getString("mongodb.uri", "mongodb://localhost:27017");
        String dbName = config.getString("mongodb.database", "prison_core");
        
        try {
            if (mongoClient != null) {
                mongoClient.close();
            }
            
            mongoClient = MongoClients.create(mongoUri);
            database = mongoClient.getDatabase(dbName);
            getLogger().info("Conexão com MongoDB estabelecida com sucesso!");
        } catch (Exception e) {
            getLogger().severe("Erro ao conectar ao MongoDB: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Inicializar gerenciadores
        chatManager = new ChatManager(this);
        tabListManager = new TabListManager(this);
        permissionManager = new PermissionManager(this);
        levelManager = new LevelManager(this);
        classManager = new ClassManager(this);
        oxygenManager = new OxygenManager(this);
        stellarEconomyManager = new StellarEconomyManager(this);
        spaceshipManager = new SpaceshipManager(this, stellarEconomyManager);
        planetManager = new PlanetManager(this);
        clanManager = new ClanManager(this);
        spaceshipManager.registerListeners();

        // Inicializar sistema de crafting espacial
        craftingGUI = new SpaceCraftingGUI();
        SpaceCraftingGUIListener craftingListener = new SpaceCraftingGUIListener(this, craftingGUI);
        getServer().getPluginManager().registerEvents(craftingListener, this);
        getCommand("spacecrafting").setExecutor(new SpaceCraftingCommand(this, craftingGUI));
        registerSpaceRecipes();

        // Inicializar GUIs do sistema de clãs
        ClanMainGUI mainGUI = new ClanMainGUI(clanManager);
        ClanInviteGUI inviteGUI = new ClanInviteGUI(clanManager);
        ClanMembersGUI membersGUI = new ClanMembersGUI(clanManager);
        ClanMemberManageGUI memberManageGUI = new ClanMemberManageGUI(clanManager);
        ClanCreateGUI createGUI = new ClanCreateGUI(clanManager);
        ClanSettingsGUI settingsGUI = new ClanSettingsGUI(clanManager);
        ClanRelationsGUI relationsGUI = new ClanRelationsGUI(clanManager);
        ClanBankGUI bankGUI = new ClanBankGUI(clanManager);
        ClanEventsGUI eventsGUI = new ClanEventsGUI(clanManager);
        ClanListGUI listGUI = new ClanListGUI(clanManager);

        // Registrar eventos
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        getServer().getPluginManager().registerEvents(new TabListListener(this), this);
        getServer().getPluginManager().registerEvents(new PermissionListener(this), this);
        getServer().getPluginManager().registerEvents(new LevelListener(this), this);
        getServer().getPluginManager().registerEvents(new ClassListener(this), this);
        getServer().getPluginManager().registerEvents(new OxygenListener(this), this);
        getServer().getPluginManager().registerEvents(new CreditosGUIListener(stellarEconomyManager, stellarEconomyManager.getCreditosGUI()), this);
        getServer().getPluginManager().registerEvents(new PlanetListener(this), this);
        
        // Registrar listeners do sistema de clãs
        getServer().getPluginManager().registerEvents(new ClanMainGUIListener(this, clanManager), this);
        getServer().getPluginManager().registerEvents(new ClanMembersGUIListener(this, clanManager, mainGUI, inviteGUI), this);
        getServer().getPluginManager().registerEvents(new ClanMemberManageGUIListener(this, clanManager, membersGUI), this);
        getServer().getPluginManager().registerEvents(new ClanInviteGUIListener(clanManager, membersGUI), this);
        getServer().getPluginManager().registerEvents(new ClanCreateGUIListener(this, clanManager, createGUI, mainGUI), this);
        getServer().getPluginManager().registerEvents(new ClanSettingsGUIListener(this, clanManager, settingsGUI, mainGUI), this);
        getServer().getPluginManager().registerEvents(new ClanRelationsGUIListener(this, clanManager, relationsGUI, mainGUI), this);
        getServer().getPluginManager().registerEvents(new ClanBankGUIListener(this, clanManager, bankGUI, mainGUI), this);
        getServer().getPluginManager().registerEvents(new ClanEventsGUIListener(this, clanManager, eventsGUI, mainGUI), this);
        getServer().getPluginManager().registerEvents(new ClanListGUIListener(this, clanManager, listGUI, createGUI, mainGUI), this);

        // Registrar PlayerListener com log de debug
        getLogger().info("[DEBUG] Registrando PlayerListener...");
        PlayerListener playerListener = new PlayerListener(this);
        getServer().getPluginManager().registerEvents(playerListener, this);
        getLogger().info("[DEBUG] PlayerListener registrado com sucesso!");

        // Registrar comandos
        getCommand("g").setExecutor(new GlobalChatCommand(this));
        getCommand("clearchat").setExecutor(new ClearChatCommand(this));
        getCommand("gm").setExecutor(new GamemodeCommand(this));
        getCommand("level").setExecutor(new LevelCommand(this));
        getCommand("permissoes").setExecutor(new PermissionCommand(this));
        getCommand("sensitive").setExecutor(new PluginReloadCommand(this));
        getCommand("creditos").setExecutor(new CreditosCommand(stellarEconomyManager));
        getCommand("nave").setExecutor(new ShipCommand(spaceshipManager));
        getCommand("clan").setExecutor(new ClanCommand(this));

        // Registrar tab completers
        getCommand("clearchat").setTabCompleter(new ClearChatCommand(this));
        getCommand("gm").setTabCompleter(new GamemodeCommand(this));
        getCommand("level").setTabCompleter(new LevelCommand(this));
        getCommand("permissoes").setTabCompleter(new PermissionCommand(this));
        getCommand("sensitive").setTabCompleter(new PluginReloadCommand(this));
        getCommand("creditos").setTabCompleter(new CreditosCommand(stellarEconomyManager));
        getCommand("clan").setTabCompleter(new ClanCommand(this));

        getLogger().info("Sistemas inicializados com sucesso!");

        // Registrar listeners
        getServer().getPluginManager().registerEvents(new ServerListPingListener(this), this);
    }

    private void registerSpaceRecipes() {
        // Anel do Poder Estelar
        List<String> ringLore = new ArrayList<>();
        ringLore.add(ChatColor.GRAY + "Um anel forjado com");
        ringLore.add(ChatColor.GRAY + "energia das estrelas");
        ringLore.add("");
        ringLore.add(ChatColor.AQUA + "Poder Base:");
        ringLore.add(ChatColor.GRAY + "• +10% Velocidade de Movimento");
        ringLore.add(ChatColor.GRAY + "• +5% Resistência no Espaço");

        ItemStack ringResult = SpaceCrafting.createSpaceItem(
            Material.GOLD_NUGGET,
            "Anel do Poder Estelar",
            ringLore,
            SpaceCrafting.CraftingQuality.COMUM
        );

        SpaceRecipe ringRecipe = new SpaceRecipe.Builder("anel_poder", ringResult)
            .addIngredient(11, new ItemStack(Material.GOLD_INGOT))
            .addIngredient(12, new ItemStack(Material.DIAMOND))
            .addIngredient(13, new ItemStack(Material.EMERALD))
            .addIngredient(20, new ItemStack(Material.NETHER_STAR))
            .setRequiredLevel(15)
            .setBaseSuccessRate(0.75)
            .addDescription("Um anel que concede poderes estelares")
            .addDescription("ao seu portador")
            .build();

        craftingGUI.registerRecipe(ringRecipe);

        // Colar da Proteção Cósmica
        List<String> necklaceLore = new ArrayList<>();
        necklaceLore.add(ChatColor.GRAY + "Um colar que protege");
        necklaceLore.add(ChatColor.GRAY + "contra a radiação espacial");
        necklaceLore.add("");
        necklaceLore.add(ChatColor.AQUA + "Poder Base:");
        necklaceLore.add(ChatColor.GRAY + "• +15% Proteção Contra Radiação");
        necklaceLore.add(ChatColor.GRAY + "• +8% Economia de Oxigênio");

        ItemStack necklaceResult = SpaceCrafting.createSpaceItem(
            Material.GOLD_INGOT,
            "Colar da Proteção Cósmica",
            necklaceLore,
            SpaceCrafting.CraftingQuality.COMUM
        );

        SpaceRecipe necklaceRecipe = new SpaceRecipe.Builder("colar_cosmico", necklaceResult)
            .addIngredient(11, new ItemStack(Material.GOLD_INGOT))
            .addIngredient(12, new ItemStack(Material.DIAMOND))
            .addIngredient(13, new ItemStack(Material.GOLD_INGOT))
            .addIngredient(21, new ItemStack(Material.EMERALD))
            .addIngredient(22, new ItemStack(Material.NETHER_STAR))
            .setRequiredLevel(20)
            .setBaseSuccessRate(0.70)
            .addDescription("Um colar que oferece proteç��o")
            .addDescription("contra os perigos do espaço")
            .build();

        craftingGUI.registerRecipe(necklaceRecipe);

        // Bracelete do Poder Gravitacional
        List<String> braceletLore = new ArrayList<>();
        braceletLore.add(ChatColor.GRAY + "Um bracelete que controla");
        braceletLore.add(ChatColor.GRAY + "a força da gravidade");
        braceletLore.add("");
        braceletLore.add(ChatColor.AQUA + "Poder Base:");
        braceletLore.add(ChatColor.GRAY + "• +12% Velocidade de Mineração");
        braceletLore.add(ChatColor.GRAY + "• +10% Chance de Drop Duplo");

        ItemStack braceletResult = SpaceCrafting.createSpaceItem(
            Material.GOLD_INGOT,
            "Bracelete do Poder Gravitacional",
            braceletLore,
            SpaceCrafting.CraftingQuality.COMUM
        );

        SpaceRecipe braceletRecipe = new SpaceRecipe.Builder("bracelete_gravitacional", braceletResult)
            .addIngredient(11, new ItemStack(Material.DIAMOND))
            .addIngredient(12, new ItemStack(Material.GOLD_INGOT))
            .addIngredient(13, new ItemStack(Material.DIAMOND))
            .addIngredient(21, new ItemStack(Material.EMERALD))
            .addIngredient(22, new ItemStack(Material.NETHER_STAR))
            .addIngredient(23, new ItemStack(Material.EMERALD))
            .setRequiredLevel(25)
            .setBaseSuccessRate(0.65)
            .addDescription("Um bracelete que permite controlar")
            .addDescription("a força gravitacional ao seu redor")
            .build();

        craftingGUI.registerRecipe(braceletRecipe);
    }

    public void reloadSystems() {
        // Desativa sistemas atuais
        if (tabListManager != null) {
            tabListManager.onDisable();
        }

        // Recarrega a configuração
        reloadConfig();
        config = getConfig();

        // Reinicializa todos os sistemas
        initializeSystems();
    }

    @Override
    public void onDisable() {
        // Remover todos os planetas
        if (planetManager != null) {
            planetManager.onDisable();
        }

        // Salvar dados de economia de todos os jogadores online
        if (stellarEconomyManager != null) {
            getLogger().info("[ECONOMY] Salvando dados econômicos de todos os jogadores...");
            for (Player player : Bukkit.getOnlinePlayers()) {
                stellarEconomyManager.unloadPlayer(player.getUniqueId());
            }
            stellarEconomyManager.saveAllPlayers();
            getLogger().info("[ECONOMY] Dados econômicos salvos com sucesso!");
        }

        // Fechar conexão com MongoDB
        if (mongoClient != null) {
            getLogger().info("Fechando conexão com MongoDB...");
            mongoClient.close();
            getLogger().info("Conexão com MongoDB fechada!");
        }

        // Desativar TabList
        if (tabListManager != null) {
            tabListManager.onDisable();
        }

        // Salvar dados das naves
        if (spaceshipManager != null) {
            for (UUID playerId : spaceshipManager.getPlayerHangars().keySet()) {
                spaceshipManager.savePlayerHangar(playerId);
            }
        }

        getLogger().info("SP_CORE desativado com sucesso!");
    }

    public static SP_CORE getInstance() {
        return instance;
    }

    public MongoDatabase getDatabase() {
        return database;
    }

    public ChatManager getChatManager() {
        return chatManager;
    }

    public TabListManager getTabListManager() {
        return tabListManager;
    }

    public PermissionManager getPermissionManager() {
        return permissionManager;
    }

    public LevelManager getLevelManager() {
        return levelManager;
    }

    public ClassManager getClassManager() {
        return classManager;
    }

    public OxygenManager getOxygenManager() {
        return oxygenManager;
    }

    public StellarEconomyManager getStellarEconomyManager() {
        return stellarEconomyManager;
    }

    public SpaceshipManager getSpaceshipManager() {
        return spaceshipManager;
    }

    public PlanetManager getPlanetManager() {
        return planetManager;
    }

    public ClanManager getClanManager() {
        return clanManager;
    }

    public SpaceCraftingGUI getCraftingGUI() {
        return craftingGUI;
    }
}
