package org.zerolegion.sp_core.permissions;

import java.util.HashSet;
import java.util.Set;

public class Group {
    private String name;
    private String prefix;
    private int weight;
    private Set<String> permissions;

    public Group(String name, String prefix, int weight) {
        this.name = name;
        this.prefix = prefix;
        this.weight = weight;
        this.permissions = new HashSet<>();
    }

    public String getName() {
        return name;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public Set<String> getPermissions() {
        return new HashSet<>(permissions);
    }

    public void addPermission(String permission) {
        permissions.add(permission);
    }

    public void removePermission(String permission) {
        permissions.remove(permission);
    }
} 