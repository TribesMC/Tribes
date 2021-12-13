package me.rey.core.events.customevents.combat;

import me.rey.core.utils.UtilEnt;
import me.rey.core.utils.UtilEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class MobDeathEvent extends Event {//EntityDeathEvent {

    private final LivingEntity dead;
    private final Entity killer;
    private final List<ItemStack> drops;
    private final EntityDamageEvent event;

    public MobDeathEvent(LivingEntity dead, Entity killer, List<ItemStack> drops, EntityDamageEvent event) {
        this.dead = dead;
        this.killer = killer;
        this.drops = drops;
        this.event = event;
    }

    private static final HandlerList HANDLERS = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public LivingEntity getEntity() {
        return dead;
    }

    public List<ItemStack> getDrops() {
        return drops;
    }

    public Entity getKiller() {
        return killer;
    }

    public EntityDamageEvent getEvent() {
        return event;
    }
}