package me.rey.core.events.customevents.ability;

import me.rey.core.classes.ClassType;
import me.rey.core.pvp.Build;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerBuildEditEvent extends Event {

    private final Player player;
    private final Build from, to;

    public PlayerBuildEditEvent(Player player, Build from, Build to) {
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

    public Build getOld() {
        return from;
    }

    public Build getNew() {
        return to;
    }
}
