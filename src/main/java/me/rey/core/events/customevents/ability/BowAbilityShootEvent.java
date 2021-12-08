package me.rey.core.events.customevents.ability;

import me.rey.core.classes.abilities.Ability;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BowAbilityShootEvent extends Event implements Cancellable {

    private final Player player;
    private final Ability ability;
    private boolean isCancelled;
    private String message;
    private final int level;

    public BowAbilityShootEvent(Player player, Ability ability, int level, String message) {
        this.ability = ability;
        this.player = player;
        this.level = level;
        this.isCancelled = false;
        this.message = message;
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

    public int getLevel() {
        return level;
    }

    public Ability getAbility() {
        return ability;
    }

    public void setShootMessage(String message) {
        this.message = message;
    }

    public String getShootMessage() {
        return message;
    }

    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.isCancelled = cancelled;
    }

}