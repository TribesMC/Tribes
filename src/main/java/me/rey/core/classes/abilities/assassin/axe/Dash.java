package me.rey.core.classes.abilities.assassin.axe;

import me.rey.core.Warriors;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.players.User;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;

public class Dash extends Ability {

    public Dash() {
        super(11, "Dash", ClassType.LEATHER, AbilityType.AXE, 1, 4, 10.00, Arrays.asList(
                "Dash forward at extreme speed,",
                "moving up to 20 blocks.",
                "",
                "Recharge: <variable>0-1*l+12</variable> (-1) Seconds"
        ));

        this.setWhileInAir(false);
    }

    @Override
    protected boolean execute(final User u, final Player p, final int level, final Object... conditions) {

        p.setWalkSpeed(1F);

        this.sendUsedMessageToPlayer(p, this.getName());
        this.setCooldown(12 - level);

        // lava pop PARTICLES
        final int points = 100;
        final double radius = 1.0d;
        final Location pLoc = p.getLocation();
        for (int i = 0; i < points; i++) {
            final double angle = 2 * Math.PI * i / points;
            final Location point = pLoc.clone().add(radius * Math.sin(angle), 0.0d, radius * Math.cos(angle));
            point.getWorld().playEffect(point, Effect.VOID_FOG, Integer.MAX_VALUE);
        }

        // FLAME PARTICLES
        final int periodTicks = 2;
        new BukkitRunnable() {

            int ticks = 0;

            @Override
            public void run() {
                if (this.ticks >= 10) {
                    p.setWalkSpeed(0.2F);
                    this.cancel();
                }

                final Location loc = p.getLocation();
                loc.setY(loc.getY() + 0.4);
                p.getWorld().playEffect(p.getLocation(), Effect.MOBSPAWNER_FLAMES, 1);

                this.ticks = this.ticks + periodTicks;
            }

        }.runTaskTimer(Warriors.getInstance().getPlugin(), 0, periodTicks);
        return true;
    }

}
