package me.rey.core.events.customevents.ability;

import me.rey.core.classes.ClassType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerClassUpdateEvent extends Event {

    private final Player player;
    private final ClassType from, to;

    public PlayerClassUpdateEvent(Player player, ClassType from, ClassType to) {
        this.player = player;
        this.from = from;
        this.to = to;
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

    public ClassType getFrom() {
        return from;
    }

    public ClassType getTo() {
        return to;
    }

}
