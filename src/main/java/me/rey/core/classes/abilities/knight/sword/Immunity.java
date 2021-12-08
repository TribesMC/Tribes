package me.rey.core.classes.abilities.knight.sword;

import me.rey.core.Warriors;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.players.User;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class Immunity extends Ability {

    private final List<UUID> players;

    public Immunity() {
        super(301, "Immunity", ClassType.IRON, AbilityType.SWORD, 2, 3, 0.00, Arrays.asList(
                "Gain an immunity period where you",
                "negate all incoming damage. This",
                "effect lasts for <variable>5+l</variable> (+1) seconds.",
                "",
                "Recharge: <variable>2.5*l+30</variable> (+2.5) Seconds."
        ));

        this.players = new ArrayList<>();
    }

    @Override
    protected boolean execute(final User u, final Player p, final int level, final Object... conditions) {
        final int seconds = 5 + level;
        p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, seconds * 20 - 4, 6));
        p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, seconds * 20 - 4, 200, false, false));

        this.players.add(p.getUniqueId());
        new BukkitRunnable() {

            @Override
            public void run() {
                Immunity.this.players.remove(p.getUniqueId());
            }

        }.runTaskLater(Warriors.getInstance().getPlugin(), seconds * 20);

        new BukkitRunnable() {
            final int height = 2;
            final double yIncrement = 0.1;
            final double radius = 0.8;
            boolean up = true;
            double a = 0;
            double x = 0;
            double y = 0;
            double z = 0;

            @Override
            public void run() {
                if (!Immunity.this.players.contains(p.getUniqueId())) {
                    final Location location = p.getEyeLocation();
                    for (double i = 0; i <= Math.PI; i += Math.PI / 10) {
                        final double radius = Math.sin(i) - 0.2;
                        final double y = Math.acos(i);
                        for (double a = 0; a < Math.PI * 2; a += Math.PI / 10) {
                            final double x = Math.cos(a) * radius;
                            final double z = Math.sin(a) * radius;
                            location.add(x, y, z);
                            location.getWorld().spigot().playEffect(location, Effect.COLOURED_DUST, 10, 0, 0F, 0F, 0F, 0F, 3, 100);
                            location.subtract(x, y, z);
                        }
                    }
                    this.cancel();
                }

                final Location loc = p.getLocation();

                this.x = Math.cos(this.a) * this.radius;
                this.z = Math.sin(this.a) * this.radius;

                loc.add(this.x, this.y, this.z);
                loc.getWorld().spigot().playEffect(loc, Effect.FLAME, 0, 0, 0F, 0F, 0F, 0F, 3, 100);
                loc.subtract(this.x, this.y, this.z);

                this.a += 0.3;

                if (this.up) {

                    if (this.y >= this.height) {
                        this.up = false;
                        this.y -= this.yIncrement;
                    } else {
                        this.y += this.yIncrement;
                    }

                } else {
                    if (this.y <= 0) {
                        this.up = true;
                        this.y += this.yIncrement;
                    } else {
                        this.y -= this.yIncrement;
                    }
                }

                if (this.a >= 360) {
                    this.a = 0;
                }
            }

        }.runTaskTimer(Warriors.getInstance().getPlugin(), 0, 1);

        this.sendUsedMessageToPlayer(p, this.getName());
        this.setCooldown(2.5 * level + 30);
        return true;
    }

    @EventHandler
    public void onPlayerDamage(final EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player)) {
            return;
        }
        if (!(this.players.contains(((Player) e.getEntity()).getUniqueId()))) {
            return;
        }

        e.setCancelled(true);
    }

    @EventHandler
    public void onEntityDamageByEntityEvent(final EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player)) {
            return;
        }
        if (!(this.players.contains(((Player) e.getDamager()).getUniqueId()))) {
            return;
        }

        e.setCancelled(true);
    }

    @EventHandler
    public void onPlayerMove(final PlayerMoveEvent e) {
        if (!this.players.contains(e.getPlayer().getUniqueId())) {
            return;
        }

        final Location from = e.getFrom();
        final Location to = e.getTo();
        final double fx = from.getX();
        final double fy = from.getY();
        final double fz = from.getZ();
        final double tx = to.getX();
        final double ty = to.getY();
        final double tz = to.getZ();

        if (fx != tx || fz != tz) {
            if (ty != fy) {
                return;
            }
            e.setTo(from);
        }
    }

}
