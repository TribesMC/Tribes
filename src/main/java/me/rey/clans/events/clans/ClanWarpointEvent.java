package me.rey.clans.events.clans;

import me.rey.clans.clans.Clan;
import org.bukkit.event.Cancellable;

public class ClanWarpointEvent extends ClanEvent implements Cancellable {

	private Clan player;
	private long killerWarpoints;
	private boolean canceled = false;
	
	public ClanWarpointEvent(Clan killer, Clan player, long killerWarpoints) {
		super(killer);
		this.player = player;
		this.killerWarpoints = killerWarpoints;
	}
	
	public Clan getKilled() {
		return player;
	}
	
	public long getKillerWarpoints() {
		return killerWarpoints;
	}

	@Override
	public boolean isCancelled() {
		return canceled;
	}

	@Override
	public void setCancelled(boolean canceled) {
		this.canceled = canceled;
	}
}
