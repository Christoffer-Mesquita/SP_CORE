package org.zerolegion.sp_core.clans;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.zerolegion.sp_core.SP_CORE;
import java.util.*;
import java.util.stream.Collectors;

public class ClanManager {
    private final SP_CORE plugin;
    private final MongoCollection<Document> clansCollection;
    private final Map<String, SpaceClan> clans;
    private final Map<UUID, String> playerClans;

    public ClanManager(SP_CORE plugin) {
        this.plugin = plugin;
        this.clansCollection = plugin.getDatabase().getCollection("clans");
        this.clans = new HashMap<>();
        this.playerClans = new HashMap<>();
        loadAllClans();
    }

    private void loadAllClans() {
        clansCollection.find().forEach(doc -> {
            SpaceClan clan = documentToClan(doc);
            if (clan != null) {
                clans.put(clan.getId(), clan);
                clan.getMembers().keySet().forEach(uuid -> playerClans.put(uuid, clan.getId()));
            }
        });
        plugin.getLogger().info("[CLANS] Carregados " + clans.size() + " clãs");
    }

    public SpaceClan createClan(String name, String tag, Player leader) {
        // Verifica se o jogador já está em um clã
        if (playerClans.containsKey(leader.getUniqueId())) {
            return null;
        }

        // Verifica se o nome ou tag já existem
        if (clans.values().stream().anyMatch(c -> c.getName().equalsIgnoreCase(name) || c.getTag().equalsIgnoreCase(tag))) {
            return null;
        }

        String id = UUID.randomUUID().toString();
        SpaceClan clan = new SpaceClan(id, name, tag, leader.getUniqueId());
        
        saveClan(clan);
        clans.put(id, clan);
        playerClans.put(leader.getUniqueId(), id);

        return clan;
    }

    public void disbandClan(String clanId) {
        SpaceClan clan = clans.remove(clanId);
        if (clan != null) {
            clan.getMembers().keySet().forEach(playerClans::remove);
            clansCollection.deleteOne(Filters.eq("id", clanId));
        }
    }

    public boolean addMember(String clanId, UUID playerId, ClanRole role) {
        SpaceClan clan = clans.get(clanId);
        if (clan == null || playerClans.containsKey(playerId)) {
            return false;
        }

        clan.addMember(playerId, role);
        playerClans.put(playerId, clanId);
        saveClan(clan);
        return true;
    }

    public boolean removeMember(String clanId, UUID playerId) {
        SpaceClan clan = clans.get(clanId);
        if (clan == null || !clan.isMember(playerId)) {
            return false;
        }

        clan.removeMember(playerId);
        playerClans.remove(playerId);
        saveClan(clan);
        return true;
    }

    public void setBase(String clanId, Location location) {
        SpaceClan clan = clans.get(clanId);
        if (clan != null) {
            clan.setBase(location);
            saveClan(clan);
        }
    }

    public boolean teleportToBase(Player player) {
        String clanId = playerClans.get(player.getUniqueId());
        if (clanId == null) return false;

        SpaceClan clan = clans.get(clanId);
        if (clan == null || clan.getBase() == null) return false;

        if (!clan.hasPermission(player.getUniqueId(), ClanPermission.TELEPORT_BASE)) {
            player.sendMessage(ChatColor.RED + "Você não tem permissão para teleportar para a base do clã!");
            return false;
        }

        player.teleport(clan.getBase());
        return true;
    }

    public void saveClan(SpaceClan clan) {
        Document doc = new Document();
        doc.put("id", clan.getId());
        doc.put("name", clan.getName());
        doc.put("tag", clan.getTag());
        doc.put("leader", clan.getLeader().toString());
        
        if (clan.getBase() != null) {
            Location base = clan.getBase();
            doc.put("base", new Document()
                .append("world", base.getWorld().getName())
                .append("x", base.getX())
                .append("y", base.getY())
                .append("z", base.getZ())
                .append("yaw", base.getYaw())
                .append("pitch", base.getPitch()));
        }

        List<Document> memberDocs = new ArrayList<>();
        clan.getMembers().forEach((uuid, role) -> 
            memberDocs.add(new Document()
                .append("uuid", uuid.toString())
                .append("role", role.name())));
        doc.put("members", memberDocs);

        doc.put("allies", new ArrayList<>(clan.getAllies()));
        doc.put("enemies", new ArrayList<>(clan.getEnemies()));
        doc.put("power", clan.getPower());
        doc.put("bank", clan.getBank());
        doc.put("description", clan.getDescription());
        doc.put("announcement", clan.getAnnouncement());

        Document settings = new Document();
        settings.put("openJoin", clan.getSetting("openJoin"));
        settings.put("friendlyFire", clan.getSetting("friendlyFire"));
        settings.put("publicBase", clan.getSetting("publicBase"));
        settings.put("allyTeleport", clan.getSetting("allyTeleport"));
        settings.put("bankDeposit", clan.getSetting("bankDeposit"));
        settings.put("bankWithdraw", clan.getSetting("bankWithdraw"));
        doc.put("settings", settings);

        clansCollection.updateOne(
            Filters.eq("id", clan.getId()),
            new Document("$set", doc),
            new UpdateOptions().upsert(true)
        );
    }

    private SpaceClan documentToClan(Document doc) {
        try {
            String id = doc.getString("id");
            String name = doc.getString("name");
            String tag = doc.getString("tag");
            UUID leader = UUID.fromString(doc.getString("leader"));

            SpaceClan clan = new SpaceClan(id, name, tag, leader);

            Document baseDoc = (Document) doc.get("base");
            if (baseDoc != null) {
                Location base = new Location(
                    plugin.getServer().getWorld(baseDoc.getString("world")),
                    baseDoc.getDouble("x"),
                    baseDoc.getDouble("y"),
                    baseDoc.getDouble("z"),
                    baseDoc.getDouble("yaw").floatValue(),
                    baseDoc.getDouble("pitch").floatValue()
                );
                clan.setBase(base);
            }

            List<Document> memberDocs = (List<Document>) doc.get("members");
            if (memberDocs != null) {
                memberDocs.forEach(memberDoc -> {
                    UUID uuid = UUID.fromString(memberDoc.getString("uuid"));
                    ClanRole role = ClanRole.valueOf(memberDoc.getString("role"));
                    clan.addMember(uuid, role);
                });
            }

            List<String> allies = (List<String>) doc.get("allies");
            if (allies != null) allies.forEach(clan::addAlly);

            List<String> enemies = (List<String>) doc.get("enemies");
            if (enemies != null) enemies.forEach(clan::addEnemy);

            clan.setDescription(doc.getString("description"));
            clan.setAnnouncement(doc.getString("announcement"));

            Document settings = (Document) doc.get("settings");
            if (settings != null) {
                settings.forEach((key, value) -> clan.setSetting(key, (Boolean) value));
            }

            return clan;
        } catch (Exception e) {
            plugin.getLogger().severe("[CLANS] Erro ao carregar clã: " + e.getMessage());
            return null;
        }
    }

    public SpaceClan getClan(String clanId) {
        return clans.get(clanId);
    }

    public SpaceClan getPlayerClan(UUID playerId) {
        String clanId = playerClans.get(playerId);
        return clanId != null ? clans.get(clanId) : null;
    }

    public Collection<SpaceClan> getAllClans() {
        return new ArrayList<>(clans.values());
    }

    public List<SpaceClan> getTopClans(int limit) {
        return clans.values().stream()
            .sorted((c1, c2) -> Integer.compare(c2.getPower(), c1.getPower()))
            .limit(limit)
            .collect(Collectors.toList());
    }
} 