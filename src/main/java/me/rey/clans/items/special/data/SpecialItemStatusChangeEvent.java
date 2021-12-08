package me.rey.clans.items.special.data;

import me.rey.clans.items.special.SpecialItem;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class SpecialItemStatusChangeEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    public SpecialItem item;
    public boolean status;

    public SpecialItemStatusChangeEvent(SpecialItem item, boolean status) {
        this.item = item;
        this.status = status;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
