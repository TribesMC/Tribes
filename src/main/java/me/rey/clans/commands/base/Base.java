package me.rey.clans.commands.base;

import me.rey.clans.Tribes;
import me.rey.clans.clans.Clan;
import me.rey.clans.clans.ClansPlayer;
import me.rey.clans.clans.ClansRank;
import me.rey.clans.commands.ClansCommand;
import me.rey.clans.commands.SubCommand;
import me.rey.clans.commands.staff.Admin;
import me.rey.clans.enums.CommandType;
import me.rey.clans.gui.GuiClan;
import me.rey.clans.gui.GuiClanInfo;
import me.rey.clans.utils.ErrorCheck;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Base extends ClansCommand {

    public Base() {
        super("c", "Main command for Clans", "/c help", ClansRank.NONE, CommandType.HELP, true);
    }

    @Override
    public void run(final CommandSender sender, final String[] args) {
        final ClansPlayer player = new ClansPlayer((Player) sender);

        if (args.length > 1) {
            new Help().run(this, sender, args);
            return;
        } else if (args.length == 1) {
            final String name = args[0];
            if (this.sql().clanExists(name)) {
                final Clan other = Tribes.getInstance().getClan(name);
                final GuiClanInfo info = new GuiClanInfo(other, player);
                info.setup();
                info.open(player.getPlayer());
            } else if (this.sql().playerExists(args[0])) {
                final ClansPlayer toGet = this.sql().getPlayerFromName(args[0]);
                if (toGet.getRealClan() == null) {
                    ErrorCheck.clanNotExist(sender);
                    return;
                }
                final GuiClanInfo info = new GuiClanInfo(toGet.getRealClan(), player);
                info.setup();
                info.open(player.getPlayer());
            } else {
                ErrorCheck.clanNotExist(sender);
            }
            return;
        }

        if (player.getRealClan() == null) {
            ErrorCheck.noClan(sender);
            return;
        }

        final Clan clan = player.getRealClan();
        final GuiClan gui = new GuiClan(player.getPlayer(), clan, clan.getPlayerRank(player.getUniqueId()));
        gui.setup();
        gui.open(player.getPlayer());
    }

    @Override
    public SubCommand[] getChilds() {
        return new SubCommand[]{
                new Admin(),
                new Disband(),
                new Unclaim(),
                new SetHome(),
                new Claim(),
                new Kick(),
                new Promote(),
                new Demote(),
                new Ally(),
//				new Truce(), // Removed
                new Neutral(),
                new Invite(),
                new Home(),
                new Leave(),
                new Join(),
                new Create(),
                new Help()
        };
    }

}
