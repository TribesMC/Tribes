package me.rey.clans.events;

import me.rey.clans.Tribes;
import me.rey.clans.clans.Clan;
import me.rey.clans.clans.ClanRelations;
import me.rey.clans.clans.ClansPlayer;
import me.rey.clans.database.SQLManager;
import me.rey.clans.events.clans.PlayerTerritoryChangeEvent;
import me.rey.core.packets.Title;
import me.rey.core.utils.Text;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class TerritoryChange implements Listener {

    private final int titleDelaySeconds = 2;
    SQLManager sql = Tribes.getInstance().getSQLManager();

    @EventHandler
    public void onLogin(final PlayerJoinEvent e) {
        this.handleTerritoryChange(e.getPlayer(), e.getPlayer().getLocation(), null);
    }

    @EventHandler
    public void onPlayerMove(final PlayerMoveEvent e) {
        final Location to = e.getTo();
		final Location from = e.getFrom();

		if (to.getChunk() == from.getChunk()) {
			return;
		}

        this.handleTerritoryChange(e.getPlayer(), to, from);
    }

    private void handleTerritoryChange(final Player p, final Location to, final Location from) {
        final ClansPlayer cp = new ClansPlayer(p);

        final Clan owner = Tribes.getInstance().getClanFromTerritory(to.getChunk());
        if (from == null) {
            if (owner == null) {
                cp.sendMessageWithPrefix("Territory", "Wilderness");
                new Title("", Text.color("&7Wilderness"), 5, this.titleDelaySeconds * 20, 5).send(p);
                return;
            }

            ClanRelations relation = ClanRelations.NEUTRAL;
            if (cp.hasClan()) {
				relation = cp.getClan().getClanRelation(owner.getUniqueId());
			}

            cp.sendMessageWithPrefix("Territory", relation.getPlayerColor() + owner.getName());
            new Title("", Text.color(relation.getPlayerColor() + owner.getName()), 5, this.titleDelaySeconds * 20, 5).send(p);

            final PlayerTerritoryChangeEvent event = new PlayerTerritoryChangeEvent(p, owner, null);
            Bukkit.getServer().getPluginManager().callEvent(event);
            return;
        }

        final Clan clanFrom = Tribes.getInstance().getClanFromTerritory(from.getChunk());
        if (owner == null) {
            if (clanFrom != null) {
                cp.sendMessageWithPrefix("Territory", "Wilderness");
                new Title("", Text.color("&7Wilderness"), 5, this.titleDelaySeconds * 20, 5).send(p);

                final PlayerTerritoryChangeEvent event = new PlayerTerritoryChangeEvent(p, null, clanFrom);
                Bukkit.getServer().getPluginManager().callEvent(event);
            }
            return;
        }

        final PlayerTerritoryChangeEvent event = new PlayerTerritoryChangeEvent(p, owner, clanFrom);
        Bukkit.getServer().getPluginManager().callEvent(event);

        ClanRelations relation = ClanRelations.NEUTRAL;
        if (cp.hasClan()) {
			relation = cp.getClan().getClanRelation(owner.getUniqueId());
		}

        if (owner.compare(clanFrom)) {
			return;
		}
        cp.sendMessageWithPrefix("Territory", relation.getPlayerColor() + owner.getName());
        new Title("", Text.color(relation.getPlayerColor() + owner.getName()), 5, this.titleDelaySeconds * 20, 5).send(p);
    }

}
