package me.rey.clans.commands.base;

import me.rey.clans.Tribes;
import me.rey.clans.clans.Clan;
import me.rey.clans.clans.ClansPlayer;
import me.rey.clans.clans.ClansRank;
import me.rey.clans.commands.ClansCommand;
import me.rey.clans.commands.SubCommand;
import me.rey.clans.enums.CommandType;
import me.rey.clans.events.clans.ClanKickEvent;
import me.rey.clans.utils.ErrorCheck;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Kick extends SubCommand {

    public Kick() {
        super("kick", "Kick a player", "/c kick <Player>", ClansRank.ADMIN, CommandType.CLAN, true);
    }

    public static void handleKick(final Player sender, final String playerName) {

        final ClansPlayer cp = new ClansPlayer(sender);
        if (!cp.hasClan()) {
            ErrorCheck.noClan(sender);
            return;
        }

        final Clan toKick = cp.getClan();
        if (!toKick.isInClan(playerName)) {
            ErrorCheck.specifiedNotInClan(sender);
            return;
        }

        final ClansPlayer toK = toKick.getPlayer(playerName);
        final String name = toK.isOnline() ? toK.getPlayer().getName() : toK.getOfflinePlayer().getName();

        if (toKick.getPlayerRank(toK.getUniqueId()).getPower() >= toKick.getPlayerRank(cp.getUniqueId()).getPower()) {
            ErrorCheck.playerNotOurank(sender);
            return;
        }

        final boolean success = toKick.kickPlayer(toK.getUniqueId());
        if (!success) {
            cp.sendMessageWithPrefix("Error", "You cannot kick this player!");
            return;
        }

        toK.save();
        Tribes.getInstance().getSQLManager().saveClan(toKick);
        toK.kick();

        final String format = String.format("&s%s&r has &qKICKED&r &s%s &rfrom the Clan!", cp.getPlayer().getName(), name);
        toKick.announceToClan(format);
        toK.sendMessageWithPrefix("Tribe", "You were kicked from your clan by &s" + cp.getPlayer().getName() + "&r.");
        Join.cooldowns.put(toK.getUniqueId(), System.currentTimeMillis());

        /*
         * EVENT HANDLING
         */
        final ClanKickEvent event = new ClanKickEvent(toKick, cp.getPlayer(), toK);
        Bukkit.getServer().getPluginManager().callEvent(event);
    }

    @Override
    public void build(final ClansCommand source, final CommandSender sender, final String[] args) {
        if (args.length != 1) {
            this.sendUsage();
            return;
        }

        handleKick((Player) sender, args[0]);
    }

    @Override
    public SubCommand[] getChilds() {
        return new SubCommand[]{};
    }

}
