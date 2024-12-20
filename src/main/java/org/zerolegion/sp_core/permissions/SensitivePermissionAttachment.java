package org.zerolegion.sp_core.permissions;

import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.entity.Player;
import org.zerolegion.sp_core.SP_CORE;
import java.util.*;

public class SensitivePermissionAttachment {
    private final SP_CORE plugin;
    private final Player player;
    private final PermissionAttachment attachment;
    private final Set<String> playerPermissions;
    private final Map<String, Set<String>> groupPermissions;

    public SensitivePermissionAttachment(SP_CORE plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.attachment = player.addAttachment(plugin);
        this.playerPermissions = new HashSet<>();
        this.groupPermissions = new HashMap<>();
    }

    public void recalculatePermissions() {
        // Limpa todas as permissões atuais
        for (String permission : attachment.getPermissions().keySet()) {
            attachment.unsetPermission(permission);
        }

        // Limpa caches locais
        playerPermissions.clear();
        groupPermissions.clear();

        // Carrega permissões dos grupos
        List<Group> groups = plugin.getPermissionManager().getPlayerGroups(player.getUniqueId());
        for (Group group : groups) {
            Set<String> perms = group.getPermissions();
            if (perms != null) {
                groupPermissions.put(group.getName(), new HashSet<>(perms));
                for (String perm : perms) {
                    attachment.setPermission(perm, true);
                    plugin.getLogger().info("[DEBUG] Aplicando permissão de grupo '" + perm + "' do grupo '" + group.getName() + "' ao jogador " + player.getName());
                }
            }
        }

        // Carrega permissões individuais do jogador
        Set<String> perms = plugin.getPermissionManager().getPlayerPermissions(player.getUniqueId());
        if (perms != null) {
            playerPermissions.addAll(perms);
            for (String perm : perms) {
                attachment.setPermission(perm, true);
                plugin.getLogger().info("[DEBUG] Aplicando permissão individual '" + perm + "' ao jogador " + player.getName());
            }
        }

        // Adiciona permissão de operador se necessário
        if (player.isOp()) {
            attachment.setPermission("*", true);
        }

        // Debug final
        plugin.getLogger().info("[DEBUG] Permissões finais para " + player.getName() + ": " + attachment.getPermissions().keySet());

        // Atualiza o jogador
        player.recalculatePermissions();
    }

    public void cleanup() {
        if (attachment != null) {
            try {
                player.removeAttachment(attachment);
            } catch (Exception ignored) {}
        }
    }

    public boolean hasPermission(String permission) {
        // Debug da verificação de permissão
        plugin.getLogger().info("[DEBUG] Verificando permissão '" + permission + "' para " + player.getName());
        
        // Verifica permissão de operador
        if (player.isOp()) {
            plugin.getLogger().info("[DEBUG] " + player.getName() + " é OP, permissão concedida");
            return true;
        }

        // Verifica permissão direta no attachment do Bukkit
        if (attachment.getPermissions().containsKey(permission)) {
            boolean has = attachment.getPermissions().get(permission);
            plugin.getLogger().info("[DEBUG] " + player.getName() + " tem permissão direta: " + has);
            return has;
        }

        // Verifica permissões do jogador
        if (playerPermissions.contains(permission)) {
            plugin.getLogger().info("[DEBUG] " + player.getName() + " tem permissão individual");
            return true;
        }

        // Verifica permissões dos grupos
        for (Set<String> groupPerms : groupPermissions.values()) {
            if (groupPerms.contains(permission)) {
                plugin.getLogger().info("[DEBUG] " + player.getName() + " tem permissão via grupo");
                return true;
            }
        }

        // Verifica wildcards
        if (playerPermissions.contains("*") || attachment.getPermissions().containsKey("*")) {
            plugin.getLogger().info("[DEBUG] " + player.getName() + " tem permissão wildcard *");
            return true;
        }

        // Verifica wildcards em permissões individuais
        for (String perm : playerPermissions) {
            if (perm.endsWith(".*") && permission.startsWith(perm.substring(0, perm.length() - 2))) {
                plugin.getLogger().info("[DEBUG] " + player.getName() + " tem permissão via wildcard individual: " + perm);
                return true;
            }
        }

        // Verifica wildcards em permissões de grupo
        for (Set<String> groupPerms : groupPermissions.values()) {
            for (String perm : groupPerms) {
                if (perm.endsWith(".*") && permission.startsWith(perm.substring(0, perm.length() - 2))) {
                    plugin.getLogger().info("[DEBUG] " + player.getName() + " tem permissão via wildcard de grupo: " + perm);
                    return true;
                }
            }
        }

        plugin.getLogger().info("[DEBUG] " + player.getName() + " não tem a permissão " + permission);
        return false;
    }
} 