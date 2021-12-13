package me.rey.clans.events;

import me.rey.clans.features.combatlogger.CombatLogSkeleton;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CombatLoggerDamageEvent extends Event implements Cancellable {
    private final CombatLogSkeleton combatLogger;
    private final Player player;
    private boolean cancelled = false;

    private static final HandlerList HANDLERS = new HandlerList();

    public CombatLoggerDamageEvent(CombatLogSkeleton combatLogger, Player player) {
        this.combatLogger = combatLogger;
        this.player = player;
    }

    public CombatLogSkeleton getCombatLogger() {
        return combatLogger;
    }

    public Player getDamager() {
        return player;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
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
