package me.rey.core.items;

import me.rey.core.Warriors;
import me.rey.core.utils.UtilBlock;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.*;

public class Throwable implements Listener {

    public boolean fired;
    public boolean destroy;
    me.rey.core.gui.Item item;
    Item entityitem;
    boolean pickupable = false;

    public Throwable(final me.rey.core.gui.Item item, final boolean pickupable) {
        this.item = item;
        if (!pickupable) {
            this.item.setName("&4NON-PICKUPABLE");
            Bukkit.getPluginManager().registerEvents(this, Warriors.getInstance().getPlugin());
        } else {
            this.pickupable = true;
        }
    }

    public static Set<Block> checkForBlockCollision(final me.rey.core.items.Throwable item) {

        if (item == null) {
            return null;
        }

        final Item ent = item.getEntityitem();
        if (ent == null) {
            return null;
        }

        final Block self = ent.getLocation().getBlock();

        final Block bOne = self.getRelative(BlockFace.UP);
        final Block bTwo = self.getRelative(BlockFace.DOWN);
        final Block bThree = self.getRelative(BlockFace.WEST);
        final Block bFour = self.getRelative(BlockFace.EAST);
        final Block bFive = self.getRelative(BlockFace.NORTH);
        final Block bSix = self.getRelative(BlockFace.SOUTH);
        final List<Block> list = new ArrayList<>(Arrays.asList(bOne, bTwo, bThree, bFour, bFive, bSix));
        final Set<Block> toReturn = new HashSet<>();

        for (final Block b : list) {
            if (!UtilBlock.airFoliage(b)) {
                toReturn.add(b);
            }
        }


        return toReturn.isEmpty() ? null : toReturn;
    }

    public static Set<LivingEntity> checkForEntityCollision(final Throwable item, final double nearbyX, final double nearbyY, final double nearbyZ) {

        if (item == null) {
            return null;
        }

        final Item ent = item.getEntityitem();
        if (ent == null) {
            return null;
        }

        final Iterator<Entity> nearby = ent.getNearbyEntities(nearbyX, nearbyY, nearbyZ).iterator();
        final Set<LivingEntity> toReturn = new HashSet<>();

        while (nearby.hasNext()) {
            final Entity entity = nearby.next();
            if (entity instanceof Player) {
                final Player entP = (Player) entity;
                if (entP.getGameMode() == GameMode.CREATIVE || entP.getGameMode() == GameMode.SPECTATOR) {
                    continue;
                }
            }
            if (!(entity instanceof LivingEntity)) {
                continue;
            }
            if (entity instanceof ArmorStand) {
                continue;
            }
            toReturn.add((LivingEntity) entity);
        }

        return toReturn.isEmpty() ? null : toReturn;
    }

    public void fire(final Location loc, final double multiplier, final double addY) {
        this.fired = true;
        this.entityitem = loc.getWorld().dropItem(loc, this.item.get());
        final Vector direction = this.entityitem.getLocation().getDirection();
        this.entityitem.setVelocity(direction.normalize().multiply(multiplier).setY(direction.getY() + addY));
    }

    public void fire(final Location loc, final double multiplier, final double baseY, final double addY) {
        this.fired = true;
        this.entityitem = loc.getWorld().dropItem(loc, this.item.get());
        final Vector direction = this.entityitem.getLocation().getDirection();
        this.entityitem.setVelocity(direction.normalize().multiply(multiplier).setY(baseY + addY));
    }

    public void fire(final Location loc, final Vector direction, final double multiplier, final double addY) {
        this.fired = true;
        this.entityitem = loc.getWorld().dropItem(loc, this.item.get());
        this.entityitem.setVelocity(direction.normalize().multiply(multiplier).setY(direction.getY() + addY));
    }

    public void fire(final Location loc, final Vector direction, final double multiplier, final double baseY, final double addY) {
        this.fired = true;
        this.entityitem = loc.getWorld().dropItem(loc, this.item.get());
        this.entityitem.setVelocity(direction.normalize().multiply(multiplier).setY(baseY + addY));
    }

    public void fire(final Location loc, final Vector v) {
        this.fired = true;
        this.entityitem = loc.getWorld().dropItem(loc, this.item.get());
        this.entityitem.setVelocity(v);
    }

    public me.rey.core.gui.Item getItem() {
        return this.item;
    }

    public void setItem(final me.rey.core.gui.Item item) {
        this.item = item;
    }

    public Item getEntityitem() {
        return this.entityitem;
    }

    public void setEntityitem(final Item entityitem) {
        this.entityitem = entityitem;
    }

    public boolean isPickupable() {
        return this.pickupable;
    }

    public void setPickupable(final boolean pickupable) {
        this.pickupable = pickupable;
    }

    public void drop(final Location loc, final boolean naturally) {
        if (!naturally) {
            this.entityitem = loc.getWorld().dropItem(loc, this.getItem().get());
        } else {
            this.entityitem = loc.getWorld().dropItemNaturally(loc, this.getItem().get());
        }
    }

    public void destroyWhenOnGround() {
        if (this.entityitem.isOnGround()) {
            this.destroy();
        }
    }

    public void destroy() {
        this.destroy = true;
        this.entityitem.remove();
    }

    @EventHandler
    public void onPickup(final PlayerPickupItemEvent e) {
        final Item i = e.getItem();
        final ItemStack is = i.getItemStack();

        if (is == null || !is.hasItemMeta() || !is.getItemMeta().hasDisplayName()) {
            return;
        }

        final ItemMeta im = is.getItemMeta();

        if (im.getDisplayName().equals(this.item.getName()) && !this.pickupable) {
            e.setCancelled(true);
        }

    }
}
