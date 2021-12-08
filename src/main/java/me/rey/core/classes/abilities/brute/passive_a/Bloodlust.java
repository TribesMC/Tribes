package me.rey.core.classes.abilities.brute.passive_a;

import me.rey.core.Warriors;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.events.customevents.ability.PlayerUpdateAbilitiesEvent;
import me.rey.core.events.customevents.combat.DeathEvent;
import me.rey.core.players.User;
import me.rey.core.players.combat.PlayerHit;
import me.rey.core.utils.UtilLoc;
import me.rey.core.utils.UtilParticle;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Bloodlust extends Ability {

    /* STATS */
    final int duration = 6; /* in seconds */
    Map<UUID, BloodlustProfile> bloodlustProfiles = new HashMap<>();

    public Bloodlust() {
        super(631, "Bloodlust", ClassType.DIAMOND, AbilityType.PASSIVE_A, 1, 3, 0.00, Arrays.asList(
                "Killing an enemy puts you into",
                "bloodlust, you gain Speed I and",
                "Strength I for <variable>6+l*2</variable> (+2) seconds.",
                "and also heal by <variable>1+l</variable> (+1) health."));
        this.setIgnoresCooldown(true);
        this.setWhileSlowed(true);
        this.setInLiquid(true);
    }

    @Override
    protected boolean execute(final User u, final Player p, final int level, final Object... conditions) {

        final BloodlustProfile blp;

        if (this.bloodlustProfiles.containsKey(p.getUniqueId())) {
            blp = this.bloodlustProfiles.get(p.getUniqueId());
        } else {
            blp = new BloodlustProfile(p);
            this.bloodlustProfiles.put(p.getUniqueId(), blp);
        }

        blp.addStack(level);

        return true;
    }

    @EventHandler
    public void onUpdateAbilities(final PlayerUpdateAbilitiesEvent e) {
        if (this.bloodlustProfiles.containsKey(e.getPlayer().getUniqueId())) {
            this.bloodlustProfiles.get(e.getPlayer().getUniqueId()).cancelBloodlust();
            this.bloodlustProfiles.remove(e.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onDeath(final DeathEvent e) {
        final PlayerHit hit = e.getLastHit();
        if (hit != null && hit.isCausedByPlayer()) {
            final Player p = Bukkit.getServer().getPlayer(hit.getDamager());
            if (p == null || !p.isOnline()) {
                return;
            }

            final User u = new User(p);

            if (!u.isUsingAbility(this)) {
                return;
            }
            super.run(u.getPlayer(), null, true);
        }
    }

    class BloodlustProfile {

        Player p;
        int stacks = 0;
        long wearoffCD = 0L;
        BukkitTask wearoff;
        BukkitTask visuals;

        public BloodlustProfile(final Player p) {
            this.p = p;
        }

        void cancelBloodlust() {
            if (this.p != null && this.p.isOnline()) {
                this.p.removePotionEffect(PotionEffectType.SPEED);
                this.p.removePotionEffect(PotionEffectType.INCREASE_DAMAGE);

                for (final Location s : UtilLoc.circleLocations(this.p.getLocation().add(0, 1.5, 0), 2, 90)) {
                    this.p.getWorld().playSound(s, Sound.ZOMBIE_PIG_DEATH, 0.25F, 1F);
                }

                if (this.stacks > 0) {
                    new User(this.p).sendMessageWithPrefix(Bloodlust.this.getName(), "&e" + Bloodlust.this.getName() + "&r ended at level &g" + this.stacks + "&r.");
                }
            }

            this.stacks = 0;

            if (this.visuals != null) {
                this.visuals.cancel();
                this.visuals = null;
            }
            if (this.wearoff != null) {
                this.wearoff.cancel();
                this.wearoff = null;
            }
        }

        void addStack(final int level) {
            this.p = Bukkit.getPlayer(this.p.getUniqueId());
            if (this.p == null || !this.p.isOnline()) {
                this.cancelBloodlust();
                return;
            }

            this.stacks = Math.min(Math.max(1, this.stacks + 1), level);

            /* reset cooldown */
            if (this.wearoff != null) {
                this.wearoff.cancel();
            }

            /* effect */
            final double time = 6 + level * 2;
            final PotionEffect speed = new PotionEffect(PotionEffectType.SPEED, (int) Math.floor(20 * time), this.stacks - 1);
            final PotionEffect strength = new PotionEffect(PotionEffectType.INCREASE_DAMAGE, (int) Math.floor(20 * time), this.stacks - 1);

            final double health = this.p.getHealth() + (1 + level);
            if (health < this.p.getMaxHealth()) {
                this.p.setHealth(health);
            }

            for (final Location s : UtilLoc.circleLocations(this.p.getLocation().add(0, 1.5, 0), 2, 90)) {
                this.p.getWorld().playSound(s, Sound.ZOMBIE_PIG_ANGRY, 0.25F, 1.0F + (this.stacks * 0.33F));
            }

            this.p.removePotionEffect(PotionEffectType.SPEED);
            this.p.removePotionEffect(PotionEffectType.INCREASE_DAMAGE);
            this.p.addPotionEffect(speed);
            this.p.addPotionEffect(strength);

            /* wearing off stacks */
            final int currentStacks = this.stacks;
            this.wearoff = new BukkitRunnable() {
                @Override
                public void run() {
                    /* remove player when they left */
                    if (BloodlustProfile.this.p == null || !BloodlustProfile.this.p.isOnline()) {
                        Bloodlust.this.bloodlustProfiles.remove(BloodlustProfile.this.p.getUniqueId());
                        this.cancel();
                        return;
                    }

                    if (BloodlustProfile.this.stacks != currentStacks) {
                        return;
                    }

                    BloodlustProfile.this.cancelBloodlust();
                }
            }.runTaskLater(Warriors.getInstance().getPlugin(), (int) (time * 20L));

            if (this.visuals != null) {
                return;
            }

            this.visuals = new BukkitRunnable() {
                int ticks = 0;
                int sound;

                @Override
                public void run() {
                    this.sound = 4;

                    /* remove player when they left */
                    if (BloodlustProfile.this.p == null || !BloodlustProfile.this.p.isOnline()) {
                        Bloodlust.this.bloodlustProfiles.remove(BloodlustProfile.this.p.getUniqueId());
                        this.cancel();
                        return;
                    }

                    if (this.ticks % 4 == 0) {

                        if (!new User(BloodlustProfile.this.p).isUsingAbility(Bloodlust.this)) {
                            BloodlustProfile.this.cancelBloodlust();
                            return;
                        }

                        final Location location = BloodlustProfile.this.p.getLocation();
                        this.ticks = 0;

                        for (final Location prt : UtilLoc.circleLocations(location.add(0, 1.2, 0), 0.25, 200 - (BloodlustProfile.this.stacks * 50))) {
                            UtilParticle.playColoredParticle(prt, 255, 155 - (BloodlustProfile.this.stacks * 50), 155 - (BloodlustProfile.this.stacks * 50));
                        }

                    }

                    this.ticks++;
                }
            }.runTaskTimer(Warriors.getInstance().getPlugin(), 0, 1L);

        }

    }


}
