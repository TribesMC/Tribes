package me.rey.core.classes.abilities.assassin.passive_b;

import me.rey.core.Warriors;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.classes.abilities.IDamageTrigger.IPlayerDamagedEntity;
import me.rey.core.effects.repo.Shock;
import me.rey.core.events.customevents.combat.DamageEvent;
import me.rey.core.players.User;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class BlitzStrikes extends Ability implements IPlayerDamagedEntity {

    private final double bleedCooldown = 4.0;

    Set<Player> onCooldown = new HashSet<>();

    public BlitzStrikes() {
        super(041, "Blitz Strikes", ClassType.LEATHER, AbilityType.PASSIVE_B, 1, 3, 0.00, Arrays.asList(
                "Each hit shocks your current enemy",
                "for <variable>1+l</variable> Seconds!"
        ));

        this.setIgnoresCooldown(true);
    }

    @Override
    protected boolean execute(final User u, final Player p, final int level, final Object... conditions) {
        final DamageEvent e = ((DamageEvent) conditions[0]);
        final double shockTime = 1 + level;

        if (e.isCancelled()) {
            return false;
        }

        if (!this.onCooldown.contains(p)) {
            this.onCooldown.add(p);

            new BukkitRunnable() {
                @Override
                public void run() {
                    BlitzStrikes.this.onCooldown.remove(p);
                }

            }.runTaskLaterAsynchronously(Warriors.getInstance().getPlugin(), (int) (20 * this.bleedCooldown));
        }


        new Shock().apply(e.getDamagee(), shockTime);
        return true;
    }

}
