package me.rey.clans.features.punishments;

import me.rey.clans.Tribes;
import me.rey.clans.events.PunishmentsUpdateEvent;
import me.rey.clans.features.punishments.gui.PunishMenu;
import me.rey.clans.utils.UtilTime;
import me.rey.core.players.User;
import me.rey.core.utils.Activatable;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.util.*;

public class PunishmentManager implements Activatable, Listener {

    Map<UUID, List<Punishment>> punishments;
    public String appealUrl = "Fuck you.";

    @Override
    public void onEnable() {
        punishments = new HashMap<>();
        Bukkit.getPluginManager().registerEvents(this, Tribes.getInstance().getPlugin());
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
        OfflinePlayer target = Bukkit.getOfflinePlayer(punishment.getPlayer());
        if (target.isOnline()) {
            if (punishment.getPunishmentType() == PunishmentType.KICK) {
                String reason = ChatColor.RED + "" + ChatColor.BOLD + "You have been kicked" +
                        "\n" + ChatColor.WHITE + punishment.getReason() +
                        "\n" + ChatColor.DARK_GREEN + "Continuing to break the rules will result in a ban!";
                target.getPlayer().kickPlayer(reason);
                return;
            } else if (punishment.getPunishmentType() == PunishmentType.BAN || punishment.getPunishmentType() == PunishmentType.IPBAN) {
                String duration;
                if (punishment.getHours() > 0) {
                    long calc = (long) (punishment.getTime() + (punishment.getHours() * 3600000)) - System.currentTimeMillis();
                    if (punishment.getHours() > 24.0d) {
                        duration = "for " + UtilTime.convert(calc, 0, UtilTime.TimeUnit.DAYS) + " days";
                    } else {
                        duration = "for " + UtilTime.convert(calc, 0, UtilTime.getBestUnit(calc)) + " " + UtilTime.getBestUnit(calc).name().toLowerCase();
                    }
                } else {
                    duration = "permanently";
                }

                String reason = ChatColor.RED + "" + ChatColor.BOLD + "You are banned " + duration +
                        "\n" + ChatColor.WHITE + punishment.getReason() +
                        "\n" + ChatColor.DARK_GREEN + "Want to appeal? " + ChatColor.GREEN + appealUrl;
                target.getPlayer().kickPlayer(reason);
            } else if (punishment.getPunishmentType() == PunishmentType.MUTE) {
                String duration;
                if (punishment.getHours() > 0) {
                    long calc = (long) (punishment.getTime() + (punishment.getHours() * 3600000)) - System.currentTimeMillis();
                    if (punishment.getHours() > 24.0d) {
                        duration = "for " + UtilTime.convert(calc, 0, UtilTime.TimeUnit.DAYS) + " days";
                    } else {
                        duration = "for " + UtilTime.convert(calc, 0, UtilTime.getBestUnit(calc)) + " " + UtilTime.getBestUnit(calc).name().toLowerCase();
                    }
                } else {
                    duration = "permanently";
                }

                String reason = "\n" + ChatColor.RED + "" + ChatColor.BOLD + "You have been muted " + duration +
                        "\n" + ChatColor.WHITE + punishment.getReason() +
                        "\n" + ChatColor.DARK_GREEN + "Want to appeal? " + ChatColor.GREEN + appealUrl +
                        "\n";
                for (String msg : reason.split("\n")) {
                    target.getPlayer().sendMessage(msg);
                }
            } else if (punishment.getPunishmentType() == PunishmentType.REPORTBAN) {
                String duration;
                if (punishment.getHours() > 0) {
                    long calc = (long) (punishment.getTime() + (punishment.getHours() * 3600000)) - System.currentTimeMillis();
                    if (punishment.getHours() > 24.0d) {
                        duration = "for " + UtilTime.convert(calc, 0, UtilTime.TimeUnit.DAYS) + " days";
                    } else {
                        duration = "for " + UtilTime.convert(calc, 0, UtilTime.getBestUnit(calc)) + " " + UtilTime.getBestUnit(calc).name().toLowerCase();
                    }
                } else {
                    duration = "permanently";
                }

                String reason = "\n" + ChatColor.RED + "" + ChatColor.BOLD + "You have been banned from reporting players " + duration +
                        "\n" + ChatColor.WHITE + punishment.getReason() +
                        "\n" + ChatColor.DARK_GREEN + "Want to appeal? " + ChatColor.GREEN + appealUrl +
                        "\n";
                for (String msg : reason.split("\n")) {
                    target.getPlayer().sendMessage(msg);
                }
            }
        }

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
        if (player != null) {
            new User(player).sendMessageWithPrefix("Punish", "Successfully removed &s" + Bukkit.getOfflinePlayer(punishment.getPlayer()).getName() + "&r's punishment!");
        }
        punishments.put(punishment.getPlayer(), getPunishments(punishment.getPlayer()));
    }

    public void reapplyPunishment(Punishment punishment) {
        reapplyPunishment(punishment, null);
    }

    public void reapplyPunishment(Punishment punishment, Player player) {
        Tribes.getInstance().getSQLManager().reapplyPunishment(punishment);
        OfflinePlayer target = Bukkit.getOfflinePlayer(punishment.getPlayer());
        if (target.isOnline()) {
            if (punishment.getPunishmentType() == PunishmentType.BAN || punishment.getPunishmentType() == PunishmentType.IPBAN) {
                String duration;
                if (punishment.getHours() > 0) {
                    long calc = (long) (punishment.getTime() + (punishment.getHours() * 3600000)) - System.currentTimeMillis();
                    if (punishment.getHours() > 24.0d) {
                        duration = "for " + UtilTime.convert(calc, 0, UtilTime.TimeUnit.DAYS) + " days";
                    } else {
                        duration = "for " + UtilTime.convert(calc, 0, UtilTime.getBestUnit(calc)) + " " + UtilTime.getBestUnit(calc).name().toLowerCase();
                    }
                } else {
                    duration = "permanently";
                }

                String reason = ChatColor.RED + "" + ChatColor.BOLD + "You are banned " + duration +
                        "\n" + ChatColor.WHITE + punishment.getReason() +
                        "\n" + ChatColor.DARK_GREEN + "Want to appeal? " + ChatColor.GREEN + appealUrl;
                target.getPlayer().kickPlayer(reason);
            } else if (punishment.getPunishmentType() == PunishmentType.MUTE) {
                String duration;
                if (punishment.getHours() > 0) {
                    long calc = (long) (punishment.getTime() + (punishment.getHours() * 3600000)) - System.currentTimeMillis();
                    if (punishment.getHours() > 24.0d) {
                        duration = "for " + UtilTime.convert(calc, 0, UtilTime.TimeUnit.DAYS) + " days";
                    } else {
                        duration = "for " + UtilTime.convert(calc, 0, UtilTime.getBestUnit(calc)) + " " + UtilTime.getBestUnit(calc).name().toLowerCase();
                    }
                } else {
                    duration = "permanently";
                }

                String reason = "\n" + ChatColor.RED + "" + ChatColor.BOLD + "You have been muted " + duration +
                        "\n" + ChatColor.WHITE + punishment.getReason() +
                        "\n" + ChatColor.DARK_GREEN + "Want to appeal? " + ChatColor.GREEN + appealUrl +
                        "\n";
                for (String msg : reason.split("\n")) {
                    player.sendMessage(msg);
                }
            } else if (punishment.getPunishmentType() == PunishmentType.REPORTBAN) {
                String duration;
                if (punishment.getHours() > 0) {
                    long calc = (long) (punishment.getTime() + (punishment.getHours() * 3600000)) - System.currentTimeMillis();
                    if (punishment.getHours() > 24.0d) {
                        duration = "for " + UtilTime.convert(calc, 0, UtilTime.TimeUnit.DAYS) + " days";
                    } else {
                        duration = "for " + UtilTime.convert(calc, 0, UtilTime.getBestUnit(calc)) + " " + UtilTime.getBestUnit(calc).name().toLowerCase();
                    }
                } else {
                    duration = "permanently";
                }

                String reason = "\n" + ChatColor.RED + "" + ChatColor.BOLD + "You have been banned from reporting players " + duration +
                        "\n" + ChatColor.WHITE + punishment.getReason() +
                        "\n" + ChatColor.DARK_GREEN + "Want to appeal? " + ChatColor.GREEN + appealUrl +
                        "\n";
                for (String msg : reason.split("\n")) {
                    target.getPlayer().sendMessage(msg);
                }
            }
        }
        new User(player).sendMessageWithPrefix("Punish", "Successfully reapplied &s" + Bukkit.getOfflinePlayer(punishment.getPlayer()).getName() + "&r's punishment!");
        punishments.put(punishment.getPlayer(), getPunishments(punishment.getPlayer()));
    }

    public void deletePunishment(Punishment punishment) {
        deletePunishment(punishment, null);
    }

    public void deletePunishment(Punishment punishment, Player player) {
        Tribes.getInstance().getSQLManager().deletePunishment(punishment);
        if (player != null) {
            new User(player).sendMessageWithPrefix("Punish", "Successfully deleted &s" + Bukkit.getOfflinePlayer(punishment.getPlayer()).getName() + "&r's punishment!");
        }
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

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onLogin(AsyncPlayerPreLoginEvent event) {
        List<Punishment> punishments = this.punishments.getOrDefault(event.getUniqueId(), getPunishments(event.getUniqueId()));
        if (punishments == null || punishments.isEmpty()) return;
        for (Punishment punishment : punishments) {
            if (punishment.getPunishmentType() != PunishmentType.IPBAN && punishment.getPunishmentType() != PunishmentType.BAN) continue;
            if (!punishment.isActive()) continue;

            String duration;
            if (punishment.getHours() > 0) {
                long calc = (long) (punishment.getTime() + (punishment.getHours() * 3600000)) - System.currentTimeMillis();
                if (punishment.getHours() > 24.0d) {
                    duration = "for " + UtilTime.convert(calc, 0, UtilTime.TimeUnit.DAYS) + " days";
                } else {
                    duration = "for " + UtilTime.convert(calc, 0, UtilTime.getBestUnit(calc)) + " " + UtilTime.getBestUnit(calc).name().toLowerCase();
                }
            } else {
                duration = "permanently";
            }

            String reason = ChatColor.RED + "" + ChatColor.BOLD + "You are banned " + duration +
                    "\n" + ChatColor.WHITE + punishment.getReason() +
                    "\n" + ChatColor.DARK_GREEN + "Want to appeal?" + ChatColor.GREEN + appealUrl;
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, reason);
            return;
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    private void onChat(AsyncPlayerChatEvent event) {
        List<Punishment> punishments = this.punishments.getOrDefault(event.getPlayer().getUniqueId(), getPunishments(event.getPlayer().getUniqueId()));
        if (punishments == null || punishments.isEmpty()) return;
        for (Punishment punishment : punishments) {
            if (punishment.getPunishmentType() != PunishmentType.MUTE) continue;
            if (!punishment.isActive()) continue;

            String duration;
            if (punishment.getHours() > 0) {
                long calc = (long) (punishment.getTime() + (punishment.getHours() * 3600000)) - System.currentTimeMillis();
                if (punishment.getHours() > 24.0d) {
                    duration = "for " + UtilTime.convert(calc, 0, UtilTime.TimeUnit.DAYS) + " days";
                } else {
                    duration = "for " + UtilTime.convert(calc, 0, UtilTime.getBestUnit(calc)) + " " + UtilTime.getBestUnit(calc).name().toLowerCase();
                }
            } else {
                duration = "permanently";
            }

            new User(event.getPlayer()).sendMessageWithPrefix("Punish", "Hey! Quiet there... You're muted " + duration + " for &s" + punishment.getReason() + "&r!");
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onSignChange(SignChangeEvent event) {
        List<Punishment> punishments = this.punishments.getOrDefault(event.getPlayer().getUniqueId(), getPunishments(event.getPlayer().getUniqueId()));
        if (punishments == null || punishments.isEmpty()) return;
        for (Punishment punishment : punishments) {
            if (punishment.getPunishmentType() != PunishmentType.MUTE) continue;
            if (!punishment.isActive()) continue;

            String duration;
            if (punishment.getHours() > 0) {
                long calc = (long) (punishment.getTime() + (punishment.getHours() * 3600000)) - System.currentTimeMillis();
                if (punishment.getHours() > 24.0d) {
                    duration = "for " + UtilTime.convert(calc, 0, UtilTime.TimeUnit.DAYS) + " days";
                } else {
                    duration = "for " + UtilTime.convert(calc, 0, UtilTime.getBestUnit(calc)) + " " + UtilTime.getBestUnit(calc).name().toLowerCase();
                }
            } else {
                duration = "permanently";
            }

            new User(event.getPlayer()).sendMessageWithPrefix("Punish", "Oi you naughty muted player, you..! You're muted " + duration + " for &s" + punishment.getReason() + "&r!");
            event.setCancelled(true);
            return;
        }
    }
}
