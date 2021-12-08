package me.rey.clans.commands.staff;

import me.rey.clans.Tribes;
import me.rey.clans.clans.Clan;
import me.rey.clans.clans.ClansPlayer;
import me.rey.clans.clans.ClansRank;
import me.rey.clans.commands.ClansCommand;
import me.rey.clans.commands.SubCommand;
import me.rey.clans.enums.CommandType;
import me.rey.clans.events.clans.ClanJoinEvent;
import me.rey.clans.events.clans.ClanJoinEvent.JoinReason;
import me.rey.clans.utils.ErrorCheck;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ForceJoin extends SubCommand {

    public ForceJoin() {
        super("forcejoin", "Join a clan without an invite", "/c x forcejoin <Clan>", ClansRank.NONE, CommandType.STAFF, true);
    }

    @Override
    public void build(final ClansCommand source, final CommandSender sender, final String[] args) {
        if (args.length != 1) {
            this.sendUsage();
            return;
        }

        final ClansPlayer cp = new ClansPlayer((Player) sender);
        if (cp.hasClan()) {
            ErrorCheck.hasClan(sender);
            return;
        }

        final Clan toJoin = Tribes.getInstance().getClan(args[0]);
        if (toJoin == null) {
            ErrorCheck.clanNotExist(sender);
            return;
        }

        this.sendMessageWithPrefix("Tribe", "You have joined Clan &s" + toJoin.getName() + "&r.");
        toJoin.addPlayer(cp.getUniqueId(), ClansRank.RECRUIT);
        this.sql().saveClan(toJoin);

        toJoin.announceToClan("&s" + cp.getPlayer().getName() + " &rjoined your Clan!", cp);

        /*
         * EVENT HANDLING
         */
        final ClanJoinEvent event = new ClanJoinEvent(toJoin, cp.getPlayer(), JoinReason.FORCE);
        Bukkit.getServer().getPluginManager().callEvent(event);
    }

    @Override
    public SubCommand[] getChilds() {
        return new SubCommand[]{};
    }

}
