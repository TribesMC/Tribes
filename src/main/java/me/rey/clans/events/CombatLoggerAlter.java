package me.rey.clans.events;

import me.rey.clans.events.clans.ClanWarpointEvent;
import me.rey.clans.features.CombatLogger;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CombatLoggerAlter extends Event {

    private final UUID player;
    private final CombatLogger.CombatLoggerInfo info;
    private final Player killer;
    private final ClanWarpointEvent warpointEvent;
    private final CombatLoggerEventType type;

    public CombatLoggerAlter(UUID player, CombatLogger.CombatLoggerInfo info, CombatLoggerEventType type) {
        this.player = player;
        this.info = info;
        this.type = type;
        this.killer = null;
        this.warpointEvent = null;
    }

    public CombatLoggerAlter(UUID player, CombatLogger.CombatLoggerInfo info, Player killer, ClanWarpointEvent warpointEvent, CombatLoggerEventType type) {
        this.player = player;
        this.info = info;
        this.killer = killer;
        this.warpointEvent = warpointEvent;
        this.type = type;
    }

    public UUID getPlayer() {
        return player;
    }

    public CombatLogger.CombatLoggerInfo getInfo() {
        return info;
    }

    public Player getKiller() {
        return killer;
    }

    public ClanWarpointEvent getWarpointEvent() {
        return warpointEvent;
    }

    public CombatLoggerEventType getType() {
        return type;
    }

    public enum CombatLoggerEventType {
        SPAWNED,
        DESPAWNED,
        KILLED
    }

    private static final HandlerList HANDLERS = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public String toString() {
        Map<String, Object> map = new HashMap<>();
        map.put("player", player);
        map.put("info", info);
        map.put("killer", player);
        map.put("warpointEvent", warpointEvent);
        map.put("type", type);
        return map.toString();
    }
}
