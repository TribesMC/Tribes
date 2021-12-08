package me.rey.core.classes.abilities.bandit.passive_a;

import me.rey.core.Warriors;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.classes.abilities.IConstant.IDroppable;
import me.rey.core.classes.abilities.IDamageTrigger.IPlayerDamagedEntity;
import me.rey.core.events.customevents.combat.CombatKnockbackEvent;
import me.rey.core.events.customevents.combat.DamageEvent;
import me.rey.core.players.User;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class BruteForce extends Ability implements IDroppable, IPlayerDamagedEntity {

    private static final Set<UUID> online = new HashSet<>();

    public BruteForce() {
        super(131, "Brute Force", ClassType.BLACK, AbilityType.PASSIVE_A, 1, 3, 25.00, Arrays.asList(
                "You will now deal knockback to",
                "your enemies and your hits will",
                "deal 60% more damage for the next",
                "<variable>2+l</variable> (+1) Seconds.",
                "",
                "You will also receive Nausea",
                "for <variable>3.5+(0.5*l)</variable> (+0.5) Seconds."
        ));
    }

    @Override
    protected boolean execute(final User u, final Player p, final int level, final Object... conditions) {

        this.setSkipCooldownCheck(true);

        /*
         * DAMAGE CONDITIONS
         */
        if (conditions != null && conditions.length == 1 && conditions[0] != null && conditions[0] instanceof DamageEvent) {
            this.setCooldownCanceled(true);
            if (!online.contains(p.getUniqueId())) {
                return false;
            }

            ((DamageEvent) conditions[0]).addMult(60); /* MULTIPLYING DAMAGE */
            return true;
        }


        /*
         * TOGGLING IT ON
         */
        if (!this.hasCooldown(p)) {
            this.sendUsedMessageToPlayer(p, this.getName());
            online.add(p.getUniqueId());
            p.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, (int) Math.round(20 * (3.5 + (0.5 * level))), 0, false, false));
            new BukkitRunnable() {
                @Override
                public void run() {
                    online.remove(p.getUniqueId());
                }
            }.runTaskLater(Warriors.getInstance().getPlugin(), (int) (20 * (2 + level)));
        } else {
            this.sendCooldownMessage(p);
            this.setCooldownCanceled(true);
        }
        return true;
    }

    @EventHandler
    public void onKB(final CombatKnockbackEvent e) {
        if (!(e.getDamager() instanceof Player)) {
            return;
        }
        if (!new User((Player) e.getDamager()).isUsingAbility(this)) {
            return;
        }

        if (online.contains(e.getDamager().getUniqueId())) {
            e.setCancelled(false); /* UN-CANCELLING IT */
        }
    }

}
