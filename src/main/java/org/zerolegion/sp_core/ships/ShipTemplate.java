package org.zerolegion.sp_core.ships;

import java.util.List;
import java.util.Map;

public class ShipTemplate {
    private final String id;
    private final String name;
    private final ShipType type;
    private final boolean purchasable;
    private final double price;
    private final Map<String, Double> stats;
    private final List<String> description;

    public ShipTemplate(String id, String name, ShipType type, boolean purchasable, 
                       double price, Map<String, Double> stats, List<String> description) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.purchasable = purchasable;
        this.price = price;
        this.stats = stats;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ShipType getType() {
        return type;
    }

    public boolean isPurchasable() {
        return purchasable;
    }

    public double getPrice() {
        return price;
    }

    public Map<String, Double> getStats() {
        return stats;
    }

    public List<String> getDescription() {
        return description;
    }

    public double getStat(String key) {
        return stats.getOrDefault(key, 0.0);
    }
} 