package me.rey.core.classes.abilities.brute.passive_b;

import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.classes.abilities.IDamageTrigger;
import me.rey.core.events.customevents.combat.DamageEvent;
import me.rey.core.events.customevents.combat.DamagedByEntityEvent;
import me.rey.core.players.User;
import me.rey.core.utils.UtilEnt;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;

public class Colossus extends Ability implements IDamageTrigger.IPlayerDamagedByEntity {


    public Colossus() {
        super(643, "Colossus", ClassType.DIAMOND, AbilityType.PASSIVE_B, 2, 1, 0.0, Arrays.asList(
                "Your huge size now makes you",
                "take 30% less knockback from",
                "all attacks.",
                "",
                "If you're sneaking, you take 0%",
                "knockback from all attacks."
        ));

        this.setIgnoresCooldown(true);
    }

    @Override
    protected boolean execute(User u, Player p, int level, Object... conditions) {

        if (conditions != null && conditions.length > 0 && conditions[0] instanceof DamagedByEntityEvent) {
            DamagedByEntityEvent event = (DamagedByEntityEvent) conditions[0];

            event.setKnockbackMult(event.getDamagee().isSneaking() ? 0 : 0.70);
            return true;
        }

        return false;
    }
}
