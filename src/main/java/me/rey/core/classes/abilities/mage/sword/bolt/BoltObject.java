package me.rey.core.classes.abilities.mage.sword.bolt;

import me.rey.core.Warriors;
import me.rey.core.effects.repo.Shock;
import me.rey.core.utils.UtilBlock;
import me.rey.core.utils.UtilEnt;
import me.rey.core.utils.UtilLoc;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class BoltObject {

    Bolt.BoltProfile bo;

    Location loc;
    Location origin;

    boolean onlyvisual;

    boolean incurve = false;

    Location locWithNoCurve;
    double travelDistance = 20;
    double travelledDistanceSinceLastCurve = 0;
    double maxCurveDistance = 5;

    HashMap<UUID, Integer> stacks;
    HashMap<UUID, Long> stackdecay;

    ArrayList<UUID> damagedplayers = new ArrayList<UUID>();

    public BoltObject(Bolt.BoltProfile bo, HashMap<UUID, Integer> stacks, HashMap<UUID, Long> stackdecay, boolean onlyvisual, Location origin) {
        this.bo = bo;
        this.onlyvisual = onlyvisual;
        this.origin = origin;
        this.loc = origin.clone();

        this.stacks = stacks;
        this.stackdecay = stackdecay;

        this.travelDistance = bo.maxTravelDistance * (bo.charge/bo.maxcharge);

        new BukkitRunnable() {
            @Override
            public void run() {
                if(!destroy()) {
                    while(loc.distance(origin) < travelDistance && !destroy()) {
                        tick();
                    }
                } else {
                    this.cancel();
                }
            }
        }.runTaskTimer(Warriors.getInstance().getPlugin(), 1L, 1L);

    }

    public void tick() {

        checkCollision();

        double yMultiplier = Math.sqrt(1-Math.pow(UtilLoc.getYCordsMultiplierByPitch(loc.getPitch()), 2));
        loc.setX(UtilLoc.getXZCordsFromDegree(loc, yMultiplier, loc.getYaw() + 90)[0]);
        loc.setY(loc.getY() + UtilLoc.getYCordsMultiplierByPitch(loc.getPitch()));
        loc.setZ(UtilLoc.getXZCordsFromDegree(loc, yMultiplier, loc.getYaw() + 90)[1]);

    }

    public void checkCollision() {

        if(onlyvisual) {
            return;
        }

        Location collisionloc = loc.clone();
        collisionloc.setY(loc.getY() - 2.0);
        for(Entity e : collisionloc.getWorld().getNearbyEntities(collisionloc, bo.hitbox, bo.hitbox + 1, bo.hitbox)) {
            if(e instanceof LivingEntity) {
                if(e == bo.shooter || damagedplayers.contains(e.getUniqueId()))
                    continue;

                if(e instanceof Player) {
                    Player p = (Player) e;
                    if(bo.user.getTeam().contains(p)) {
                        continue;
                    }
                }

                damagedplayers.add(e.getUniqueId());

                // Increment stack
                final int stack = stacks.getOrDefault(bo.shooter.getUniqueId(), 0);
                if(stack < bo.maxstacks) {
                    stacks.put(bo.shooter.getUniqueId(), stack + 1);
                }

                // Set decay timer
                stackdecay.put(bo.shooter.getUniqueId(), System.currentTimeMillis() + 5000L);

                UtilEnt.damage(calculateDamage(bo, stack + 1), "Lightning Bolt", (LivingEntity) e, bo.shooter);
                new Shock().apply((LivingEntity) e, bo.shockSeconds);
            }
        }
    }

    public boolean destroy() {

        if((UtilBlock.airFoliage(loc.getBlock()) && loc.distance(origin) < travelDistance) || (loc.getBlock().isLiquid() && loc.distance(origin) < travelDistance)) {

            return false;
        }

        if(!onlyvisual) {
            if (damagedplayers.isEmpty()) {
                stacks.put(bo.shooter.getUniqueId(), 0);
            }
        }

        return true;
    }

    private double calculateDamage(Bolt.BoltProfile profile, int stack) {
        return profile.baseDamage + (profile.level * profile.damagePerLevel) + (profile.damagePerStack * stack);
    }
}
