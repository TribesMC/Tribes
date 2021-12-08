package me.rey.clans.events;

import me.rey.clans.Tribes;
import me.rey.clans.clans.Clan;
import me.rey.clans.clans.ClanRelations;
import me.rey.clans.clans.ClansPlayer;
import me.rey.clans.database.SQLManager;
import me.rey.clans.events.clans.ClanWarpointEvent;
import me.rey.clans.siege.Siege;
import me.rey.core.events.customevents.combat.DeathEvent;
import me.rey.core.players.combat.DeathMessage;
import me.rey.core.players.combat.PlayerHit;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class PlayerDeath implements Listener {

    private final SQLManager sql = Tribes.getInstance().getSQLManager();

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDeath(final DeathEvent e) {
        e.cancelDeathMessage(true);

        final Player player = e.getPlayer();
        final ClansPlayer cp = new ClansPlayer(player);
        final PlayerHit lastBlow = e.getLastHit();
        final DeathMessage dm = e.getDeathMessage();

        for (final Player online : Bukkit.getOnlinePlayers()) {
            final ClansPlayer to = new ClansPlayer(online);
            final ClanRelations relation = cp.hasClan() ? to.hasClan() ? cp.getClan().getClanRelation(to.getClan().getUniqueId()) : ClanRelations.NEUTRAL : ClanRelations.NEUTRAL;

            dm.setPlayerName(relation.getPlayerColor() + ChatColor.stripColor(dm.getPlayerName()));

            if (lastBlow != null && lastBlow.isCausedByPlayer()) {
                final Player killer = (Player) lastBlow.getEntityCause();
                final ClansPlayer cpk = new ClansPlayer(killer);
                final ClanRelations kRelation = cpk.hasClan() ? to.hasClan() ? cpk.getClan().getClanRelation(to.getClan().getUniqueId()) : ClanRelations.NEUTRAL : ClanRelations.NEUTRAL;

                dm.setKillerName(kRelation.getPlayerColor() + ChatColor.stripColor(dm.getKillerName()));
            }

            to.sendMessage(dm.get());
        }

        if (lastBlow != null) {
            final Player k = (Player) lastBlow.getEntityCause();
            final ClansPlayer killer = new ClansPlayer(k);

            if (!killer.hasClan() || !cp.hasClan()) {
				return;
			}

            final Clan toGiveWP = killer.getClan();
            final Clan toLoseWP = cp.getClan();

            boolean isInSiege = false;
            for (final Siege siege : toGiveWP.getClansSiegedBySelf()) {
                if (siege.getClanSieged().getUniqueId().equals(toLoseWP.getUniqueId())) {
					isInSiege = true;
				}
            }

            for (final Siege siege : toGiveWP.getClansSiegingSelf()) {
                if (siege.getClanSieging().getUniqueId().equals(toLoseWP.getUniqueId())) {
					isInSiege = true;
				}
            }


            // Cancelling Warpoint in siege
            if (isInSiege) {
				return;
			}

            final long lost = toLoseWP.getWarpointsOnClan(toGiveWP.getUniqueId()) - 1;
            final long won = toGiveWP.getWarpointsOnClan(toLoseWP.getUniqueId()) + 1;
            final ChatColor color = lost <= -10 || lost >= 10 ? ChatColor.DARK_RED : ChatColor.YELLOW;
            final String sLost = lost > 0 ? "+" : "";
            final String sWon = won > 0 ? "+" : "";

            final ClanWarpointEvent event = new ClanWarpointEvent(toGiveWP, toLoseWP, won);
            Bukkit.getServer().getPluginManager().callEvent(event);

            if (!event.isCancelled()) {
                toLoseWP.setWarpoint(toGiveWP.getUniqueId(), lost); // Removing WP on clan that died
                toGiveWP.setWarpoint(toLoseWP.getUniqueId(), won); // Adding WP on clan that got kill

                toLoseWP.announceToClan("&9(!) &7Your clan has &qLOST &ra War Point to &s" + color + toGiveWP.getName() + " &7(" + color + sLost + lost + "&7).", false);
                toGiveWP.announceToClan("&9(!) &7Your clan has &wGAINED &ra War Point on &s" + color + toLoseWP.getName() + " &7(" + color + sWon + won + "&7).", false);

				this.sql.saveClan(toGiveWP);
				this.sql.saveClan(toLoseWP);
            }

        }
    }

}
