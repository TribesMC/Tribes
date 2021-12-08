package me.rey.core.utils;

import me.rey.core.events.customevents.combat.CustomKnockbackEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;

public class UtilVelocity {

    public static void velocity(Entity ent, @Nullable Entity cause, double mult, double str, double yAdd, double yMax, boolean groundBoost) {
        velocity(ent, cause, ent.getLocation().getDirection(), mult, str, false, 0.0D, yAdd, yMax, groundBoost);
    }

    public static void velocity(Entity ent, @Nullable Entity cause, Vector vec, double mult, double str, boolean ySet, double yBase, double yAdd, double yMax, boolean groundBoost){
        if ((Double.isNaN(vec.getX())) || (Double.isNaN(vec.getY())) || (Double.isNaN(vec.getZ())) || (vec.length() == 0.0D))
            return;

        if (ySet) vec.setY(yBase);

        vec.normalize();
        vec.multiply(str);

        vec.setY(vec.getY() + yAdd);

        if (vec.getY() > yMax) vec.setY(yMax);
        if (groundBoost && ent.isOnGround()) vec.setY(vec.getY() + 0.2D);

        ent.setFallDistance(0.0F);
        velocity(ent, cause, vec.multiply(mult));
    }

    public static void velocity(Entity ent, @Nullable Entity cause, Vector vector) {

        CustomKnockbackEvent kbEvent = new CustomKnockbackEvent(ent, cause, vector);
        Bukkit.getServer().getPluginManager().callEvent(kbEvent);

        if (kbEvent.isCancelled()) {
            return;
        }

        ent.setVelocity(kbEvent.getVector());
    }

    public static Vector getTrajectory(Vector from, Vector to) {
        return to.clone().subtract(from).normalize();
    }

    public static Vector getTrajectory2D(Vector from, Vector to) {
        return to.clone().subtract(from).setY(0).normalize();
    }

}
