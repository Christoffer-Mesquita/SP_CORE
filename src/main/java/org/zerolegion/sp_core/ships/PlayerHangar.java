package org.zerolegion.sp_core.ships;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerHangar {
    private final UUID ownerId;
    private int level;
    private final List<PlayerShip> ships;

    public PlayerHangar(UUID ownerId, int level) {
        this.ownerId = ownerId;
        this.level = level;
        this.ships = new ArrayList<>();
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = Math.max(1, level);
    }

    public List<PlayerShip> getShips() {
        return new ArrayList<>(ships);
    }

    public void addShip(PlayerShip ship) {
        ships.add(ship);
    }

    public boolean removeShip(PlayerShip ship) {
        return ships.remove(ship);
    }

    public PlayerShip getShip(int index) {
        if (index >= 0 && index < ships.size()) {
            return ships.get(index);
        }
        return null;
    }

    public PlayerShip getShipByName(String name) {
        return ships.stream()
            .filter(ship -> ship.getName().equalsIgnoreCase(name))
            .findFirst()
            .orElse(null);
    }

    public boolean hasSpace() {
        // O número máximo de naves é determinado pelo nível do hangar
        // Este valor deve ser configurável
        return ships.size() < (level * 2);
    }

    public void upgradeHangar() {
        level++;
    }
} 