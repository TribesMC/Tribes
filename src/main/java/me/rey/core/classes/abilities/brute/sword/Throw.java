package me.rey.core.classes.abilities.brute.sword;

import me.rey.core.Warriors;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.effects.SoundEffect;
import me.rey.core.players.User;
import me.rey.core.utils.UtilVelocity;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.spigotmc.event.entity.EntityDismountEvent;

import java.util.*;

public class Throw extends Ability {

    public static HashMap<UUID, ThrowProfile> throwProfiles = new HashMap<UUID, ThrowProfile>();
    public static ArrayList<UUID> denyFall = new ArrayList();

    public Throw() {
        super(602, "Throw", ClassType.DIAMOND, AbilityType.SWORD, 1, 1, 10.00, Arrays.asList(
                "Hold with your sword to pick up a",
                "player. Releasing will throw the",
                "in the direction you are looking.",
                "",
                "Automatically throws the picked up",
                "player after 2.5 seconds.",
                "",
                "Recharge: 10 seconds"
        ));
    }

    @Override
    protected boolean execute(final User u, final Player p, final int level, final Object... conditions) {

        final double RANGE = 1;
        final double ACCURACY = 0.8D;

        final ThrowProfile tp;

        if (throwProfiles.containsKey(p.getUniqueId())) {
            tp = throwProfiles.get(p.getUniqueId());
        } else {
            tp = new ThrowProfile(p);
            throwProfiles.put(p.getUniqueId(), tp);
        }

        if (tp.state == State.IDLE) {
            /*
             * Getting entities in line of sight and picking closest
             */
            final Set<LivingEntity> inSight = new HashSet<>();
            final Location origin = p.getEyeLocation();
            final Vector direction = p.getLocation().getDirection();

            for (int i = 1; i <= RANGE; i++) {
                final double x = direction.getX() * i;
                final double y = direction.getY() * i;
                final double z = direction.getZ() * i;
                origin.add(x, y, z);

                origin.getWorld().getNearbyEntities(origin, ACCURACY, ACCURACY, ACCURACY).forEach((ent) -> {
                    if (ent instanceof LivingEntity && ent != p && ent.getPassenger() == null && ent.getCustomName() == null) {
                        inSight.add((LivingEntity) ent);
                    }
                });

                origin.subtract(x, y, z);
            }

            LivingEntity toPickup = null;
            for (final LivingEntity ent : inSight) {
                if (toPickup == null || ent.getLocation().distance(p.getLocation()) < toPickup.getLocation().distance(p.getLocation())) {
                    toPickup = ent;
                }
            }

            if (toPickup != null) {
                tp.pickup(this, toPickup);
                return true;
            }
        }

        this.setCooldownCanceled(true);
        return true;
    }

    @EventHandler
    public void onEntityDamage(final EntityDamageEvent e) {
        if (e.getEntity().getType() == EntityType.PLAYER) {
            if (e.getCause() == EntityDamageEvent.DamageCause.FALL) {
                final Player p = (Player) e.getEntity();
                if (denyFall.contains(p.getUniqueId())) {
                    e.setCancelled(true);
                    denyFall.remove(p.getUniqueId());
                }
            }
        }
    }

    @EventHandler
    public void onAttack(final EntityDamageByEntityEvent e) {

        if (e.getDamager() instanceof Player) {
            final Player p = (Player) e.getDamager();
            if (p.getVehicle() != null && p.getVehicle().getType() == EntityType.PLAYER) {
                e.setCancelled(true);
            }
        }

        if (e.getEntity() instanceof Player) {
            final Player p = (Player) e.getEntity();
            if (p.getVehicle() != null && p.getVehicle().getType() == EntityType.PLAYER) {
                e.setCancelled(true);
            }
        }

    }

    @EventHandler
    public void onDismount(final EntityDismountEvent e) {
        if (e.getDismounted().getType() == EntityType.PLAYER) {
            final Player p = (Player) e.getDismounted();

            if (throwProfiles.containsKey(p.getUniqueId())) {
                final ThrowProfile tp = throwProfiles.get(p.getUniqueId());

                if (tp.state != State.PICKEDUP) {
                    return;
                }

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        p.setPassenger(e.getEntity());
                    }
                }.runTaskLater(Warriors.getInstance().getPlugin(), 1L);
            }

        }
    }

    enum State {
        IDLE, PICKEDUP
    }

    class ThrowProfile {

        Player p;
        LivingEntity pickedup;
        State state = State.IDLE;

        public ThrowProfile(final Player p) {
            this.p = p;
        }

        void pickup(final Ability a, final LivingEntity pickedup) {
            this.pickedup = pickedup;

            this.state = State.PICKEDUP;

            new SoundEffect(Sound.CHICKEN_EGG_POP, 1.2F).play(pickedup.getLocation()); /* CUSTOM SOUND */
            a.sendAbilityMessage(this.p, "You picked up &s" + pickedup.getName() + "&r.");

            /* SENDING MESSAGE OF HIT IF THE ENTITY WAS A PLAYER */
            if (pickedup instanceof Player) {
                a.sendAbilityMessage(pickedup, "&s" + this.p.getName() + " &rhas picked you up!");
            }

            this.p.setPassenger(pickedup);

            new BukkitRunnable() {
                int t = 0;

                @Override
                public void run() {
                    if ((this.t >= 2.5 * 20 || !ThrowProfile.this.p.isBlocking()) && ThrowProfile.this.state == State.PICKEDUP) {
                        ThrowProfile.this.throwEntity();
                        this.cancel();
                    }

                    this.t++;
                }
            }.runTaskTimer(Warriors.getInstance().getPlugin(), 0L, 1L);

        }

        void throwEntity() {
            if (this.state != State.PICKEDUP || this.pickedup == null) {
                return;
            }

            this.state = State.IDLE;

            this.pickedup.leaveVehicle();

            UtilVelocity.velocity(this.pickedup, this.p, this.p.getLocation().getDirection(), 1, 1.5, false, 0.75, 0, 1.2, false);
            this.p.getWorld().playSound(this.p.getLocation(), Sound.ENDERDRAGON_WINGS, 1.0F, 0.8F);

            if (!new User(this.p).getTeam().contains(this.pickedup)) {
                return;
            }

            denyFall.add(this.pickedup.getUniqueId());

            new BukkitRunnable() {
                int fallTimer = 0;

                @Override
                public void run() {
                    if ((this.fallTimer >= 5 * 20 || ThrowProfile.this.pickedup.isOnGround())) {
                        denyFall.remove(ThrowProfile.this.pickedup.getUniqueId());
                        this.cancel();
                        return;
                    }

                    this.fallTimer++;
                }
            }.runTaskTimer(Warriors.getInstance().getPlugin(), 0L, 1L);

        }

    }

}
