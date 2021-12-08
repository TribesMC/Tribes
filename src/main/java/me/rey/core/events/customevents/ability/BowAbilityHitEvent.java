package me.rey.core.events.customevents.ability;

import me.rey.core.classes.abilities.Ability;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;

public class BowAbilityHitEvent extends Event implements Cancellable {

    private final Player player;
    private final Ability ability;
    private boolean isCancelled;
    private final LivingEntity damagee;
    private String message, message_;
    private final int level;
    private final Location landingLocation;
    private final Vector arrowVelocity;

    public BowAbilityHitEvent(Player player, Ability ability, int level, @Nullable LivingEntity damagee, Location landingLocation, Vector arrowVelocity, String messageForShooter, String messageForDamagee) {
        this.ability = ability;
        this.player = player;
        this.level = level;
        this.isCancelled = false;
        this.damagee = damagee;
        this.message_ = messageForDamagee;
        this.message = messageForShooter;
        this.landingLocation = landingLocation;
        this.arrowVelocity = arrowVelocity;
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

    public LivingEntity getDamagee() {
        return damagee;
    }

    public void setMessageForDamagee(String message) {
        this.message_ = message;
    }

    public String getMessageForDamagee() {
        return message_;
    }

    public void setMessageForShooter(String message) {
        this.message = message;
    }

    public String getMessageForShooter() {
        return message;
    }

    public Location getLandingLocation() {
        return landingLocation;
    }

    public Vector getArrowVelocity() {
        return arrowVelocity;
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