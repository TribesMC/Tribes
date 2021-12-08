package me.rey.core.classes.abilities.bandit.axe;

import me.rey.core.Warriors;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.enums.AbilityFail;
import me.rey.core.events.customevents.ability.AbilityFailEvent;
import me.rey.core.players.User;
import me.rey.core.utils.UtilBlock;
import me.rey.core.utils.UtilLoc;
import me.rey.core.utils.Utils;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

public class Blink extends Ability {

    private final int deblinkSeconds = 4;
    private final String deblink = "De-Blink";
    private final HashMap<UUID, Location> canDeblink;

    public Blink() {
        super(111, "Blink", ClassType.BLACK, AbilityType.AXE, 1, 4, 12, Arrays.asList(
                "Instantly teleport forwards <variable>3*l+9</variable> (+3) Blocks.",
                "",
                "Using again within <variable>4</variable> seconds De-Blinks,",
                "returning you to your original location.",
                "Cannot be used while Slowed",
                "",
                "Recharge: 12.0 Seconds"
        ));

        this.canDeblink = new HashMap<>();
        this.setWhileSlowed(false);
        this.setInLiquid(false);
    }

    @Override
    protected boolean execute(final User u, final Player p, final int level, final Object... conditions) {
        final Location init = p.getLocation();
        final double range = 3 * level + 9;

        Block b = null;

        if (this.isSafeForTeleport(p.getLocation())) { /* TODO Temporary Condition Solution for non-cubic blocks */

            if (!UtilLoc.atBlockGap(p, p.getLocation().getBlock()) && !UtilLoc.atBlockGap(p, UtilBlock.getBlockAbove(p.getLocation().getBlock()))) {
                for (double i = 1; i < range; i += 0.5D) {

                    if (i >= 8D) {
                        break;
                    }

                    if (UtilLoc.atBlockGap(p, UtilBlock.getTargetBlock(p, i)) || UtilLoc.atBlockGap(p, UtilBlock.getBlockAbove(UtilBlock.getTargetBlock(p, i)))) {
                        b = UtilBlock.getTargetBlock(p, i - 1);
                        break;
                    }

                    if (this.isSafeForTeleport(UtilBlock.getTargetBlock(p, i).getLocation())) {
                        b = UtilBlock.getTargetBlock(p, i);
                    } else {
                        break;
                    }
                }
            }
        }


        Location loc = null;

        if (b != null) {
            loc = b.getLocation();
            loc.add(0.5, -1, 0.5);

            loc.setYaw(p.getLocation().getYaw());
            loc.setPitch(p.getLocation().getPitch());
        }

        if (loc != null) {
            Location tpLoc = null;

            for (int i = 1; i <= range; i += 1) {

                if (this.isSafeForTeleport(p.getTargetBlock((Set<Material>) null, i).getLocation())) {

                    final Block tb = p.getTargetBlock((Set<Material>) null, i);
                    final float dir = (float) Math.toDegrees(Math.atan2(p.getLocation().getBlockX() - tb.getX(), tb.getZ() - p.getLocation().getBlockZ()));
                    final BlockFace face = UtilBlock.getClosestFace(dir);

                    if (face == BlockFace.NORTH || face == BlockFace.EAST || face == BlockFace.SOUTH || face == BlockFace.WEST) {
                        final Location tloc = tb.getLocation();

                        if (face == BlockFace.NORTH) {
                            tloc.setX(tloc.getX() + 1.35);
                            tloc.setZ(tloc.getZ() + 0.5);
                        }

                        if (face == BlockFace.EAST) {
                            tloc.setZ(tloc.getZ() + 1.35);
                            tloc.setX(tloc.getX() + 0.5);
                        }

                        if (face == BlockFace.SOUTH) {
                            tloc.setX(tloc.getX() - 0.35);
                            tloc.setZ(tloc.getZ() + 0.5);
                        }

                        if (face == BlockFace.WEST) {
                            tloc.setZ(tloc.getZ() - 0.35);
                            tloc.setX(tloc.getX() + 0.5);
                        }

                        tloc.setY(loc.getY());

                        if (loc.getPitch() >= 5.7) {
                            tloc.add(0, +1, 0);
                        }

                        tloc.setYaw(p.getLocation().getYaw());
                        tloc.setPitch(p.getLocation().getPitch());

                        boolean enemyCollision = false;
                        final double hitbox = 0.5;
                        for (final Entity e : tloc.getWorld().getNearbyEntities(tloc, 0.5, 1, 0.5)) {
                            if (e instanceof LivingEntity) {
                                final LivingEntity le = (LivingEntity) e;

                                if (!u.getTeam().contains(le)) {
                                    enemyCollision = true;
                                    break;
                                }
                            }
                        }

                        if (this.isSafeForTeleport(tloc) && !enemyCollision) {
                            tpLoc = tloc;
                        } else {
                            break;
                        }
                    }
                }
            }

            if (tpLoc != null) {
                this.makeParticlesBetween(p.getLocation(), tpLoc);
                p.teleport(tpLoc);
            }

        }

        p.setFallDistance(0);
        p.getWorld().playEffect(p.getLocation(), Effect.BLAZE_SHOOT, 0);
        this.sendUsedMessageToPlayer(p, this.getName());

        // ADDING TO DEBLINK USER LIST
        this.canDeblink.put(p.getUniqueId(), init);
        new BukkitRunnable() {

            @Override
            public void run() {
                // TRYING TO REMOVE IF HE HASN'T DEBLINKED
                if (Blink.this.canDeblink.containsKey(p.getUniqueId())) {
                    Blink.this.canDeblink.remove(p.getUniqueId());
                }

            }

        }.runTaskLater(Warriors.getInstance().getPlugin(), this.deblinkSeconds * 20);
        return true;
    }

    private boolean isInLiquid(final Player p) {
        return p.getLocation().getBlock() != null && p.getLocation().getBlock().isLiquid();
    }

    @EventHandler
    public void onDeblink(final AbilityFailEvent e) {
        if (e.getAbility() != this || e.getFail() != AbilityFail.COOLDOWN) {
            return;
        }
        if (!this.canDeblink.containsKey(e.getPlayer().getUniqueId())) {
            return;
        }

        if (this.isInLiquid(e.getPlayer())) {
            this.sendAbilityMessage(e.getPlayer(), "You cannot deblink in water!");
            return;
        }

        final Location to = this.canDeblink.get(e.getPlayer().getUniqueId());
        final Location from = e.getPlayer().getLocation();
        to.setPitch(from.getPitch());
        to.setYaw(from.getYaw());

        e.getPlayer().getWorld().playEffect(e.getPlayer().getLocation(), Effect.BLAZE_SHOOT, 0);
        this.makeParticlesBetween(from, to);
        e.getPlayer().teleport(to);
        e.getPlayer().setFallDistance(0);
        this.canDeblink.remove(e.getPlayer().getUniqueId());
        this.sendUsedMessageToPlayer(e.getPlayer(), this.deblink);
        e.setMessageCancelled(true);
    }

    private void makeParticlesBetween(final Location init, final Location loc) {
        final Vector pvector = Utils.getDirectionBetweenLocations(init, loc);
        for (double i = 1; i <= init.distance(loc); i += 0.2) {
            pvector.multiply(i);
            init.add(pvector);
            final Location toSpawn = init.clone();
            toSpawn.setY(toSpawn.getY() + 0.5);
            init.getWorld().spigot().playEffect(toSpawn, Effect.LARGE_SMOKE, 0, 0, 0F, 0F, 0F, 0F, 3, 100);
            init.subtract(pvector);
            pvector.normalize();
        }
    }

    private boolean isSafeForTeleport(final Location loc) {
        final Block blockunder = loc.getBlock().getRelative(BlockFace.DOWN);
        final Block blockabove = loc.getBlock().getRelative(BlockFace.UP);

        if (!loc.getBlock().getType().isSolid() && !blockabove.getType().isSolid() && !this.isLiftingBlock(blockunder)) {
            return true;
        }

        if (!loc.getBlock().getType().isSolid() && !blockunder.getType().isSolid() && !this.isLiftingBlock(blockabove)) {
            return true;
        }

        return false;
    }

    private boolean isLiftingBlock(final Block b) {
        return b.getType().toString().contains("WALL")
                || b.getType().toString().contains("FENCE");
    }

    private boolean isFloor(final Block block) {
        final Block above = block.getRelative(BlockFace.UP);
        if (this.isAir(above)) {
            return true;
        }
        return false;
    }

    private boolean isAir(final Block block) {
        if (block == null || block.getType().equals(Material.AIR)) {
            return true;
        }
        return false;
    }

}
