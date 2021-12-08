package me.rey.core.classes.abilities.bandit.sword;

import me.rey.core.Warriors;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.players.User;
import me.rey.core.utils.UtilEnt;
import me.rey.core.utils.UtilLoc;
import me.rey.core.utils.UtilVelocity;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

public class Vortex extends Ability {

    private final double radius = 5.5;

    HashMap<UUID, Double> vortexing = new HashMap<UUID, Double>();

    public Vortex() {
        super(101, "Vortex", ClassType.BLACK, AbilityType.SWORD, 1, 3, 12.0, Arrays.asList(
                "Create a vortex, pulling players into you",
                "and casting players near you afar.",
                "",
                "Players hit by the vortex take <variable>3+l</variable> damage.",
                "",
                "Recharge: <variable>12-(0.5*l)</variable> Seconds."
        ));
    }

    @Override
    protected boolean execute(final User u, final Player p, final int level, final Object... conditions) {

        this.setCooldown(12 - (level * 0.5));

        if (this.vortexing.containsKey(p.getUniqueId()) == false) {
            this.vortexing.put(p.getUniqueId(), 0D);
        }

        new BukkitRunnable() {
            @Override
            public void run() {

                if (Vortex.this.vortexing.containsKey(p.getUniqueId())) {

                    if (Vortex.this.vortexing.get(p.getUniqueId()) >= 4D) {

                        this.cancel();
                        Vortex.this.vortexing.remove(p.getUniqueId());

                    } else {

                        Vortex.this.vortexing.replace(p.getUniqueId(), Vortex.this.vortexing.get(p.getUniqueId()) + 1D);

                        final double ticks = Vortex.this.vortexing.get(p.getUniqueId());

                        p.getWorld().playSound(p.getLocation(), Sound.PIG_DEATH, 1F, 0.65F);
                        p.getWorld().playSound(p.getLocation(), Sound.LAVA_POP, 1F, 1.35F);

                        Vortex.this.playParticles(p.getLocation(), Vortex.this.radius - ticks, false);
                        Vortex.this.playParticles(p.getLocation(), Vortex.this.radius - ticks, true);
                    }
                }
            }
        }.runTaskTimer(Warriors.getInstance().getPlugin(), 1L, 1L);


        for (final Entity e : p.getNearbyEntities(this.radius, 4, this.radius)) {
            if (e instanceof LivingEntity) {
                final double distance = p.getLocation().distance(e.getLocation());
                if (this.inCircle(p, e)) {
                    final LivingEntity le = (LivingEntity) e;


                    UtilEnt.damage(3 + level, this.getName(), le, p);

                    if (distance < 2.6) {
                        this.pushAway(p, e);
                    } else {
                        this.pushIn(p, e);
                    }
                }
            }
        }

        this.sendUsedMessageToPlayer(p, this.getName());
        return false;
    }

    public boolean inCircle(final Player p, final Entity e) {

        final HashMap<Double, double[]> maxmincords = new HashMap<Double, double[]>();

        for (double degree = 0; degree <= 360; degree++) {
            maxmincords.put(degree, UtilLoc.getXZCordsFromDegree(p, this.radius, degree));
        }

        for (double degree = 0; degree <= 90; degree++) {

            final double[] maxcords = maxmincords.get(degree);
            final double[] mincords = maxmincords.get(180 + degree);

            final double maxX = maxcords[0];
            final double maxZ = maxcords[1];

            final double minX = mincords[0];
            final double minZ = mincords[1];

            if (e.getLocation().getX() <= maxX && e.getLocation().getZ() <= maxZ && e.getLocation().getX() >= minX && e.getLocation().getZ() >= minZ) {
                return true;
            } else {
                continue;
            }

        }

        return false;
    }

    public void playParticles(final Location location, final double radius, final boolean rotated) {

        final HashMap<Double, double[]> maxmincords = new HashMap<Double, double[]>();

        for (double degree = 0; degree <= 360; degree++) {
            maxmincords.put(degree, UtilLoc.getXZCordsFromDegree(location, rotated, radius, radius, degree));
        }

        for (double degree = 0; degree <= 360; degree += 4) {
            final double[] cords = maxmincords.get(degree);

            final double xCords = cords[0];
            final double zCords = cords[1];

            final Location loc = location;
            loc.setX(xCords);
            loc.setZ(zCords);

            final float red = 230;
            final float green = 0;
            final float blue = 200;

            location.getWorld().spigot().playEffect(location, Effect.COLOURED_DUST, 0, 0, red / 255, green / 255, blue / 255, 1F, 0, 50);

        }
    }

    public void pushAway(final Player user, final Entity pToPush) {
        final double pX = user.getLocation().getX();
        final double pY = user.getLocation().getY();
        final double pZ = user.getLocation().getZ();

        final double tX = pToPush.getLocation().getX();
        final double tY = pToPush.getLocation().getY();
        final double tZ = pToPush.getLocation().getZ();

        final double deltaX = tX - pX;
        final double deltaY = tY - pY;
        final double deltaZ = tZ - pZ;

        UtilVelocity.velocity(pToPush, user, new Vector(deltaX, deltaY, deltaZ).normalize().multiply(1.25D).setY(0.3D));
    }

    public void pushIn(final Player user, final Entity pToPush) {
        final double pX = user.getLocation().getX();
        final double pY = user.getLocation().getY();
        final double pZ = user.getLocation().getZ();

        final double tX = pToPush.getLocation().getX();
        final double tY = pToPush.getLocation().getY();
        final double tZ = pToPush.getLocation().getZ();

        final double deltaX = tX - pX;
        final double deltaY = tY - pY;
        final double deltaZ = tZ - pZ;

        UtilVelocity.velocity(pToPush, user, new Vector(deltaX, deltaY, deltaZ).normalize().multiply(-1.25D).setY(0.3D));
    }
}
