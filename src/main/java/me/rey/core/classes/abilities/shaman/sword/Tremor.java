package me.rey.core.classes.abilities.shaman.sword;

import me.rey.core.Warriors;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.classes.abilities.IConstant;
import me.rey.core.gui.Item;
import me.rey.core.items.Throwable;
import me.rey.core.players.User;
import me.rey.core.utils.UtilBlock;
import me.rey.core.utils.UtilEnt;
import me.rey.core.utils.UtilLoc;
import me.rey.core.utils.UtilVelocity;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;

public class Tremor extends Ability implements IConstant {

    private static final HashMap<Player, Rupture> activeTremors = new HashMap<>();
    private final float energyCost = 30 / 20, reduction = 2 / 20, explodeEnergy = 15;

    public Tremor() {
        super(501, "Tremor", ClassType.GREEN, AbilityType.SWORD, 1, 5, 0.00, Arrays.asList(
                "Hold block to summon a destructive tremor",
                "that follows your cursor and explodes upon",
                "release.",
                "",
                "When the tremor explodes it deals <variable>3.5+0.5*l</variable> (+0.5)",
                "damage to all entities nearby.",
                "",
                "Energy: <variable>32-2*l</variable> (-2) per Second",
                "Explosion Energy: 15"
        ));

        this.setIgnoresCooldown(true);
        this.setWhileInAir(false);
        this.setSound(null, 0f);
    }

    @Override
    protected boolean execute(final User u, final Player p, final int level, final Object... conditions) {

        final float energyCost = this.energyCost - (this.reduction * level);
        if (!p.isBlocking() && activeTremors.containsKey(p)) {
            if (new User(p).getEnergy() >= this.explodeEnergy) {
                this.explode(p);
            }
            return true;
        }

        if (!p.isBlocking()) {
            return false;
        }

        final int distance = 30;
        final float speedIncrement = 1.3F + (0.5F * level);
        if (new User(p).getEnergy() >= this.explodeEnergy) {
            if (!activeTremors.containsKey(p)) {
                activeTremors.put(p, new Rupture(p, u, level, distance));
            }
            activeTremors.get(p).setSpeed(Math.min(activeTremors.get(p).speed + (speedIncrement / 20), speedIncrement * 20));
            activeTremors.get(p).move(p.getTargetBlock((Set<Material>) null, distance).getLocation());
            new User(p).consumeEnergy(energyCost);

            return true;
        } else if (activeTremors.containsKey(p)) {
            this.explode(p);
            this.sendEnergyError(p);
        }

        return true;
    }

    private void explode(final Player p) {
        activeTremors.get(p).explode();
        activeTremors.remove(p);
        new User(p).consumeEnergy(this.explodeEnergy);
    }

    class Rupture {

        private final double ruptureRadius = 3;
        private final int level;
        private final double maxDistance;
        private final Player owner;
        private final User u;

        public float speed;
        private Location location;

        public Rupture(final Player owner, final User u, final int level, final double maxDistance) {
            this.owner = owner;
            this.u = u;
            this.level = level;
            this.maxDistance = maxDistance;

            this.speed = 15F;
            this.location = owner.getLocation();
            this.location.setY(UtilBlock.getHighestClosestAir(owner.getLocation().getBlock()).getY() - 1.0);
        }

        public void move(final Location to) {

            if (to != null) {
                this.location.setDirection(to.toVector().subtract(this.location.toVector()));
            } else {
                this.location.setDirection(this.owner.getLocation().toVector().subtract(this.location.toVector()));
            }

            final double degree = this.location.getYaw() + 90D;

            final double divider = 75;
            final double x = UtilLoc.getXZCordsFromDegree(this.location, this.speed / divider, degree)[0];
            final double y = UtilBlock.getHighestClosestAir(this.location.getBlock()).getY() - 1;
            final double z = UtilLoc.getXZCordsFromDegree(this.location, this.speed / divider, degree)[1];

            if (y - this.location.getY() >= 3 || y - this.location.getY() <= -3) {
                this.explode();
                activeTremors.remove(this.owner);
            } else {
                this.location = new Location(this.owner.getWorld(), x, y, z);
            }


            this.play();
        }

        public void explode() {

            this.location.getWorld().playSound(this.location, Sound.DIG_STONE, 1F, 0.8F);
            for (final Location loc : UtilLoc.circleLocations(this.location, this.ruptureRadius, 30)) {
                loc.getWorld().playEffect(loc.getBlock().getLocation(), Effect.STEP_SOUND, loc.getBlock().getRelative(BlockFace.DOWN).getTypeId());
            }

            final Location explodeloc = this.location.clone();

            explodeloc.setY(explodeloc.getY() + 1);

            for (int i = 1; i <= 10; i++) {
                final Throwable item = new Throwable(new Item(Material.DIRT).setLore(Arrays.asList(i + "")), false);

                explodeloc.setPitch(-75);
                explodeloc.setYaw(360 / 10 * i + 90);

                final Random r = new Random();
                final double radd = (double) r.nextInt(20) / 100;

                item.fire(explodeloc, 0.1 + radd, 0.5 + radd);
                final Throwable finalItem = item;
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        finalItem.destroy();
                    }
                }.runTaskLaterAsynchronously(Warriors.getInstance().getPlugin(), 40L);
            }

            final HashMap<Entity, Double> entities = UtilLoc.getEntityMapInCircle(this.location, this.ruptureRadius);
            final double kbMult = (0.5 + 0.5 * 0.05) / 2;
            for (final Entity e : entities.keySet()) {
                if (!(e instanceof LivingEntity)) {
                    continue;
                }

                UtilEnt.damage(3.5 + 0.5 * this.level, Tremor.this.getName(), (LivingEntity) e, this.owner);
                UtilVelocity.velocity(e, this.owner,
                        UtilVelocity.getTrajectory2D(this.location.toVector().add(new Vector(0.5, 0, 0.5)), e.getLocation().toVector()), 1,
                        0.8 + 0.8 * entities.get(e) * kbMult, true, 0, 0.4 + 1.0 * entities.get(e) * kbMult, 0.4 + 1.0 * kbMult, true);

            }

        }

        public void setSpeed(final float speed) {
            this.speed = speed;
        }

        private void play() {
            this.location.getWorld().playEffect(this.location.getBlock().getLocation(), Effect.STEP_SOUND, this.location.getBlock().getRelative(BlockFace.DOWN).getTypeId());
        }

    }

}
