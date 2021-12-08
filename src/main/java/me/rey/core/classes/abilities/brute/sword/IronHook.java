package me.rey.core.classes.abilities.brute.sword;

import me.rey.core.Warriors;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.gui.Item;
import me.rey.core.items.Throwable;
import me.rey.core.players.User;
import me.rey.core.utils.UtilVelocity;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

public class IronHook extends Ability {

    public HashMap<UUID, IronHookObject> ironhook = new HashMap<UUID, IronHookObject>();

    public IronHook() {
        super(601, "Iron Hook", ClassType.DIAMOND, AbilityType.SWORD, 1, 3, 10, Arrays.asList(
                "Right click to charge up an iron hook,",
                "release hook by releasing right click.",
                "",
                "Fully charging increases the hook's accuracy",
                "and pull strength by <variable>25+25*l</variable>% (+25%).",
                "",
                "Charge time: <variable>5.5-0.5*l</variable> (-0.5)",
                "",
                "Recharge: <variable>10.5-0.5*l</variable> (-0.5)"
        ));
    }

    @Override
    protected boolean execute(final User u, final Player p, final int level, final Object... conditions) {

        if (this.ironhook.containsKey(p.getUniqueId()) == false) {
            this.ironhook.put(p.getUniqueId(), new IronHookObject(p, level));
            this.setCooldownCanceled(true);

            new BukkitRunnable() {
                @Override
                public void run() {

                    final IronHookObject ih = IronHook.this.ironhook.get(p.getUniqueId());

                    ih.updateLoc();

                    if (p.isBlocking() && ih.charged == false) {
                        ih.charge();
                    } else {
                        ih.charged = true;
                        ih.throwHook();

                        if (ih.thrown && IronHook.this.hasCooldown(p) == false) {
                            IronHook.this.setCooldown(10.5 - 0.5 * level);
                            IronHook.this.applyCooldown(p);
                        }

                        if (ih.destroy() || ih.tooOld()) {
                            ih.hook.destroy();
                            IronHook.this.ironhook.remove(p.getUniqueId());
                            this.cancel();
                            return;
                        }

                        ih.checkForCollision();

                    }

                }
            }.runTaskTimer(Warriors.getInstance().getPlugin(), 0L, 1L);

        }

        return true;
    }

    class IronHookObject {

        /* Charging hook */
        final double baseChargeCooldown = 5.5;
        final double subtractPerLevel = 0.5;
        /* Throwing the hook */
        final double throwBaseV = 0.75;
        final double throwChargeV = 0.5;
        final double throwLevelMultiplier = 0.5;
        /* Hook grabbing entity */
        final double grabChargeV = 0.75;
        final double grabLevelMultiplier = 0.25;
        final double grabBaseKnockup = 0.3;
        final double grabKnockupLevelMultiplier = 0.10;
        final double hitbox = 0.5;
        final int maxticksalive = 40;
        Player p;
        int level;
        Location loc;
        Vector direction;
        int chargeticks = 1;
        double charge = 0.01;
        boolean charged;
        int maxchargeticks;
        boolean thrown = false;
        Throwable hook;
        Entity hookedentity = null;
        int ticksalive = 0;

        public IronHookObject(final Player p, final int level) {
            this.p = p;
            this.level = level;
            this.maxchargeticks = (int) (this.baseChargeCooldown - this.subtractPerLevel * level) * 20;
            this.hook = new Throwable(new Item(Material.TRIPWIRE_HOOK), false);
        }

        public void updateLoc() {
            this.loc = new Location(this.p.getWorld(), this.p.getLocation().getX(), this.p.getLocation().getY() + 1, this.p.getLocation().getZ());
        }

        public void charge() {
            if (this.chargeticks < this.maxchargeticks) {
                this.chargeticks++;
                this.charge = (double) this.chargeticks / (double) this.maxchargeticks;
                if (this.chargeticks * 2 + 20 < this.maxchargeticks) {
                    this.p.playSound(this.p.getLocation(), Sound.NOTE_PIANO, 1F, 2F * (((float) (this.chargeticks * 2 + 20) / (float) this.maxchargeticks)));
                }
            }
        }

        public void throwHook() {
            if (this.thrown == false) {
                this.direction = this.p.getLocation().getDirection();
                this.p.getWorld().playSound(this.p.getLocation(), Sound.IRONGOLEM_THROW, 2f, 0.8f);
                this.hook.fire(this.p.getEyeLocation(), this.direction, this.throwBaseV + (this.throwChargeV * this.charge) * (1 + this.level * this.throwLevelMultiplier), this.direction.getY(), 0.2);
                this.thrown = true;
            }
        }

        public void checkForCollision() {
            this.ticksalive++;

            if (this.hookedentity != null) {
                return;
            }

            this.hook.getEntityitem().getWorld().playSound(this.hook.getEntityitem().getLocation(), Sound.FIRE_IGNITE, 1.4F, 0.8F);
            this.hook.getEntityitem().getWorld().spigot().playEffect(this.hook.getEntityitem().getLocation(), Effect.CRIT, 0, 0, 0, 0, 0, 0, 1, 50);

            for (final Entity e : this.hook.getEntityitem().getNearbyEntities(this.hitbox, this.hitbox, this.hitbox)) {
                if (e instanceof LivingEntity && this.hookedentity != e) {

                    if (e == this.p) {
                        continue;
                    }

                    this.hookedentity = e;
                    UtilVelocity.velocity(this.hookedentity, this.p, this.direction.normalize().multiply(-((this.grabChargeV * this.charge) + this.level * this.grabLevelMultiplier)).setY(0.5 + this.direction.normalize().getY() * this.grabBaseKnockup + this.level * this.grabKnockupLevelMultiplier));
                    this.p.playSound(this.p.getLocation(), Sound.ORB_PICKUP, 1F, 1.5F);
                    break;
                }
            }
        }

        public boolean tooOld() {
            if (this.ticksalive >= this.maxticksalive) {
                return true;
            }
            return false;
        }

        public boolean destroy() {
            if (this.hook.getEntityitem().isOnGround()) {
                return true;
            }
            return false;
        }

    }

}
