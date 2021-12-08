package me.rey.core.classes.abilities.shaman.axe;

import me.rey.core.Warriors;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.events.customevents.block.CustomBlockPlaceEvent.PlaceCause;
import me.rey.core.players.User;
import me.rey.core.utils.UtilBlock;
import me.rey.core.utils.UtilEnt;
import me.rey.core.utils.UtilLoc;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class Fissure extends Ability {

    public static HashMap<Block, Object[]> blockData = new HashMap<>();
    public static ArrayList<Block> unbreakable = new ArrayList<>();

    public Fissure() {
        super(513, "Fissure", ClassType.GREEN, AbilityType.AXE, 1, 5, 0.00, Arrays.asList(
                "Create a wall that on impact applies a",
                "Slowness 2 effect to enemies for <variable>2.5+(0.5*l)</variable> (+0.5) Seconds.",
                "",
                "If hit with fissure, enemies will take",
                "<variable>2.4+(0.4*l)</variable> (+0.4) damage and for every block traveled,",
                "it will deal <variable>0.8+(0.2*l)</variable> (+0.2) additional damage.",
                "",
                "Energy: <variable>50-(3*l)</variable> (-3)",
                "Recharge: <variable>12-l</variable> (-1) Seconds"
        ));

        this.setWhileInAir(false);
        this.setEnergyCost(50, 3);
    }

    @Override
    protected boolean execute(final User u, final Player p, final int level, final Object... conditions) {
        this.setCooldown(12 - level);

        final FissureObject fissure = new FissureObject(u, p.getLocation(), p.getLocation().getYaw(), level, 2.5 + 0.5 * level);
        fissure.start();

        return true;
    }

    @EventHandler
    public void onBlockBreak(final BlockBreakEvent e) {
        this.cancel(e, e.getBlock());
    }

    @EventHandler
    public void onInteract(final PlayerInteractEvent e) {
        if (e.getClickedBlock() == null) {
            return;
        }
        this.cancel(e, e.getClickedBlock());
    }

    private void cancel(final Cancellable c, final Block block) {
        if (!unbreakable.contains(block)) {
            return;
        }
        c.setCancelled(true);
    }

    class FissureObject {

        private static final int fissureLength = 15;
        private static final int fissureStayTimeSeconds = 7;
        private final int level;
        private final double degree; // yaw
        private final User owner;
        private final double originY;
        private final double slownessSeconds;
        private final Location origin;
        private final ArrayList<UUID> alreadyHit = new ArrayList<>();
        private int maxHeight = 1;
        private int blockIndex = 1;
        private int blockHeightIndex = 1;
        private Location updatableLoc;

        public FissureObject(final User owner, final Location origin, final double degree, final int level, final double slownessSeconds) {
            this.owner = owner;
            this.origin = origin;
            this.updatableLoc = origin;
            this.originY = origin.getBlockY();
            this.degree = degree + 90D;
            this.level = level;
            this.slownessSeconds = slownessSeconds;

        }

        public void start() {

            new BukkitRunnable() {

                @Override
                public void run() {

                    if (FissureObject.this.blockIndex >= fissureLength && FissureObject.this.blockHeightIndex > FissureObject.this.maxHeight) {
                        this.cancel();
                        return;
                    }

                    if (FissureObject.this.blockIndex == 3) {
                        FissureObject.this.maxHeight = 2;
                    } else if (FissureObject.this.blockIndex == 5) {
                        FissureObject.this.maxHeight = 3;
                    }

                    if (FissureObject.this.blockHeightIndex > FissureObject.this.maxHeight) {
                        FissureObject.this.blockHeightIndex = 1;
                        FissureObject.this.blockIndex++;
                    }

                    final double z = UtilLoc.getXZCordsFromDegree(FissureObject.this.origin, FissureObject.this.blockIndex, FissureObject.this.degree)[1];
                    final double x = UtilLoc.getXZCordsFromDegree(FissureObject.this.origin, FissureObject.this.blockIndex, FissureObject.this.degree)[0];
                    final Block found = new Location(FissureObject.this.origin.getWorld(), x, FissureObject.this.originY, z).getBlock();
                    final Block lowestFromFound = UtilBlock.getLowestBlockFrom(found);

                    final int yDiff = found.getY() - lowestFromFound.getY();

                    if (yDiff - 1 > 2 || yDiff + 1 < -2) {
                        this.cancel();
                        return;
                    }

                    final Object[] array = {lowestFromFound.getType(), lowestFromFound.getData()};
                    final Material mat = lowestFromFound.getType();
                    if ((mat.hasGravity() || UtilBlock.airFoliage(lowestFromFound))) {
                        array[0] = Material.DIRT;
                        array[1] = (byte) 0;
                    }

                    FissureObject.this.stack(lowestFromFound, yDiff - 1 + FissureObject.this.blockHeightIndex, (Material) array[0], (byte) array[1]);

                    for (final Entity next : UtilLoc.getEntitiesInCircle(FissureObject.this.updatableLoc, 0.5)) {
                        if (!(next instanceof LivingEntity)) {
                            continue;
                        }
                        final LivingEntity ent = (LivingEntity) next;

                        if (ent instanceof Player && FissureObject.this.owner.getTeam().contains(ent)) {
                            continue;
                        }

                        if (FissureObject.this.alreadyHit.contains(ent.getUniqueId())) {
                            continue;
                        }

                        final double dmg = 2.4 * (0.4 * FissureObject.this.level) + ((0.8 + (0.2 * FissureObject.this.level)) * FissureObject.this.blockIndex);
                        UtilEnt.damage(dmg, Fissure.this.getName(), ent, FissureObject.this.owner.getPlayer());
                        ent.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) Math.round(FissureObject.this.slownessSeconds * 20), 1, false, false));
                        FissureObject.this.alreadyHit.add(ent.getUniqueId());
                    }

                    FissureObject.this.blockHeightIndex++;
                }

            }.runTaskTimer(Warriors.getInstance().getPlugin(), 0, 1);

        }

        private boolean stack(final Block start, final int height, final Material mat, final byte data) {
            final Set<Block> toReplace = new HashSet<Block>();
            final int y = start.getY();
            final int x = start.getX();
            final int z = start.getZ();

            for (int i = 1; i < height + 1; i++) {
                final Block found = new Location(this.origin.getWorld(), x, y + i, z).getBlock();

                if (UtilBlock.solid(found) && !Fissure.unbreakable.contains(found)) {
                    return false;
                }
                toReplace.add(found);
            }

            for (final Block b : toReplace) {
                if (unbreakable.contains(b) || blockData.containsKey(b)) {
                    continue;
                }

                final Object[] array = {b.getType(), b.getData()};
                blockData.put(b, array);

                this.updatableLoc = b.getLocation().clone().add(0.5, 0, 0.5);
                me.rey.core.utils.UtilBlock.replaceBlock(PlaceCause.ABILITY, b, mat, data);
                b.getWorld().playEffect(b.getLocation(), Effect.STEP_SOUND, b.getTypeId());
                Fissure.unbreakable.add(b);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        me.rey.core.utils.UtilBlock.replaceBlock(PlaceCause.ABILITY, b, (Material) blockData.get(b)[0], (byte) blockData.get(b)[1]);
                        blockData.remove(b);
                        Fissure.unbreakable.remove(b);
                    }
                }.runTaskLater(Warriors.getInstance().getPlugin(), fissureStayTimeSeconds * 20);
            }

            return true;
        }

    }

}
