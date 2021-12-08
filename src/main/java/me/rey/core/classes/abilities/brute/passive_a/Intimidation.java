package me.rey.core.classes.abilities.brute.passive_a;

import me.rey.core.Warriors;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.classes.abilities.IConstant;
import me.rey.core.effects.ParticleEffect;
import me.rey.core.effects.SoundEffect;
import me.rey.core.events.customevents.ability.AbilityInteractEvent;
import me.rey.core.events.customevents.ability.PlayerUpdateAbilitiesEvent;
import me.rey.core.players.User;
import me.rey.core.utils.UtilLoc;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class Intimidation extends Ability implements IConstant.IDroppable {

    private static final Map<UUID, IntimidationProfile> profiles = new HashMap<>();

    public Intimidation() {
        super(632, "Intimidation", ClassType.DIAMOND, AbilityType.PASSIVE_A, 1, 3, 0.00, Arrays.asList(
                "You intimidate nearby enemies, giving",
                "opponents within <variable>3+l</variable> (+1) blocks",
                "constant Slowness. Intimidation lasts",
                "for <variable>8+l</variable> (+1) Seconds.",
                "",
                "Recharge: <variable>25-l</variable> (-1) Seconds"
        ));
    }

    @Override
    protected boolean execute(final User u, final Player p, final int level, final Object... conditions) {
        profiles.remove(p.getUniqueId());

        final IntimidationProfile profile = new IntimidationProfile(p, level);
        profile.start();
        profiles.put(p.getUniqueId(), profile);
        this.sendUsedMessageToPlayer(p, this.getName());
        new SoundEffect(Sound.HORSE_IDLE, 0.5f).play(p.getLocation());

        this.setCooldown(25 - level);
        return true;
    }

    @EventHandler
    public void onUpdateAbilities(final PlayerUpdateAbilitiesEvent e) {
        if (profiles.containsKey(e.getPlayer().getUniqueId())) {
            profiles.get(e.getPlayer().getUniqueId()).cancelIntimidation();
            profiles.remove(e.getPlayer().getUniqueId());
        }
    }

    class IntimidationProfile {

        final int intimSeconds, level, radius;
        final Player p;
        final int loopTicks = 4;
        final PotionEffect slow = new PotionEffect(PotionEffectType.SLOW, 20, 0);
        BukkitTask task = null;

        public IntimidationProfile(final Player p, final int level) {
            this.p = p;
            this.level = level;
            this.intimSeconds = 8 + level;
            this.radius = 3 + level;
        }

        public void cancelIntimidation() {
            if (this.task != null) {
                this.task.cancel();
                this.task = null;
            }

            new User(this.p).sendMessageWithPrefix(Intimidation.this.getName(), "&g" + Intimidation.this.getName() + " &rended.");
        }

        public void start() {
            if (this.task != null) {
                return;
            }

            this.task = new BukkitRunnable() {
                int ticks = 0;
                int degree = 0;

                @Override
                public void run() {
                    this.ticks++;

                    if (IntimidationProfile.this.p == null || !IntimidationProfile.this.p.isOnline()) {
                        IntimidationProfile.this.cancelIntimidation();
                        return;
                    }

                    final int degreeMod = 10;
                    this.degree += degreeMod;
                    if (this.degree > 360) {
                        this.degree = 0;
                    }

                    // Big Ring
                    for (int i = -2; i < 5; i++) {
                        final double[] locs = UtilLoc.getXZCordsFromDegree(IntimidationProfile.this.p, IntimidationProfile.this.radius, this.degree + (degreeMod * i));
                        final Location particleLocation = new Location(IntimidationProfile.this.p.getWorld(), locs[0], IntimidationProfile.this.p.getLocation().getY(), locs[1]);
                        new ParticleEffect.ColoredParticle(178, 42, 209).play(particleLocation);
                    }

                    // 9 seconds - 255
                    // 5 second - X

                    // Small Ring - Indicator
                    for (int i = -2; i < 5; i++) {
                        final double[] locs = UtilLoc.getXZCordsFromDegree(IntimidationProfile.this.p, 2, this.degree + (degreeMod * i));
                        final Location particleLocation = new Location(IntimidationProfile.this.p.getWorld(), locs[0], IntimidationProfile.this.p.getLocation().getY(), locs[1]);

                        final double colorMod = Math.max(1, Math.min(254, (this.ticks / 20D) * 255D / IntimidationProfile.this.intimSeconds));
                        new ParticleEffect.ColoredParticle((float) colorMod, 255 - (float) colorMod, 0).play(particleLocation);
                    }

                    if (this.ticks % IntimidationProfile.this.loopTicks != 0) {
                        return;
                    }
                    if (this.ticks > IntimidationProfile.this.intimSeconds * 20D) {
                        IntimidationProfile.this.cancelIntimidation();
                        return;
                    }


                    final Iterator<Entity> ents = UtilLoc.getEntitiesInCircle(IntimidationProfile.this.p.getLocation(), IntimidationProfile.this.radius).iterator();
                    while (ents.hasNext()) {
                        final Entity ent = ents.next();
                        if (!(ent instanceof LivingEntity)) {
                            continue;
                        }
                        if (ent instanceof ArmorStand) {
                            continue;
                        }

                        final LivingEntity le = (LivingEntity) ent;
                        if (le instanceof Player && new User(IntimidationProfile.this.p).getTeam().contains(le)) {
                            continue;
                        }

                        final AbilityInteractEvent aie = new AbilityInteractEvent(le, IntimidationProfile.this.p, Intimidation.this, IntimidationProfile.this.level);
                        Bukkit.getPluginManager().callEvent(aie);
                        if (!aie.isCancelled()) {
                            le.removePotionEffect(IntimidationProfile.this.slow.getType());
                            le.addPotionEffect(IntimidationProfile.this.slow);
                        }
                    }

                }
            }.runTaskTimer(Warriors.getInstance().getPlugin(), 0L, 1);

        }

    }
}
