package me.rey.core.utils;

import me.rey.core.classes.ClassType;
import me.rey.core.pvp.ToolType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.*;

public class Utils {

    public static ItemStack updateItem(ItemStack item) {
        if (item.getType().equals(Material.AIR)) return item;
        if (item.getMaxStackSize() > 1) return item;

        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) return item;

        String name = null;
        boolean edit = false;
        for (ToolType toolType : ToolType.values()) {
            if (!toolType.getType().equals(item.getType())) continue;
            name = toolType.getName();
            edit = true;
        }

        for (ClassType classType : ClassType.values()) {
            if (ItemUtils.compareLeatherArmor(item, classType.getHelmet().get())) name = classType.getName() + " Helmet";
            if (ItemUtils.compareLeatherArmor(item, classType.getChestplate().get())) name = classType.getName() + " Chestplate";
            if (ItemUtils.compareLeatherArmor(item, classType.getLeggings().get())) name = classType.getName() + " Leggings";
            if (ItemUtils.compareLeatherArmor(item, classType.getBoots().get())) name = classType.getName() + " Boots";
            edit = true;
        }

        ItemMeta meta = item.getItemMeta();
        if (edit) {
            item.setDurability((short) 0);
            meta.spigot().setUnbreakable(true);
            item.setItemMeta(meta);
        }

        if (name == null) return item;
        meta.setDisplayName(ChatColor.RESET + name);
        item.setItemMeta(meta);
        return item;
    }


    public static Vector getDirectionBetweenLocations(Location start, Location end) {
        Vector from = start.toVector();
        Vector to = end.toVector();
        return to.subtract(from);
    }

    public static boolean isInPlayerView(Player player, LivingEntity toFind, double accuracy) {
        final double maxDistance = 200.0;
        final double precision = accuracy;
        final Location nearbyToPlayer = player.getLocation(); // this is the starting point of a ray

        Set<LivingEntity> nearbyToStart = getNearbyEntities(nearbyToPlayer, maxDistance);

        final Vector direction = player.getLocation().getDirection();
        for(int i = 0; i < maxDistance; i += precision) { // then for a distance as big as our specified maximum
            Vector offset = direction.clone().multiply(i);
            Location pointCheck = nearbyToPlayer.clone().add(offset);// we check a lot of points on the line depending on the precision
            Set<LivingEntity> nearbyToPoint = getNearbyEntities(pointCheck, precision, nearbyToStart); // to see if there are players near that point

            if(! nearbyToPoint.isEmpty() && nearbyToPoint.contains(toFind)) { // if there is at least one
                return true; // then that's our target
            }
        }

        return false;
    }

    private static Set<LivingEntity> getNearbyEntities(Location to, double maxDistance) {
        List<Entity> playersInSameWorld = to.getWorld().getEntities();
        List<LivingEntity> le = new ArrayList<LivingEntity>();
        for(Entity e : playersInSameWorld) {
            if(e instanceof LivingEntity)
                le.add((LivingEntity) e);
        }
        return getNearbyEntities(to, maxDistance, le);
    }

    private static Set<LivingEntity> getNearbyEntities(Location to, double maxDistance, Collection<LivingEntity> through) {
        Set<LivingEntity> nearbyPlayers = new HashSet<>();

        for(Entity nearby : through) {
            if(!(nearby instanceof LivingEntity))
                continue;
            if(nearby.getLocation().distanceSquared(to) > maxDistance * maxDistance)
                continue;
            nearbyPlayers.add((LivingEntity) nearby);
        } return nearbyPlayers;
    }

    public static void showPlayer(Player p) {
        for(Player online : Bukkit.getOnlinePlayers()) {
            if(online.getUniqueId().equals(p.getUniqueId())) continue;
            online.showPlayer(p);
        }
    }

    public static void hidePlayer(Player p) {
        for(Player online : Bukkit.getOnlinePlayers()) {
            if(online.getUniqueId().equals(p.getUniqueId())) continue;
            online.hidePlayer(p);
        }
    }

}
