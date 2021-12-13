package me.rey.clans.events;

import me.rey.clans.features.combatlogger.CombatLogSkeleton;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CombatLoggerDespawnEvent extends Event {
    private final CombatLogSkeleton combatLogger;

    private static final HandlerList HANDLERS = new HandlerList();

    public CombatLoggerDespawnEvent(CombatLogSkeleton combatLogger) {
        this.combatLogger = combatLogger;
    }

    public CombatLogSkeleton getCombatLogger() {
        return combatLogger;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
