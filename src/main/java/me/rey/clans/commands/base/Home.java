package me.rey.clans.commands.base;

import me.rey.Main;
import me.rey.clans.clans.Clan;
import me.rey.clans.clans.ClansPlayer;
import me.rey.clans.clans.ClansRank;
import me.rey.clans.commands.ClansCommand;
import me.rey.clans.commands.SubCommand;
import me.rey.clans.enums.CommandType;
import me.rey.clans.utils.References;
import me.rey.core.packets.Title;
import me.rey.core.utils.Text;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class Home extends SubCommand implements Listener {

    private static final HashMap<UUID, Long> teleportLog = new HashMap<>();
    private static final ArrayList<UUID> teleporting = new ArrayList<>();
    private final double homeTimer = 15.00;
    private final double homeTimerSafe = 7.00;

    public Home() {
        super("home", "Teleport to Clan home", "/c home", ClansRank.RECRUIT, CommandType.CLAN, true);
        Bukkit.getServer().getPluginManager().registerEvents(this, JavaPlugin.getPlugin(Main.class));
    }

    @Override
    public void build(final ClansCommand source, final CommandSender sender, final String[] args) {
        final ClansPlayer cp = new ClansPlayer((Player) sender);
        final Clan self = cp.getClan();

        if (self.getHome() == null) {
            cp.sendMessageWithPrefix("Tribe", "Your clan home is not set!");
            return;
        }

        if (cp.isInCombat()) {
            cp.sendMessageWithPrefix("Error", "You cannot teleport while in combat!");
            return;
        }

        if (teleporting.contains(cp.getUniqueId())) {
            cp.sendMessageWithPrefix("Error", "You are already teleporting!");
            return;
        }

        if (teleportLog.containsKey(cp.getUniqueId())) {
            final double cooldownTimeLeft = References.CLAN_HOME_COOLDOWN_SECONDS - (System.currentTimeMillis() - teleportLog.get(cp.getUniqueId())) / 1000D;

            if (cooldownTimeLeft <= 0) {
                teleportLog.remove(cp.getUniqueId());
            } else {
                final String timeLeft;

                if (cooldownTimeLeft > (60 * 60)) {
                    timeLeft = String.format("%.1f hours", cooldownTimeLeft / 60 / 60D);
                } else if (cooldownTimeLeft > 60) {
                    timeLeft = String.format("%.1f minutes", cooldownTimeLeft / 60);
                } else {
                    timeLeft = String.format("%.1f seconds", cooldownTimeLeft);
                }

                cp.sendMessageWithPrefix("Error", String.format("You cannot teleport home for another &g%s&7.", timeLeft));
                return;
            }
        }

        final Location home = self.getHome();
        home.setX(home.getBlockX() + 0.5);
        home.setY(home.getBlockY() + 1);
        home.setZ(home.getBlockZ() + 0.5);

        final UUID uuid = cp.getUniqueId();
        teleporting.add(uuid);

        final double decrement = 0.1;
        new BukkitRunnable() {

            double timer = cp.isInSafeZone() ? Home.this.homeTimerSafe : Home.this.homeTimer;

            @Override
            public void run() {
                if (!teleporting.contains(uuid)) {
                    new Title("", Text.color("&cTeleport cancelled."), 0, (int) (decrement * 20) + 1, 20).send((Player) sender);
                    this.cancel();
                    return;
                }

                if (this.timer <= 0) {
                    ((Player) sender).teleport(home);
                    cp.sendMessageWithPrefix("Tribe", "You have teleported to your Clan home.");
                    teleporting.remove(uuid);
                    teleportLog.put(uuid, System.currentTimeMillis());
                    this.cancel();
                    return;
                }

                final String format = String.format("%.1f", this.timer);
                new Title("", Text.color("Teleporting in &a" + format + " &fseconds."), 0, (int) (decrement * 20) + 1, 0).send((Player) sender);
                this.timer = this.timer - decrement;
            }

        }.runTaskTimer(JavaPlugin.getPlugin(Main.class), 0, (int) (decrement * 20));
    }

    @EventHandler
    public void onHomeCancel(final PlayerMoveEvent e) {
        if (!teleporting.contains(e.getPlayer().getUniqueId())) {
            return;
        }
        if (e.getTo().getBlockX() == e.getFrom().getBlockX() && e.getTo().getBlockY() == e.getFrom().getBlockY() && e.getTo().getBlockZ() == e.getFrom().getBlockZ()) {
            return;
        }

        teleporting.remove(e.getPlayer().getUniqueId());
    }

    @Override
    public SubCommand[] getChilds() {
        return new SubCommand[]{};
    }

}
