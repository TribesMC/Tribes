package me.rey.clans.shops;

import me.rey.clans.Tribes;
import me.rey.core.gui.Gui;
import me.rey.core.packets.Freeze;
import me.rey.core.utils.Text;
import me.rey.parser.Parser;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import java.util.Iterator;

public class ShopNPC implements Listener {

    private final int id;
    private final String NPC_NAME;
    private final Gui GUI;
    private final Parser parser;

    public ShopNPC(final int id, final String npcName, final Gui guiToOpen, final Parser parser) {
        this.id = id;
        this.NPC_NAME = Text.color(npcName);
        this.GUI = guiToOpen;
        this.parser = parser;

        Bukkit.getServer().getPluginManager().registerEvents(this, Tribes.getInstance().getPlugin());
    }

    public int getId() {
        return this.id;
    }

    public Parser getParser() {
        return this.parser;
    }

    public void open(final Player player) {
        this.GUI.open(player);
    }

    public void spawn(final Location location) {
        final Villager villager = (Villager) location.getWorld().spawnEntity(location, EntityType.VILLAGER);
        villager.setProfession(Profession.PRIEST);
        villager.setCanPickupItems(false);


        /*
         * NAME
         */
        final ArmorStand as1 = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        as1.setGravity(false);
        as1.setCanPickupItems(true);
        as1.setCustomName(Text.color(this.NPC_NAME));
        as1.setCustomNameVisible(true);
        as1.setVisible(false);

        new Freeze().send(villager);
    }

    @EventHandler
    public void onInteract(final PlayerInteractEntityEvent e) {
        if (e.getRightClicked().getType() != EntityType.VILLAGER) {
            return;
        }
        if (!this.matches(e.getRightClicked())) {
            return;
        }

        e.setCancelled(true);
        this.open(e.getPlayer());
    }

    @EventHandler
    public void onNPCDamage(final EntityDamageEvent e) {
        if (e.getEntity().getType() != EntityType.VILLAGER) {
            return;
        }
        if (!this.matches(e.getEntity())) {
            return;
        }

        e.setCancelled(true);
    }

    private boolean matches(final Entity villager) {
        if (villager.getNearbyEntities(0, 0, 0).isEmpty()) {
            return false;
        }
        final Iterator<Entity> entities = villager.getNearbyEntities(0, 0, 0).iterator();

        while (entities.hasNext()) {
            final Entity next = entities.next();
            if (next.getCustomName() != null && next.getCustomName().equals(Text.color(this.NPC_NAME))) {
                return true;
            }
        }
        return false;
    }
}