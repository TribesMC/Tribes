package me.rey.core.events.customevents.combat;

import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.Optional;

public class CustomKnockbackEvent extends Event implements Cancellable {

    private boolean cancelled = false;

    private final Entity entity;
    private final Entity entityCause;
    private final Vector vector;

    private static final HandlerList HANDLERS = new HandlerList();

    public CustomKnockbackEvent(Entity entity, @Nullable Entity entityCause, Vector vector) {
        this.entity = entity;
        this.entityCause = entityCause;
        this.vector = vector;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public Entity getEntity() {
        return entity;
    }

    public Optional<Entity> getEntityCause() {
        return Optional.ofNullable(entityCause);
    }

    public Vector getVector() {
        return vector;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
