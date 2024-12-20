package org.zerolegion.sp_core.permissions;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bukkit.entity.Player;
import org.zerolegion.sp_core.SP_CORE;
import java.util.*;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.Bukkit;
import com.mongodb.client.model.Filters;
import org.zerolegion.sp_core.permissions.placeholders.RankPlaceholders;

public class PermissionManager {
    private final SP_CORE plugin;
    private final MongoCollection<Document> groupsCollection;
    private final MongoCollection<Document> playersCollection;
    private final Map<UUID, PermissionAttachment> attachments;
    private final Map<String, Set<String>> groupPermissions;
    private final Map<String, String> groupPrefixes;
    private final Map<String, Integer> groupWeights;
    private final Map<UUID, Set<String>> playerPermissions;
    private final Map<UUID, Set<String>> playerGroups;
    private final Map<UUID, SensitivePermissionAttachment> sensitiveAttachments;
    private final Map<String, Group> groups;
    private RankPlaceholders placeholders;

    public PermissionManager(SP_CORE plugin) {
        this.plugin = plugin;
        this.groupsCollection = plugin.getDatabase().getCollection("permissions_groups");
        this.playersCollection = plugin.getDatabase().getCollection("permissions_players");
        this.attachments = new HashMap<>();
        this.groupPermissions = new HashMap<>();
        this.groupPrefixes = new HashMap<>();
        this.groupWeights = new HashMap<>();
        this.playerPermissions = new HashMap<>();
        this.playerGroups = new HashMap<>();
        this.sensitiveAttachments = new HashMap<>();
        this.groups = new HashMap<>();
        
        loadGroups();

        // Registrar placeholders se o PlaceholderAPI estiver presente
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            this.placeholders = new RankPlaceholders(this);
            this.placeholders.register();
            plugin.getLogger().info("Placeholders do sistema de ranks registrados com sucesso!");
        }
    }

    private void loadGroups() {
        groupsCollection.find().forEach(doc -> {
            String groupName = doc.getString("name");
            Set<String> permissions = new HashSet<>((List<String>) doc.get("permissions", List.class));
            String prefix = doc.getString("prefix");
            Integer weight = doc.getInteger("weight", 0);
            
            // Criar nova instância de Group
            Group group = new Group(groupName, prefix, weight);
            permissions.forEach(group::addPermission);
            groups.put(groupName.toLowerCase(), group);
            
            // Manter os maps existentes para compatibilidade
            groupPermissions.put(groupName, permissions);
            if (prefix != null) {
                groupPrefixes.put(groupName, prefix);
            }
            groupWeights.put(groupName, weight);
        });
    }

    public void createGroup(String name, String prefix) {
        Document group = new Document("name", name)
                .append("permissions", new ArrayList<String>())
                .append("prefix", prefix)
                .append("weight", 0);
        
        groupsCollection.insertOne(group);
        groupPermissions.put(name, new HashSet<>());
        groupPrefixes.put(name, prefix);
        groupWeights.put(name, 0);
    }

    public void deleteGroup(String name) {
        groupsCollection.deleteOne(new Document("name", name));
        groupPermissions.remove(name);
        groupPrefixes.remove(name);
        
        // Remove o grupo de todos os jogadores que o possuem
        playersCollection.updateMany(
            Filters.in("groups", name),
            Updates.pull("groups", name)
        );
    }

    public void addGroupPermission(String group, String permission) {
        groupsCollection.updateOne(
            new Document("name", group),
            Updates.addToSet("permissions", permission)
        );
        
        groupPermissions.computeIfAbsent(group, k -> new HashSet<>()).add(permission);
        
        // Atualiza as permissões de todos os jogadores no grupo
        for (UUID playerId : playerGroups.keySet()) {
            if (playerGroups.get(playerId).contains(group)) {
                updatePlayerPermissions(Bukkit.getPlayer(playerId));
            }
        }
    }

    public void removeGroupPermission(String group, String permission) {
        groupsCollection.updateOne(
            new Document("name", group),
            Updates.pull("permissions", permission)
        );
        
        groupPermissions.getOrDefault(group, new HashSet<>()).remove(permission);
        
        // Atualiza as permissões de todos os jogadores no grupo
        for (UUID playerId : playerGroups.keySet()) {
            if (playerGroups.get(playerId).contains(group)) {
                updatePlayerPermissions(Bukkit.getPlayer(playerId));
            }
        }
    }

    public void addPlayerToGroup(UUID playerId, String group) {
        Player player = Bukkit.getPlayer(playerId);
        if (player != null && !player.hasPermission("sensitive.group." + group)) {
            return;
        }

        playersCollection.updateOne(
            new Document("uuid", playerId.toString()),
            Updates.addToSet("groups", group),
            new UpdateOptions().upsert(true)
        );
        
        playerGroups.computeIfAbsent(playerId, k -> new HashSet<>()).add(group);
        if (player != null) {
            updatePlayerPermissions(player);
            // Atualiza tags
            plugin.getChatManager().updatePlayerPrefix(player);
            plugin.getTabListManager().updatePlayerTeam(player);
        }
    }

    public void removePlayerFromGroup(UUID playerId, String group) {
        playersCollection.updateOne(
            new Document("uuid", playerId.toString()),
            Updates.pull("groups", group)
        );
        
        playerGroups.getOrDefault(playerId, new HashSet<>()).remove(group);
        Player player = Bukkit.getPlayer(playerId);
        if (player != null) {
            updatePlayerPermissions(player);
            // Atualiza tags
            plugin.getChatManager().updatePlayerPrefix(player);
            plugin.getTabListManager().updatePlayerTeam(player);
        }
    }

    public void addPlayerPermission(UUID playerId, String permission) {
        // Atualiza no MongoDB
        playersCollection.updateOne(
            new Document("uuid", playerId.toString()),
            Updates.addToSet("permissions", permission),
            new UpdateOptions().upsert(true)
        );
        
        // Atualiza o cache local
        playerPermissions.computeIfAbsent(playerId, k -> new HashSet<>()).add(permission);
        
        // Atualiza as permissões do jogador se estiver online
        Player player = Bukkit.getPlayer(playerId);
        if (player != null) {
            plugin.getLogger().info("Adicionando permissão '" + permission + "' ao jogador " + player.getName());
            SensitivePermissionAttachment attachment = sensitiveAttachments.get(playerId);
            if (attachment != null) {
                attachment.recalculatePermissions();
            }
            player.recalculatePermissions();
        }
    }

    public void removePlayerPermission(UUID playerId, String permission) {
        // Atualiza no MongoDB
        playersCollection.updateOne(
            new Document("uuid", playerId.toString()),
            Updates.pull("permissions", permission)
        );
        
        // Atualiza o cache local
        Set<String> perms = playerPermissions.get(playerId);
        if (perms != null) {
            perms.remove(permission);
        }
        
        // Atualiza as permissões do jogador se estiver online
        Player player = Bukkit.getPlayer(playerId);
        if (player != null) {
            SensitivePermissionAttachment attachment = sensitiveAttachments.get(playerId);
            if (attachment != null) {
                attachment.recalculatePermissions();
            }
            player.recalculatePermissions();
        }
    }

    public void loadPlayer(Player player) {
        UUID playerId = player.getUniqueId();
        
        // Remove attachment antigo se existir
        unloadPlayer(player);
        
        // Carrega dados do MongoDB
        Document playerDoc = playersCollection.find(new Document("uuid", playerId.toString())).first();
        
        // Inicializa as coleções
        Set<String> groups = new HashSet<>();
        Set<String> permissions = new HashSet<>();
        
        if (playerDoc != null) {
            // Carrega grupos do jogador
            List<String> groupsList = playerDoc.get("groups", List.class);
            if (groupsList != null) {
                groups.addAll(groupsList);
            }
            
            // Carrega permissões individuais
            List<String> permissionsList = playerDoc.get("permissions", List.class);
            if (permissionsList != null) {
                permissions.addAll(permissionsList);
                plugin.getLogger().info("[DEBUG] Carregando permissões para " + player.getName() + ": " + permissions);
            }
        }
        
        // Atualiza os caches locais
        playerGroups.put(playerId, groups);
        playerPermissions.put(playerId, permissions);
        
        // Cria e configura o attachment personalizado
        SensitivePermissionAttachment attachment = new SensitivePermissionAttachment(plugin, player);
        sensitiveAttachments.put(playerId, attachment);
        attachment.recalculatePermissions();

        // Debug: Verifica se a permissão está presente
        plugin.getLogger().info("[DEBUG] Verificando permissão gamemode para " + player.getName() + ": " + hasPermission(player, "sensitive.gamemode"));
        plugin.getLogger().info("[DEBUG] Permissões atuais: " + getPlayerPermissions(playerId));

        // Atualiza tags
        plugin.getChatManager().updatePlayerPrefix(player);
        plugin.getTabListManager().updatePlayerTeam(player);
    }

    public void unloadPlayer(Player player) {
        UUID playerId = player.getUniqueId();
        
        // Remove o attachment personalizado
        SensitivePermissionAttachment attachment = sensitiveAttachments.remove(playerId);
        if (attachment != null) {
            attachment.cleanup();
        }
        
        playerGroups.remove(playerId);
        playerPermissions.remove(playerId);
    }

    private void updatePlayerPermissions(Player player) {
        if (player == null) return;
        
        UUID playerId = player.getUniqueId();
        SensitivePermissionAttachment attachment = sensitiveAttachments.get(playerId);
        if (attachment != null) {
            attachment.recalculatePermissions();
        }
    }

    public boolean hasPermission(Player player, String permission) {
        UUID playerId = player.getUniqueId();
        SensitivePermissionAttachment attachment = sensitiveAttachments.get(playerId);
        return attachment != null && attachment.hasPermission(permission);
    }

    public Set<String> getGroupPermissions(String group) {
        return new HashSet<>(groupPermissions.getOrDefault(group, new HashSet<>()));
    }

    public Set<String> getPlayerPermissions(UUID playerId) {
        return new HashSet<>(playerPermissions.getOrDefault(playerId, new HashSet<>()));
    }

    public String getGroupPrefix(String group) {
        return groupPrefixes.get(group);
    }

    public Set<String> getAllGroups() {
        return new HashSet<>(groupPermissions.keySet());
    }

    public void setGroupPrefix(String group, String prefix) {
        groupsCollection.updateOne(
            new Document("name", group),
            Updates.set("prefix", prefix)
        );
        groupPrefixes.put(group, prefix);

        // Atualiza as tags de todos os jogadores no grupo
        for (UUID playerId : playerGroups.keySet()) {
            if (playerGroups.get(playerId).contains(group)) {
                Player player = Bukkit.getPlayer(playerId);
                if (player != null) {
                    plugin.getChatManager().updatePlayerPrefix(player);
                    plugin.getTabListManager().updatePlayerTeam(player);
                }
            }
        }
    }

    public void setGroupWeight(String group, int weight) {
        groupsCollection.updateOne(
            new Document("name", group),
            Updates.set("weight", weight)
        );
        groupWeights.put(group, weight);

        // Atualiza a tablist de todos os jogadores no grupo
        for (UUID playerId : playerGroups.keySet()) {
            if (playerGroups.get(playerId).contains(group)) {
                Player player = Bukkit.getPlayer(playerId);
                if (player != null) {
                    plugin.getTabListManager().updatePlayerTeam(player);
                }
            }
        }
    }

    public int getGroupWeight(String group) {
        return groupWeights.getOrDefault(group, 0);
    }

    public String getHighestGroup(UUID playerId) {
        List<Group> groups = getPlayerGroups(playerId);
        if (groups.isEmpty()) return null;

        return groups.stream()
            .max((g1, g2) -> Integer.compare(g1.getWeight(), g2.getWeight()))
            .map(Group::getName)
            .orElse(null);
    }

    public boolean canManageGroup(Player admin, String targetGroup) {
        if (admin.isOp()) return true;

        // Pega o grupo mais alto do admin
        String adminHighestGroup = getHighestGroup(admin.getUniqueId());
        if (adminHighestGroup == null) return false;

        int adminWeight = getGroupWeight(adminHighestGroup);
        int targetWeight = getGroupWeight(targetGroup);

        // Admin só pode gerenciar grupos com peso menor que o seu
        return adminWeight > targetWeight;
    }

    public List<Group> getPlayerGroups(UUID playerId) {
        Set<String> groupNames = playerGroups.getOrDefault(playerId, new HashSet<>());
        List<Group> playerGroups = new ArrayList<>();
        
        for (String groupName : groupNames) {
            Group group = groups.get(groupName.toLowerCase());
            if (group != null) {
                playerGroups.add(group);
            }
        }
        
        return playerGroups;
    }
} 