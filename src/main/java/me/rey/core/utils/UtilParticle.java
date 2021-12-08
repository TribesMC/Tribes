package me.rey.core.utils;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.util.Objects;

import me.rey.core.effects.ParticleEffect;
import org.bukkit.Effect;
import org.bukkit.util.Vector;

public class UtilParticle {
	public static void showParticle(Player player, Location location, int typeAsInt, float xoffset, float yoffset, float zoffset, float speed, int amount, int... moreData) {
		try {
			Object particleEnum = Objects.requireNonNull(UtilPacket.getClassNMS("EnumParticle")).getEnumConstants()[typeAsInt];
			Constructor<?> particlePacket = Objects.requireNonNull(UtilPacket.getClassNMS("PacketPlayOutWorldParticles").getConstructor(Objects.requireNonNull(UtilPacket.getClassNMS("EnumParticle")), boolean.class, float.class, float.class, float.class, float.class, float.class, float.class, float.class, int.class, int[].class));
			Object packet = particlePacket.newInstance(particleEnum, true, (float) location.getX(), (float) location.getY(), (float) location.getZ(), xoffset, yoffset, zoffset, speed, amount, moreData);
			UtilPacket.sendPacket(player, packet);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void makeParticlesBetween(Location init, Location loc, ParticleEffect effect, double particleSeparation) {
		Vector pvector = Utils.getDirectionBetweenLocations(init, loc);
		for(double i = particleSeparation; i <= init.distance(loc); i += particleSeparation) {
			pvector.multiply(i);
			init.add(pvector);
			Location toSpawn = init.clone();
			toSpawn.setY(toSpawn.getY() + 0.5);

			effect.play(toSpawn);

			init.subtract(pvector);
			pvector.normalize();
		}
	}

	public static void playColoredParticle(Location loc, float red, float green, float blue) {
		loc.getWorld().spigot().playEffect(loc, Effect.COLOURED_DUST, 0, 0, red/255, green/255, blue/255, 1F, 0, 30);
	}

	public static void playColoredParticle(Location loc, float red, float green, float blue, int particlecount, int radius) {
		loc.getWorld().spigot().playEffect(loc, Effect.COLOURED_DUST, 0, 0, red/255, green/255, blue/255, 1F, particlecount, radius);
	}
}