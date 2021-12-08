package me.rey.core.events;

import me.rey.core.classes.abilities.Ability;
import me.rey.core.events.customevents.ability.*;
import me.rey.core.events.customevents.combat.DeathEvent;
import me.rey.core.players.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ServerEvents implements Listener {

    @EventHandler
    public void onShootBow(EntityShootBowEvent e) {
        if (!(e.getEntity() instanceof Player)) return;

        Player p = (Player) e.getEntity();
        User user = new User(p);
        boolean disallowShot = user.getWearingClass() == null || !user.getWearingClass().canUseBow();

        if (disallowShot) {
            e.setCancelled(true);

            if (user.getWearingClass() != null) user.sendMessageWithPrefix("Projectile", "&s" + user.getWearingClass().getName() + " &rcannot use a bow!");
            else user.sendMessageWithPrefix("Projectile", "You do not have a class equipped!");
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        PlayerUpdateAbilitiesEvent event = new PlayerUpdateAbilitiesEvent(e.getPlayer());
        Bukkit.getServer().getPluginManager().callEvent(event);
    }

    @EventHandler
    public void onClassSwitch(PlayerClassUpdateEvent e) {
        PlayerUpdateAbilitiesEvent event = new PlayerUpdateAbilitiesEvent(e.getPlayer());
        Bukkit.getServer().getPluginManager().callEvent(event);
    }

    @EventHandler
    public void onDeath(DeathEvent e) {
        PlayerUpdateAbilitiesEvent event = new PlayerUpdateAbilitiesEvent(e.getPlayer());
        Bukkit.getServer().getPluginManager().callEvent(event);
    }

    @EventHandler
    public void onSelectBuild(PlayerBuildSelectEvent e) {
        PlayerUpdateAbilitiesEvent event = new PlayerUpdateAbilitiesEvent(e.getPlayer());
        Bukkit.getServer().getPluginManager().callEvent(event);
    }

    @EventHandler
    public void onEditBuild(PlayerBuildEditEvent e) {
        PlayerUpdateAbilitiesEvent event = new PlayerUpdateAbilitiesEvent(e.getPlayer());
        Bukkit.getServer().getPluginManager().callEvent(event);
    }

}
