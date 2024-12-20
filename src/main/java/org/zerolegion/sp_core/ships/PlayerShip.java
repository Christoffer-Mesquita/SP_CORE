package org.zerolegion.sp_core.ships;

import java.util.Map;

public class PlayerShip {
    private final String templateId;
    private String name;
    private double fuel;
    private Map<String, Integer> upgrades;

    public PlayerShip(String templateId, String name, double fuel, Map<String, Integer> upgrades) {
        this.templateId = templateId;
        this.name = name;
        this.fuel = fuel;
        this.upgrades = upgrades;
    }

    public String getTemplateId() {
        return templateId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getFuel() {
        return fuel;
    }

    public void setFuel(double fuel) {
        this.fuel = Math.max(0, Math.min(100, fuel));
    }

    public void addFuel(double amount) {
        setFuel(this.fuel + amount);
    }

    public boolean useFuel(double amount) {
        if (fuel >= amount) {
            setFuel(fuel - amount);
            return true;
        }
        return false;
    }

    public Map<String, Integer> getUpgrades() {
        return upgrades;
    }

    public int getUpgradeLevel(String upgrade) {
        return upgrades.getOrDefault(upgrade, 0);
    }

    public void setUpgradeLevel(String upgrade, int level) {
        upgrades.put(upgrade, Math.max(0, level));
    }

    public boolean upgradeComponent(String component) {
        int currentLevel = getUpgradeLevel(component);
        setUpgradeLevel(component, currentLevel + 1);
        return true;
    }
} 