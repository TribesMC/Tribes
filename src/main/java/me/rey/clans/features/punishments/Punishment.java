package me.rey.clans.features.punishments;

import me.rey.clans.Tribes;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
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
    private long removedAt;
    private long reappliedAt;
    private String removeStaff;
    private String removeReason; // start uuids with 'UUID:'
    private String reapplyStaff;
    private String reapplyReason; // start uuids with 'UUID:'

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

    private Punishment(Punishment punishment) {
        this.id = punishment.id;
        this.player = punishment.player;
        this.punishmentType = punishment.punishmentType;
        this.category = punishment.category;
        this.reason = punishment.reason;
        this.staff = punishment.staff;
        this.hours = punishment.hours;
        this.severity = punishment.severity;
        this.time = punishment.time;
        this.removed = punishment.removed;
        this.removeStaff = punishment.removeStaff;
        this.removeReason = punishment.removeReason;
        this.removedAt = punishment.removedAt;
        this.reapplyStaff = punishment.reapplyStaff;
        this.reapplyReason = punishment.reapplyReason;
        this.reappliedAt = punishment.reappliedAt;
    }

    public void remove(Player player, String reason) {
        if (!isActive()) return;
        this.removeStaff = "UUID:" + player.getUniqueId();
        this.removeReason = reason;
        this.removedAt = System.currentTimeMillis();
        this.removed = true;
        Tribes.getInstance().getPunishmentManager().removePunishment(this, player);
    }

    public void remove(String staff, String reason) {
        if (!isActive()) return;
        this.removeStaff = staff;
        this.removeReason = reason;
        this.removedAt = System.currentTimeMillis();
        this.removed = true;
        Tribes.getInstance().getPunishmentManager().removePunishment(this);
    }

    public void reapply(Player player, String reason) {
        if (isActive()) return;
        if (hours > 0 && time + (hours * 3600000) < System.currentTimeMillis()) return;
        this.reapplyStaff = player.getUniqueId().toString();
        this.reapplyReason = reason;
        this.reappliedAt = System.currentTimeMillis();
        Tribes.getInstance().getPunishmentManager().reapplyPunishment(this, player);
    }

    public void reapply(String staff, String reason) {
        if (isActive()) return;
        if (hours > 0 && time + (hours * 3600000) < System.currentTimeMillis()) return;
        this.reapplyStaff = staff;
        this.reapplyReason = reason;
        this.reappliedAt = System.currentTimeMillis();
        Tribes.getInstance().getPunishmentManager().reapplyPunishment(this);
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
        return !(hours > 0) || (time + (hours * 3600000) > System.currentTimeMillis());
    }

    public boolean isRemoved() {
        return removed || (hours > 0 && (time + (hours * 3600000) > System.currentTimeMillis()));
    }

    public boolean wasRemoved() {
        return removed;
    }

    public boolean wasRemovedPreviously() {
        return removeStaff != null;
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

    @Override
    public String toString() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("player", player);
        map.put("punishmentType", punishmentType);
        map.put("category", category);
        map.put("reason", reason);
        map.put("staff", staff);
        map.put("hours", hours);
        map.put("severity", severity);
        map.put("time", time);
        map.put("removed", removed);
        map.put("removedAt", removedAt);
        map.put("reappliedAt", reappliedAt);
        map.put("removeStaff", removeStaff);
        map.put("removeReason", removeReason);
        map.put("reapplyStaff", reapplyStaff);
        map.put("reapplyReason", reapplyReason);
        return map.toString();
    }
}
