package me.rey.clans.events.clans;

import me.rey.clans.clans.Clan;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerTerritoryChangeEvent extends Event {

    final Player player;
    final Clan to, from;

    public PlayerTerritoryChangeEvent(Player player, Clan to, Clan from) {
        this.player = player;
        this.to = to;
        this.from = from;
    }

    private static final HandlerList HANDLERS = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public Clan getFrom() {
        return from;
    }

    public Clan getTo() {
        return to;
    }

    public Player getPlayer() {
        return player;
    }
}
