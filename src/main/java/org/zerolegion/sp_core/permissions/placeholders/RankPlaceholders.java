package org.zerolegion.sp_core.permissions.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.zerolegion.sp_core.permissions.PermissionManager;
import org.zerolegion.sp_core.permissions.Group;

import java.util.List;
import java.util.Comparator;

public class RankPlaceholders extends PlaceholderExpansion {
    private final PermissionManager permissionManager;

    public RankPlaceholders(PermissionManager permissionManager) {
        this.permissionManager = permissionManager;
    }

    @Override
    public String getIdentifier() {
        return "stellarrank";
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

        // Obter todos os grupos do jogador
        List<Group> playerGroups = permissionManager.getPlayerGroups(player.getUniqueId());
        if (playerGroups.isEmpty()) {
            return ChatColor.GRAY + "Membro";
        }

        // Encontrar o grupo com maior peso
        Group highestGroup = playerGroups.stream()
                .max(Comparator.comparingInt(Group::getWeight))
                .orElse(null);

        if (highestGroup == null) {
            return ChatColor.GRAY + "Membro";
        }

        switch (identifier) {
            // %stellarrank_name% - Nome do rank
            case "name":
                return highestGroup.getName();

            // %stellarrank_prefix% - Prefixo do rank
            case "prefix":
                return highestGroup.getPrefix();

            // %stellarrank_colored_name% - Nome colorido do rank
            case "colored_name":
                return highestGroup.getPrefix() + highestGroup.getName();

            // %stellarrank_weight% - Peso do rank
            case "weight":
                return String.valueOf(highestGroup.getWeight());

            // %stellarrank_display% - Display completo do rank (prefixo + nome)
            case "display":
                return highestGroup.getPrefix() + " " + highestGroup.getName();

            // %stellarrank_all_groups% - Lista todos os grupos do jogador
            case "all_groups":
                return playerGroups.stream()
                        .map(group -> group.getPrefix() + group.getName())
                        .reduce((a, b) -> a + ChatColor.GRAY + ", " + b)
                        .orElse(ChatColor.GRAY + "Nenhum");
        }

        return null;
    }
} 