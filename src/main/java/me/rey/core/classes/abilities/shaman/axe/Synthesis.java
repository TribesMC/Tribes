package me.rey.core.classes.abilities.shaman.axe;

import me.rey.core.Warriors;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.players.User;
import me.rey.core.utils.UtilLoc;
import me.rey.core.utils.UtilParticle;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

public class Synthesis extends Ability {
    public HashMap<UUID, Integer> synthTicks = new HashMap<UUID, Integer>();

    public Synthesis() {
        super(511, "Synthesis", ClassType.GREEN, AbilityType.AXE, 1, 4, 12, Arrays.asList(
                "Summon the power of the sun, giving",
                "allies within <variable>4.5+0.5*l</variable> (+0.5) blocks Strength for",
                "<variable>2.5+0.5*l</variable> (+0.5) Seconds",
                "",
                "Energy: 60 ",
                "",
                "Recharge: 12 Seconds"
        ));
        this.setEnergyCost(60, 0);
    }

    @Override
    protected boolean execute(final User u, final Player p, final int level, final Object... conditions) {

        final double radius = 4.5 + 0.5 * level;
        final int duration = (int) (2.5 + 0.5 * level) * 20;
        final Location loc = p.getLocation().clone();

        loc.getWorld().playSound(loc, Sound.CAT_HISS, 1F, 1.5F);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (Synthesis.this.synthTicks.containsKey(p.getUniqueId()) == false) {
                    Synthesis.this.synthTicks.put(p.getUniqueId(), 0);
                    for (final Entity e : UtilLoc.getEntitiesInCircle(p.getLocation(), radius)) {
                        if (e instanceof Player) {
                            final Player p = (Player) e;
                            if (u.getTeam().contains(p)) {
                                p.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, duration, 0));
                            }
                        }
                    }
                } else {

                    if (Synthesis.this.synthTicks.get(p.getUniqueId()) > 5) {
                        Synthesis.this.synthTicks.remove(p.getUniqueId());
                        this.cancel();
                        return;
                    } else {
                        Synthesis.this.synthTicks.replace(p.getUniqueId(), Synthesis.this.synthTicks.get(p.getUniqueId()) + 1);
                    }

                    for (final Location loc : UtilLoc.circleLocations(loc, radius * Synthesis.this.synthTicks.get(p.getUniqueId()) / 5, 5)) {
                        UtilParticle.playColoredParticle(loc, 250F, 250F, 120F);
                    }

                }
            }
        }.runTaskTimer(Warriors.getInstance().getPlugin(), 0L, 1L);

        return true;
    }
}
