package me.rey.core.classes.abilities.shaman.spade;

import me.rey.core.Warriors;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.players.User;
import me.rey.core.utils.UtilEnt;
import me.rey.core.utils.UtilLoc;
import me.rey.core.utils.UtilParticle;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

public class Miasma extends Ability {

    private final HashMap<UUID, Integer> particleticks = new HashMap<UUID, Integer>();

    public Miasma() {
        super(523, "Miasma", ClassType.GREEN, AbilityType.SPADE, 1, 4, 10, Arrays.asList(
                "Create a poison clouds that infects",
                "nearby enemies in a <variable>5+l</variable> (+1) radius",
                "for a duration of <variable>1+0.5*l</variable> (+0.5) Seconds.",
                "",
                "Enemies also take 6 damage upon hit.",
                "",
                "Energy: <variable>70-5*l</variable> (-2)",
                "Recharge: 10 Seconds"
        ));
        this.setEnergyCost(70, 2);
    }

    @Override
    protected boolean execute(final User u, final Player p, final int level, final Object... conditions) {

        final double radius = 5 + level;
        final double effectDuration = 1 + 0.5 * level;

        for (final Entity e : UtilLoc.getEntitiesInCircle(p.getLocation(), radius)) {
            if (e instanceof LivingEntity) {
                final LivingEntity le = (LivingEntity) e;

                if (le instanceof Player) {
                    if (u.getTeam().contains(le) || le == p) {
                        continue;
                    }
                }

                le.addPotionEffect(new PotionEffect(PotionEffectType.POISON, (int) effectDuration * 20, 1));
                le.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, (int) effectDuration * 20, 0));
                UtilEnt.damage(6.0, this.getName(), le, p);

            }
        }

        /* Particles */

        this.particleticks.put(p.getUniqueId(), 0);

        new BukkitRunnable() {
            @Override
            public void run() {

                if (Miasma.this.particleticks.get(p.getUniqueId()) < 10) {
                    Miasma.this.particleticks.replace(p.getUniqueId(), Miasma.this.particleticks.get(p.getUniqueId()) + 1);
                } else {
                    Miasma.this.particleticks.remove(p.getUniqueId());
                    this.cancel();
                    return;
                }

                for (int r = (int) radius; r >= 1; r -= 2) {

                    p.getWorld().playSound(p.getLocation(), Sound.HORSE_BREATHE, 1F, 0.75F);

                    for (double degree = 0; degree <= 360; degree += 40) {

                        final Random ry = new Random();
                        final double randomY = (ry.nextInt(200) + 100) / 100;

                        final Location particle = p.getLocation().clone();
                        particle.setX(UtilLoc.getXZCordsFromDegree(particle, r, degree + Miasma.this.particleticks.get(p.getUniqueId()) * 10)[0]);
                        particle.setY(particle.getY() + randomY);
                        particle.setZ(UtilLoc.getXZCordsFromDegree(particle, r, degree + Miasma.this.particleticks.get(p.getUniqueId()) * 10)[1]);

                        UtilParticle.playColoredParticle(particle, 240, 200, 250);

                    }
                }
            }
        }.runTaskTimer(Warriors.getInstance().getPlugin(), 1L, 1L);

        return true;
    }
}
