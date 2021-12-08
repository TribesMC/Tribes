package me.rey.core.classes.abilities.knight.axe;

import me.rey.core.Warriors;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.classes.abilities.IConstant.IDroppable;
import me.rey.core.classes.abilities.IDamageTrigger.IPlayerDamagedEntity;
import me.rey.core.effects.ParticleEffect;
import me.rey.core.effects.SoundEffect;
import me.rey.core.events.customevents.combat.CombatKnockbackEvent;
import me.rey.core.events.customevents.combat.DamageEvent;
import me.rey.core.players.User;
import org.bukkit.Effect;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Charge extends Ability implements IDroppable, IPlayerDamagedEntity {

    Set<LivingEntity> onCharge = new HashSet<>(), noKB = new HashSet<>();

    public Charge() {
        super(331, "Charge", ClassType.IRON, AbilityType.AXE, 1, 3, 0.00, Arrays.asList(
                "Dropping your sword allows you to",
                "gain Speed II for <variable>3+l</variable> Seconds.",
                "",
                "If you hit someone while you have speed",
                "you deal no KB, but inflict Slow 4",
                "for <variable>2+(0.5*l)</variable> Seconds.", "",
                "Recharge: <variable>9-(0.75*l)</variable> Seconds"
        ));
    }

    @Override
    protected boolean execute(final User u, final Player p, final int level, final Object... conditions) {
        this.setSkipCooldownCheck(true);

        if (conditions != null && conditions.length > 0 && conditions[0] instanceof DamageEvent) {
            this.setCooldownCanceled(true);
            final DamageEvent e = (DamageEvent) conditions[0];

            if (!this.onCharge.contains(p)) {
                return false;
            }

            e.getDamagee().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) Math.round((2 + (0.5 * level)) * 20), 3, false, false));
            e.getDamager().removePotionEffect(PotionEffectType.SPEED);

            this.sendAbilityMessage(e.getDamager(), "You hit " + this.SECONDARY + e.getDamagee().getName() + this.MAIN + " with " + this.VARIABLE + this.getName() + this.MAIN + ".");
            this.sendAbilityMessage(e.getDamagee(), this.SECONDARY + e.getDamager().getName() + this.MAIN + " hit you with " + this.VARIABLE + this.getName() + this.MAIN + ".");

            new SoundEffect(Sound.ENDERMAN_SCREAM, 0F).setVolume(1.5F).play(p.getLocation());
            new SoundEffect(Sound.ZOMBIE_METAL, 0.5F).setVolume(1.5F).play(p.getLocation());

            this.onCharge.remove(p);
            this.noKB.add(p);

            return true;
        }

        if (this.hasCooldown(p)) {
            this.setCooldownCanceled(true);
            this.sendCooldownMessage(p);
            return false;
        }

        final double chargeSeconds = 3 + level;
        this.sendUsedMessageToPlayer(p, this.getName());
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, (int) Math.round(chargeSeconds * 20), 1, false, false));
        this.onCharge.add(p);
        new BukkitRunnable() {
            @Override
            public void run() {
                Charge.this.onCharge.remove(p);
            }
        }.runTaskLater(Warriors.getInstance().getPlugin(), (int) Math.round(chargeSeconds * 20));

        this.setCooldown(9 - (0.75 - level));
        this.applyCooldown(p);

        this.setCooldownCanceled(true);

        new SoundEffect(Sound.ENDERMAN_SCREAM, 0F).setVolume(1.5F).play(p.getLocation());
        new ParticleEffect(Effect.STEP_SOUND).setData(49).play(p.getLocation());

        return true;
    }

    @EventHandler
    public void onBullsCharge(final CombatKnockbackEvent e) {
        if (!(e.getDamager() instanceof Player)) {
            return;
        }

        final Player ent = (Player) e.getDamager();
        if (this.noKB.contains(ent)) {
            e.setCancelled(true);
            this.noKB.remove(ent);
        }
    }

}
