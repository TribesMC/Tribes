package me.rey.clans.siege.bombs;

import me.rey.clans.Tribes;
import me.rey.clans.siege.bombs.CustomExplosion.Explodable;
import me.rey.core.effects.ParticleEffect;
import me.rey.core.effects.SoundEffect;
import me.rey.core.gui.Item;
import me.rey.core.utils.ChargingBar;
import me.rey.core.utils.UtilBlock;
import me.rey.core.utils.UtilLoc;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class Bomb2 extends Explodable {

    public static Set<Block> unbreakable = new HashSet<>();

    private static final Material[] bombMats = {
            Material.TNT,
            Material.COAL_BLOCK
    };

    private static final boolean overrideBlockDura = true;
    private static final double BombSeconds = 60;

    private final CustomExplosion customExplosion = new CustomExplosion("C4", 10D, overrideBlockDura, CustomExplosion.C4_RADIUS);

    @Override
    public Item getItem() {
        return new Item(Material.TNT)
                .setName("&4C4")
                .setGlow(true);
    }

    @Override
    public void start(final Player player, final Location location) {

        final BlockFace direction = this.getCardinalDirection(player).name().toUpperCase().endsWith("_WEST")
                || this.getCardinalDirection(player).name().toUpperCase().endsWith("_EAST")
                ? this.getCardinalDirection(player, 135)
                : this.getCardinalDirection(player);
        final BlockFace direction2 = this.getCardinalDirection(player, 45);
        final BlockFace direction3 = this.getCardinalDirection(player, 90);

        final Block[] found = {
                location.getBlock(),
                location.getBlock().getRelative(BlockFace.UP),
                location.getBlock().getRelative(direction),
                location.getBlock().getRelative(direction).getRelative(BlockFace.UP),
                location.getBlock().getRelative(direction2),
                location.getBlock().getRelative(direction2).getRelative(BlockFace.UP),
                location.getBlock().getRelative(direction3),
                location.getBlock().getRelative(direction3).getRelative(BlockFace.UP),
        };

        location.getBlock().setType(Material.AIR);
        final List<Block> air = new ArrayList<>();
        for (int i = 0; i < found.length; i++) {
			if (UtilBlock.airFoliage(found[i])) {
				air.add(found[i]);
			}
		}


        final Location explosion = UtilLoc.getMidPoint(found[0].getLocation().clone().add(0.5, 0, 0.5), found[5].getLocation().clone().add(0.5, 0, 0.5));

        final int bars = 10;
        final NameTag tag = new NameTag(new ChargingBar(bars, 0).getBarString());
        tag.spawn(explosion.clone().add(0, 0.5, 0));

        new BukkitRunnable() {
            int matIndex = 0;
            int ticks = 0;

            Block[] tntBlocks = null;

            @Override
            public void run() {
                if (this.tntBlocks == null) {
					this.tntBlocks = new Block[air.size()];
					this.tntBlocks = air.toArray(this.tntBlocks);
                }

                if (this.ticks / 20 >= BombSeconds) {
					Bomb2.this.customExplosion.blow(Bomb2.this.getClanWhoPlaced(), player, explosion, this.tntBlocks);
                    tag.kill();

                    for (final Block b : found) {
						unbreakable.remove(b);
					}

                    this.cancel();
                    return;
                }

                if (this.ticks % 20 == 0) {
                    new SoundEffect(Sound.EXPLODE, 1F).setVolume(0.3F).play(explosion);

                    tag.setName(new ChargingBar(bars, (this.ticks / 20) * 100D / BombSeconds).getBarString());

					Bomb2.this.changeBlocks(this.tntBlocks, bombMats[this.matIndex]);
					this.matIndex = this.matIndex + 1 <= bombMats.length - 1 ? this.matIndex + 1 : 0;

                    final List<Block> blocks = new ArrayList<>(UtilLoc.getBlocksInRadius(explosion, 5D, 2D).keySet());
                    final int loopCount = 8;

                    for (int i = 0; i < loopCount; i++) {
                        final Block b = blocks.get(new Random().nextInt(blocks.size()));
                        new ParticleEffect(Effect.SMOKE).play(b.getLocation().clone().add(0.5, 0, 0.5));
                    }

                }

				this.ticks++;
            }
        }.runTaskTimer(Tribes.getInstance().getPlugin(), 0, 1);

    }

    private void changeBlocks(final Block[] blocks, final Material material) {
        for (final Block b : blocks) {
            unbreakable.add(b);
            b.setType(material);
        }
    }

    @EventHandler
    public void onRemove(final BlockBreakEvent e) {
        if (unbreakable.contains(e.getBlock())) {
			e.setCancelled(true);
		}
    }

    @EventHandler
    public void onIgnite(final BlockIgniteEvent e) {
        if (unbreakable.contains(e.getBlock())) {
			e.setCancelled(true);
		}
    }

    @EventHandler
    public void onExplode(final BlockExplodeEvent e) {
        if (unbreakable.contains(e.getBlock())) {
			e.setCancelled(true);
		}
    }

    private BlockFace getCardinalDirection(final Player player, final double offset) {
        double rotation = (player.getLocation().getYaw() - 180 + offset) % 360;
        if (rotation < 0) {
			rotation += 360;
		}

        if (0 <= rotation && rotation < 22.5) {
			return BlockFace.NORTH;
		} else if (22.5 <= rotation && rotation < 67.5) {
			return BlockFace.NORTH_EAST;
		} else if (67.5 <= rotation && rotation < 112.5) {
			return BlockFace.EAST;
		} else if (112.5 <= rotation && rotation < 157.5) {
			return BlockFace.SOUTH_EAST;
		} else if (157.5 <= rotation && rotation < 202.5) {
			return BlockFace.SOUTH;
		} else if (202.5 <= rotation && rotation < 247.5) {
			return BlockFace.SOUTH_WEST;
		} else if (247.5 <= rotation && rotation < 292.5) {
			return BlockFace.WEST;
		} else if (292.5 <= rotation && rotation < 337.5) {
			return BlockFace.NORTH_WEST;
		} else if (337.5 <= rotation && rotation < 360) {
			return BlockFace.NORTH;
		}

        return BlockFace.NORTH;
    }

    private BlockFace getCardinalDirection(final Player player) {
        return this.getCardinalDirection(player, 0);
    }
}
