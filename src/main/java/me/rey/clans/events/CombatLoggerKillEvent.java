package me.rey.clans.events;

import me.rey.clans.features.combatlogger.CombatLogSkeleton;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CombatLoggerKillEvent extends Event {
    private final CombatLogSkeleton combatLogger;
    private final Player player;

    private static final HandlerList HANDLERS = new HandlerList();

    public CombatLoggerKillEvent(CombatLogSkeleton combatLogger, Player player) {
        this.combatLogger = combatLogger;
        this.player = player;
    }

    public CombatLogSkeleton getCombatLogger() {
        return combatLogger;
    }

    public Player getKiller() {
        return player;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
