package me.rey.clans.events;

import me.rey.clans.features.punishments.Punishment;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.List;
import java.util.UUID;

public class PunishmentsUpdateEvent extends Event {
    private final UUID uuid;
    private final List<Punishment> punishments;

    private static final HandlerList HANDLERS = new HandlerList();

    public PunishmentsUpdateEvent(UUID uuid, List<Punishment> punishments) {
        this.uuid = uuid;
        this.punishments = punishments;
    }

    public UUID getUUID() {
        return uuid;
    }

    public List<Punishment> getPunishments() {
        return punishments;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}