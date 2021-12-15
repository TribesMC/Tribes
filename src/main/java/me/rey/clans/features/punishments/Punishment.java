package me.rey.clans.features.punishments;

import java.util.UUID;

public class Punishment {
    private int id;
    private final UUID player;
    private final PunishmentType punishmentType;
    private final PunishmentCategory category;
    private final String reason;
    private final String staff; // start uuids with 'UUID:'
    private final double hours;
    private final int severity;
    private final long time;
    private boolean removed; //must always be true if expiry has passed
    private final long removedAt, reappliedAt;
    private final String removeStaff, removeReason; // start uuids with 'UUID:'
    private final String reapplyStaff, reapplyReason; // start uuids with 'UUID:'

    public Punishment(int id, UUID player, PunishmentType punishmentType, PunishmentCategory category, String reason, String staff, double hours, int severity, long time, boolean removed, String removeStaff, String removeReason, long removedAt, String reapplyStaff, String reapplyReason, long reappliedAt) {
        this.id = id;
        this.player = player;
        this.punishmentType = punishmentType;
        this.category = category;
        this.reason = reason;
        this.staff = staff;
        this.hours = hours;
        this.severity = severity;
        this.time = time;
        this.removed = removed;
        this.removeStaff = removeStaff;
        this.removeReason = removeReason;
        this.removedAt = removedAt;
        this.reapplyStaff = reapplyStaff;
        this.reapplyReason = reapplyReason;
        this.reappliedAt = reappliedAt;
    }

    public Punishment(UUID player, PunishmentType punishmentType, PunishmentCategory category, String reason, String staff, double hours, int severity, long time) {
        this.id = -1;
        this.player = player;
        this.punishmentType = punishmentType;
        this.category = category;
        this.reason = reason;
        this.staff = staff;
        this.hours = hours;
        this.severity = severity;
        this.time = time;
        this.removed = false;
        this.removeStaff = null;
        this.removeReason = null;
        this.removedAt = -1;
        this.reapplyStaff = null;
        this.reapplyReason = null;
        this.reappliedAt = -1;
    }

    public boolean isReactivatable() {
        if (!isRemoved()) return false;
        if (category == PunishmentCategory.WARN) return false;
        if (category == PunishmentCategory.KICK) return false;
        if (category == PunishmentCategory.OTHER) return false;
        return !(hours > 0) || !(time + (hours * 1000) > System.currentTimeMillis());
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public UUID getPlayer() {
        return player;
    }

    public PunishmentType getPunishmentType() {
        return punishmentType;
    }

    public PunishmentCategory getCategory() {
        return category;
    }

    public String getReason() {
        return reason;
    }

    public String getStaff() {
        return staff;
    }

    public double getHours() {
        return hours;
    }

    public int getSeverity() {
        return severity;
    }

    public long getTime() {
        return time;
    }

    public boolean isActive() {
        if (removed) return false;
        if (category == PunishmentCategory.WARN) return false;
        if (category == PunishmentCategory.KICK) return false;
        if (category == PunishmentCategory.OTHER) return false;
        return !(hours > 0) || (!(time + (hours * 1000) > System.currentTimeMillis()));
    }

    public boolean isRemoved() {
        return removed || (removed = (hours > 0 && (time + (hours * 1000) > System.currentTimeMillis())));
    }

    public boolean wasRemoved() {
        return removed;
    }

    public boolean wasReactivated() {
        return reapplyStaff != null;
    }

    public boolean isReactivated() {
        return !removed && reapplyStaff != null;
    }

    public String getRemoveStaff() {
        return removeStaff;
    }

    public String getRemoveReason() {
        return removeReason;
    }

    public long getRemovedAt() {
        return removedAt;
    }

    public String getReapplyStaff() {
        return reapplyStaff;
    }

    public String getReapplyReason() {
        return reapplyReason;
    }

    public long getReappliedAt() {
        return reappliedAt;
    }
}
