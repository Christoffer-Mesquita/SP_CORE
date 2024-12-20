package org.zerolegion.sp_core.economy;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Sorts;
import org.bson.Document;
import org.zerolegion.sp_core.economy.StellarEconomyManager.TransactionType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class TransactionHistory {
    private final MongoCollection<Document> transactionCollection;

    public TransactionHistory(MongoDatabase database) {
        this.transactionCollection = database.getCollection("transactions");
    }

    public void addTransaction(UUID fromId, UUID toId, double amount, TransactionType type) {
        Document transaction = new Document()
            .append("from_uuid", fromId.toString())
            .append("to_uuid", toId.toString())
            .append("amount", amount)
            .append("type", type.toString())
            .append("timestamp", new Date());

        transactionCollection.insertOne(transaction);
    }

    public List<Document> getPlayerTransactions(UUID playerId, int limit) {
        Document query = new Document("$or", Arrays.asList(
            new Document("from_uuid", playerId.toString()),
            new Document("to_uuid", playerId.toString())
        ));

        return transactionCollection.find(query)
            .sort(Sorts.descending("timestamp"))
            .limit(limit)
            .into(new ArrayList<>());
    }

    public List<Document> getRecentTransactions(int limit) {
        return transactionCollection.find()
            .sort(Sorts.descending("timestamp"))
            .limit(limit)
            .into(new ArrayList<>());
    }

    public List<Document> getTransactionsByType(TransactionType type, int limit) {
        return transactionCollection.find(
            new Document("type", type.name())
        )
        .sort(Sorts.descending("timestamp"))
        .limit(limit)
        .into(new ArrayList<>());
    }

    public void clearOldTransactions(int daysToKeep) {
        Date cutoffDate = new Date(System.currentTimeMillis() - (daysToKeep * 24L * 60L * 60L * 1000L));
        transactionCollection.deleteMany(
            new Document("timestamp", new Document("$lt", cutoffDate))
        );
    }

    public long getTransactionCount(UUID playerId) {
        Document query = new Document("$or", Arrays.asList(
            new Document("from_uuid", playerId.toString()),
            new Document("to_uuid", playerId.toString())
        ));
        
        return transactionCollection.countDocuments(query);
    }
} 