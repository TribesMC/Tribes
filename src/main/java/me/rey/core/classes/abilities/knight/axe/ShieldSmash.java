package me.rey.core.classes.abilities.knight.axe;

import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.effects.ParticleEffect;
import me.rey.core.effects.SoundEffect;
import me.rey.core.players.User;
import me.rey.core.utils.UtilEnt;
import me.rey.core.utils.UtilVelocity;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

public class ShieldSmash extends Ability {

    public ShieldSmash() {
        super(312, "Shield Smash", ClassType.IRON, AbilityType.AXE, 1, 5, 20.00, Arrays.asList(
                "Smash your enemies with your",
                "shield and deal <variable>50+l*10</variable>% (+10%) knockback",
                "to them.",
                "",
                "Recharge: <variable>20-l</variable> (-1) Seconds"
        ));
    }

    @Override
    protected boolean execute(User u, Player p, int level, Object... conditions) {

        this.setCooldown(20-level);
        this.sendUsedMessageToPlayer(p, this.getName());

        double mult = 1 + (0.50+level*(0.1));

        HashMap<LivingEntity, Double> inSight = UtilEnt.getEntitiesInSight(p, 2, 0.5);
        for (LivingEntity le : inSight.keySet()) {
            le.setFallDistance(-1);
            UtilVelocity.velocity(le, p, p.getLocation().getDirection().clone(), 1.5, mult, true, 0.3, 0, 1.2, true);
        }

        if (!inSight.isEmpty()) new SoundEffect(Sound.ZOMBIE_METAL, 0.9f).play(p.getLocation());
        else new SoundEffect(Sound.ZOMBIE_METAL, 0.4f).play(p.getLocation());

        Location loc = p.getEyeLocation().clone().add(p.getEyeLocation().clone().getDirection().multiply(0.3));
        Random rand = new Random();
        for (int i = 0; i < 5; i++) {
            new ParticleEffect(Effect.CLOUD).setOffset(rand.nextFloat(), rand.nextFloat(), rand.nextFloat()).play(loc);
            new ParticleEffect(Effect.SNOWBALL_BREAK).setOffset(rand.nextFloat(), rand.nextFloat(), rand.nextFloat()).play(loc);
        }

        return true;
    }

}
