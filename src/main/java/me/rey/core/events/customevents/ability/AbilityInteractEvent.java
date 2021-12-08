package me.rey.core.events.customevents.ability;

import me.rey.core.classes.abilities.Ability;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class AbilityInteractEvent extends Event implements Cancellable {

    private final LivingEntity target;
    private final Player conjurer;
    private final Ability ability;
    private boolean isCancelled;
    private final int level;

    public AbilityInteractEvent(LivingEntity target, Player conjurer, Ability ability, int level) {
        this.target = target;
        this.conjurer = conjurer;
        this.ability = ability;
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

    public LivingEntity getTarget() {
        return target;
    }

    public Player getConjurer() {
        return conjurer;
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
