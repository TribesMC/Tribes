package me.rey.core.classes.abilities.marksman.sword;

import me.rey.core.Warriors;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.classes.abilities.IDamageTrigger;
import me.rey.core.events.customevents.combat.DamagedByEntityEvent;
import me.rey.core.players.User;
import me.rey.core.utils.UtilVelocity;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

public class Escape extends Ability implements IDamageTrigger, IDamageTrigger.IPlayerDamagedByEntity {
    HashMap<UUID, EscapeProfile> escape = new HashMap<>();

    public Escape() {
        super(401, "Escape", ClassType.CHAIN, AbilityType.SWORD, 1, 4, 15, Arrays.asList(
                "",
                ""
        ));
    }

    @Override
    protected boolean execute(final User u, final Player p, final int level, final Object... conditions) {

        if (conditions.length > 0 && conditions[0] != null && conditions[0] instanceof DamagedByEntityEvent) {
            final Object arg = conditions[0];

            this.setCooldownCanceled(true);

            if (this.escape.containsKey(p.getUniqueId())) {
                if (!this.escape.get(p.getUniqueId()).hit) {
                    if (this.escape.get(p.getUniqueId()).u.getTeam().contains(((DamagedByEntityEvent) arg).getDamager())) {
                        return false;
                    }

                    ((DamagedByEntityEvent) arg).setCancelled(true);
                    ((DamagedByEntityEvent) arg).getDamager().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * this.escape.get(p.getUniqueId()).level, 3));

                    final Location turn = ((DamagedByEntityEvent) arg).getDamager().getLocation().clone();

                    this.escape.get(p.getUniqueId()).hit = true;

                    UtilVelocity.velocity(p, null, turn.getDirection().multiply(2).setY(1.1));
                    p.getWorld().playSound(p.getLocation(), Sound.ITEM_BREAK, 1F, 2F);

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (Escape.this.escape.containsKey(p.getUniqueId())) {
                                Escape.this.escape.remove(p.getUniqueId());
                            }
                        }
                    }.runTaskLaterAsynchronously(Warriors.getInstance().getPlugin(), 60L);

                    this.setCooldownCanceled(false);
                }
            }

            return true;
        }

        if (this.escape.containsKey(p.getUniqueId())) {
            this.setCooldownCanceled(true);
            return true;
        }

        this.setCooldown(10 - level);

        if (this.escape.containsKey(p.getUniqueId()) == false) {
            this.escape.put(p.getUniqueId(), new EscapeProfile(u, level));
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!p.isBlocking() && Escape.this.escape.containsKey(p.getUniqueId())) {
                    if (Escape.this.escape.get(p.getUniqueId()).hit == false) {
                        Escape.this.escape.remove(p.getUniqueId());
                        Escape.this.applyCooldown(p);
                        Escape.this.sendAbilityMessage(p, "Failed to use Escape.");
                        this.cancel();
                    } else {
                        this.cancel();
                    }
                } else {
                    if (Escape.this.escape.containsKey(p.getUniqueId())) {
                        if (Escape.this.escape.get(p.getUniqueId()).ticks >= 20) {
                            this.cancel();
                        } else {
                            Escape.this.escape.get(p.getUniqueId()).ticks++;
                        }
                    } else {
                        this.cancel();
                    }
                }
            }
        }.runTaskTimerAsynchronously(Warriors.getInstance().getPlugin(), 1L, 1L);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (Escape.this.escape.containsKey(p.getUniqueId())) {
                    if (Escape.this.escape.get(p.getUniqueId()).hit == false) {
                        Escape.this.escape.remove(p.getUniqueId());
                        Escape.this.applyCooldown(p);
                        Escape.this.sendAbilityMessage(p, "Failed to use Escape.");
                    }
                }
            }
        }.runTaskLaterAsynchronously(Warriors.getInstance().getPlugin(), 20L);

        this.setCooldownCanceled(true);
        return true;
    }

    @EventHandler
    public void onEntityDamage(final EntityDamageEvent e) {
        if (e.getEntity().getType() == EntityType.PLAYER) {
            if (e.getCause() == EntityDamageEvent.DamageCause.FALL) {
                final Player p = (Player) e.getEntity();
                if (this.escape.containsKey(p.getUniqueId())) {
                    if (this.escape.get(p.getUniqueId()).hit) {
                        e.setCancelled(true);
                        this.escape.remove(p.getUniqueId());
                    }
                }
            }
        }
    }

    class EscapeProfile {

        User u;
        int level;
        boolean hit = false;
        int ticks = 0;

        public EscapeProfile(final User u, final double level) {
            this.u = u;
            this.level = (int) level;
        }
    }

}
