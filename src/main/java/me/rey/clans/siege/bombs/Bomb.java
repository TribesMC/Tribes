package me.rey.clans.siege.bombs;

import me.rey.clans.Tribes;
import me.rey.clans.siege.bombs.CustomExplosion.Explodable;
import me.rey.core.effects.SoundEffect;
import me.rey.core.gui.Item;
import me.rey.core.utils.ChargingBar;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;

public class Bomb extends Explodable {

    public static Set<Block> unbreakable = new HashSet<>();

    private static final Material[] bombMats = {
            Material.TNT,
            Material.COAL_BLOCK
    };

    private static final boolean overrideBlockDura = false;
    private static final double BombSeconds = 30;

    private final CustomExplosion customExplosion = new CustomExplosion("C4", 10D, overrideBlockDura, CustomExplosion.BOMB_RADIUS);

    @Override
    public Item getItem() {
        return new Item(Material.TNT)
                .setName("&4BOMB");
    }

    @Override
    public void start(final Player player, final Location location) {

        final Location explosion = location.getBlock().getLocation().clone().add(0.5, 0, 0.5);

        final int bars = 10;
        final NameTag tag = new NameTag(new ChargingBar(bars, 0).getBarString());
        tag.spawn(explosion.clone().add(0, 0, 0));

        unbreakable.add(location.getBlock());

        new BukkitRunnable() {
            int matIndex = 0;
            int ticks = 0;

            @Override
            public void run() {

                if (this.ticks / 20 >= BombSeconds) {
                    final Block[] blocks = {location.getBlock()};
					Bomb.this.customExplosion.blow(Bomb.this.getClanWhoPlaced(), player, explosion, blocks);
                    tag.kill();

                    unbreakable.remove(location.getBlock());

                    this.cancel();
                    return;
                }

                if (this.ticks % 20 == 0) {
                    new SoundEffect(Sound.EXPLODE, 1.5F).setVolume(0.1F).play(explosion);

                    tag.setName(new ChargingBar(bars, (this.ticks / 20) * 100D / BombSeconds).getBarString());

                    location.getBlock().setType(bombMats[this.matIndex]);
					this.matIndex = this.matIndex + 1 <= bombMats.length - 1 ? this.matIndex + 1 : 0;

                }

				this.ticks++;
            }
        }.runTaskTimer(Tribes.getInstance().getPlugin(), 0, 1);

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

}
