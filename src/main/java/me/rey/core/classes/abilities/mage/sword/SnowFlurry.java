package me.rey.core.classes.abilities.mage.sword;

import me.rey.core.Warriors;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.classes.abilities.IConstant;
import me.rey.core.players.User;
import me.rey.core.utils.UtilLoc;
import me.rey.core.utils.UtilVelocity;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.Random;

public class SnowFlurry extends Ability implements IConstant {

    final double baseAmount = 0;
    final double amountPerLevel = 1;

    public SnowFlurry() {
        super(203, "Snow Flurry", ClassType.GOLD, AbilityType.SWORD, 1, 3, 0.0, Arrays.asList(
                "Fire a pile of <variable>1*l</variable> (+1) snowballs, pushing hit",
                "enemies into the air.",
                "",
                "Energy: <variable>40-l</variable> (-1) per second."
        ));
        this.setIgnoresCooldown(true);
        this.setEnergyCost(40 / 20, 1 / 20);
    }

    @Override
    protected boolean execute(final User u, final Player p, final int level, final Object... conditions) {

        this.setEnergyCost(0, 0);

        if (!p.isBlocking()) {
            return false;
        }

        this.setEnergyCost(40 / 20, 1 / 20);

        for (int i = 1; i <= this.baseAmount + this.amountPerLevel * level; i++) {
            new FlurryObject(p, u, level);
        }

        return true;
    }

    @EventHandler
    public void onDamage(final EntityDamageByEntityEvent e) {
        if (e.getDamager().getType() == EntityType.SNOWBALL && e.getDamager().getCustomName().contains("§4FL")) {
            e.setCancelled(true);
        }
    }

    class FlurryObject {

        final double maxspreadH = 0.25;
        final double maxspreadY = 0.25;
        Player p;
        User u;
        int level;
        Snowball flurry;

        public FlurryObject(final Player p, final User u, final int level) {
            this.p = p;
            this.u = u;
            this.level = level;

            final Location origin = p.getLocation();

            final Location loc = p.getEyeLocation();
            final double yMultiplier = Math.sqrt(1 - Math.pow(UtilLoc.getYCordsMultiplierByPitch(p.getLocation().getPitch()), 2));
            loc.setX(UtilLoc.getXZCordsFromDegree(p.getLocation(), yMultiplier, p.getLocation().getYaw() + 90)[0]);
            loc.setY(loc.getY() + UtilLoc.getYCordsMultiplierByPitch(p.getLocation().getPitch()) - 0.5);
            loc.setZ(UtilLoc.getXZCordsFromDegree(p.getLocation(), yMultiplier, p.getLocation().getYaw() + 90)[1]);

            final double xMultiplier = UtilLoc.getXZCordsMultipliersFromDegree(p.getLocation().getYaw() + 90 + 45)[0];
            final double zMultiplier = UtilLoc.getXZCordsMultipliersFromDegree(p.getLocation().getYaw() + 90 + 45)[1];

            final Random sx = new Random();
            double xspread = (double) sx.nextInt((int) (this.maxspreadH * 1000)) / 1000;

            final Random sz = new Random();
            double zspread = (double) sz.nextInt((int) (this.maxspreadH * 1000)) / 1000;

            final Random pnH = new Random();
            final boolean positiveH = pnH.nextBoolean();

            if (!positiveH) {
                xspread = -xspread;
                zspread = -zspread;
            }

            final Random sy = new Random();
            final double yspread = (double) sy.nextInt((int) (this.maxspreadY * 1000)) / 1000;

            loc.setX(loc.getX() + xspread * xMultiplier);
            loc.setY(loc.getY() + yspread);
            loc.setZ(loc.getZ() + zspread * zMultiplier);

            this.flurry = (Snowball) p.getWorld().spawnEntity(loc, EntityType.SNOWBALL);
            this.flurry.setCustomName("§4FL");

            /* Vertical random angle spread */

            final Random pnyr = new Random();

            final Random rdy = new Random();
            double randomPitchDegree = rdy.nextInt(10); /* Minimum curve 10, max 25 */

            if (!pnyr.nextBoolean()) {
                randomPitchDegree = -randomPitchDegree;
            }

            origin.setPitch(origin.getPitch() + (float) randomPitchDegree);

            /* Vertical random angle spread */

            /* Horizontal random angle spread */

            final Random pnhr = new Random();

            final Random rdh = new Random();
            double randomYawDegree = rdh.nextInt(10); /* Minimum curve 10, max 25 */

            if (!pnhr.nextBoolean()) {
                randomYawDegree = -randomYawDegree;
            }

            origin.setYaw(origin.getYaw() + (float) randomYawDegree);

            /* Horizontal random angle spread */

            final Vector direction = origin.getDirection();
            UtilVelocity.velocity(this.flurry, null, direction.normalize().multiply(1));

            this.flurry.getWorld().playSound(this.flurry.getLocation(), Sound.HORSE_BREATHE, 1F, 1.25F);

            new BukkitRunnable() {
                @Override
                public void run() {

                    for (final Entity e : FlurryObject.this.flurry.getWorld().getNearbyEntities(FlurryObject.this.flurry.getLocation(), 0.5, 1.5, 0.5)) {
                        if (e instanceof LivingEntity) {
                            if (e != p) {
                                UtilVelocity.velocity(e, p, FlurryObject.this.flurry.getVelocity().normalize().multiply(0.15).setY(0.15));
                                e.setFallDistance(0);
                            }
                        }
                    }

                    if (origin.distance(FlurryObject.this.flurry.getLocation()) >= 15 || FlurryObject.this.flurry.isDead()) {
                        FlurryObject.this.flurry.remove();
                        this.cancel();
                        return;
                    }
                }
            }.runTaskTimer(Warriors.getInstance().getPlugin(), 1L, 1L);

        }

    }

}
