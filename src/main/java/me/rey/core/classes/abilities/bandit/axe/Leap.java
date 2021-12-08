package me.rey.core.classes.abilities.bandit.axe;

import me.rey.core.Warriors;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.enums.AbilityFail;
import me.rey.core.events.customevents.ability.AbilityFailEvent;
import me.rey.core.players.User;
import me.rey.core.utils.UtilVelocity;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

public class Leap extends Ability {

    private final ArrayList<UUID> wallkickPlayers;
    String wallkick = "Wall Kick";
    double wallkickCooldown = 0.50;

    public Leap() {
        super(112, "Leap", ClassType.BLACK, AbilityType.AXE, 1, 4, 3.00, Arrays.asList(
                "Take a great leap forwards",
                "",
                "Wall Kick by using Leap with your",
                "back against a wall. This doesn't",
                "trigger Leaps Recharge.",
                "",
                "Cannot be used while Slowed.",
                "Recharge: <variable>4.8-0.2*l</variable> (-0.2) Seconds"));

        this.wallkickPlayers = new ArrayList<>();
        this.setWhileSlowed(false);
    }

    @Override
    protected boolean execute(final User u, final Player p, final int level, final Object... conditions) {
        return this.leap(p, level, false);
    }

    @EventHandler
    public void CooldownEvent(final AbilityFailEvent e) {
        if (e.getAbility() == this && e.getFail() == AbilityFail.COOLDOWN) {
            e.setCancelled(this.leap(e.getPlayer(), e.getLevel(), true));
        }
    }

    private boolean leap(final Player p, final int level, final boolean wallKickOnly) {

        final Location loc = p.getLocation();
        boolean wallKick = false;
        final double mod = 0.7;
        for (double i = -1; i < 2; i++) {
            final Vector vectorBehind = p.getLocation().getDirection().setY(0).multiply(-1 + (i * mod));
            final Block blockBehind = p.getLocation().add(vectorBehind).getBlockY() == p.getLocation().getBlockY() ? p.getLocation().add(vectorBehind).getBlock() : null;
            if (blockBehind != null && blockBehind.getType().isSolid()) {
                wallKick = true;
                break;
            }
        }

        if (!wallKick && wallKickOnly) {
            return false;
        }

        if (wallKick) {
            if (this.wallkickPlayers.contains(p.getUniqueId())) {
                return false;
            }
            this.sendUsedMessageToPlayer(p, this.wallkick);
            this.setMessage(null);
            this.setSound(null, 2F);
            this.setCooldownCanceled(true);

            this.wallkickPlayers.add(p.getUniqueId());

            new BukkitRunnable() {

                @Override
                public void run() {
                    Leap.this.wallkickPlayers.remove(p.getUniqueId());
                }

            }.runTaskLater(Warriors.getInstance().getPlugin(), (int) (this.wallkickCooldown * 20));

            final Vector vec = loc.getDirection();
            vec.setY(0);
            UtilVelocity.velocity(p, null, vec, 1.0, 0.7D, false, 0.0D, 0.7D, 2.0D, true);
            p.setFallDistance(-7);

        } else {

            UtilVelocity.velocity(p, null, 1.0, 1.2D, 0.2D, 1.0D, true);
            UtilVelocity.velocity(p, null, p.getLocation().getDirection().multiply(1.6).add(p.getLocation().getDirection().multiply(new Vector(0.2, 0.05, 0.2))));
            p.setFallDistance(-7);

            this.sendUsedMessageToPlayer(p, this.getName());
        }

        final int points = 20;
        final double radius = 1.0d;
        final Location pLoc = p.getLocation();
        for (int i = 0; i < points; i++) {
            final double angle = 2 * Math.PI * i / points;
            final Location point = pLoc.clone().add(radius * Math.sin(angle), 0.0d, radius * Math.cos(angle));
            point.getWorld().playEffect(point, Effect.SNOWBALL_BREAK, Integer.MAX_VALUE);
        }

        p.getWorld().playEffect(p.getLocation(), Effect.STEP_SOUND, 80);
        p.getWorld().playSound(p.getLocation(), Sound.BAT_TAKEOFF, 2.0F, 1.2F);

        this.setCooldown(4.8 - 0.2 * level);
        return true;
    }


}
