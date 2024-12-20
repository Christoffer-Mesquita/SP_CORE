package org.zerolegion.sp_core.economy.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.zerolegion.sp_core.economy.StellarEconomyManager;
import org.jetbrains.annotations.NotNull;

public class StellarEconomyPlaceholders extends PlaceholderExpansion {
    private final StellarEconomyManager economyManager;

    public StellarEconomyPlaceholders(StellarEconomyManager economyManager) {
        this.economyManager = economyManager;
    }

    @Override
    @NotNull
    public String getIdentifier() {
        return "stellareconomy";
    }

    @Override
    @NotNull
    public String getAuthor() {
        return "ZeroLegion";
    }

    @Override
    @NotNull
    public String getVersion() {
        return "1.0";
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (player == null) {
            return "";
        }

        // %stellareconomy_balance_raw% - Saldo sem formatação
        if (identifier.equals("balance_raw")) {
            return String.valueOf(economyManager.getBalance(player.getUniqueId()));
        }

        // %stellareconomy_balance_formatted% - Saldo formatado
        if (identifier.equals("balance_formatted")) {
            return economyManager.formatValue(economyManager.getBalance(player.getUniqueId()));
        }

        return null;
    }
} 