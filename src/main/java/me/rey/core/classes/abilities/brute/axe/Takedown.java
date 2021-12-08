package me.rey.core.classes.abilities.brute.axe;

import me.rey.core.Warriors;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.players.User;
import me.rey.core.utils.UtilVelocity;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;

public class Takedown extends Ability {

    public Takedown() {
        super(612, "Takedown", ClassType.DIAMOND, AbilityType.AXE, 1, 5, 18.0D, Arrays.asList(
                "Launch forward, damaging a target",
                "by <variable>5.0+0.5*l</variable> (+0.5) and both of you receiving",
                "Slow IV for <variable>2.7 + l * 0.2</variable> (+0.2) seconds",
                "",
                "Recharge: <variable>19-l</variable> (-1) Seconds"
        ));

        this.setWhileSilenced(true);
    }

    @Override
    protected boolean execute(final User u, final Player p, final int level, final Object... conditions) {

        this.setCooldown(19 - level);
        this.sendUsedMessageToPlayer(p, this.getName());

        UtilVelocity.velocity(p, null, p.getLocation().getDirection(), 1, 1.3, false, 0, 0.3, 0.4, false);

        p.getWorld().playSound(p.getLocation(), Sound.DIG_GRASS, 1.2F, 0.8F);
        p.getWorld().playSound(p.getLocation(), Sound.ENDERDRAGON_WINGS, 1.2F, 0.8F);

        new BukkitRunnable() {
            int ticksinair = 0;
            int stillValid = 0;

            @Override
            public void run() {

                if (p.isOnGround() || this.ticksinair >= 60) {
                    if (this.stillValid < 5) {
                        this.stillValid++;
                    } else {
                        this.cancel();
                        return;
                    }
                } else {
                    this.ticksinair++;
                }

                for (final Entity en : p.getWorld().getNearbyEntities(p.getLocation(), 1, 1, 1)) {
                    if (en instanceof LivingEntity) {
                        final LivingEntity le = (LivingEntity) en;

                        if (le.getUniqueId().equals(p.getUniqueId()) || u.getTeam().contains(en)) {
                            continue;
                        }

                        le.damage(2 + 0.5 * level);

                        final float duration = 2.7F + level * 0.2F;
                        final PotionEffect slow = new PotionEffect(PotionEffectType.SLOW, (int) duration * 20, 3);

                        le.addPotionEffect(slow);
                        p.addPotionEffect(slow);
                        p.getWorld().playSound(p.getLocation(), Sound.ZOMBIE_WOOD, 0.8F, 0.5F);

                        if (le instanceof Player) {
                            Takedown.this.sendAbilityMessage(le, "&s" + p.getName() + " &rhit you with &g" + Takedown.this.getName() + " " + level + "&r.");
                        }
                        Takedown.this.sendAbilityMessage(p, "You hit &s" + le.getName() + "&r with &g" + Takedown.this.getName() + " " + level + "&r.");

                        this.stillValid++;
                        this.cancel();
                        return;
                    }
                }

            }
        }.runTaskTimer(Warriors.getInstance().getPlugin(), 5L, 1L);

        return true;
    }

}
