package me.rey.clans.siege;

import me.rey.clans.Tribes;
import me.rey.clans.clans.Clan;
import me.rey.clans.clans.ClansPlayer;
import me.rey.clans.events.ClaimProtection;
import me.rey.clans.events.clans.ClanWarpointEvent;
import me.rey.clans.events.clans.PlayerEditClaimEvent;
import me.rey.clans.events.clans.PlayerEditClaimEvent.ClaimPermission;
import me.rey.clans.events.clans.PlayerEditClaimEvent.EditAction;
import me.rey.clans.events.custom.ContainerOpenEvent;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;

public class SiegeTriggerEvent implements Listener {

    private static Clan isInOtherClaim(final Player player, final Block block) {
        final Clan owner = Tribes.getInstance().getClanFromTerritory(block.getChunk());
        final ClansPlayer self = new ClansPlayer(player);
        return owner == null ? null : (self.hasClan() && self.getClan().compare(owner) ? null : owner);
    }

    public static boolean isInSiegerTerritory(final Player player, final Block block) {
        if (!(new ClansPlayer(player).hasClan())) {
			return false;
		}
        if (isInOtherClaim(player, block) == null) {
			return false;
		}

        final Clan on = isInOtherClaim(player, block);
        final Clan self = new ClansPlayer(player).getClan();
        if (!Siege.sieges.containsKey(self.getUniqueId())) {
			return false;
		}
        if (Siege.sieges.get(self.getUniqueId()) == null || Siege.sieges.get(self.getUniqueId()).isEmpty()) {
			return false;
		}

        final ArrayList<Siege> currentlySieging = Siege.sieges.get(self.getUniqueId());
        for (final Siege siege : currentlySieging) {
            if (siege.getClanSieged().getUniqueId().equals(on.getUniqueId())) {
				return true;
			}
        }
        return false;

    }

    @EventHandler
    public void onSiege(final ClanWarpointEvent e) {
        if (e.getKillerWarpoints() < 25) {
			return;
		}

        final Clan sieger = e.getClan();
        final Clan sieged = e.getKilled();

        final Siege siege = new Siege(sieger, sieged, System.currentTimeMillis());
        siege.start();

        sieger.setWarpoint(sieged.getUniqueId(), 0);
        sieged.setWarpoint(sieger.getUniqueId(), 0);
        Tribes.getInstance().getSQLManager().saveClan(sieger); // saving SIEGER
        Tribes.getInstance().getSQLManager().saveClan(sieged); // saving SIEGED

    }

    @EventHandler
    public void onOpenContainer(final ContainerOpenEvent e) {
        if (!isInSiegerTerritory(e.getPlayer(), e.getContainer())) {
			return;
		}

        e.setAllowed(true);
    }

    @EventHandler
    public void onEditClaim(final PlayerEditClaimEvent e) {
        if (!e.getAction().equals(EditAction.BREAK)) {
			return;
		}
        if (!isInSiegerTerritory(e.getPlayer(), e.getBlockToReplace())) {
			return;
		}
        if (!ClaimProtection.containers.contains(e.getBlockToReplace().getType())) {
			return;
		}

        e.setPermission(ClaimPermission.ALLOW);
    }

}
