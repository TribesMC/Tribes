package me.rey.core.classes.abilities.marksman.bow;

import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.classes.abilities.IBowPreparable;
import me.rey.core.events.customevents.ability.BowAbilityHitEvent;
import me.rey.core.players.User;
import me.rey.core.utils.UtilVelocity;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class GrapplingArrow extends Ability implements IBowPreparable, IBowPreparable.IBowEvents {

    private Set<UUID> prepared = new HashSet<>(), shot = new HashSet<UUID>();

    public GrapplingArrow() {
        super(421 , "Grappling Arrow", ClassType.CHAIN, AbilityType.BOW, 1, 3, 8.00, Arrays.asList(
                "Your next arrow will pull",
                "you towards it on impact at",
                "a <variable>10+10*l</variable>% (+10) velocity.",
                "",
                "Recharge: <variable>8-l</variable> Seconds"
        ));

    }

    @Override
    protected boolean execute(User u, Player p, int level, Object... conditions) {
        this.setCooldown(8-level);

        if (conditions.length == 1 && conditions[0] instanceof PlayerInteractEvent && !prepared.contains(p.getUniqueId())) {
            this.prepare(p);
            this.setCooldownCanceled(true);
            return true;
        }

        if (conditions.length == 1 && conditions[0] instanceof BowAbilityHitEvent) {
            this.setCooldownCanceled(false);
            BowAbilityHitEvent event = (BowAbilityHitEvent) conditions[0];
            event.setMessageForDamagee(null);
            event.setMessageForShooter(null);

            Location arrowLoc = event.getLandingLocation();
            double mult = (event.getArrowVelocity().length() / 3d);
            Vector vec = UtilVelocity.getTrajectory(event.getPlayer().getLocation().toVector(), arrowLoc.toVector());

            // Entity ent, Vector vec, double mult, double str, boolean ySet, double yBase, double yAdd, double yMax, boolean groundBoost
            UtilVelocity.velocity(event.getPlayer(), p, vec, 1, 0.4 + mult, false, 0, 0.3 * mult, 1.2 * mult, true);
            return true;
        }

        this.setCooldownCanceled(true);
        return false;
    }

    @Override
    public boolean prepare(Player player) {
        return prepared.add(player.getUniqueId());
    }

    @Override
    public boolean isPrepared(Player player) {
        return prepared.contains(player.getUniqueId());
    }

    @Override
    public boolean unprepare(Player player) {
        return prepared.remove(player.getUniqueId());
    }

    @Override
    public boolean shoot(Player player) {
        return shot.add(player.getUniqueId());
    }

    @Override
    public boolean hasShot(Player player) {
        return shot.contains(player.getUniqueId());
    }

    @Override
    public boolean unshoot(Player player) {
        return shot.remove(player.getUniqueId());
    }

}
