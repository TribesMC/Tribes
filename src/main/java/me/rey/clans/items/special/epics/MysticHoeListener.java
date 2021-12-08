package me.rey.clans.items.special.epics;

import me.rey.clans.Tribes;
import me.rey.clans.items.special.ClansTool;
import me.rey.core.utils.UtilMath;
import me.rey.core.utils.UtilPacket;
import me.rey.core.utils.UtilParticle;
import me.rey.core.utils.UtilPlayer;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Crops;
import org.bukkit.material.MaterialData;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class MysticHoeListener extends EpicItem {

    // CHANGES \\
    // Ability "Spontaneous Growth" grows all crops and saplings around it a random amount
    // Spontaneous Growth cooldown set to 30
    // Mystic Hoe will automatically replace crops mined with seeds from the inventory
    // Mystic Hoe will add 2 crops to those collected

    private final Map<UUID, Long> cooldown;

    public MysticHoeListener(final Tribes plugin) {
        super(plugin, "MYSTIC_HOE", EpicType.MYSTIC_HOE, ClansTool.MYSTIC_HOE, ClansTool.MYSTIC_HOE_DISABLED);
        this.cooldown = new HashMap<>();
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
            if ((System.currentTimeMillis() - this.cooldown.get(uuid)) / 1000L > 30L) {
                this.cooldown.remove(uuid);
                if (!this.isOfArchetype(player.getItemInHand(), this.epic)) {
                    continue;
                }
                UtilPacket.sendActionBarMessage(player, ChatColor.GREEN + "" + ChatColor.BOLD + "Spontaneous Growth Recharged");
            } else {
                if (!this.isOfArchetype(player.getItemInHand(), this.epic)) {
                    continue;
                }
                final double x = 31.0 - Math.pow(10.0, -1.0) * ((double) (System.currentTimeMillis() - this.cooldown.get(player.getUniqueId())) / 100L);
                final double divide = (System.currentTimeMillis() - this.cooldown.get(uuid)) / 31000.0;
                final String[] doubleParse = Double.toString(x).replace('.', '-').split("-");
                final String concat = doubleParse[0] + "." + doubleParse[1].charAt(0);
                UtilPacket.displayProgress(ChatColor.BOLD + "Spontaneous Growth", divide, ChatColor.WHITE + " " + concat + " Seconds", false, player);
            }
        }
    }

    @EventHandler
    private void onToggleSneak(final PlayerToggleSneakEvent event) {
        if (this.specialItemUpdater.isDisabled(this)) {
            return;
        }
        final Player player = event.getPlayer();
        if (!this.isOfArchetype(player.getItemInHand(), this.epic)) {
            return;
        }
        if (this.cooldown.containsKey(player.getUniqueId())) {
            return;
        }
        this.cooldown.put(player.getUniqueId(), System.currentTimeMillis());

        final Location playerLoc = player.getLocation();
        final Location top;
        final Location bottom;
        top = new Location(playerLoc.getWorld(), playerLoc.getX() + 3, playerLoc.getY() + 3, playerLoc.getZ() + 3);
        bottom = new Location(playerLoc.getWorld(), playerLoc.getX() - 3, playerLoc.getY() - 3, playerLoc.getZ() - 3);

        for (final Location loc : UtilMath.getLocationsBetween(top, bottom)) {
            this.growBlock(loc);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onBlockBreak(final BlockBreakEvent event) {
        if (this.specialItemUpdater.isDisabled(this)) {
            return;
        }
        if (event.isCancelled()) {
            return;
        }

        if (this.isOfArchetype(event.getPlayer().getItemInHand(), this.epic)) {

            final Material type = event.getBlock().getType();
            final BlockState bs = event.getBlock().getState();
            final MaterialData md = bs.getData();

            switch (type) {
                case CARROT:
                    final Crops crops = (Crops) md;
                    final CropState cs = crops.getState();
                    if (cs.getData() == 7) {
                        event.getBlock().getLocation().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(Material.CARROT_ITEM, 2));
                    }
                    if (UtilPlayer.countItem(event.getPlayer(), new ItemStack(Material.CARROT_ITEM)) > 0) {
                        event.setCancelled(true);
                        if (cs.getData() == 7) {
                            event.getBlock().getLocation().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(Material.CARROT_ITEM, 1));
                        }
                        crops.setState(CropState.getByData((byte) 1));
                        bs.setData(crops);
                        bs.update(true);
                        UtilPlayer.removeAmountOf(event.getPlayer(), new ItemStack(Material.CARROT_ITEM), 1);
                    }
                    break;
                case POTATO:
                    final Crops crops1 = (Crops) md;
                    final CropState cs1 = crops1.getState();
                    if (cs1.getData() == 7) {
                        event.getBlock().getLocation().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(Material.POTATO_ITEM, 2));
                    }
                    if (UtilPlayer.countItem(event.getPlayer(), new ItemStack(Material.POTATO_ITEM)) > 0) {
                        event.setCancelled(true);
                        if (cs1.getData() == 7) {
                            event.getBlock().getLocation().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(Material.POTATO_ITEM, 1));
                        }
                        crops1.setState(CropState.getByData((byte) 1));
                        bs.setData(crops1);
                        bs.update(true);
                        UtilPlayer.removeAmountOf(event.getPlayer(), new ItemStack(Material.POTATO_ITEM), 1);
                    }
                    break;
                case CROPS:
                    final Crops crops2 = (Crops) md;
                    final CropState cs2 = crops2.getState();
                    if (cs2.getData() == 7) {
                        event.getBlock().getLocation().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(Material.WHEAT, 2));
                    }
                    if (UtilPlayer.countItem(event.getPlayer(), new ItemStack(Material.SEEDS)) > 0) {
                        event.setCancelled(true);
                        event.getBlock().getLocation().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(Material.SEEDS, cs2.getData() == 7 ? new Random().nextInt(2) + 1 : 1));
                        if (cs2.getData() == 7) {
                            event.getBlock().getLocation().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(Material.WHEAT, 1));
                        }
                        crops2.setState(CropState.getByData((byte) 1));
                        bs.setData(crops2);
                        bs.update(true);
                        UtilPlayer.removeAmountOf(event.getPlayer(), new ItemStack(Material.SEEDS), 1);
                    }
                    break;
            }
        }
    }

    @EventHandler
    private void onInteract(final PlayerInteractEvent event) {
        if (this.specialItemUpdater.isDisabled(this)) {
            return;
        }
        final Player player = event.getPlayer();
        final ItemStack item = player.getItemInHand();
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (!this.isOfArchetype(item, this.epic)) {
                return;
            }

            final Block block = event.getClickedBlock();
            if (block == null) {
                return;
            }

            if (block.getType() == Material.GRASS || block.getType() == Material.DIRT) {
                block.setType(Material.SOIL);
                block.getLocation().getWorld().playSound(block.getLocation(), Sound.DIG_GRASS, 1, 1);
            }
        }
    }

    @Override
    public void serverShutdown() {
        this.cooldown.clear();
    }

    @EventHandler
    private void onPlayerQuit(final PlayerQuitEvent event) {
        this.cooldown.remove(event.getPlayer().getUniqueId());
    }

    private void growBlock(final Location location) {
        if (this.specialItemUpdater.isDisabled(this)) {
            return;
        }
        final Block block = location.getWorld().getBlockAt(location);
        final BlockState bs = block.getState();
        final MaterialData md = bs.getData();

        int data;
        switch (block.getType()) {
            case CARROT:
            case POTATO:
            case CROPS:
                final Crops crops = (Crops) md;
                final CropState cs = crops.getState();
                data = cs.getData() + new Random().nextInt(3) + 1;
                if (data > 7) {
                    data = 7;
                }
                crops.setState(CropState.getByData((byte) data));
                bs.setData(md);
                bs.update(true);
                for (final Player player : Bukkit.getOnlinePlayers()) {
                    UtilParticle.showParticle(player, location.add(0.5, 0.5, 0.5), 21, 0.5f, 0.5f, 0.2f, 0, 5);
                }
                break;
            case SAPLING:
                if (new Random().nextInt(3) + 1 == 3) {
                    TreeType treeType = null;
                    switch (block.getData()) {
                        case 0:
                            treeType = TreeType.TREE;
                            break;
                        case 1:
                            treeType = TreeType.REDWOOD;
                            break;
                        case 2:
                            treeType = TreeType.BIRCH;
                            break;
                        case 3:
                            treeType = TreeType.SMALL_JUNGLE;
                            break;
                        case 4:
                            treeType = TreeType.ACACIA;
                            break;
                        case 5:
                            treeType = TreeType.DARK_OAK;
                            break;
                    }
                    if (treeType == null) {
                        return;
                    }
                    block.getLocation().getWorld().generateTree(block.getLocation(), treeType);
                }
                for (final Player player : Bukkit.getOnlinePlayers()) {
                    UtilParticle.showParticle(player, location.add(0.5, 0.5, 0.5), 21, 0.5f, 0.5f, 0.2f, 0, 5);
                }
                break;
            case PUMPKIN_STEM:
            case MELON_STEM:
                data = bs.getData().getData() + new Random().nextInt(3) + 1;
                if (data > 7) {
                    data = 7;
                }
                bs.setRawData((byte) data);
                bs.update(true);
                for (final Player player : Bukkit.getOnlinePlayers()) {
                    UtilParticle.showParticle(player, location.add(0.5, 0.5, 0.5), 21, 0.5f, 0.5f, 0.2f, 0, 5);
                }
                break;
        }
    }
}