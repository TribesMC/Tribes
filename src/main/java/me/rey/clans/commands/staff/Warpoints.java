package me.rey.clans.commands.staff;

import me.rey.clans.Tribes;
import me.rey.clans.clans.Clan;
import me.rey.clans.clans.ClansPlayer;
import me.rey.clans.clans.ClansRank;
import me.rey.clans.commands.ClansCommand;
import me.rey.clans.commands.SubCommand;
import me.rey.clans.enums.CommandType;
import me.rey.clans.enums.MathAction;
import me.rey.clans.events.clans.ClanWarpointEvent;
import me.rey.clans.utils.ErrorCheck;
import me.rey.core.utils.Text;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Warpoints extends SubCommand {

    public Warpoints() {
        super("warpoints", "Edit your Clan's warpoints on another", "/c x warpoints <add|remove> <Clan> <Value>", ClansRank.NONE, CommandType.STAFF, true);
    }

    @Override
    public void build(final ClansCommand source, final CommandSender sender, final String[] args) {
        if (args.length != 3) {
            this.sendUsage();
            return;
        }

        final ClansPlayer cp = new ClansPlayer((Player) sender);
        if (!cp.hasClan()) {
            ErrorCheck.noClan(sender);
            return;
        }

        MathAction action = null;
        int toAdd = -1;
        for (final MathAction tAction : MathAction.values()) {
            if (tAction == MathAction.SET) {
				continue;
			}
            if (tAction.name().equalsIgnoreCase(args[0])) {
				action = tAction;
			}
        }

        if (Text.isInteger(args[2])) {
			toAdd = Integer.parseInt(args[2]);
		}

        if (action == null || toAdd == -1) {
            this.sendUsage();
            return;
        }

        if (toAdd < 0) {
            ErrorCheck.invalidNumber(sender);
            return;
        }

        if (!this.sql().clanExists(args[1])) {
            ErrorCheck.clanNotExist(sender);
            return;
        }

        final Clan clan = Tribes.getInstance().getClan(args[1]);
		final Clan self = new ClansPlayer((Player) sender).getClan();
		final long currentWarpoints = self.getWarpointsOnClan(clan.getUniqueId());
        final long toSet = action.calc(currentWarpoints, toAdd);

        final ClanWarpointEvent event = new ClanWarpointEvent(self, clan, action == MathAction.REMOVE ? toSet : -toSet);
        Bukkit.getServer().getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            self.setWarpoint(clan.getUniqueId(), toSet);
            this.sql().saveClan(self);
            clan.setWarpoint(self.getUniqueId(), -toSet);
            this.sql().saveClan(clan);

            clan.announceToClan(String.format("Your War Points on &s%s &rhave been set to: &s%s&r.", self.getName(), -toSet));
            self.announceToClan(String.format("Your War Points on &s%s &rhave been set to: &s%s&r.", clan.getName(), toSet));
        } else {
            cp.sendMessageWithPrefix("Error", "Warpoint command was cancelled! (Disallowed action)");
        }

    }

    @Override
    public SubCommand[] getChilds() {
        return new SubCommand[]{};
    }

}
