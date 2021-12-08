package me.rey.core.players.combat;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.rey.core.Warriors;
import me.rey.core.events.customevents.combat.CombatTimerEndEvent;
import me.rey.core.events.customevents.combat.CombatTimerTickEvent;

public class CombatTimer extends BukkitRunnable {
	
	private static PlayerHitCache cache = Warriors.getInstance().getHitCache();
	private final int interval = 1;
	Player player;
	long timeIssued;
	
	public CombatTimer(Player player) {
		this.player = player;
		
		this.timeIssued = System.currentTimeMillis();
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public CombatTimer init() {
		this.runTaskTimer(Warriors.getInstance().getPlugin(), 0, interval);
		return this;
	}
	
	public double getRemaining(long currentTime) {
		return (double) PlayerHitCache.COMBAT_TIMER - ((double) (currentTime-timeIssued)/1000.0);
	}
	
	public long getTimeIssued() {
		return timeIssued;
	}
	
	@Override
	public void run() {
		
		if(!cache.hasCombatTimer(this.getPlayer()) || cache.getCombatTimer(this.getPlayer()) != this) {
			CombatTimerEndEvent event = new CombatTimerEndEvent(this.getPlayer(), this);
			Bukkit.getServer().getPluginManager().callEvent(event);
			this.cancel();
			return;
		}
		
		CombatTimerTickEvent event = new CombatTimerTickEvent(this.getPlayer(), this);
		Bukkit.getServer().getPluginManager().callEvent(event);
	}

}
