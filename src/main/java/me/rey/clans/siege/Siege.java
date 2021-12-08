package me.rey.clans.siege;

import me.rey.clans.Tribes;
import me.rey.clans.clans.Clan;
import me.rey.clans.clans.ClansPlayer;
import me.rey.clans.utils.References;
import me.rey.clans.utils.UtilText;
import me.rey.core.packets.Title;
import me.rey.core.utils.Text;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class Siege {

    // Sieger -> Set<Clans they siege>
    public static HashMap<UUID, ArrayList<Siege>> sieges = new HashMap<>();

    private final Clan sieger;
	private final Clan sieged;
    private final SiegeRunnable runnable;
    private final long time;

    public Siege(final Clan sieger, final Clan sieged, final long timeIssued) {
        this.sieger = sieger;
        this.sieged = sieged;
        this.time = timeIssued;

		this.runnable = new SiegeRunnable(sieger, sieged, this);
    }

    public String getRemainingString(final long timeCurrent) {
        boolean seconds = false;
        double remainingMinutes = (double) References.SIEGE_MINUTES - ((double) ((timeCurrent - this.time) / 1000.0 / 60.0));
        if (remainingMinutes <= 1) {
            remainingMinutes = remainingMinutes * 60.0;
            seconds = true;
        }

        return String.format("%.1f", remainingMinutes <= 0 ? 0.0 : remainingMinutes) + " " + (seconds ? "Seconds" : "Minutes");
    }

    public void end() {
        if (this.runnable != null) {
			this.runnable.cancel();
		}

        final ArrayList<Siege> toRemove = sieges.get(this.sieger.getUniqueId()) == null ? new ArrayList<>() : sieges.get(this.sieger.getUniqueId());
        toRemove.remove(this);
        sieges.put(this.sieger.getUniqueId(), toRemove);
		this.runnable.cancel();

        final Clan newSieged = Tribes.getInstance().getClan(this.sieged.getUniqueId());
        final Clan newSieger = Tribes.getInstance().getClan(this.sieger.getUniqueId());

        try {
            for (final UUID uuid : newSieged.getPlayers().keySet()) {
                final ClansPlayer cp = new ClansPlayer(uuid);
                if (!cp.isOnline()) {
					continue;
				}
                new Title(Text.color("&qSIEGE ENDED!"), Text.color("&rBy: &e" + this.sieger.getName()), 2, 2 * 20, 5).send(cp.getPlayer());
            }

            for (final UUID uuid : newSieger.getPlayers().keySet()) {
                final ClansPlayer cp = new ClansPlayer(uuid);
                if (!cp.isOnline()) {
					continue;
				}
                new Title(Text.color("&qSIEGE ENDED!"), Text.color("&rOn: &e" + this.sieged.getName()), 2, 2 * 20, 5).send(cp.getPlayer());
            }
        } catch (final Exception ignore) {

        }
    }

    public void start() {

        final ArrayList<Siege> currentlySieging = sieges.get(this.sieger.getUniqueId()) == null ? new ArrayList<>() : sieges.get(this.sieger.getUniqueId());

        if (currentlySieging.contains(this)) {
			return;
		}
        currentlySieging.add(this);

        Siege.sieges.put(this.sieger.getUniqueId(), currentlySieging);

		this.runnable.start();

        for (final UUID uuid : this.sieger.getPlayers().keySet()) {
            final ClansPlayer cp = new ClansPlayer(uuid);
            if (!cp.isOnline()) {
				continue;
			}
            new Title(Text.color("&c&lSIEGE STARTED!"), Text.color("&rNow sieging: &e" + this.sieged.getName()), 2, 5 * 20, 20).send(cp.getPlayer());
        }

        for (final UUID uuid : this.sieged.getPlayers().keySet()) {
            final ClansPlayer cp = new ClansPlayer(uuid);
            if (!cp.isOnline()) {
				continue;
			}
            new Title(Text.color("&c&lSIEGE STARTED!"), Text.color("&rBy: &s" + this.sieger.getName()), 2, 5 * 20, 20).send(cp.getPlayer());
        }

        UtilText.announceToServer("Siege", "&9" + this.sieger.getName() + " &7has started a siege on &9" + this.sieged.getName() + "&7!");

        new BukkitRunnable() {

            @Override
            public void run() {
				Siege.this.end();
            }

        }.runTaskLater(Tribes.getInstance().getPlugin(), (References.SIEGE_MINUTES * 60) * 20);

    }

    public Clan getClanSieging() {
        return this.sieger;
    }

    public Clan getClanSieged() {
        return this.sieged;
    }

}
