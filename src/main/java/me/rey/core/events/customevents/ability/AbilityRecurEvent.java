package me.rey.core.events.customevents.ability;

import me.rey.core.classes.abilities.Ability;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class AbilityRecurEvent extends Event implements Cancellable {

    private final Player player;
    private final Ability ability;
    private boolean isCancelled;
    private final int level;

    public AbilityRecurEvent(Player player, Ability ability, int level) {
        this.ability = ability;
        this.player = player;
        this.level = level;
        this.isCancelled = false;
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

    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.isCancelled = cancelled;
    }
}
