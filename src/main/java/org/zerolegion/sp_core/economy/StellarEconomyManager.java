package org.zerolegion.sp_core.economy;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.IndexOptions;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.zerolegion.sp_core.SP_CORE;
import org.zerolegion.sp_core.economy.gui.CreditosGUI;
import org.zerolegion.sp_core.economy.placeholders.StellarEconomyPlaceholders;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class StellarEconomyManager {
    private final SP_CORE plugin;
    private final MongoCollection<Document> economyCollection;
    private final TransactionHistory transactionHistory;
    private final CreditosGUI creditosGUI;
    private StellarEconomyPlaceholders placeholders = null;
    private final Map<UUID, Double> balances;
    private final DecimalFormat formatter;

    public StellarEconomyManager(SP_CORE plugin) {
        this.plugin = plugin;
        this.economyCollection = plugin.getDatabase().getCollection("economy");
        this.balances = new HashMap<>();
        this.formatter = new DecimalFormat("#,##0.00");
        this.transactionHistory = new TransactionHistory(plugin.getDatabase());
        this.creditosGUI = new CreditosGUI(this, transactionHistory);

        // Criar índice único para UUID se não existir
        economyCollection.createIndex(Indexes.ascending("uuid"), new IndexOptions().unique(true));

        // Registrar placeholders se o PlaceholderAPI estiver presente
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            this.placeholders = new StellarEconomyPlaceholders(this);
            this.placeholders.register();
            plugin.getLogger().info("Placeholders do sistema de economia registrados com sucesso!");
        }
    }

    public void loadPlayer(UUID playerId) {
        plugin.getLogger().info("[ECONOMY] Iniciando carregamento de economia para " + playerId);
        
        try {
            // Buscar documento do jogador
            Document doc = economyCollection.find(new Document("uuid", playerId.toString())).first();
            
            if (doc != null) {
                // Se existe, carrega o saldo
                Object balanceObj = doc.get("balance");
                double balance;
                
                if (balanceObj instanceof Integer) {
                    balance = ((Integer) balanceObj).doubleValue();
                } else if (balanceObj instanceof Double) {
                    balance = (Double) balanceObj;
                } else if (balanceObj instanceof Long) {
                    balance = ((Long) balanceObj).doubleValue();
                } else {
                    balance = 0.0;
                }
                
                balances.put(playerId, balance);
                plugin.getLogger().info("[ECONOMY] Carregado saldo de " + balance + " para jogador " + playerId);
            } else {
                // Se não existe, cria novo documento com saldo inicial
                double initialBalance = 0.0;
                Player player = Bukkit.getPlayer(playerId);
                String playerName = player != null ? player.getName() : "Unknown";
                
                Document newDoc = new Document()
                    .append("uuid", playerId.toString())
                    .append("balance", initialBalance)
                    .append("name", playerName)
                    .append("lastUpdated", new Date());
                
                economyCollection.insertOne(newDoc);
                balances.put(playerId, initialBalance);
                plugin.getLogger().info("[ECONOMY] Criado novo saldo para jogador " + playerName + " (" + playerId + ")");
            }
        } catch (Exception e) {
            plugin.getLogger().severe("[ECONOMY] Erro ao carregar economia para " + playerId + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void savePlayer(UUID playerId) {
        Double balance = balances.get(playerId);
        if (balance != null) {
            // Atualiza ou insere o documento
            Document update = new Document("$set", new Document("balance", balance));
            economyCollection.updateOne(
                new Document("uuid", playerId.toString()),
                update,
                new UpdateOptions().upsert(true)
            );
            plugin.getLogger().info("[DEBUG] Salvo saldo de " + balance + " para jogador " + playerId);
        }
    }

    public void saveAllPlayers() {
        for (Map.Entry<UUID, Double> entry : balances.entrySet()) {
            savePlayer(entry.getKey());
        }
        plugin.getLogger().info("[DEBUG] Salvos todos os saldos");
    }

    public double getBalance(UUID playerId) {
        return balances.getOrDefault(playerId, 0.0);
    }

    public void setBalance(UUID playerId, double amount) {
        balances.put(playerId, amount);
        Player player = Bukkit.getPlayer(playerId);
        if (player != null) {
            updateBalance(player, amount);
        } else {
            savePlayer(playerId);
        }
        plugin.getLogger().info("[DEBUG] Definido novo saldo de " + amount + " para jogador " + playerId);
    }

    public void addBalance(UUID playerId, double amount) {
        double currentBalance = getBalance(playerId);
        setBalance(playerId, currentBalance + amount);
    }

    public boolean removeBalance(UUID playerId, double amount) {
        double currentBalance = getBalance(playerId);
        if (currentBalance >= amount) {
            setBalance(playerId, currentBalance - amount);
            return true;
        }
        return false;
    }

    public void unloadPlayer(UUID playerId) {
        savePlayer(playerId); // Salva antes de remover do cache
        balances.remove(playerId);
        plugin.getLogger().info("[DEBUG] Descarregado jogador " + playerId);
    }

    public void getTopBalance(Player requester, int limit) {
        // Busca os top jogadores no MongoDB
        List<Document> topPlayers = economyCollection.find()
                .sort(Sorts.descending("balance"))
                .limit(limit)
                .into(new ArrayList<>());
        
        // Envia o top
        requester.sendMessage("");
        requester.sendMessage(ChatColor.YELLOW + "✧ TOP " + limit + " CRÉDITOS ESTELARES ✧");
        requester.sendMessage("");
        
        int position = 1;
        for (Document doc : topPlayers) {
            String name = doc.getString("name");
            double balance = doc.getDouble("balance");
            
            String prefix;
            if (position == 1) prefix = ChatColor.GOLD + "1º ";
            else if (position == 2) prefix = ChatColor.GRAY + "2º ";
            else if (position == 3) prefix = ChatColor.RED + "3º ";
            else prefix = ChatColor.GRAY + String.valueOf(position) + "º ";
            
            requester.sendMessage(prefix + ChatColor.WHITE + name + ChatColor.GRAY + ": " + 
                                ChatColor.YELLOW + formatter.format(balance) + " ⭐");
            position++;
        }
        requester.sendMessage("");
    }

    public void saveAllData() {
        for (Map.Entry<UUID, Double> entry : balances.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player != null) {
                updateBalance(player, entry.getValue());
            }
        }
    }

    public void openGUI(Player player) {
        creditosGUI.openMainMenu(player);
    }

    public TransactionHistory getTransactionHistory() {
        return transactionHistory;
    }

    public CreditosGUI getCreditosGUI() {
        return creditosGUI;
    }

    public List<Document> getTopPlayersData(int limit) {
        return economyCollection.find()
                .sort(Sorts.descending("balance"))
                .limit(limit)
                .into(new ArrayList<>());
    }

    public SP_CORE getPlugin() {
        return plugin;
    }

    public String getFormattedBalance(Player player) {
        double balance = getBalance(player.getUniqueId());
        return formatter.format(balance);
    }

    private void updateBalance(Player player, double balance) {
        Document update = new Document("$set", new Document()
            .append("balance", balance)
            .append("name", player.getName()));
        
        economyCollection.updateOne(
            new Document("uuid", player.getUniqueId().toString()),
            update,
            new UpdateOptions().upsert(true)
        );
        plugin.getLogger().info("[DEBUG] Atualizado saldo de " + balance + " para jogador " + player.getName());
    }

    public String formatValue(double amount) {
        String[] suffixes = new String[] { "", "K", "M", "B", "T", "Q", "QQ", "S", "SS", "O", "N", "D" };
        int suffixIndex = 0;
        
        while (amount >= 1000 && suffixIndex < suffixes.length - 1) {
            amount /= 1000;
            suffixIndex++;
        }
        
        if (suffixIndex == 0) {
            return formatter.format(amount);
        } else {
            // Se o número for maior que 100, não mostra decimais
            if (amount >= 100) {
                return String.format("%.0f%s", amount, suffixes[suffixIndex]);
            }
            // Se o número for maior que 10, mostra 1 decimal
            else if (amount >= 10) {
                return String.format("%.1f%s", amount, suffixes[suffixIndex]);
            }
            // Se for menor que 10, mostra 2 decimais
            else {
                return String.format("%.2f%s", amount, suffixes[suffixIndex]);
            }
        }
    }

    public void transfer(Player from, Player to, double amount) {
        UUID fromId = from.getUniqueId();
        UUID toId = to.getUniqueId();
        
        // Verificar se o remetente tem saldo suficiente
        if (getBalance(fromId) >= amount) {
            // Remover do remetente
            removeBalance(fromId, amount);
            // Adicionar ao destinatário
            addBalance(toId, amount);
            
            // Registrar a transação no histórico
            transactionHistory.addTransaction(fromId, toId, amount, TransactionType.TRANSFER);
            
            // Notificar os jogadores
            from.sendMessage(ChatColor.YELLOW + "✧ Você enviou " + formatValue(amount) + " ⭐ para " + to.getName());
            to.sendMessage(ChatColor.YELLOW + "✧ Você recebeu " + formatValue(amount) + " ⭐ de " + from.getName());
            
            // Log da transação
            plugin.getLogger().info("[ECONOMY] Transferência: " + from.getName() + " enviou " + 
                formatValue(amount) + " créditos para " + to.getName());
        }
    }

    public void adminAddBalance(Player target, double amount, Player admin) {
        addBalance(target.getUniqueId(), amount);
        transactionHistory.addTransaction(admin.getUniqueId(), target.getUniqueId(), amount, TransactionType.ADMIN_ADD);
        
        // Log da ação administrativa
        plugin.getLogger().info("[ECONOMY] Admin " + admin.getName() + " adicionou " + 
            formatValue(amount) + " créditos para " + target.getName());
    }

    public void adminRemoveBalance(Player target, double amount, Player admin) {
        if (removeBalance(target.getUniqueId(), amount)) {
            transactionHistory.addTransaction(target.getUniqueId(), admin.getUniqueId(), amount, TransactionType.ADMIN_REMOVE);
            
            // Log da ação administrativa
            plugin.getLogger().info("[ECONOMY] Admin " + admin.getName() + " removeu " + 
                formatValue(amount) + " créditos de " + target.getName());
        }
    }

    // Enum para tipos de transação
    public enum TransactionType {
        TRANSFER,
        ADMIN_ADD,
        ADMIN_REMOVE,
        SYSTEM
    }
} 