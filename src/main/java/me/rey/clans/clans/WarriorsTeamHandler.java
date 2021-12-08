package me.rey.clans.clans;

import me.rey.clans.Tribes;
import me.rey.core.events.customevents.team.TeamProcessEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class WarriorsTeamHandler implements Listener {

    public final List<ClanRelations> TEAMMATES = Arrays.asList(ClanRelations.ALLY, ClanRelations.SELF);

    @EventHandler
    public void onTeamProcess(final TeamProcessEvent e) {
		this.updateTeams(e, e.getPlayer());
    }

    public void updateTeams(final TeamProcessEvent e, final Player p) {
        final ClansPlayer cp = new ClansPlayer(p);
        if (cp.getRealClan() != null) {
			this.processClanTeam(cp.getRealClan(), e);
        }

        if (cp.getFakeClan() != null) {
			this.processClanTeam(cp.getFakeClan(), e);
        }
    }

    private void processClanTeam(final Clan clan, final TeamProcessEvent e) {
        for (final ClansPlayer cm : clan.getOnlinePlayers(false).keySet()) {
            e.addTeammate(cm.getPlayer());
        }

        for (final UUID uuid : clan.getRelations().keySet()) {
            if (this.TEAMMATES.contains(clan.getClanRelation(uuid))) {
                for (final ClansPlayer cm : Tribes.getInstance().getClan(uuid).getOnlinePlayers(false).keySet()) {
                    e.addTeammate(cm.getPlayer());
                }
            }
        }
    }

}
