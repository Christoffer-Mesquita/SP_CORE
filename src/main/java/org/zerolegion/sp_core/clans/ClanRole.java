package org.zerolegion.sp_core.clans;

import org.bukkit.ChatColor;
import java.util.Set;
import java.util.HashSet;

public enum ClanRole {
    LEADER(ChatColor.DARK_RED + "Líder", 4, getAllPermissions()),
    OFFICER(ChatColor.RED + "Oficial", 3, getOfficerPermissions()),
    MEMBER(ChatColor.GREEN + "Membro", 2, getMemberPermissions()),
    RECRUIT(ChatColor.GRAY + "Recruta", 1, getRecruitPermissions());

    private final String display;
    private final int level;
    private final Set<ClanPermission> permissions;

    ClanRole(String display, int level, Set<ClanPermission> permissions) {
        this.display = display;
        this.level = level;
        this.permissions = permissions;
    }

    public String getDisplay() {
        return display;
    }

    public int getLevel() {
        return level;
    }

    public boolean hasPermission(ClanPermission permission) {
        return permissions.contains(permission);
    }

    private static Set<ClanPermission> getAllPermissions() {
        Set<ClanPermission> perms = new HashSet<>();
        for (ClanPermission perm : ClanPermission.values()) {
            perms.add(perm);
        }
        return perms;
    }

    private static Set<ClanPermission> getOfficerPermissions() {
        Set<ClanPermission> perms = new HashSet<>();
        perms.add(ClanPermission.INVITE_MEMBER);
        perms.add(ClanPermission.KICK_MEMBER);
        perms.add(ClanPermission.SET_MEMBER_ROLE);
        perms.add(ClanPermission.TELEPORT_BASE);
        perms.add(ClanPermission.BANK_DEPOSIT);
        perms.add(ClanPermission.BANK_WITHDRAW);
        perms.add(ClanPermission.MANAGE_ALLIES);
        perms.add(ClanPermission.SET_ANNOUNCEMENT);
        return perms;
    }

    private static Set<ClanPermission> getMemberPermissions() {
        Set<ClanPermission> perms = new HashSet<>();
        perms.add(ClanPermission.TELEPORT_BASE);
        perms.add(ClanPermission.BANK_DEPOSIT);
        return perms;
    }

    private static Set<ClanPermission> getRecruitPermissions() {
        Set<ClanPermission> perms = new HashSet<>();
        perms.add(ClanPermission.TELEPORT_BASE);
        return perms;
    }
} 