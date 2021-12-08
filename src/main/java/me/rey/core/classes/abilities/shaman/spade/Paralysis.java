package me.rey.core.classes.abilities.shaman.spade;

import me.rey.core.Warriors;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.gui.Item;
import me.rey.core.items.Throwable;
import me.rey.core.players.User;
import me.rey.core.utils.UtilEnt;
import me.rey.core.utils.UtilLoc;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;

public class Paralysis extends Ability {

    public Paralysis() {
        super(522, "Paralysis", ClassType.GREEN, AbilityType.SPADE, 1, 3, 8, Arrays.asList(
                "Shoot a slime ball in the air, rooting enemies",
                "who got hit by it into the ground for 2 seconds",
                "and dealing 4 damage.",
                "",
                "Energy: <variable>42-2*l</variable> (-2)",
                "",
                "Recharge: <variable>10-0.2*l</variable> (-0.2) Seconds"
        ));
        this.setEnergyCost(42, 2);
    }

    @Override
    protected boolean execute(final User u, final Player p, final int level, final Object... conditions) {

        this.setCooldown(10 - level * 0.2);

        final ParalysisObject slimeball = new ParalysisObject(p, u);
        slimeball.shoot();

        new BukkitRunnable() {
            @Override
            public void run() {
                if (slimeball.ball.destroy) {
                    this.cancel();
                    return;
                } else {
                    slimeball.tick();
                }
            }
        }.runTaskTimer(Warriors.getInstance().getPlugin(), 0L, 1L);

        return false;
    }

    class ParalysisObject {

        Player shooter;
        User user;
        Throwable ball;

        public ParalysisObject(final Player p, final User u) {
            this.shooter = p;
            this.user = u;
            this.ball = new Throwable(new Item(Material.SLIME_BALL), false);
        }

        public void shoot() {
            if (this.ball.fired == false) {
                this.ball.fired = true;
                this.shooter.getWorld().playSound(this.shooter.getLocation(), Sound.SLIME_ATTACK, 1F, 1F);
                this.ball.fire(this.shooter.getEyeLocation(), this.shooter.getLocation().getDirection(), 1, 0);
            }
        }

        public void tick() {
            this.ball.destroyWhenOnGround();
            this.checkCollision();
        }

        public void checkCollision() {
            if (this.ball.fired == false) {
                return;
            }

            this.ball.getEntityitem().getWorld().spigot().playEffect(this.ball.getEntityitem().getLocation(), Effect.SLIME);

            for (final Entity e : this.ball.getEntityitem().getNearbyEntities(0.5, 0.5, 0.5)) {
                if (e instanceof LivingEntity && e != this.shooter && this.user.getTeam().contains(e) == false) {
                    this.ball.destroy();

                    final LivingEntity le = (LivingEntity) e;

                    UtilEnt.damage(4.0, "Paralysis", le, this.shooter);

                    le.getLocation().getWorld().playSound(le.getLocation(), Sound.DIG_GRASS, 1F, 1F);
                    le.getLocation().getWorld().playSound(le.getLocation(), Sound.DIG_GRASS, 1F, 1F);
                    for (double radius = 2; radius >= 1; radius -= 0.25) {
                        for (final Location loc : UtilLoc.circleLocations(le.getLocation(), radius, 40)) {
                            loc.getWorld().spigot().playEffect(loc, Effect.CRIT, 0, 0, 0, 0, 0, 0, 0, 30);
                        }
                    }

                    le.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 40, 200));
                    le.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 200));

                }
            }

        }

    }

}
