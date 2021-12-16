package me.rey.clans.features.punishments;

import me.rey.clans.Tribes;
import me.rey.clans.events.PunishmentsUpdateEvent;
import me.rey.clans.utils.UtilTime;
import me.rey.core.players.User;
import me.rey.core.utils.Activatable;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class PunishmentManager implements Activatable {

    Map<UUID, List<Punishment>> punishments;

    @Override
    public void onEnable() {
        punishments = new HashMap<>();
    }

    @Override
    public void onDisable() {
        punishments.clear();
        punishments = null;
    }

    public List<Punishment> getPunishments(UUID uuid) {
        return getPunishments(uuid, -1, true);
    }

    public List<Punishment> getPunishments(UUID uuid, int limit, boolean updateCache) {
        List<Punishment> punishments = Tribes.getInstance().getSQLManager().getPunishments(uuid, limit);
        if (updateCache) {
            this.punishments.put(uuid, punishments);
            Bukkit.getPluginManager().callEvent(new PunishmentsUpdateEvent(uuid, punishments));
        }
        return punishments;
    }

    public void addPunishment(Punishment punishment, boolean sendMessage, boolean sendPublicMessage) {
        Tribes.getInstance().getSQLManager().uploadPunishment(punishment);
        List<UUID> publicMessageBlacklist = new ArrayList<>();
        if (sendMessage) {
            String message = getStaffMessage(punishment);
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!player.isOp()) continue; //todo do this properly
                if (sendPublicMessage) publicMessageBlacklist.add(player.getUniqueId());
                new User(player).sendMessageWithPrefix("Punish", message);
            }
        }
        if (sendPublicMessage) {
            String message = getPublicMessage(punishment);
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (publicMessageBlacklist.contains(player.getUniqueId())) continue;
                new User(player).sendMessageWithPrefix("Punish", message);
            }
        }
        punishments.put(punishment.getPlayer(), getPunishments(punishment.getPlayer()));
    }

    public void removePunishment(Punishment punishment) {
        removePunishment(punishment, null);
    }

    public void removePunishment(Punishment punishment, Player player) {
        Tribes.getInstance().getSQLManager().removePunishment(punishment);
        new User(player).sendMessageWithPrefix("Punish", "Successfully removed &s" + Bukkit.getOfflinePlayer(punishment.getPlayer()).getName() + "&r's punishment!");
        punishments.put(punishment.getPlayer(), getPunishments(punishment.getPlayer()));
    }

    public void reapplyPunishment(Punishment punishment) {
        reapplyPunishment(punishment, null);
    }

    public void reapplyPunishment(Punishment punishment, Player player) {
        Tribes.getInstance().getSQLManager().reapplyPunishment(punishment);
        new User(player).sendMessageWithPrefix("Punish", "Successfully reapplied &s" + Bukkit.getOfflinePlayer(punishment.getPlayer()).getName() + "&r's punishment!");
        punishments.put(punishment.getPlayer(), getPunishments(punishment.getPlayer()));
    }

    public String getStaffMessage(Punishment punishment) {
        String punisher;
        if (punishment.getStaff().toLowerCase().startsWith("uuid:")) {
            punisher = Bukkit.getOfflinePlayer(UUID.fromString(punishment.getStaff().substring(5))).getName();
        } else {
            punisher = punishment.getStaff();
        }

        if (punishment.getCategory() != PunishmentCategory.WARN && punishment.getCategory() != PunishmentCategory.KICK) {
            String duration;
            if (punishment.getHours() > 0) {
                if (punishment.getHours() > 24.0d) {
                    duration = "&r for &s" + UtilTime.convert((long) punishment.getHours() * 3600000, 0, UtilTime.TimeUnit.DAYS) + " days";
                } else {
                    duration = "&r for &s" + UtilTime.convert((long) punishment.getHours() * 3600000, 0, UtilTime.getBestUnit((long) punishment.getHours() * 3600000)) + " " + UtilTime.getBestUnit((long) punishment.getHours() * 3600000).name().toLowerCase();
                }
            } else {
                duration = "&r permanently";
            }

            return "&s" + Bukkit.getOfflinePlayer(punishment.getPlayer()).getName() + " &rhas been " + punishment.getPunishmentType().pastTense() + " by &s" + punisher + "&r for &s" + punishment.getReason() + duration + "&r!";
        } else {
            return "&s" + Bukkit.getOfflinePlayer(punishment.getPlayer()).getName() + " &rhas been " + punishment.getPunishmentType().pastTense() + " by &s" + punisher + "&r for &s" + punishment.getReason() + "&r!";
        }
    }

    public String getPublicMessage(Punishment punishment) {
        String duration;
        if (punishment.getHours() > 0) {
            if (punishment.getHours() > 24.0d) {
                duration = "&r for &s" + UtilTime.convert((long) punishment.getHours() * 3600000, 0, UtilTime.TimeUnit.DAYS) + " days";
            } else {
                duration = "&r for &s" + UtilTime.convert((long) punishment.getHours() * 3600000, 0, UtilTime.getBestUnit((long) punishment.getHours() * 3600000)) + " " + UtilTime.getBestUnit((long) punishment.getHours() * 3600000).name().toLowerCase();
            }
        } else {
            duration = "&r permanently";
        }

        return "&s" + Bukkit.getOfflinePlayer(punishment.getPlayer()).getName() + " &rhas been " + punishment.getPunishmentType().pastTense() + " for &s" + punishment.getReason() + duration + "&r!";
    }
}
