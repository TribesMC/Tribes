package me.rey.core.classes.abilities.shaman.axe;

import me.rey.core.Warriors;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.players.User;
import me.rey.core.utils.UtilBlock;
import me.rey.core.utils.UtilLoc;
import me.rey.core.utils.UtilParticle;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

public class Overgrown extends Ability {
    public HashMap<UUID, Integer> ticks = new HashMap<UUID, Integer>();

    public Overgrown() {
        super(512, "Overgrown", ClassType.GREEN, AbilityType.AXE, 1, 3, 10, Arrays.asList(
                "Right clicking grants you Absorption I, while allies receive",
                "Absorption II in a radius of <variable>4.5+0.5*l</variable> (+0.5) blocks.",
                "for 5 seconds.",
                "",
                "Energy: <variable>94-4*l</variable> (-4) ",
                "",
                "Recharge: <variable>10.5-0.5*l</variable> (-0.5) Seconds"
        ));
        this.setEnergyCost(10.5, 0.5);
    }

    @Override
    protected boolean execute(final User u, final Player p, final int level, final Object... conditions) {
        this.setCooldown(10.5 - 0.5 * level);

        final double radius = 4.5 + 0.5 * level;

        for (final Entity e : UtilLoc.getEntitiesInCircle(p.getLocation(), radius)) {
            if (e instanceof Player) {
                final Player ps = (Player) e;

                if (ps == p) {
                    ps.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 20 * 5, 0));
                    continue;
                }

                if (u.getTeam().contains(ps)) {
                    ps.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 20 * 5, 1));
                }
            }
        }

        this.ticks.put(p.getUniqueId(), 0);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (Overgrown.this.ticks.get(p.getUniqueId()) <= 10) {
                    Overgrown.this.ticks.replace(p.getUniqueId(), Overgrown.this.ticks.get(p.getUniqueId()) + 1);
                } else {
                    Overgrown.this.ticks.remove(p.getUniqueId());
                    this.cancel();
                    return;
                }

                p.getWorld().playSound(p.getLocation(), Sound.CREEPER_HISS, 1F, 0.3F);

                for (int degree = 0; degree <= 360; degree += 40) {

                    final Location loc = p.getLocation().clone();

                    final double x = UtilLoc.getXZCordsFromDegree(loc, radius, degree)[0];
                    final double z = UtilLoc.getXZCordsFromDegree(loc, radius, degree)[1];

                    loc.setX(x);
                    loc.setZ(z);

                    UtilParticle.playColoredParticle(loc, 100, 255, 180);

                    if (((double) degree + Overgrown.this.ticks.get(p.getUniqueId()) * 10) % 45 == 0) {
                        for (double i = 0; i <= radius; i += 0.25) {
                            final Location lineloc = p.getLocation().clone();

                            final double x2 = UtilLoc.getXZCordsFromDegree(lineloc, i, degree)[0];
                            final double z2 = UtilLoc.getXZCordsFromDegree(lineloc, i, degree)[1];

                            lineloc.setX(x2);
                            lineloc.setZ(z2);

                            UtilParticle.playColoredParticle(lineloc, 100, 255, 180);

                            final Block b = UtilLoc.highestLocation(lineloc).getBlock();

                            if (b.getType() == Material.AIR &&
                                    (UtilBlock.getBlockUnderneath(b).getType() == Material.GRASS || UtilBlock.getBlockUnderneath(b).getType() == Material.DIRT)) {
                                b.setType(Material.LONG_GRASS);
                                b.setData((byte) 1);

                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        if (b.getType() == Material.LONG_GRASS) {
                                            b.setType(Material.AIR);
                                        }
                                    }
                                }.runTaskLater(Warriors.getInstance().getPlugin(), 40L);

                            }

                        }

                    }

                }

            }
        }.runTaskTimer(Warriors.getInstance().getPlugin(), 0L, 1L);


        return true;
    }
}
