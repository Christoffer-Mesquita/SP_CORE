package org.zerolegion.sp_core.level.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.zerolegion.sp_core.level.LevelManager;

import java.text.DecimalFormat;

public class LevelPlaceholders extends PlaceholderExpansion {
    private final LevelManager levelManager;
    private final DecimalFormat formatter;

    public LevelPlaceholders(LevelManager levelManager) {
        this.levelManager = levelManager;
        this.formatter = new DecimalFormat("#,###");
    }

    @Override
    public String getIdentifier() {
        return "stellarlevel";
    }

    @Override
    public String getAuthor() {
        return "ZeroLegion";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null) {
            return "";
        }

        // %stellarlevel_level% - Nível atual formatado
        if (identifier.equals("level")) {
            return formatter.format(levelManager.getPlayerLevel(player.getUniqueId()));
        }

        // %stellarlevel_level_raw% - Nível atual sem formatação
        if (identifier.equals("level_raw")) {
            return String.valueOf(levelManager.getPlayerLevel(player.getUniqueId()));
        }

        // %stellarlevel_xp% - XP atual (placeholder para futura implementação)
        if (identifier.equals("xp")) {
            return "0"; // Implementar quando o sistema de XP estiver pronto
        }

        // %stellarlevel_xp_required% - XP necessário para o próximo nível (placeholder para futura implementação)
        if (identifier.equals("xp_required")) {
            return "1000"; // Implementar quando o sistema de XP estiver pronto
        }

        return null;
    }
} 