package me.rey.core.classes.abilities.mage.sword;

import me.rey.core.Warriors;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.classes.abilities.IConstant;
import me.rey.core.events.customevents.ability.AbilityRecurEvent;
import me.rey.core.gui.Item;
import me.rey.core.items.Throwable;
import me.rey.core.players.User;
import me.rey.core.utils.UtilEnt;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;

public class Blaze extends Ability implements IConstant {

    public Blaze() {
        super(202, "Blaze", ClassType.GOLD, AbilityType.SWORD, 1, 5, 0.0, Arrays.asList(
                "Right click to shoot fire, dealing ",
                "damage and setting them on fire for",
                "<variable>0.6+0.2*l</variable> (+0.2) seconds.",
                "",
                "Energy: <variable>40-l</variable> (-1)"
        ));

        this.setIgnoresCooldown(true);
        this.setEnergyCost(40 / 20, 1 / 20);
    }

    @Override
    protected boolean execute(final User u, final Player p, final int level, final Object... conditions) {

        this.setEnergyCost(0, 0);

        if (!p.isBlocking()) {
            return false;
        }

        final AbilityRecurEvent recurEvent = new AbilityRecurEvent(p, this, level);
        Bukkit.getPluginManager().callEvent(recurEvent);

        if (recurEvent.isCancelled()) {
            return false;
        }

        this.setEnergyCost(40 / 20, 1 / 20);

        p.getWorld().playSound(p.getLocation(), Sound.GHAST_FIREBALL, 0.5F, 1.2F);

        new BlazeObject(p, u, level);

        return true;
    }

    class BlazeObject {

        final double baseVelocity = 0.5;
        final double velocityPerLevel = 0.15;

        final double igniteDuration = 0.6;
        final double igniteDurationPerLevel = 0.2;

        public BlazeObject(final Player p, final User user, final int level) {
            final Throwable fire = new Throwable(new Item(Material.BLAZE_POWDER).setLore(Arrays.asList(p.getName() + System.currentTimeMillis())), false);
            fire.fire(p.getEyeLocation(), p.getLocation().getDirection(), this.baseVelocity + level * this.velocityPerLevel, 0);

            new BukkitRunnable() {
                @Override
                public void run() {

                    fire.destroyWhenOnGround();

                    if (fire.destroy) {
                        this.cancel();
                        return;
                    }

                    fire.getEntityitem().getWorld().spigot().playEffect(fire.getEntityitem().getLocation(), Effect.LAVADRIP);

                    for (final Entity e : fire.getEntityitem().getNearbyEntities(0.5, 0.5, 0.5)) {

                        if (e instanceof LivingEntity) {

                            final LivingEntity le = (LivingEntity) e;

                            if (le instanceof Player) {
                                if (user.getTeam().contains(le) || le == p) {
                                    continue;
                                }
                            }

                            UtilEnt.damage(0.5, "Blaze", le, p);

                            le.setFireTicks((int) (20 * (BlazeObject.this.igniteDuration + BlazeObject.this.igniteDurationPerLevel)));

                            fire.destroy();
                            this.cancel();
                            return;
                        }
                    }
                }
            }.runTaskTimer(Warriors.getInstance().getPlugin(), 1L, 1L);

        }
    }

}
