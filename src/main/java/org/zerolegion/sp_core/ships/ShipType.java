package org.zerolegion.sp_core.ships;

public enum ShipType {
    MINING("§bMineração"),
    COMBAT("§cCombate"),
    TRANSPORT("§eTransporte"),
    SPECIAL("§5Especial");

    private final String displayName;

    ShipType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
} 