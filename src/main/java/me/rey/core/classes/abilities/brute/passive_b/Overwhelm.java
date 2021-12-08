package me.rey.core.classes.abilities.brute.passive_b;

import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.classes.abilities.IDamageTrigger;
import me.rey.core.events.customevents.combat.DamageEvent;
import me.rey.core.players.User;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class Overwhelm extends Ability implements IDamageTrigger.IPlayerDamagedEntity {

    public Overwhelm() {
        super(641, "Overwhelm", ClassType.DIAMOND, AbilityType.PASSIVE_B, 1, 3, 0.0, Arrays.asList(
                "For every 1 health you have more",
                "than your opponent, you deal an",
                "additional 0.25 damage.",
                "",
                "Max Damage: <variable>1+0.5*l</variable> (+0.5) Damage"
        ));

        this.setIgnoresCooldown(true);
    }

    @Override
    protected boolean execute(User u, Player p, int level, Object... conditions) {

        if (conditions != null && conditions.length > 0 && conditions[0] instanceof DamageEvent) {
            DamageEvent event = (DamageEvent) conditions[0];

            long healthDifference = Math.round(event.getDamager().getHealth() - event.getDamagee().getHealth());
            double damageToAdd = Math.min(1 + (0.5 * level), healthDifference*0.25);

            if (damageToAdd <= 0) return false;

            event.addMod(damageToAdd);
            return true;
        }

        return false;
    }

}
