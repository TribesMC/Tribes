package me.rey.core.events.customevents.update;

import me.rey.core.energy.EnergyHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class EnergyReplenishEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private double energy, extraCapacity, extraSpeed, increment;
    private Player player;
    private EnergyHandler handler;

    private boolean cancelled;

    public EnergyReplenishEvent(double incrementPerTick, Player player, double energy, EnergyHandler handler) {
        this.increment = incrementPerTick;
        this.player = player;
        this.energy = energy;
        this.extraCapacity = 0;
        this.extraSpeed = 0;
        this.handler = handler;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public double getIncrement() {
        return increment;
    }

    public void setIncrement(double incrementPerTick) {
        this.increment = incrementPerTick;
    }

    public Player getPlayer() {
        return player;
    }

    public double getEnergy() {
        return energy;
    }

    public double getExtraCapacity() {
        return extraCapacity;
    }

    public double getExtraSpeed() {
        return extraSpeed;
    }

    public void addExtraSpeed(double speed) {
        this.extraSpeed += speed;
        handler.setExtraSpeed(player.getUniqueId(), extraSpeed);
    }

    public void addExtraCapacity(double capacity) {
        this.extraCapacity += capacity;
        handler.setExtraCapacity(player.getUniqueId(), extraCapacity);
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
