package me.rey.core.events.customevents.update;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class UpdateEvent extends Event {
	
	private static final HandlerList HANDLERS = new HandlerList();

	private final TickType type;

	public UpdateEvent(long ticks) {
		this.type = TickType.parse(ticks);
	}

	public enum TickType {
		DOUBLE_MINUTE(2400),
		MINUTE(1200),
		DOUBLE_SECOND(40),
		SECOND(20),
		HALF_SECOND(10),
		QUARTER_SECOND(5),
		DOUBLE_TICK(2),
		TICK(1);

		public final int ticks;

		TickType(int ticks) {
			this.ticks = ticks;
		}

		public static TickType parse(long tick) {
			for (TickType type : TickType.values()) {
				if (tick % type.ticks == 0) {
					return type;
				}
			}
			return TICK;
		}
	}

	public TickType getType() {
		return type;
	}

	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}
	
	public static HandlerList getHandlerList() {
		return HANDLERS;
	}
}
