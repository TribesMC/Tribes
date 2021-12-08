package me.rey.clans.commands.base;

import me.rey.clans.Tribes;
import me.rey.clans.clans.Clan;
import me.rey.clans.clans.ClansPlayer;
import me.rey.clans.clans.ClansRank;
import me.rey.clans.commands.ClansCommand;
import me.rey.clans.commands.SubCommand;
import me.rey.clans.enums.CommandType;
import me.rey.clans.events.clans.ClanTerritoryUnclaimEvent;
import me.rey.clans.events.clans.ClanTerritoryUnclaimEvent.UnclaimReason;
import me.rey.clans.gui.ConfirmationGUI;
import me.rey.clans.utils.ErrorCheck;
import me.rey.clans.utils.References;
import me.rey.clans.utils.UtilText;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;

public class Unclaim extends SubCommand {

    public static final long chunkCooldown = 900000; // Chunk cooldown time in millis (900000 = 15 mins)

    public Unclaim() {
        super("unclaim", "Unclaim a piece of land", ClansRank.ADMIN.getColor() + "/c unclaim <All>", ClansRank.NONE, CommandType.CLAN, true);
    }

    @Override
    public void build(final ClansCommand source, final CommandSender sender, final String[] args) {
        if (args.length > 1) {
            this.sendUsage();
            return;
        }

        final Player player = (Player) sender;
        final Chunk standing = player.getLocation().getChunk();
        final ClansPlayer cp = new ClansPlayer(player);
        final Clan self = cp.getClan();

        // EVENT
        final ClanTerritoryUnclaimEvent event;

        if (args.length == 1 && args[0].equalsIgnoreCase("all")) {
            if (self == null) {
                ErrorCheck.noClan(sender);
                return;
            }

            if (!self.getPlayerRank(player.getUniqueId()).equals(ClansRank.LEADER)) {
                ErrorCheck.incorrectRank(sender, ClansRank.LEADER);
                return;
            }

            if (self.getTerritory().isEmpty()) {
                this.sendMessageWithPrefix("Error", "You do not have any land claimed!");
                return;
            }

            /*
             * EVENT HANDLING
             */
            event = new ClanTerritoryUnclaimEvent(self, player, self.getTerritory(), UnclaimReason.NORMAL, true);
            Bukkit.getServer().getPluginManager().callEvent(event);

            for (final Chunk chunk : self.getTerritory()) {
                self.removeTerritory(standing);
                Tribes.getInstance().territoryCooldowns.put(chunk, System.currentTimeMillis() + chunkCooldown);
            }
            self.unclaimAll();
            self.announceToClan("&s" + player.getName() + " &rhas &qUNCLAIMED &rall your land.");
            this.sql().saveClan(self);

            return;
        }

        if (Tribes.getInstance().getClanFromTerritory(standing) == null) {
            this.sendMessageWithPrefix("Error", "This territory is not owned by anybody!");
            return;
        }

        if (Tribes.getInstance().getClanFromTerritory(standing) != null && (self == null || !Tribes.getInstance().getClanFromTerritory(standing).compare(self))) {
            final Clan toUnclaim = Tribes.getInstance().getClanFromTerritory(standing);

            final int maxChunks = Math.min(toUnclaim.getPossibleTerritory(), References.MAX_TERRITORY);
            if (!(toUnclaim.getTerritory().size() > maxChunks) || toUnclaim.isServerClan()) {
                this.sendMessageWithPrefix("Error", "You cannot unclaim this land!");
                return;
            }

            Tribes.getInstance().territoryCooldowns.put(standing, System.currentTimeMillis() + chunkCooldown);
            toUnclaim.removeTerritory(standing);
            this.sql().saveClan(toUnclaim);
            final String unclaimer = self == null ? "Player &s" + player.getName() : "Clan &s" + self.getName();
            UtilText.announceToServer("Tribe", unclaimer + " &rhas &4&lUNCLAIMED &ra from &s" + toUnclaim.getName()
                    + "&r. (&s" + standing.getX() + "&r, &s" + standing.getZ() + "&r)");

            /*
             * EVENT HANDLING
             */
            event = new ClanTerritoryUnclaimEvent(self, player, new ArrayList<>(Collections.singletonList(standing)), UnclaimReason.FORCE, false);
            Bukkit.getServer().getPluginManager().callEvent(event);
            return;
        }

        if (self == null) {
            ErrorCheck.noClan(sender);
            return;
        }

        if (self.getPlayerRank(player.getUniqueId()).getPower() < ClansRank.ADMIN.getPower()) {
            ErrorCheck.incorrectRank(sender, ClansRank.ADMIN);
            return;
        }

        /* CONFIRMATION GUI */
        ((Player) sender).openInventory(new ConfirmationGUI("Unclaim this territory?").getInv());
    }

    @Override
    public SubCommand[] getChilds() {
        return new SubCommand[]{};
    }
}
