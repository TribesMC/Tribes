package me.rey.clans.utils.old;

import me.rey.core.effects.ParticleEffect;
import me.rey.core.utils.Utils;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class UtilParticleExtension {

    public static void makeParticlesBetween(final Location init, final Location loc, final ParticleEffect effect, final double particleSeparation) {
        final Vector pvector = Utils.getDirectionBetweenLocations(init, loc);
        for (double i = particleSeparation; i <= init.distance(loc); i += particleSeparation) {
            pvector.multiply(i);
            init.add(pvector);
            final Location toSpawn = init.clone();
            toSpawn.setY(toSpawn.getY() + 0.5);
            effect.play(toSpawn);
            init.subtract(pvector);
            pvector.normalize();
        }
    }

    public static void playColoredParticle(final Location loc, final float red, final float green, final float blue) {
        loc.getWorld().spigot().playEffect(loc, Effect.COLOURED_DUST, 0, 0, red / 255.0f, green / 255.0f, blue / 255.0f, 1.0f, 0, 30);
    }

    public static void playColoredParticle(final Location loc, final float red, final float green, final float blue, final int particlecount, final int radius) {
        loc.getWorld().spigot().playEffect(loc, Effect.COLOURED_DUST, 0, 0, red / 255.0f, green / 255.0f, blue / 255.0f, 1.0f, particlecount, radius);
    }
}
