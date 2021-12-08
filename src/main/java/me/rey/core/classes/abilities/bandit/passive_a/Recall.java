package me.rey.core.classes.abilities.bandit.passive_a;

import me.rey.core.Warriors;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.classes.abilities.IConstant.IDroppable;
import me.rey.core.effects.SoundEffect;
import me.rey.core.enums.AbilityFail;
import me.rey.core.events.customevents.ability.AbilityFailEvent;
import me.rey.core.events.customevents.update.UpdateEvent;
import me.rey.core.players.User;
import me.rey.core.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

public class Recall extends Ability implements IDroppable {

    HashMap<UUID, RecallProfile> profiles = new HashMap<>();

    public Recall() {
        super(132, "Recall", ClassType.BLACK, AbilityType.PASSIVE_A, 1, 3, 25, Arrays.asList(
                "Travel back in time to the",
                "last 4 seconds while instantly",
                "healing for <variable>6+l</variable> (+1) health points.",
                "",
                "Use this ability while shifting to",
                "trigger Secondary Recall and",
                "teleport back <variable>1.75+(0.25*l)</variable> (+0.25) seconds while",
                "healing for <variable>2.25+(0.25*l)</variable> (+0.25) health points.",
                "",
                "Recharge: <variable>27-(2*l)</variable> (-2) Seconds",
                "Secondary: <variable>15-l</variable> (-1) Seconds"
        ));

        this.setWhileSlowed(false);
    }

    @Override
    protected boolean execute(final User u, final Player p, final int level, final Object... conditions) {

        this.profiles.get(p.getUniqueId()).recall(false);

        this.setCooldownCanceled(true);

        return true;
    }

    @Override
    @EventHandler
    public void onUpdate(final UpdateEvent e) {
        for (final Player p : Bukkit.getOnlinePlayers()) {

            if (!new User(p).isUsingAbility(this)) {
                continue;
            }

            this.resetProfile(p, new User(p).getSelectedBuild(this.getClassType()).getAbilityLevel(this.getAbilityType()));
        }
    }

    @EventHandler
    public void onSecondaryRecall(final AbilityFailEvent e) {
        if (e.getAbility() == this && e.getFail() == AbilityFail.SLOWED) {
            e.setMessageCancelled(true);
            this.resetProfile(e.getPlayer(), e.getLevel());

            this.profiles.get(e.getPlayer().getUniqueId()).recall(true);
        }
    }

    private void resetProfile(final Player p, final int level) {
        if (!this.profiles.containsKey(p.getUniqueId())) {
            this.profiles.put(p.getUniqueId(), new RecallProfile(p, level));
            this.profiles.get(p.getUniqueId()).startSaving();
        }
    }

    /*
     * PARTICLES
     */
    private void makeParticlesBetween(final Location init, final Location loc) {
        final Vector pvector = Utils.getDirectionBetweenLocations(init, loc);
        for (double i = 1; i <= init.distance(loc); i += 0.2) {
            pvector.multiply(i);
            init.add(pvector);
            final Location toSpawn = init.clone();
            toSpawn.setY(toSpawn.getY() + 0.5);
            init.getWorld().spigot().playEffect(toSpawn, Effect.WITCH_MAGIC, 0, 0, 0F, 0.3F, 0F, 0F, 10, 50);
            init.subtract(pvector);
            pvector.normalize();
        }
    }

    class RecallProfile {

        final int loopTicks = 4;

        final Player p;
        final int level;
        final ArrayList<Location> locations = new ArrayList<>();
        final double recallCD, secondaryCD;
        final double recallTime, secondaryTime;
        final double recallHealth, secondaryHealth;
        BukkitTask task = null;

        public RecallProfile(final Player p, final int level) {
            this.p = p;
            this.level = level;

            this.recallTime = 4;
            this.recallHealth = 6 + level;
            this.recallCD = 27 - 2 * level;

            this.secondaryTime = level * 0.25 + 1.75;
            this.secondaryHealth = 2.25 + 0.5 * level;
            this.secondaryCD = 15 - level;
        }

        void addLocation(final Location location) {
            if (this.locations.size() >= 20D / this.loopTicks * this.recallTime) {
                this.locations.remove(0);
            }

            this.locations.add(location);
        }

        public void recall(final boolean forceSecond) {
            if (Recall.this.hasCooldown(this.p)) {
                Recall.this.sendCooldownMessage(this.p);
                return;
            }

            final boolean secondary = this.p.isSneaking() || forceSecond;

            final int index = secondary ? (2 * 20 / this.loopTicks) - 1 : 0;

            final Location pLoc = this.p.getLocation();
            final Location loc = this.locations.get(index).clone();
            loc.setYaw(pLoc.getYaw());
            loc.setPitch(pLoc.getPitch());

            Location prev = null;
            for (int i = this.locations.size() - 1; i >= index; i--) {
                if (prev != null) {
                    Recall.this.makeParticlesBetween(prev, this.locations.get(i));
                }
                prev = this.locations.get(i);
            }

            Recall.this.sendUsedMessageToPlayer(this.p, (secondary ? "Secondary " : "") + Recall.this.getName());
            new SoundEffect(Sound.ZOMBIE_UNFECT, 2f).setVolume(1).play(pLoc);
            this.p.setHealth(Math.min(20, Math.max(0, this.p.getHealth() + (secondary ? this.secondaryHealth : this.recallHealth))));

            this.p.teleport(loc);
            new SoundEffect(Sound.ZOMBIE_UNFECT, 2f).setVolume(2).play(loc);
            new SoundEffect(Sound.ZOMBIE_UNFECT, 2f).setVolume(2).play(this.p);

            this.locations.clear();

            Recall.this.setCooldown(secondary ? this.secondaryCD : this.recallCD);
            Recall.this.applyCooldown(this.p);
        }

        public void startSaving() {

            this.task = new BukkitRunnable() {
                @Override
                public void run() {

                    RecallProfile.this.addLocation(RecallProfile.this.p.getLocation());

                }

            }.runTaskTimer(Warriors.getInstance().getPlugin(), 0, this.loopTicks);

        }

    }

}
