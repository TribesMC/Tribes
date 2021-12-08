package me.rey.clans.events;

import me.rey.clans.Tribes;
import me.rey.clans.clans.Clan;
import me.rey.clans.clans.ClansPlayer;
import me.rey.clans.clans.ClansRank;
import me.rey.clans.commands.base.Claim;
import me.rey.clans.events.clans.PlayerEditClaimEvent;
import me.rey.clans.events.clans.PlayerEditClaimEvent.ClaimPermission;
import me.rey.clans.events.clans.PlayerEditClaimEvent.EditAction;
import me.rey.clans.events.custom.ContainerOpenEvent;
import me.rey.clans.utils.ErrorCheck;
import me.rey.clans.utils.References;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.material.Door;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Openable;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class ClaimProtection implements Listener {

    /*
     * Events Listened for: - DamageEvent (Custom) - PlayerInteractEvent -
     * PlayerBedEnterEvent - PlayerBucketEvent - BlockBreakEvent - BlockPlaceEvent -
     * BlockPistonEvent - BlockBurnEvent - LeavesDecayEvent - WeatherChangeEvent -
     * EnchantItemEvent - PrepareItemEnchantEvent - PlayerFishEvent
     */

    public static List<Material> containers = Arrays.asList(Material.DISPENSER, Material.CHEST, Material.TRAPPED_CHEST, Material.FURNACE, Material.DROPPER, Material.HOPPER, Material.ANVIL);

    public static List<Material> interactables = Arrays.asList(
            /* Fence Gates */    Material.FENCE_GATE, Material.ACACIA_FENCE_GATE, Material.BIRCH_FENCE_GATE, Material.DARK_OAK_FENCE_GATE, Material.JUNGLE_FENCE_GATE, Material.SPRUCE_FENCE_GATE,
            /* Doors */        Material.WOODEN_DOOR, Material.ACACIA_DOOR, Material.BIRCH_DOOR, Material.DARK_OAK_DOOR, Material.JUNGLE_DOOR, Material.SPRUCE_DOOR,
            /* Etc. */            Material.WOOD_BUTTON, Material.STONE_BUTTON, Material.TRAP_DOOR, Material.LEVER,
            /* Redstone */        Material.REDSTONE_COMPARATOR, Material.REDSTONE_COMPARATOR_ON, Material.REDSTONE_COMPARATOR_OFF, Material.DIODE, Material.DIODE_BLOCK_ON, Material.DIODE_BLOCK_OFF,
            /* Press. Plates */    Material.WOOD_PLATE, Material.STONE_PLATE, Material.IRON_PLATE, Material.GOLD_PLATE);

    public static Clan isNearOtherClaim(final Player player, final Block block) {
        final ClansPlayer cp = new ClansPlayer(player);

        final World w = block.getWorld();
        final Block[] sides = new Block[8];
        final int x = block.getX();
		final int y = block.getY();
		final int z = block.getZ();
		sides[0] = w.getBlockAt(new Location(w, x - 1, y, z));
        sides[1] = w.getBlockAt(new Location(w, x + 1, y, z)); // sides
        sides[2] = w.getBlockAt(new Location(w, x, y, z - 1));
        sides[3] = w.getBlockAt(new Location(w, x, y, z + 1)); // sides
        sides[4] = w.getBlockAt(new Location(w, x + 1, y, z + 1));
        sides[5] = w.getBlockAt(new Location(w, x + 1, y, z - 1)); // corners
        sides[6] = w.getBlockAt(new Location(w, x - 1, y, z + 1));
        sides[7] = w.getBlockAt(new Location(w, x - 1, y, z - 1)); // corners

        for (final Block b : sides) {
            final Chunk chunk = b.getChunk();
            if (chunk.equals(block.getChunk())) {
				continue;
			}
            if (isInClaim(chunk) == null) {
				continue;
			}

            final Clan other = isInClaim(chunk);
            if (other.isServerClan()) {
				continue;
			}
            if (cp.hasClan() && other.compare(cp.getClan())) {
				continue;
			}

            return other;
        }

        return null;
    }

    public static Clan isInOtherClaim(final Player player, final Block block) {
        final Clan owner = Tribes.getInstance().getClanFromTerritory(block.getChunk());
        final ClansPlayer self = new ClansPlayer(player);
        return owner == null ? null : (self.hasClan() && self.getClan().compare(owner) ? null : owner);
    }

    public static Clan isInClaim(final Chunk chunk) {
        return Tribes.getInstance().getClanFromTerritory(chunk);
    }

    @EventHandler
    public void onInteract(final PlayerInteractEvent e) {

        for (final Set<Block> set : Claim.fakeBlocks) {
            if (e.getClickedBlock() == null) {
				break;
			}
            if (set.contains(e.getClickedBlock())) {

                for (final Player online : Bukkit.getOnlinePlayers()) {
					for (final Block b : set) {
						online.sendBlockChange(b.getLocation(), b.getType(), (byte) b.getData());
					}
				}

                Claim.fakeBlocks.remove(set);
                break;
            }
        }

        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}

        if (e.getClickedBlock() != null && e.getClickedBlock().getType().equals(References.HOME_BLOCK)) {
			e.setCancelled(true);
		}
        final Block clicked = e.getClickedBlock();

        if (isInOtherClaim(e.getPlayer(), clicked) != null) {

            if (containers.contains(clicked.getType()) || interactables.contains(clicked.getType())) {
                final ContainerOpenEvent event = new ContainerOpenEvent(e.getPlayer(), clicked, false);
                Bukkit.getServer().getPluginManager().callEvent(event);
                if (event.isAllowed()) {
					return;
				}
            }

            final Clan found = isInOtherClaim(e.getPlayer(), clicked);

            final PlayerEditClaimEvent event = new PlayerEditClaimEvent(found, e.getPlayer(), ClaimPermission.DENY, EditAction.PLACE, e.getPlayer().getItemInHand(), e.getClickedBlock());
            Bukkit.getServer().getPluginManager().callEvent(event);

            if (event.getPermission().equals(ClaimPermission.ALLOW)) {
				return;
			}

            final ClansPlayer self = new ClansPlayer(e.getPlayer());
            self.sendMessageWithPrefix("Error", ErrorCheck.getInClaimMessage(self, found));
            e.setCancelled(true);

        } else {

            // IRON DOOR
            if (clicked.getType().equals(Material.IRON_DOOR_BLOCK)) {
                BlockState blockState = clicked.getState();
                if (((Door) blockState.getData()).isTopHalf()) {
                    blockState = clicked.getRelative(BlockFace.DOWN).getState();
                }

                final Openable openable = (Openable) blockState.getData();
                openable.setOpen(!openable.isOpen());
                blockState.setData((MaterialData) openable);

                blockState.update();
                e.getPlayer().getWorld().playSound(e.getPlayer().getLocation(), Sound.DOOR_OPEN, 5L, 1F);
                return;
            }

            // IRON TRAP DOOR
            if (clicked.getType().equals(Material.IRON_TRAPDOOR)) {
                final BlockState blockState = clicked.getState();

                final Openable openable = (Openable) blockState.getData();
                openable.setOpen(!openable.isOpen());
                blockState.setData((MaterialData) openable);

                blockState.update();
                e.getPlayer().getWorld().playSound(e.getPlayer().getLocation(), Sound.DOOR_OPEN, 5L, 1F);
            }

        }
    }

    @EventHandler
    public void onBlockBreak(final BlockBreakEvent e) {
        if (e.getBlock() == null || e.getBlock().getType().equals(Material.AIR)) {
			return;
		}

        final Block broken = e.getBlock();
        final PlayerEditClaimEvent event = new PlayerEditClaimEvent(this.isInAClaim(broken), e.getPlayer(),
                ClaimPermission.ALLOW, EditAction.BREAK, e.getPlayer().getItemInHand(), e.getBlock());
        Bukkit.getServer().getPluginManager().callEvent(event);

        if (event.getPermission().equals(ClaimPermission.DENY)) {
			e.setCancelled(true);
		}

    }

    @EventHandler
    public void onBlockPlace(final BlockPlaceEvent e) {
        if (e.getBlock() == null || e.getBlock().getType().equals(Material.AIR)) {
			return;
		}

        final PlayerEditClaimEvent event = new PlayerEditClaimEvent(isInOtherClaim(e.getPlayer(), e.getBlock()),
                e.getPlayer(), ClaimPermission.ALLOW, EditAction.PLACE, e.getPlayer().getItemInHand(), e.getBlock());
        Bukkit.getServer().getPluginManager().callEvent(event);

        if (event.getPermission().equals(ClaimPermission.DENY)) {
			e.setCancelled(true);
		}

    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEditClaim(final PlayerEditClaimEvent e) {
        final Block block = e.getBlockToReplace();
        final ClansPlayer self = new ClansPlayer(e.getPlayer());

        switch (e.getAction()) {

            case BREAK:

                if (e.getTerritoryOwner() != null) {
                    final Clan other = e.getTerritoryOwner();

                    /*
                     * Handling Clan Home Break
                     */
                    if (block.getType().equals(References.HOME_BLOCK) && other.getHome() != null) {
                        final int x = other.getHome().getBlockX();
						final int y = other.getHome().getBlockY();
						final int z = other.getHome().getBlockZ();

						if (block.getX() == x && block.getY() == y && block.getZ() == z) {

                            if (self.hasClan() && self.getClan().compare(other) && self.getClan()
                                    .getPlayerRank(e.getPlayer().getUniqueId()).getPower() < ClansRank.ADMIN.getPower()) {

                                ErrorCheck.incorrectRank(self.getPlayer(), ClansRank.ADMIN);
                                e.setPermission(ClaimPermission.DENY);
                                e.setSendPermissionMessage(false);
                                return;
                            }

                            block.getDrops().clear();
                            e.setPermission(ClaimPermission.ALLOW);
                            e.setSendPermissionMessage(false);

                            self.sendMessageWithPrefix("Tribe", "You broke the Clan home of &s" + other.getName() + "&r.");
                            block.setType(Material.AIR);
                            other.setHome(null);
                            Tribes.getInstance().getSQLManager().saveClan(other);

                            return;
                        }
                    }

                    /*
                     * If trying to place in a foreign claim
                     */
                    if (!self.hasClan() || !other.compare(self.getClan())) {
                        e.setPermissionMessage(ErrorCheck.getInClaimMessage(self, other));
                        e.setPermission(ClaimPermission.DENY);
                        return;
                    }


                }

                break;

            case PLACE:

                /*
                 * Check if they placed INSIDE claim
                 */
                if (e.getTerritoryOwner() != null) {
                    e.setPermissionMessage(null);
                    e.setPermission(ClaimPermission.DENY);
                    return;
                }

                /* Check if they placed 1 BLOCK NEAR a claim */
                final Clan near = isNearOtherClaim(e.getPlayer(), block);
                if (near != null) {
                    e.setPermission(ClaimPermission.DENY);
                    e.setPermissionMessage(ErrorCheck.getNearClaimMessage(self, near));
                    return;
                }

                break;
        }

    }

    @EventHandler
    public void onPermissionMessage(final PlayerEditClaimEvent e) {
        if (!e.shouldSendPermissionMessage() || e.getPermissionMessage() == null) {
			return;
		}

        final String prefix = e.getPermission().equals(ClaimPermission.DENY) ? "Error" : "Territory";
        new ClansPlayer(e.getPlayer()).sendMessageWithPrefix(prefix, e.getPermissionMessage());
    }

    @EventHandler
    public void fishEvent(final PlayerFishEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void prepareEnchant(final PrepareItemEnchantEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void enchantItem(final EnchantItemEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void weatherEvent(final WeatherChangeEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void leavesDecay(final LeavesDecayEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void blockBurnEvent(final BlockBurnEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void playerBucketEvent(final PlayerBucketEmptyEvent e) {
        if (!e.getPlayer().isOp()) {
			e.setCancelled(true);
		}
    }

    @EventHandler
    public void playerBucketFill(final PlayerBucketFillEvent e) {
        if (!e.getPlayer().isOp()) {
			e.setCancelled(true);
		}
    }

    @EventHandler
    public void bedEnter(final PlayerBedEnterEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onWaterSpread(final BlockFromToEvent e) {
        if (e.getBlock().getType() == Material.WATER || e.getBlock().getType() == Material.STATIONARY_WATER || e.getBlock().getType() == Material.LAVA || e.getBlock().getType() == Material.STATIONARY_LAVA) {
            final Clan origin = Tribes.getInstance().getClanFromTerritory(e.getBlock().getChunk());
            final Clan newArea = Tribes.getInstance().getClanFromTerritory(e.getToBlock().getChunk());
            final String originName = origin != null ? origin.getName() : "null";
            final String newName = newArea != null ? newArea.getName() : "null";

            if (!newName.equals(originName)) {
                e.setCancelled(true);
            }
        }
    }

    public Clan isInAClaim(final Block block) {
        return isInClaim(block.getChunk());
    }

}
