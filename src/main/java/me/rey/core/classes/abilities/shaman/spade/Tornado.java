package me.rey.core.classes.abilities.shaman.spade;

import me.rey.core.Warriors;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.effects.SoundEffect;
import me.rey.core.packets.ActionBar;
import me.rey.core.players.User;
import me.rey.core.pvp.ToolType;
import me.rey.core.utils.ChargingBar;
import me.rey.core.utils.UtilBlock;
import me.rey.core.utils.UtilLoc;
import me.rey.core.utils.UtilVelocity;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

public class Tornado extends Ability {
    public HashMap<UUID, TornadoObject> tornado = new HashMap<>();

    public Tornado() {
        super(521, "Tornado", ClassType.GREEN, AbilityType.SPADE, 1, 3, 13.0, Arrays.asList(
                "Charge up a tornado by holding right click",
                "in a direction that will knock up and slow all ",
                "enemies for <variable>2+l</variable> (+1) Seconds hit by it.",
                "",
                "Tornado travels for a max distance of <variable>21+l*3</variable> (+3) blocks.",
                "",
                "Recharge: <variable>13.0-l*2</variable> (-2) Seconds"
        ));
    }

    @Override
    public boolean run(final Player p, final ToolType toolType, final boolean messages, final Object... conditions) {

        final User user = new User(p);

        if (user.getWearingClass() != this.getClassType() || !this.matchesAbilityTool(this.match(p.getItemInHand()))) {
            return super.run(p, toolType, messages, conditions);
        }

        if (this.tornado.containsKey(p.getUniqueId())) {
            final TornadoObject t = this.tornado.get(p.getUniqueId());
            t.updatePrepareTick();
        } else {
            final TornadoObject to = new TornadoObject(p, this, 1, p.getLocation());
            this.tornado.put(p.getUniqueId(), to);
            SoundEffect.playCustomSound(p.getLocation(), "preparetornado", 2F, 1F);
        }

        return super.run(p, toolType, messages, conditions);
    }

    @Override
    protected boolean execute(final User u, final Player p, final int level, final Object... conditions) {

        if (this.tornado.containsKey(p.getUniqueId()) == false) {
            return false;
        }
        this.setCooldownCanceled(true);
        final TornadoObject ot = this.tornado.get(p.getUniqueId());

        if (ot.preparing == false) {
            return false;
        }

        final double distance = 21 + (level * 3);
        ot.traveldistance = distance;
        ot.slowduration += level * 20;
        ot.preparing = false;

        final Location origin = p.getLocation().clone();
        new BukkitRunnable() {
            @Override
            public void run() {

                final TornadoObject t = Tornado.this.tornado.get(p.getUniqueId());

                t.updateTicks();
                final boolean prepare = t.checkPrepare();
                if (prepare) {
                    Tornado.this.setCooldown(13.0 - level * 2);
                    Tornado.this.applyCooldown(p);
                }

                final Location found = t.move().clone();
                found.setY(origin.getY());

                if (found.distance(origin) >= t.traveldistance * t.charge) {
                    Tornado.this.tornado.remove(p.getUniqueId());
                    this.cancel();
                    return;
                }


                t.whirl();
                t.knockup(p);

            }
        }.runTaskTimer(Warriors.getInstance().getPlugin(), 0L, 1L);

        return true;
    }

    class TornadoObject {

        final double spread = 1.5;
        final int particlecount = 20; /* INDIRECT PROPORTIONAL! Lower value -> Higher count/particle thickness */
        final double maxheight = 5.55;
        final double travelspeed = 0.5;
        final double rotationspeed = 20;
        final double radius = 2;
        final double knockup = 1;
        final double maxchargeticks = 60;
        double ticks;
        double lastpreparetick;
        boolean preparing = true;
        double traveldistance;
        int slowduration = 40;
        double charge = 0.01;
        double cordsAdders[];
        Player p;
        Ability a;
        Location loc;
        //final double maxticks = 80;
        ArrayList<LivingEntity> knockUped = new ArrayList<>();

        public TornadoObject(final Player p, final Ability a, final double traveldistance, final Location startloc) {
            this.p = p;
            this.a = a;
            this.loc = startloc;
            this.traveldistance = traveldistance;

            this.ticks = 1;
        }

        public void updateTicks() {
            this.ticks += 1;
        }

        public void updatePrepareTick() {
            if (this.lastpreparetick != -1) {
                this.lastpreparetick = this.ticks;
                this.charge = this.ticks / this.maxchargeticks;

                ActionBar.getChargingBar(this.a.getName(), new ChargingBar(ChargingBar.ACTIONBAR_BARS, this.charge * 100)).send(this.p);
            }
        }

        public boolean checkPrepare() {
            if ((this.ticks - this.lastpreparetick > 6 || this.ticks >= this.maxchargeticks) && this.lastpreparetick != -1) {
                this.lastpreparetick = -1;

                Block targetblock = null;
                double direction = this.p.getLocation().getYaw() + 90;

                for (int i = 0; i <= 30; i++) {
                    if (UtilBlock.getTargetBlock(this.p, i).getType().isSolid()) {
                        targetblock = UtilBlock.getTargetBlock(this.p, i);
                        break;
                    }
                }

                if (targetblock != null) {
                    final double deltaX = targetblock.getX() - this.loc.getX();
                    final double deltaZ = targetblock.getZ() - this.loc.getZ();
                    direction = Math.toDegrees(Math.atan(deltaZ / deltaX));
                    if (deltaX < 0) {
                        direction += 180;
                    }
                }

                this.cordsAdders = UtilLoc.getXZCordsMultipliersFromDegree(direction);
                SoundEffect.playCustomSound(this.loc, "tornado", 2F, 1F);
                return true;
            }
            return false;
        }

        public Location move() {
            if (this.lastpreparetick == -1) {
                this.loc.setX(this.loc.getX() + this.cordsAdders[0] * this.travelspeed * this.charge);
                if (UtilLoc.highestLocation(this.loc) != null) {
                    this.loc.setY(UtilLoc.highestLocation(this.loc).getY());
                }
                this.loc.setZ(this.loc.getZ() + this.cordsAdders[1] * this.travelspeed * this.charge);
            }
            return this.loc;
        }

        public void whirl() {

            final HashMap<Double, double[]> dCords = new HashMap<Double, double[]>();

            for (double degree = 0; degree <= 720D; degree++) {
                dCords.put(degree, UtilLoc.getXZCordsMultipliersFromDegree(degree - this.ticks * this.rotationspeed));
            }

            for (double degree = 0; degree <= 720D; degree += this.particlecount) {
                final double[] mults = dCords.get(degree);

                final Location ploc = this.loc.clone();

                ploc.setX(this.loc.getX() + mults[0] * degree / 720 * this.spread * this.charge);
                ploc.setY(this.loc.getY() + degree * ((this.maxheight * this.charge) / 1000));
                ploc.setZ(this.loc.getZ() + mults[1] * degree / 720 * this.spread * this.charge);

                ploc.getWorld().spigot().playEffect(ploc, Effect.SNOW_SHOVEL, 0, 0, 0, 0, 0, 0, 0, 50);

            }

        }

        public void knockup(final Player p) {

            if (this.lastpreparetick != -1) {
                return;
            }

            for (final Entity e : this.loc.getWorld().getNearbyEntities(this.loc, 5, 2, 5)) {
                if (e instanceof LivingEntity) {

                    final LivingEntity le = (LivingEntity) e;

                    if (this.knockUped.contains(le)) {
                        continue;
                    }

                    final HashMap<Double, double[]> maxmincords = new HashMap<Double, double[]>();

                    for (double degree = 0; degree <= 360; degree++) {
                        maxmincords.put(degree, UtilLoc.getXZCordsFromDegree(this.loc, this.radius, degree));
                    }

                    for (double degree = 0; degree <= 90; degree++) {

                        final double[] maxcords = maxmincords.get(degree);
                        final double[] mincords = maxmincords.get(180 + degree);

                        final double maxX = maxcords[0];
                        final double maxZ = maxcords[1];

                        final double minX = mincords[0];
                        final double minZ = mincords[1];

                        if (le.getLocation().getX() <= maxX && le.getLocation().getZ() <= maxZ && le.getLocation().getX() >= minX && le.getLocation().getZ() >= minZ) {

                            UtilVelocity.velocity(le, p, le.getVelocity().setY(this.knockup * this.charge));
                            if (le.getName().equalsIgnoreCase(p.getName()) == false) {
                                (le).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, this.slowduration, 0));
                            }
                            this.knockUped.add(le);
                        } else {
                            continue;
                        }
                    }
                }
            }
        }
    }
}
