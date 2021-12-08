package me.rey.clans.siege.bombs;

import me.rey.clans.Tribes;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;

public class NameTag implements Listener {

    private ArmorStand armorStand = null;
    private boolean spawned = false;

    private String name;

    public NameTag(final String text) {
        this.name = text;
        Bukkit.getServer().getPluginManager().registerEvents(this, Tribes.getInstance().getPlugin());
    }

    public NameTag setName(final String text) {
        this.name = text;

        if (this.spawned) {
            this.armorStand.setCustomName(this.name);
        }
        return this;
    }

    public void spawn(final Location location) {
        if (this.spawned) {
            return;
        }

        final ArmorStand as1 = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        as1.setGravity(false);
        as1.setCanPickupItems(true);
        as1.setCustomName(this.name);
        as1.setCustomNameVisible(true);
        as1.setVisible(false);

        this.spawned = true;
        this.armorStand = as1;
    }

    public void kill() {
        if (this.spawned) {
            this.armorStand.remove();
            this.armorStand = null;
            this.spawned = false;
        }
    }

    public void move(final Location location) {
        if (!this.spawned) {
            return;
        }

        this.armorStand.teleport(location);
    }

    @EventHandler
    public void onEntityDeath(final EntityExplodeEvent e) {
        if (e.getEntity().equals(this.armorStand)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamage(final EntityDamageEvent e) {
        if (e.getEntity().equals(this.armorStand)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onArmorStand(final PlayerArmorStandManipulateEvent e) {
        if (e.getRightClicked().equals(this.armorStand)) {
            e.setCancelled(true);
        }
    }


}
