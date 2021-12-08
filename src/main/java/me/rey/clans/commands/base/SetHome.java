package me.rey.clans.commands.base;

import me.rey.clans.Tribes;
import me.rey.clans.clans.Clan;
import me.rey.clans.clans.ClansPlayer;
import me.rey.clans.clans.ClansRank;
import me.rey.clans.commands.ClansCommand;
import me.rey.clans.commands.SubCommand;
import me.rey.clans.enums.CommandType;
import me.rey.clans.utils.References;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class SetHome extends SubCommand {

    public SetHome() {
        super("sethome", "Set your Clan home", "/c sethome", ClansRank.ADMIN, CommandType.CLAN, true);
    }

    private static boolean isPlaceSuitableForHome(final Location location) {
        final int x = location.getBlockX();
		final int y = location.getBlockY();
		final int z = location.getBlockZ();
		final World w = Tribes.getInstance().getClansWorld();
        final List<Block> space = Arrays.asList(location.getBlock(), new Location(w, x, y + 1, z).getBlock(), new Location(w, x, y + 2, z).getBlock());

        for (final Block block : space) {
            if (block == null || (!block.getType().equals(Material.AIR) && !block.getType().isBlock())) {
                return false;
            }
        }

        return true;
    }

    public static boolean setHome(final Clan clan, final Location location) {
        if (!isPlaceSuitableForHome(location)) {
            return false;
        }

        final int x = location.getBlockX();
		final int y = location.getBlockY();
		final int z = location.getBlockZ();

		location.getBlock().setType(References.HOME_BLOCK);
        final Location home = new Location(Tribes.getInstance().getClansWorld(), x, y, z);

        if (clan.getHome() != null && clan.getHome().getBlock() != null && !clan.getHome().getBlock().getType().equals(Material.AIR)) {
            clan.getHome().getBlock().setType(Material.AIR);
        }

        clan.setHome(home);
        Tribes.getInstance().getSQLManager().saveClan(clan);
        return true;
    }

    @Override
    public void build(final ClansCommand source, final CommandSender sender, final String[] args) {
        final Player player = (Player) sender;
        final Clan clan = new ClansPlayer(player).getClan();

        if (Tribes.getInstance().getClanFromTerritory(player.getLocation().getChunk()) == null || !Tribes.getInstance().getClanFromTerritory(player.getLocation().getChunk()).equals(new ClansPlayer(player).getClan())) {
            new ClansPlayer(player).sendMessageWithPrefix("Error", "You are not in your claim!");
            return;
        }

        final boolean success = setHome(clan, player.getLocation());

        if (!success) {
            new ClansPlayer(player).sendMessageWithPrefix("Error", "This is not a suitable place for a home!");
        } else {
            final int x = player.getLocation().getBlockX();
			final int y = player.getLocation().getBlockY();
			final int z = player.getLocation().getBlockZ();
			clan.announceToClan("&s" + player.getName() + " &rset the Clan home to (&s" + x + "&r, &s" + y + "&r, &s" + z + "&r).");
        }
    }

    @Override
    public SubCommand[] getChilds() {
        return new SubCommand[]{};
    }

}
