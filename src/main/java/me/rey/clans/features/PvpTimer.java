package me.rey.clans.features;

import me.rey.clans.Tribes;
import me.rey.clans.clans.ClansPlayer;
import me.rey.clans.utils.UtilTime;
import me.rey.core.events.customevents.ability.AbilityInteractEvent;
import me.rey.core.events.customevents.ability.AbilityRecurEvent;
import me.rey.core.events.customevents.ability.AbilityUseEvent;
import me.rey.core.events.customevents.combat.DamagedByEntityEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class PvpTimer implements Listener {

    private static final Map<UUID, Long> wearsOff = new HashMap<>();
    private static final Map<UUID, Boolean> removables = new HashMap<>(); // complete this alongside wearsOff
    private static final Map<UUID, Long> previousSaves = new HashMap<>();
    private static final Set<UUID> messageDebounce = new HashSet<>();
    private final long defaultTime = 3600000L; // milliseconds
    private final long debounceSeconds = 5;
    // This Map saves the last millisecond a players pvp data was saved.
    // So it subtracts that from the current millisecond which gives it
    // the time the player has been on since.

    public PvpTimer() {

        // The below runnable is to safeguard in the uncommon event of a server crash.
        // It saves all needed players PVP timer data every 60 seconds (60 * 20 = 1200)
        // to prevent the SQLite from becoming overloaded which could happen if the
        // delay was significantly lower.
        new BukkitRunnable() {
            @Override
            public void run() {
                PvpTimer.this.saveAllPlayers();
            }
        }.runTaskTimer(Tribes.getInstance().getPlugin(), 1200, 1200);

        new BukkitRunnable() {
            @Override
            public void run() {
                for (final Map.Entry<UUID, Long> entry : new HashMap<>(wearsOff).entrySet()) {
                    if (!Bukkit.getOfflinePlayer(entry.getKey()).isOnline()) {
                        continue;
                    }
                    if (System.currentTimeMillis() >= entry.getValue()) {
                        PvpTimer.this.removePvpTimer(Bukkit.getOfflinePlayer(entry.getKey()), false);
                    }
                }
            }
        }.runTaskTimer(Tribes.getInstance().getPlugin(), 20, 20);
    }

    public static Map<UUID, Boolean> getRemovables() {
        return removables;
    }

    public void applyPvpTimer(final OfflinePlayer player, final long time, final boolean removable) {
        if (time <= 0) {
            this.removePvpTimer(player, false);
            return;
        }

        Tribes.getInstance().getLocalSQLiteManager().setPlayerPvpTimer(player, time, removable);
        previousSaves.put(player.getUniqueId(), System.currentTimeMillis());
        wearsOff.put(player.getUniqueId(), System.currentTimeMillis() + time);
        removables.put(player.getUniqueId(), removable);
    }

    public void removePvpTimer(final OfflinePlayer player, final boolean forcefully) {
        Tribes.getInstance().getLocalSQLiteManager().setPlayerPvpTimer(player, -1, true);
        previousSaves.remove(player.getUniqueId());
        wearsOff.remove(player.getUniqueId());
        removables.remove(player.getUniqueId());
        if (!forcefully) {
            if (player.isOnline()) {
                new ClansPlayer(player.getPlayer()).sendMessageWithPrefix("PVP Timer", "Your PVP timer has expired!");
            }
        }
    }

    public long getPvpTimer(final OfflinePlayer player) {
        return Tribes.getInstance().getLocalSQLiteManager().getPlayerPvpTimer(player);
    }

    public long getLivePvpTimer(final OfflinePlayer player) {
        return this.getPvpTimer(player) - (System.currentTimeMillis() - previousSaves.getOrDefault(player.getUniqueId(), System.currentTimeMillis()));
    }

    public void loadPlayer(final Player player) {
        if (!Tribes.getInstance().getLocalSQLiteManager().isOnPvpTimerList(player, true)) {
            this.applyPvpTimer(player, this.defaultTime, true);
            new ClansPlayer(player).sendMessageWithPrefix("PVP Timer", "You have gained a PVP timer! This will last for " + ChatColor.YELLOW + UtilTime.convert(this.defaultTime, 0, UtilTime.TimeUnit.MINUTES) + " minutes" + ChatColor.GRAY + " of ingame time!");
        } else {
            final long time = this.getPvpTimer(player);
            final boolean removable = Tribes.getInstance().getLocalSQLiteManager().isPvpTimerRemovable(player);
            if (time > 0) {
                this.applyPvpTimer(player, time, removable);
                new ClansPlayer(player).sendMessageWithPrefix("PVP Timer", "You have gained a PVP timer! This will last for " + ChatColor.YELLOW + UtilTime.convert(time, 0, UtilTime.TimeUnit.MINUTES) + " minutes" + ChatColor.GRAY + " of ingame time!");
            }
        }
    }

    public void unloadPlayer(final Player player) {
        this.saveAllPlayers();
        previousSaves.remove(player.getUniqueId());
        wearsOff.remove(player.getUniqueId());
        removables.remove(player.getUniqueId());
    }

    public void saveAllPlayers() {
        for (final Map.Entry<UUID, Long> entry : previousSaves.entrySet()) {
            final OfflinePlayer player = Bukkit.getOfflinePlayer(entry.getKey());
            Tribes.getInstance().getLocalSQLiteManager().setPlayerPvpTimer(player, this.getPvpTimer(player) - (System.currentTimeMillis() - entry.getValue()), Tribes.getInstance().getLocalSQLiteManager().isPvpTimerRemovable(player));
            if (player.isOnline()) {
                previousSaves.put(entry.getKey(), System.currentTimeMillis());
            } else {
                previousSaves.remove(entry.getKey(), entry.getValue());
            }
        }
    }

    @EventHandler
    private void damaged(final DamagedByEntityEvent e) {
        if (wearsOff.containsKey(e.getDamagee().getUniqueId()) || (e.getDamager() instanceof Player && wearsOff.containsKey(e.getDamager().getUniqueId()))) {
            e.setCancelled(true);
            if (wearsOff.containsKey(e.getDamagee().getUniqueId())) {
                if (e.getDamager() instanceof Player) {
                    new ClansPlayer((Player) e.getDamager()).sendMessageWithPrefix("PVP Timer", ChatColor.YELLOW + e.getDamagee().getName() + ChatColor.GRAY + " has an active PVP timer!");
                }
            } else {
                if (e.getDamager() instanceof Player) {
                    new ClansPlayer((Player) e.getDamager()).sendMessageWithPrefix("PVP Timer", "You cannot attack players with an active PVP timer!");
                }
            }
        }
    }

    @EventHandler
    private void ability(final AbilityInteractEvent e) {
        if (wearsOff.containsKey(e.getConjurer().getUniqueId()) || (e.getTarget() instanceof Player && wearsOff.containsKey(e.getTarget().getUniqueId()))) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    private void usingAbility(final AbilityUseEvent e) {
        if (wearsOff.containsKey(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
            new ClansPlayer(e.getPlayer()).sendMessageWithPrefix("PVP Timer", "You cannot use abilities with an active PVP timer!");
        }
    }

    @EventHandler
    private void usingAbility(final AbilityRecurEvent e) {
        if (wearsOff.containsKey(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    private void pickup(final PlayerPickupItemEvent e) {
        if (wearsOff.containsKey(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
            if (!messageDebounce.contains(e.getPlayer().getUniqueId())) {
                new ClansPlayer(e.getPlayer()).sendMessageWithPrefix("PVP Timer", "You cannot pick up items with an active PVP timer!");
                messageDebounce.add(e.getPlayer().getUniqueId());

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        messageDebounce.remove(e.getPlayer().getUniqueId());
                    }
                }.runTaskLater(Tribes.getInstance().getPlugin(), this.debounceSeconds * 20);
            }
        }
    }

    public long getDefault() {
        return this.defaultTime;
    }
}
