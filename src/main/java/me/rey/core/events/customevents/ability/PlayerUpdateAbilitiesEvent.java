package me.rey.core.events.customevents.ability;

import me.rey.core.pvp.Build;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerUpdateAbilitiesEvent extends Event {

    private final Player player;

    public PlayerUpdateAbilitiesEvent(Player player) {
        this.player = player;
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

}
