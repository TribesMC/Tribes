package me.rey.core.classes.abilities.mage.axe;

import me.rey.core.Warriors;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.enums.AbilityFail;
import me.rey.core.events.customevents.ability.AbilityFailEvent;
import me.rey.core.events.customevents.block.CustomBlockPlaceEvent.PlaceCause;
import me.rey.core.gui.Item;
import me.rey.core.players.User;
import me.rey.core.utils.UtilBlock;
import me.rey.core.utils.UtilLoc;
import me.rey.core.utils.UtilMath;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.HashMap;

public class IcePrison extends Ability {

    /* Throwing the item */
    final double throwBaseV = 0.5;
    final double throwChargeV = 0.25;
    final double throwLevelMultiplier = 0.1;
    public HashMap<Player, HashMap<Block, Object[]>> toRestore = new HashMap<>();

    public IcePrison() {
        super(212, "Ice Prison", ClassType.GOLD, AbilityType.AXE, 1, 5, 20.00, Arrays.asList(
                "Spawn a sphere of ice that can",
                "last up to <variable>4+l</variable> (+1) Seconds. Using",
                "Shift-Right Click will destroy all",
                "your active ice prisons.", "",
                "Energy: <variable>100-(5*l)</variable> (-5)",
                "Recharge: <variable>20-l</variable> (-1) Seconds"
        ));

        this.setEnergyCost(100, 5);
    }

    @Override
    protected boolean execute(final User u, final Player p, final int level, final Object... conditions) {
        return this.spawnIcePrison(p, level, false);
    }

    @EventHandler
    public void onFail(final AbilityFailEvent e) {
        if (e.getAbility() != this) {
            return;
        }
        if (e.getFail().equals(AbilityFail.COOLDOWN)) {
            if (e.getPlayer().isSneaking()) {
                this.spawnIcePrison(e.getPlayer(), e.getLevel(), true);
                e.setMessageCancelled(true);
            }
        }
    }

    private boolean spawnIcePrison(final Player p, final int level, final boolean forceDestroy) {

        if (!this.toRestore.containsKey(p)) {
            this.toRestore.put(p, new HashMap<Block, Object[]>());
        }
        if (p.isSneaking() || forceDestroy) {
            if (this.toRestore.containsKey(p)) {

                final HashMap<Block, Object[]> blocks = (HashMap<Block, Object[]>) this.toRestore.get(p).clone();

                for (final Block b : blocks.keySet()) {
                    this.replaceBlock(p, b);
                    b.getWorld().playEffect(b.getLocation(), Effect.STEP_SOUND, 79);
                }

                if (forceDestroy || (!forceDestroy && p.isSneaking() && !blocks.isEmpty())) {
                    return true;
                }
            }

            if (forceDestroy) {
                return false;
            }
        }

        final me.rey.core.items.Throwable throwable = new me.rey.core.items.Throwable(new Item(Material.ICE), false);

        final Vector vec = (p.getLocation().getDirection().normalize()
                .multiply(this.throwBaseV + (this.throwChargeV) * (1 + level * this.throwLevelMultiplier))
                .setY(p.getLocation().getDirection().getY() + 0.2));
        throwable.fire(p.getEyeLocation(), vec);

        new BukkitRunnable() {

            int ticks = 0;

            @Override
            public void run() {

                final boolean check = me.rey.core.items.Throwable.checkForBlockCollision(throwable) != null;
                if (check || this.ticks > (10 * 20)) {
                    final Block block = throwable.getEntityitem().getLocation().getBlock();
                    final Location loc = block.getLocation();
                    loc.setX(loc.getX() + 0.5);
                    loc.setZ(loc.getZ() + 0.5);
                    throwable.destroy();

                    final HashMap<Block, Double> blocks = UtilLoc.getBlocksInRadius(loc, 4.2D);

                    for (final Block cur : blocks.keySet()) {
                        if (UtilBlock.airFoliage(cur)) {

                            final double offset = UtilMath.offset(block.getLocation(), cur.getLocation());
                            if (offset >= 2.8D && offset <= 4.1) {

                                if ((cur.getX() != block.getX()) || (cur.getZ() != block.getZ())
                                        || (cur.getY() <= block.getY())) {

                                    IcePrison.this.FreezeBlock(p, cur, block, level);
                                }
                            }
                        }
                    }

                    this.cancel();
                    return;
                }

                this.ticks++;
            }
        }.runTaskTimer(Warriors.getInstance().getPlugin(), 0, 1);


        this.setCooldown(20 - (level));
        return true;
    }

    public void FreezeBlock(final Player p, final Block freeze, final Block mid, final int level) {
        if (!UtilBlock.airFoliage(freeze)) {
            return;
        }

        double time = 4 + (1 * level);

        final int yDiff = freeze.getY() - mid.getY();
        time = (time - (yDiff * 1 - Math.random() * 1D));

        this.restoreLater(p, freeze, Material.ICE, time);
        freeze.getWorld().playEffect(freeze.getLocation(), Effect.STEP_SOUND, Material.ICE.getId());
    }

    public void restoreLater(final Player p, final Block block, final Material toReplace, final double time) {

        final Material type = block == null ? Material.AIR : block.getType();
        final Object[] array = new Object[2];
        array[0] = type;
        array[1] = block.getData();

        final HashMap<Block, Object[]> self = this.toRestore.get(p);
        self.put(block, array);

        me.rey.core.utils.UtilBlock.replaceBlock(PlaceCause.ABILITY, block, toReplace, (byte) 0);

        this.toRestore.replace(p, self);

        new BukkitRunnable() {

            @Override
            public void run() {
                IcePrison.this.replaceBlock(p, block);
            }

        }.runTaskLater(Warriors.getInstance().getPlugin(), (int) (time * 20));
    }

    private void replaceBlock(final Player p, final Block block) {
        if (!this.toRestore.containsKey(p)) {
            return;
        }

        final HashMap<Block, Object[]> self = this.toRestore.get(p);
        if (!self.containsKey(block)) {
            return;
        }

        final Object[] objects = self.get(block);
        self.remove(block);

        final boolean success = me.rey.core.utils.UtilBlock.replaceBlock(PlaceCause.ABILITY, block, (Material) objects[0], (byte) objects[1]);
        if (success) {
            final Location loc = block.getLocation();
            loc.setZ(loc.getZ() + 0.5);
            loc.setX(loc.getX() + 0.5);

            block.getLocation().getWorld().spigot().playEffect(loc, Effect.CLOUD, 0, 0, 0F, 0F, 0F, 0F, 1, 50);
        }

        this.toRestore.replace(p, self);
        if (self.isEmpty()) {
            this.toRestore.remove(p);
        }
    }

    @EventHandler
    public void onBlockBreak(final BlockBreakEvent e) {
        for (final Player keys : this.toRestore.keySet()) {
            for (final Block b : this.toRestore.get(keys).keySet()) {
                if (b.equals(e.getBlock())) {
                    e.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onIceMelt(final BlockFadeEvent e) {
        for (final Player keys : this.toRestore.keySet()) {
            for (final Block b : this.toRestore.get(keys).keySet()) {
                if (b.equals(e.getBlock())) {
                    e.setCancelled(true);
                    return;
                }
            }
        }
    }

}
