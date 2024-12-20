package org.zerolegion.sp_core.clans;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import java.util.*;

public class SpaceClan {
    private final String id;
    private String name;
    private String tag;
    private UUID leader;
    private Location base;
    private final Map<UUID, ClanRole> members;
    private final Set<String> allies;
    private final Set<String> enemies;
    private int power;
    private double bank;
    private String description;
    private String announcement;
    private final Map<String, Boolean> settings;

    public SpaceClan(String id, String name, String tag, UUID leader) {
        this.id = id;
        this.name = name;
        this.tag = tag;
        this.leader = leader;
        this.members = new HashMap<>();
        this.allies = new HashSet<>();
        this.enemies = new HashSet<>();
        this.power = 0;
        this.bank = 0;
        this.settings = new HashMap<>();
        
        // Adiciona o líder como membro
        this.members.put(leader, ClanRole.LEADER);
        
        // Configurações padrão
        initDefaultSettings();
    }

    private void initDefaultSettings() {
        settings.put("openJoin", false);          // Se o clã está aberto para novos membros
        settings.put("friendlyFire", false);      // Se membros podem se atacar
        settings.put("publicBase", false);        // Se a base é pública
        settings.put("allyTeleport", true);       // Se aliados podem teleportar para a base
        settings.put("bankDeposit", false);       // Se membros podem depositar no banco
        settings.put("bankWithdraw", false);      // Se membros podem sacar do banco
    }

    public boolean hasPermission(UUID playerId, ClanPermission permission) {
        ClanRole role = members.get(playerId);
        if (role == null) return false;
        return role.hasPermission(permission);
    }

    public boolean isMember(UUID playerId) {
        return members.containsKey(playerId);
    }

    public boolean isLeader(UUID playerId) {
        return playerId.equals(leader);
    }

    public boolean isOfficer(UUID playerId) {
        ClanRole role = members.get(playerId);
        return role == ClanRole.OFFICER;
    }

    public void addMember(UUID playerId, ClanRole role) {
        members.put(playerId, role);
        calculatePower();
    }

    public void removeMember(UUID playerId) {
        members.remove(playerId);
        calculatePower();
    }

    public void setBase(Location location) {
        this.base = location.clone();
    }

    public void addAlly(String clanId) {
        allies.add(clanId);
    }

    public void removeAlly(String clanId) {
        allies.remove(clanId);
    }

    public void addEnemy(String clanId) {
        enemies.add(clanId);
        allies.remove(clanId); // Remove dos aliados se existir
    }

    public void removeEnemy(String clanId) {
        enemies.remove(clanId);
    }

    public void deposit(double amount) {
        this.bank += amount;
    }

    public boolean withdraw(double amount) {
        if (bank >= amount) {
            bank -= amount;
            return true;
        }
        return false;
    }

    private void calculatePower() {
        // Fórmula para calcular o poder do clã
        // Base: número de membros + nível médio dos membros + recursos no banco
        this.power = members.size() * 10; // Implementação básica
    }

    // Getters e Setters
    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }
    public UUID getLeader() { return leader; }
    public void setLeader(UUID leader) { this.leader = leader; }
    public Location getBase() { return base != null ? base.clone() : null; }
    public Map<UUID, ClanRole> getMembers() { return new HashMap<>(members); }
    public Set<String> getAllies() { return new HashSet<>(allies); }
    public Set<String> getEnemies() { return new HashSet<>(enemies); }
    public int getPower() { return power; }
    public double getBank() { return bank; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getAnnouncement() { return announcement; }
    public void setAnnouncement(String announcement) { this.announcement = announcement; }
    public boolean getSetting(String key) { return settings.getOrDefault(key, false); }
    public void setSetting(String key, boolean value) { settings.put(key, value); }
} 