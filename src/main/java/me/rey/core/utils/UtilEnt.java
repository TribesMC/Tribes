package me.rey.core.utils;

import me.rey.core.combat.CombatManager;
import me.rey.core.combat.DamageHandler;
import me.rey.core.events.customevents.combat.CustomDamageEvent;
import me.rey.core.events.customevents.combat.DamageEvent;
import me.rey.core.events.customevents.combat.DamagedByEntityEvent;
import me.rey.core.packets.Nametag;
import me.rey.core.players.combat.PlayerHit;
import me.rey.core.pvp.ToolType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashMap;

public class UtilEnt {

    public static void damage(double damage, String cause, LivingEntity damagee, LivingEntity damager) {
        if (damager instanceof Player && damagee instanceof Player && damager.getUniqueId().equals(damagee.getUniqueId())) return;

        final double originalDamage = damage;
        damage = DamageHandler.calcEffects(damage, damagee, damager);
        damage = DamageHandler.calcArmor(damage, damagee, null);

        CustomDamageEvent event = null;
        if(damager instanceof Player) {
            event = new DamageEvent(ToolType.HitType.OTHER, (Player) damager, damagee, damage, null, null);
            ((Player) damager).setLevel((int) Math.round(originalDamage));

            if(damagee instanceof Player) {
                event.setHit(new PlayerHit((Player) damagee, ((Player) damager).getName(), damage, ChatColor.GREEN.toString() + cause));
                ((DamageEvent) event).storeCache();
            }

        } else if (damagee instanceof Player) {
            event = new DamagedByEntityEvent(ToolType.HitType.OTHER, damager, (Player) damagee, damage, null, null);
            ((DamagedByEntityEvent) event).storeCache();
        }

        boolean allow = true;
        if(event != null) {
            Bukkit.getServer().getPluginManager().callEvent(event);
            if(event.isCancelled()) allow = false;
        }

        if(allow) {
            damagee.setHealth(Math.max(0, Math.min(damagee.getHealth() - damage, damagee.getMaxHealth())));
            CombatManager.resetTime(damagee);

            DamageHandler.playEntitySound(damagee);
        }
    }


    public static boolean isGrounded(Entity ent) {
        if (ent instanceof CraftEntity) {
            return ((CraftEntity)ent).getHandle().onGround;
        }
        return ent.getLocation().getBlock().getRelative(BlockFace.DOWN).getType().isSolid();
    }

    public static boolean isBehind(final Player attacker, final LivingEntity target) {
        if(target instanceof Player) {
            return attacker.getLocation().getDirection().dot(target.getLocation().getDirection()) > 0.8;
        } else {
            return attacker.getLocation().getDirection().dot(target.getLocation().getDirection()) > 0.65;
        }
    }

    public static HashMap<LivingEntity, Double> getEntitiesInSight(Player p, double range, double accuracy) {

        HashMap<LivingEntity, Double> inSight = new HashMap<>();
        Location origin = p.getEyeLocation();
        Vector direction = p.getLocation().getDirection();

        for (double i = 0; i <= range; i+=0.3) {
            double x = direction.getX() * i, y = direction.getY() * i, z = direction.getZ() * i;
            origin.add(x, y, z);

            origin.getWorld().getNearbyEntities(origin, accuracy, accuracy, accuracy).forEach((ent) -> {
                if(ent instanceof LivingEntity) inSight.put((LivingEntity) ent, ent.getLocation().distance(p.getLocation()));
            });

            origin.subtract(x, y , z);
        }

        inSight.remove(p);
        return inSight;
    }

    public static void updatePlayerNameTag(Player player, Player viewer, String team, String namePrefix) {
        Nametag packet = new Nametag(player, team, namePrefix);
        packet.send(viewer);
    }

}
