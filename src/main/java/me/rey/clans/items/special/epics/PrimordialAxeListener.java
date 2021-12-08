package me.rey.clans.items.special.epics;

import me.rey.clans.Tribes;
import me.rey.clans.items.special.ClansTool;
import me.rey.core.utils.UtilPacket;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class PrimordialAxeListener extends EpicItem {

    // CHANGES \\
    // Seismic Chop ability automatically mines wood connected to the one mined

    private final Map<UUID, Long> cooldown;
    private final Queue<Pair<Location, Material>> logQueue;
    private BukkitTask blockBreakerTimer;

    public PrimordialAxeListener(final Tribes plugin) {
        super(plugin, "PRIMORDIAL_AXE", EpicType.PRIMORDIAL_AXE, ClansTool.PRIMORDIAL_AXE, ClansTool.PRIMORDIAL_AXE_DISABLED);
        this.cooldown = new HashMap<>();

        this.logQueue = new LinkedList<>();

        this.blockBreakerTimer = new BukkitRunnable() {
            @Override
            public void run() {
                if (PrimordialAxeListener.this.logQueue.size() <= 0) {
                    return;
                }

                final Pair<Location, Material> dataSet = PrimordialAxeListener.this.logQueue.poll();
                if (dataSet == null) {
                    return;
                }
                final Block block = dataSet.getLeft().getWorld().getBlockAt(dataSet.getLeft());
                if (block == null) {
                    return;
                }
                if (block.getType() != dataSet.getRight()) {
                    return;
                }
                block.breakNaturally();
            }
        }.runTaskTimer(plugin.getPlugin(), 0, 0);
    }

    @Override
    public void update() {
        if (this.specialItemUpdater.isDisabled(this)) {
            return;
        }
        for (final UUID uuid : this.cooldown.keySet()) {
            final Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline()) {
                return;
            }
            if ((System.currentTimeMillis() - this.cooldown.get(uuid)) / 1000L > 5L) {
                this.cooldown.remove(uuid);
                if (!this.isOfArchetype(player.getItemInHand(), this.epic)) {
                    continue;
                }
                UtilPacket.sendActionBarMessage(player, ChatColor.GREEN + "" + ChatColor.BOLD + "Seismic Chop Recharged");
            } else {
                if (!this.isOfArchetype(player.getItemInHand(), this.epic)) {
                    continue;
                }
                final double x = 6.0 - Math.pow(10.0, -1.0) * ((double) (System.currentTimeMillis() - this.cooldown.get(player.getUniqueId())) / 100L);
                final double divide = (System.currentTimeMillis() - this.cooldown.get(uuid)) / 6000.0;
                final String[] doubleParse = Double.toString(x).replace('.', '-').split("-");
                final String concat = doubleParse[0] + "." + doubleParse[1].charAt(0);
                UtilPacket.displayProgress(ChatColor.BOLD + "Seismic Chop", divide, ChatColor.WHITE + " " + concat + " Seconds", false, player);
            }
        }
    }

    @Override
    public void serverShutdown() {
        this.cooldown.clear();
        this.logQueue.clear();
        this.blockBreakerTimer.cancel();
        this.blockBreakerTimer = null;
    }

    @EventHandler
    private void onBlockBreak(final BlockBreakEvent event) {
        if (this.specialItemUpdater.isDisabled(this)) {
            return;
        }
        if (event.isCancelled()) {
            return;
        }

        if (this.isOfArchetype(event.getPlayer().getItemInHand(), this.epic)) {
            if (this.cooldown.containsKey(event.getPlayer().getUniqueId())) {
                return;
            }
            this.cooldown.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());

            for (final Block block : this.getConnectedLogs(event.getBlock())) {
                this.logQueue.add(new ImmutablePair<>(block.getLocation(), block.getType()));
            }
        }
    }

    private List<Block> getConnectedLogs(final Block block) {
        if (this.specialItemUpdater.isDisabled(this)) {
            return new ArrayList<>();
        }
        final List<Block> logs = new ArrayList<>();
        final List<Block> referenceNext = new ArrayList<>(Collections.singletonList(block));
        final List<Block> referencedPreviously = new ArrayList<>(Collections.singletonList(block));

        while (logs.size() <= 30 && referenceNext.size() > 0) {
            final Block reference = referenceNext.remove(0);
            if (!logs.contains(reference)) {
                logs.add(reference);
            }

            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    for (int y = -1; y <= 1; y++) {
                        final Block detected = reference.getRelative(x, y, z);
                        if (detected == null || detected.getType() == Material.AIR) {
                            continue;
                        }
                        if (detected.getType() != Material.LOG && detected.getType() != Material.LOG_2) {
                            continue;
                        }
                        if (detected.getState().getData().toItemStack().getDurability() != reference.getState().getData().toItemStack().getDurability()) {
                            continue;
                        }

                        if (!referencedPreviously.contains(detected)) {
                            referenceNext.add(detected);
                            referencedPreviously.add(detected);
                        }
                    }
                }
            }
        }

        return logs;
    }

    @EventHandler
    private void onPlayerQuit(final PlayerQuitEvent event) {
        this.cooldown.remove(event.getPlayer().getUniqueId());
    }
}