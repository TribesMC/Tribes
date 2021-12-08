package me.rey.clans.siege;

import me.rey.clans.Tribes;
import me.rey.clans.clans.Clan;
import me.rey.clans.clans.ClansPlayer;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Iterator;
import java.util.UUID;

public class SiegeRunnable extends BukkitRunnable {

    private final double intervalInSeconds = 0.1;
    private final Clan sieging;
	private final Clan sieged;
    private final Siege siege;

    public SiegeRunnable(final Clan sieging, final Clan sieged, final Siege siege) {
        this.sieging = sieging;
        this.sieged = sieged;
        this.siege = siege;
    }

    public void start() {
        this.runTaskTimer(Tribes.getInstance().getPlugin(), 0, (int) (this.intervalInSeconds * 20));
    }

    @Override
    public void run() {

        final String timeRemaining = this.siege.getRemainingString(System.currentTimeMillis());

        /*
         * NOT displaying action bar if the clan is being sieged by more than 1 other clan(s)
         */

        final Clan newSieged = Tribes.getInstance().getClan(this.sieged.getUniqueId());
        final Iterator<Siege> iterator = newSieged.getClansSiegingSelf().iterator();
        if (iterator.hasNext() || !this.sieging.getUniqueId().equals(iterator.next().getClanSieging().getUniqueId())) {
            for (final UUID uuid : newSieged.getPlayers().keySet()) {

                final ClansPlayer cp = new ClansPlayer(uuid);
                if (!cp.isOnline()) {
					continue;
				}

//				NOT NEEDED
//				new ActionBar(Text.color("&cSieged by &a" + sieging.getName() + String.format(" &c(&a%s&c)", timeRemaining))).send(cp.getPlayer());
            }
        }
        // END

        /*
         * NOT displaying action bar if the clan is SIEGING more than 1 clan
         */
        final Clan newSieger = Tribes.getInstance().getClan(this.sieging.getUniqueId());
        if (newSieger.isBeingSieged()) {
			return;
		}
        final Iterator<Siege> iterator2 = newSieger.getClansSiegedBySelf().iterator();
        if (iterator2.hasNext() && this.sieged.getUniqueId().equals(iterator2.next().getClanSieged().getUniqueId())) {
            for (final UUID uuid : newSieger.getPlayers().keySet()) {

                final ClansPlayer cp = new ClansPlayer(uuid);
                if (!cp.isOnline()) {
					continue;
				}

//				NOT NEEDED
//				new ActionBar(Text.color("&cSieging &a" + sieged.getName() + String.format(" &c(&a%s&c)", timeRemaining))).send(cp.getPlayer());
            }
        }

    }

}
