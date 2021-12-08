package me.rey.clans.features;

import me.rey.clans.Tribes;
import me.rey.clans.clans.Clan;
import me.rey.clans.clans.ServerClan;
import me.rey.clans.shops.ShopNPC;
import me.rey.clans.shops.guis.PvP;
import me.rey.parser.ParseEvent;
import me.rey.parser.ParseType;
import me.rey.parser.Parser;
import me.rey.parser.ParserPlugin;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ServerParser implements Listener {

    Parser safeZone = new Parser("Safe", Material.BEDROCK, ParseType.CUBOID, true);
    Set<ShopNPC> shops = new HashSet<>(Arrays.asList(
            new ShopNPC(1, "&a&lPvP Shop", new PvP(), new Parser("ShopPvP", Material.CLAY, ParseType.SINGLE, true))
//				new ShopNPC(1, "&a&lTest", new Test(), new Parser("ShopTest", Material.WOOL, ParseType.SINGLE, true))
    ));

    public void init() {
        // Shop NPCs
        for (final ShopNPC shop : this.shops) {
            ParserPlugin.getInstance().registerParser(shop.getParser());
        }

        // safe zone
        ParserPlugin.getInstance().registerParser(this.safeZone);

        // server territories
        for (final ServerClan type : ServerClan.values()) {
            if (type.getParser() == null) {
				continue;
			}
            ParserPlugin.getInstance().registerParser(type.getParser());
        }
    }

    @EventHandler
    public void onShopNPC(final ParseEvent e) {
        if (e.getParser() == null) {
			return;
		}

        for (final ShopNPC type : this.shops) {
            if (type.getParser() == null || !type.getParser().getName().equals(e.getParser().getName())) {
				continue;
			}
            // matches

            final Iterator<Block> found = e.getParsedPoints().iterator();
            while (found.hasNext()) {
                final Block b = found.next();
                b.setType(Material.AIR);
                if (e.getParser().useSponge()) {
					b.getRelative(BlockFace.DOWN).setType(Material.AIR);
				}

                final Location toSpawn = b.getLocation().clone();
                toSpawn.setX(toSpawn.getBlockX() + 0.5);
                toSpawn.setY(toSpawn.getBlockY() - 1);
                toSpawn.setZ(toSpawn.getBlockZ() + 0.5);

                type.spawn(toSpawn);
            }
        }
    }


    /*
     * SERVER SAFE ZONES
     */
    @EventHandler
    public void onSafeZone(final ParseEvent e) {
        if (e.getParser() == null || !e.getParser().getName().equals(this.safeZone.getName())) {
			return;
		}
        if (e.getParsedPoints().isEmpty()) {
			return;
		}
        final Iterator<Block> blocks = e.getParsedPoints().iterator();
        final Set<String> coords = new HashSet<String>();

        final Set<String> clone = Tribes.getInstance().getSQLManager().getSafeZones();
        while (blocks.hasNext()) {
            final Block found = blocks.next();
            final String text = String.format("%s;%s;%s", found.getX(), found.getY(), found.getZ());
            if (clone.contains(text)) {
				continue;
			}
            coords.add(text);
        }

        Tribes.getInstance().getSQLManager().saveSafeZones(coords);
        Tribes.safeZoneCoords = Tribes.getInstance().getSQLManager().getSafeZones();
    }

    /*
     * SERVER TERRITORIES
     */
    @EventHandler
    public void onParse(final ParseEvent e) {
        if (e.getParser() == null) {
			return;
		}

        for (final ServerClan type : ServerClan.values()) {
            if (type.getParser() == null) {
				continue;
			}

            // matches parser
            if (e.getParser().getName().equals(type.getParser().getName())) {
                if (e.getParsedPoints() == null) {
					continue;
				}
                final Iterator<Block> blocks = e.getParsedPoints().iterator();

                final Set<Chunk> inClaim = new HashSet<Chunk>();
                while (blocks.hasNext()) {
                    final Block found = blocks.next();

                    inClaim.add(found.getChunk());
                }

                final Clan clan = Tribes.getInstance().getSQLManager().getServerClan(type);
                for (final Chunk claimed : inClaim) {
                    clan.addTerritory(claimed);
                }

                Tribes.getInstance().getSQLManager().saveClan(clan);

                //deleting thsoe blocks
                final Iterator<Block> toRemove = e.getParsedPoints().iterator();
                while (toRemove.hasNext()) {
                    final Block next = toRemove.next();
                    if (next.getType().equals(e.getParser().getDataBlock())) {
                        next.setType(Material.AIR);

                        if (e.getParser().useSponge()) {
							next.getRelative(BlockFace.DOWN).setType(Material.AIR);
						}
                    }
                }
            }
        }
    }
}
