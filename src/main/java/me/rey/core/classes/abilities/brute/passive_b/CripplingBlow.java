package me.rey.core.classes.abilities.brute.passive_b;

import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.classes.abilities.IDamageTrigger;
import me.rey.core.events.customevents.combat.DamageEvent;
import me.rey.core.players.User;
import me.rey.core.utils.UtilEnt;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;

public class CripplingBlow extends Ability implements IDamageTrigger.IPlayerDamagedEntity {

    public CripplingBlow() {
        super(642, "Crippling Blow", ClassType.DIAMOND, AbilityType.PASSIVE_B, 2, 1, 0.0, Arrays.asList(
                "Hits with your axe will now apply",
                "Slowness 2 for 2.5 Seconds to your",
                "opponent and deal 1 less damage.",
                "",
                "Hits from behind the opponent deal",
                "no knockback.,"
        ));

        this.setIgnoresCooldown(true);
    }

    @Override
    protected boolean execute(User u, Player p, int level, Object... conditions) {

        if (conditions != null && conditions.length > 0 && conditions[0] instanceof DamageEvent) {
            DamageEvent event = (DamageEvent) conditions[0];

            if (!event.getDamager().getItemInHand().getType().name().toUpperCase().contains("AXE")) return false;

            PotionEffect slow = new PotionEffect(PotionEffectType.SLOW, (int) (20 * (2.5)), 1);
            event.getDamagee().addPotionEffect(slow);

            if (UtilEnt.isBehind(event.getDamager(), event.getDamagee())) {
                event.setKnockbackMult(0);
            }

            event.addMod(-1);
            return true;
        }

        return false;
    }

}
