package me.rey.core.classes.abilities.mage.axe;

import me.rey.core.Warriors;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.players.User;
import me.rey.core.utils.UtilLoc;
import me.rey.core.utils.UtilVelocity;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class FireBlast extends Ability {

    private final HashMap<UUID, FireProfile> fireballs;

    public FireBlast() {
        super(211, "Fire Blast", ClassType.GOLD, AbilityType.AXE, 1, 5, 12.00, Arrays.asList(
                "Launch a fireball which explodes on impact",
                "dealing large knockback to enemies within",
                "<variable>0.5*l+3</variable> (+0.5) Blocks range. Also ignites enemies",
                "for up to <variable>2*l+2</variable> (+2) seconds.",
                "",
                "Energy: <variable>0-4*l+100</variable> (-4)",
                "Recharge: <variable>0-1*l+13</variable> (-1) Seconds"));

        this.fireballs = new HashMap<>();
        this.setEnergyCost(100, 4);
    }

    @Override
    protected boolean execute(final User u, final Player p, final int level, final Object... conditions) {

        this.fireballs.put(p.getUniqueId(), new FireProfile(p, u, level));

        this.setCooldown(-1 * level + 13);
        return true;
    }

    @EventHandler
    public void onEntityDamage(final ProjectileHitEvent e) {
        if (!(e.getEntity() instanceof Fireball && e.getEntityType() == EntityType.FIREBALL)) {
            return;
        }
        final Fireball fireball = (Fireball) e.getEntity();

        FireProfile fp = null;
        for (final UUID u : this.fireballs.keySet()) {
            if (this.fireballs.get(u).fireball == fireball) {
                fp = this.fireballs.get(u);
            }
        }

        if (fp == null) {
            return;
        }

        final Player p = fp.shooter;
        final double fireSeconds = 2 * fp.level + 2;

        for (final Location cloc : UtilLoc.circleLocations(fp.fireball.getLocation(), 3.0 + (double) fp.level / 2, 30)) {
            for (int i = 0; i < 10; i++) {
                cloc.getWorld().spigot().playEffect(cloc, Effect.FLAME, 0, 0, 0, 0, 0, 1F, 1, 30);
            }
        }

        final Location loc = e.getEntity().getLocation();
        final Collection<Entity> entities = loc.getWorld().getNearbyEntities(loc, 3.0 + (double) fp.level / 2, 3.0, 3.0 + (double) fp.level / 2);

        final Set<Player> team = fp.u.getTeam();
        for (final Entity ent : entities) {
            if (ent instanceof LivingEntity) {
                final LivingEntity livingEnt = (LivingEntity) ent;

                if (livingEnt instanceof Player) {
                    final Player playerEnt = (Player) livingEnt;
                    final boolean equal = playerEnt.equals(p);
                    if (!equal && team.contains(playerEnt)) {
                        continue;
                    }

                    final Vector vc = livingEnt.getLocation().toVector().subtract(loc.toVector());
                    UtilVelocity.velocity(livingEnt, p, vc.normalize().multiply(1.8));
                    UtilVelocity.velocity(livingEnt, p, new Vector(livingEnt.getVelocity().getX(), 1, livingEnt.getVelocity().getZ()));

                    if (!equal) {
                        livingEnt.damage(4D, p);
                        Bukkit.getScheduler().runTaskLater(Warriors.getInstance().getPlugin(), () -> this.setOnFire(livingEnt, fireSeconds), 0L);
                    } else {
                        p.setFallDistance(-8);
                    }
                } else {
                    livingEnt.damage(4D, p);
                    Bukkit.getScheduler().runTaskLater(Warriors.getInstance().getPlugin(), () -> this.setOnFire(livingEnt, fireSeconds), 0L);

                    final Vector vc = livingEnt.getLocation().toVector().subtract(loc.toVector());
                    UtilVelocity.velocity(livingEnt, p, vc.normalize().multiply(1.8));
                    UtilVelocity.velocity(livingEnt, p, new Vector(livingEnt.getVelocity().getX(), 1, livingEnt.getVelocity().getZ()));
                }

                for (final Location cLoc : UtilLoc.circleLocations(fp.fireball.getLocation(), 1, 45)) {
                    cLoc.getWorld().spigot().playEffect(cLoc, Effect.LAVA_POP);
                }

            }
        }

        this.fireballs.remove(p.getUniqueId());
    }

    private void setOnFire(final LivingEntity entity, final double fireSeconds) {
        if (entity.isDead()) {
            return;
        }

        entity.setFireTicks(-1);
        entity.setFireTicks((int) (fireSeconds * 20));
    }

    static class FireProfile {

        Player shooter;
        User u;
        int level;
        Fireball fireball;

        public FireProfile(final Player p, final User u, final int level) {

            this.shooter = p;
            this.u = u;
            this.level = level;

            final Location direction = p.getEyeLocation().toVector().add(p.getLocation().getDirection().multiply(1)).toLocation(p.getWorld(),
                    p.getLocation().getYaw(), p.getLocation().getPitch());
            this.fireball = p.launchProjectile(Fireball.class);

            UtilVelocity.velocity(this.fireball, null, direction.getDirection());
            this.fireball.setShooter(p);
            this.fireball.setFireTicks(0);
            this.fireball.setYield(0F);
            this.fireball.setIsIncendiary(false);
            UtilVelocity.velocity(this.fireball, null, p.getEyeLocation().getDirection().multiply(1));

            new BukkitRunnable() {
                int seconds = 0;

                @Override
                public void run() {
                    if (FireProfile.this.fireball.isDead()) {
                        this.cancel();
                        return;
                    }

                    if (this.seconds >= 10) {
                        this.cancel();
                        FireProfile.this.fireball.remove();
                    } else {
                        FireProfile.this.fireball.setTicksLived(1);
                    }
                    this.seconds++;
                }

            }.runTaskTimer(Warriors.getInstance().getPlugin(), 0, 20);
        }

    }
}
