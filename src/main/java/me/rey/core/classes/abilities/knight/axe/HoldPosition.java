package me.rey.core.classes.abilities.knight.axe;

import me.rey.core.Warriors;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.effects.ParticleEffect;
import me.rey.core.effects.SoundEffect;
import me.rey.core.players.User;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class HoldPosition extends Ability {

    private final Map<Player, Long> IN_HOLD_POSITION = new HashMap<>();

    public HoldPosition() {
        super(311, "Hold Position", ClassType.IRON, AbilityType.AXE, 1, 5, 18, Arrays.asList(
                "Hold your position, gaining",
                "Protection 4, Slow 4 and no",
                "knockback for <variable>l+3</variable> (+1) seconds.",
                "",
                "Recharge: <variable>2*l+16</variable> (+2) Seconds"
        ));

        this.setWhileSlowed(true);
    }

    @Override
    protected boolean execute(final User u, final Player p, final int level, final Object... conditions) {
        final int duration = (level + 3) * 20;

        final long desiredCooldownInMs = ((level * 2) + 16) * 1000;
        final long lastUpdateTime = this.IN_HOLD_POSITION.getOrDefault(p, 0L);

        if (System.currentTimeMillis() - lastUpdateTime >= desiredCooldownInMs) {
            this.IN_HOLD_POSITION.put(p, System.currentTimeMillis());

            new SoundEffect(Sound.ENDERMAN_SCREAM, 0F).setVolume(1.5F).play(p.getLocation());
            new ParticleEffect(Effect.STEP_SOUND).setData(49).play(p.getLocation());

            p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, duration, 3, false, false));
            p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, duration, 3, false, false));
            p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, duration, 200, false, false));

            this.sendUsedMessageToPlayer(p, this.getName());

            new BukkitRunnable() {
                final double r = 1;
                int ticks = 0;
                double t = 0;

                @Override
                public void run() {

                    this.t += Math.PI / 8;
                    final double x = this.r * Math.cos(this.t);
                    final double z = this.r * Math.sin(this.t);

                    final Location loc = p.getLocation().clone().add(x, 0, z);

                    new ParticleEffect.ColoredParticle(1, 1, 1).setEffect(Effect.POTION_SWIRL).play(loc);
                    this.ticks++;

                    if (this.ticks >= duration) {
                        HoldPosition.this.IN_HOLD_POSITION.remove(p);
                        this.cancel();
                    }
                }
            }.runTaskTimerAsynchronously(Warriors.getInstance().getPlugin(), 1L, 1L);
            return true;
        }
        return false;
    }

    @EventHandler
    public void onEntityDamage(final EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            final Player player = (Player) e.getEntity();
            if (this.IN_HOLD_POSITION.containsKey(player) && e.getCause() == DamageCause.FALL) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onVelocityEvent(final PlayerVelocityEvent e) {
        if (this.IN_HOLD_POSITION.containsKey(e.getPlayer())) {
            e.setCancelled(true);
        }
    }
}
