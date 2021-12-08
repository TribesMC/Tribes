package me.rey.core.events.customevents.update;

import me.rey.core.energy.EnergyHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class EnergyConsumeEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    private double energyToTake;
    private Player player;
    private boolean cancelled;

    public EnergyConsumeEvent(double energyToTake, Player player) {
        this.energyToTake = energyToTake;
        this.player = player;
    }

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

    public double getEnergyToTake() {
        return energyToTake;
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
