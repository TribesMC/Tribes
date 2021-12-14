package me.rey.clans.features.punishments;

import me.rey.clans.Tribes;
import me.rey.clans.utils.UtilTime;
import me.rey.core.players.User;
import me.rey.core.utils.Activatable;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PunishmentManager implements Activatable {

    @Override
    public void onEnable() {

    }

    @Override
    public void onDisable() {

    }

    public List<Punishment> getPunishments(UUID uuid) {
        return getPunishments(uuid, -1);
    }

    public List<Punishment> getPunishments(UUID uuid, int limit) {
        return Tribes.getInstance().getSQLManager().getPunishments(uuid, limit);
    }

    public void addPunishment(Punishment punishment, boolean sendMessage, boolean sendPublicMessage) {
        Tribes.getInstance().getSQLManager().uploadPunishment(punishment);
        List<UUID> publicMessageBlacklist = new ArrayList<>();
        if (sendMessage) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!player.isOp()) continue; //todo do this properly
                if (sendPublicMessage) publicMessageBlacklist.add(player.getUniqueId());
                String punisher = "console";
                if (punishment.getStaff().toLowerCase().startsWith("uuid:")) {
                    punisher = Bukkit.getOfflinePlayer(UUID.fromString(punishment.getStaff().substring(5))).getName();
                }
                String duration;
                if (punishment.getHours() > 0) {
                    duration = "&r for &s" + UtilTime.convert((long) punishment.getHours() * 3600000, 0, UtilTime.getBestUnit((long) punishment.getHours() * 3600000)) + " " + UtilTime.getBestUnit((long) punishment.getHours() * 3600000).name().toLowerCase();
                } else {
                    duration = "permanently";
                }
                new User(player).sendMessageWithPrefix("Punish", "&s" + Bukkit.getOfflinePlayer(punishment.getPlayer()).getName() + " &rhas been " + punishment.getPunishmentType().pastTense() + " by &s" + punisher + " &r for &s" + punishment.getReason() + duration + "&r!");
            }
        }
        if (sendPublicMessage) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (publicMessageBlacklist.contains(player.getUniqueId())) continue;
                String duration;
                if (punishment.getHours() > 0) {
                    duration = "&r for &s" + UtilTime.convert((long) punishment.getHours() * 3600000, 0, UtilTime.getBestUnit((long) punishment.getHours() * 3600000)) + " " + UtilTime.getBestUnit((long) punishment.getHours() * 3600000).name().toLowerCase();
                } else {
                    duration = "permanently";
                }
                new User(player).sendMessageWithPrefix("Punish", "&s" + Bukkit.getOfflinePlayer(punishment.getPlayer()).getName() + " &rhas been " + punishment.getPunishmentType().pastTense() + " for &s" + punishment.getReason() + duration + "&r!");
            }
        }
    }
}
