package me.rey.core.classes.abilities.bandit.sword;

import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.effects.SoundEffect;
import me.rey.core.players.User;
import me.rey.core.utils.UtilEnt;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

public class Daze extends Ability {

    public Daze() {
        super(102, "Daze", ClassType.BLACK, AbilityType.SWORD, 2, 1, 8.0, Arrays.asList(
                "Right click an enemy to make them look",
                "in a random direction.",
                "",
                "Recharge: 8 seconds"
        ));
    }

    @Override
    protected boolean execute(User u, Player p, int level, Object... conditions) {

        double RANGE = 5, ACCURACY = 0.3D;

        HashMap<LivingEntity, Double> inSight = UtilEnt.getEntitiesInSight(p, RANGE, ACCURACY);

        LivingEntity toDaze = null;
        for (LivingEntity ent : inSight.keySet())
            if (toDaze == null || ent.getLocation().distance(p.getLocation()) < toDaze.getLocation().distance(p.getLocation()))
                toDaze = ent;

        if (toDaze != null && !u.getTeam().contains(toDaze)) {

            new SoundEffect(Sound.ENDERDRAGON_WINGS, 0.7F).play(toDaze.getLocation());
            toDaze.getWorld().spigot().playEffect(toDaze.getEyeLocation(), Effect.VILLAGER_THUNDERCLOUD);

            this.sendUsedMessageToPlayer(p, this.getName());

            Location loc = toDaze.getLocation().clone();
            loc.setYaw((loc.getYaw() + 90) + new Random().nextInt(180));

            toDaze.teleport(loc);

            if (toDaze instanceof Player) this.sendAbilityMessage(toDaze, "You have been dazed!");
        } else {
            this.setCooldownCanceled(true);
        }

        return false;
    }

}
