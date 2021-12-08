package me.rey.core.events.customevents;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CustomPlayerInteractEvent extends Event {

	private final Player player;
	private final Event event;
	
	public CustomPlayerInteractEvent(Player player, Event event) {
		this.player = player;
		this.event = event;
	}
	
	private static final HandlerList HANDLERS = new HandlerList();
	
	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}
	
	public static HandlerList getHandlerList() {
		return HANDLERS;
	}
	
	public Player getPlayer() {
		return player;
	}

	public Event getEvent() {
		return event;
	}
}
