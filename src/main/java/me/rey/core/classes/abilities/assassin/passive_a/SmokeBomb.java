package me.rey.core.classes.abilities.assassin.passive_a;

import me.rey.core.Warriors;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.classes.abilities.IConstant.IDroppable;
import me.rey.core.events.customevents.CustomPlayerInteractEvent;
import me.rey.core.events.customevents.ability.AbilityUseEvent;
import me.rey.core.events.customevents.update.UpdateEvent;
import me.rey.core.players.User;
import me.rey.core.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SmokeBomb extends Ability implements IDroppable {

    private final double particleInterval = 0.7;
    private final Set<UUID> enabled = new HashSet<>();

    public SmokeBomb() {
        super(32, "Smoke Bomb", ClassType.LEATHER, AbilityType.PASSIVE_A, 1, 3, 25, Arrays.asList(
                "Gain <variable>6.5+(0.5*l)</variable> (+0.5) seconds of",
                "Invisibility. While in Smoke Bomb, you",
                "leave a trail of smoke behind you.", "",
                "Recharge: <variable>27-(2*l)</variable> (-2) Seconds."
        ));
    }

    @Override
    protected boolean execute(final User u, final Player p, final int level, final Object... conditions) {

        final double seconds = (6.5 + 0.5 * level);

        this.enabled.add(p.getUniqueId());
        p.removePotionEffect(PotionEffectType.INVISIBILITY);
        p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, (int) (20 * seconds), 1, true, false));
        Utils.hidePlayer(p);
        this.sendAbilityMessage(p, "You are now invisible.");

        // Explosion Particle & Sound
        p.getWorld().spigot().playEffect(p.getLocation(), Effect.EXPLOSION_LARGE, 0, 0, 0F, 0F, 0F, 0F, 1, 50);
        p.getWorld().playSound(p.getLocation(), Sound.EXPLODE, 1, 1);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (SmokeBomb.this.enabled.contains(p.getUniqueId())) {
                    SmokeBomb.this.sendNoLongerInvis(p);
                }
            }
        }.runTaskLater(Warriors.getInstance().getPlugin(), (int) (seconds * 20));

        new BukkitRunnable() {

            @Override
            public void run() {

                if (!SmokeBomb.this.enabled.contains(p.getUniqueId())) {
                    this.cancel();
                    return;
                }

                Utils.hidePlayer(p);

                final Location toSpawn = p.getLocation().getBlock().getLocation();
                toSpawn.setX(toSpawn.getX() + 0.5);
                toSpawn.setZ(toSpawn.getZ() + 0.5);
                p.getWorld().spigot().playEffect(toSpawn, Effect.PARTICLE_SMOKE, 0, 0, 0F, 0.35F, 0F, 0F, 7, 50);
            }
        }.runTaskTimer(Warriors.getInstance().getPlugin(), 0, (int) (20 * this.particleInterval));


        this.setCooldown(27 - (2 * level));
        return true;
    }

    @Override
    @EventHandler
    public void onUpdate(final UpdateEvent e) {

        for (final Player player : Bukkit.getOnlinePlayers()) {

            if (!new User(player).isUsingAbility(this)) {
                if (this.enabled.contains(player.getUniqueId())) {
                    this.sendNoLongerInvis(player);
                }
                continue;
            }

            if (this.enabled.contains(player.getUniqueId())) {
                new User(player).resetCombatTimer();
            }

        }

    }

    @EventHandler
    public void onDamage(final CustomPlayerInteractEvent e) {
        if (!this.enabled.contains(e.getPlayer().getUniqueId())) {
            return;
        }
        if (e.getEvent() instanceof AbilityUseEvent && ((AbilityUseEvent) e.getEvent()).getAbility() instanceof SmokeBomb) {
            return;
        }

        this.sendNoLongerInvis(e.getPlayer());
    }

    private void sendNoLongerInvis(final Player p) {
        Utils.showPlayer(p);
        this.enabled.remove(p.getUniqueId());
        p.removePotionEffect(PotionEffectType.INVISIBILITY);
        this.sendAbilityMessage(p, "You are no longer invisible.");
    }
}
