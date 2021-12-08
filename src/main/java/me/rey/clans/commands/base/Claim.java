package me.rey.clans.commands.base;

import me.rey.clans.Tribes;
import me.rey.clans.clans.Clan;
import me.rey.clans.clans.ClansPlayer;
import me.rey.clans.clans.ClansRank;
import me.rey.clans.commands.ClansCommand;
import me.rey.clans.commands.SubCommand;
import me.rey.clans.enums.CommandType;
import me.rey.clans.events.clans.ClanTerritoryClaimEvent;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Claim extends SubCommand {

    public static ArrayList<Set<Block>> fakeBlocks = new ArrayList<>();

    public Claim() {
        super("claim", "Claim a piece of land", "/c claim", ClansRank.ADMIN, CommandType.CLAN, true);
    }

    @Override
    public void build(final ClansCommand source, final CommandSender sender, final String[] args) {
        final Player player = (Player) sender;
        final Chunk standing = player.getLocation().getChunk();

        final Clan clan = new ClansPlayer(player).getClan();
        if (clan.hasMaxTerritory()) {
            this.sendMessageWithPrefix("Error", "You already claimed &s" + clan.getTerritory().size() + " &rChunks!");
            return;
        }

        if (Tribes.getInstance().getClanFromTerritory(standing) != null && Tribes.getInstance().getClanFromTerritory(standing).getUniqueId() != clan.getUniqueId()) {
            this.sendMessageWithPrefix("Error", "This territory is owned by &s" + Tribes.getInstance().getClanFromTerritory(standing).getName() + "&r.");
            return;
        }

        Tribes.getInstance().updateTerritoryCooldownCache();
        if (Tribes.getInstance().territoryCooldowns.containsKey(standing) && System.currentTimeMillis() < Tribes.getInstance().territoryCooldowns.get(standing)) {
            this.sendMessageWithPrefix("Error", "This territory is on cooldown!");
            return;
        }

        if (Tribes.getInstance().getClanFromTerritory(standing) != null && Tribes.getInstance().getClanFromTerritory(standing).getUniqueId() == clan.getUniqueId()) {
            this.sendMessageWithPrefix("Error", "You have already claimed this territory!");
            return;
        }

        for (final Player pp : Bukkit.getOnlinePlayers()) {
            if (pp.getLocation().getChunk().equals(standing)) {
                if (clan.getPlayer(pp.getName()) == null) {
                    this.sendMessageWithPrefix("Error", "You can not claim territory while enemies are in it!");
                    return;
                }
            }
        }

        final int x = standing.getX();
		final int z = standing.getZ();
		final World w = standing.getWorld();
        final Chunk[] sides = new Chunk[4];
		final Chunk[] corners = new Chunk[4];
		sides[0] = w.getChunkAt(x - 1, z);
        sides[1] = w.getChunkAt(x + 1, z);
        sides[2] = w.getChunkAt(x, z - 1);
        sides[3] = w.getChunkAt(x, z + 1);
        corners[0] = w.getChunkAt(x - 1, z - 1);
        corners[1] = w.getChunkAt(x + 1, z + 1);
        corners[2] = w.getChunkAt(x - 1, z + 1);
        corners[3] = w.getChunkAt(x + 1, z + 1);

        boolean isNextToSelf = clan.getTerritory().isEmpty();
        boolean isNextToCooldown = false;
        Clan isNextToOther = null;

        Tribes.getInstance().updateTerritoryCooldownCache();
        for (final Chunk near : sides) {
            if (Tribes.getInstance().territoryCooldowns.containsKey(near) && System.currentTimeMillis() < Tribes.getInstance().territoryCooldowns.get(near)) {
                isNextToCooldown = true;
            }
            if (Tribes.getInstance().getClanFromTerritory(near) != null) {
                final Clan claimed = Tribes.getInstance().getClanFromTerritory(near);
                if (claimed.compare(clan)) {
					isNextToSelf = true;
				} else {
					isNextToOther = claimed;
				}
            }
        }

        Tribes.getInstance().updateTerritoryCooldownCache();
        for (final Chunk near : corners) {
            if (Tribes.getInstance().territoryCooldowns.containsKey(near) && System.currentTimeMillis() < Tribes.getInstance().territoryCooldowns.get(near)) {
                isNextToCooldown = true;
            }
            if (Tribes.getInstance().getClanFromTerritory(near) != null) {
                final Clan claimed = Tribes.getInstance().getClanFromTerritory(near);
                if (!claimed.compare(clan)) {
					isNextToOther = claimed;
				}
            }
        }

        if (isNextToCooldown) {
            this.sendMessageWithPrefix("Error", "You cannot claim here because a nearby chunk is on cooldown!");
            return;
        }

        if (isNextToSelf) {

            if (isNextToOther != null) {
                this.sendMessageWithPrefix("Error", "You cannot claim next to &s" + isNextToOther.getName() + "&r!");
                return;
            }
			this.drawBorders(standing, player);
            clan.addTerritory(standing);
            this.sendMessageWithPrefix("Tribe", "Successfully claimed chunk (&s" + standing.getX() + "&r, &e" + standing.getZ() + "&r).");

            // Setting home if it's first chunk
            if (clan.getTerritory().size() == 1) {
                SetHome.setHome(clan, player.getLocation());
            }

            this.sql().saveClan(clan);

            /*
             * EVENT HANDLING
             */
            final ClanTerritoryClaimEvent event = new ClanTerritoryClaimEvent(clan, player, new ArrayList<Chunk>(Arrays.asList(standing)));
            Bukkit.getServer().getPluginManager().callEvent(event);
        } else {
            this.sendMessageWithPrefix("Error", "You must claim next to your owned territory!");
        }
    }

    public void drawBorders(final Chunk standing, final Player p) {
        final Set<Block> toReplace = new HashSet<>();

        for (int a = 0; a <= 15; a++) {

            final Block blockC = standing.getBlock(a, 0, 0);
            final Block highestblock = p.getWorld().getHighestBlockAt(blockC.getX(), blockC.getZ());
            final Block block = p.getWorld().getBlockAt(highestblock.getX(), highestblock.getY() - 1, highestblock.getZ());

            toReplace.add(block);
        }

        for (int b = 0; b <= 14; b++) {

            final Block blockC = standing.getBlock(0, 0, b + 1);
            final Block highestblock = p.getWorld().getHighestBlockAt(blockC.getX(), blockC.getZ());
            final Block block = p.getWorld().getBlockAt(highestblock.getX(), highestblock.getY() - 1, highestblock.getZ());

            toReplace.add(block);
        }

        for (int c = 0; c <= 14; c++) {

            final Block blockC = standing.getBlock(15, 0, c + 1);
            final Block highestblock = p.getWorld().getHighestBlockAt(blockC.getX(), blockC.getZ());
            final Block block = p.getWorld().getBlockAt(highestblock.getX(), highestblock.getY() - 1, highestblock.getZ());

            toReplace.add(block);
        }

        for (int d = 0; d <= 13; d++) {

            final Block blockC = standing.getBlock(d + 1, 0, 15);
            final Block highestblock = p.getWorld().getHighestBlockAt(blockC.getX(), blockC.getZ());
            final Block block = p.getWorld().getBlockAt(highestblock.getX(), highestblock.getY() - 1, highestblock.getZ());

            toReplace.add(block);
        }

        for (final Player online : Bukkit.getOnlinePlayers()) {
			for (final Block b : toReplace) {
				online.sendBlockChange(b.getLocation(), Material.SEA_LANTERN, (byte) 0);
			}
		}

        fakeBlocks.add(toReplace);
    }

    @Override
    public SubCommand[] getChilds() {
        return new SubCommand[]{};
    }

}
