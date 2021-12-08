package me.rey.clans.commands.test;

import me.rey.clans.clans.ClansRank;
import me.rey.clans.commands.ClansCommand;
import me.rey.clans.commands.SubCommand;
import me.rey.clans.enums.CommandType;
import me.rey.clans.utils.ErrorCheck;
import me.rey.core.Warriors;
import org.bukkit.command.CommandSender;

public class TestMode extends ClansCommand {

    public TestMode() {
        super("toggletestmode", "Toggles server test mode", "/toggletestmode", ClansRank.NONE, CommandType.STAFF, false);
        super.setStaff(true);
    }

    @Override
    public void run(CommandSender sender, String[] args) {
        if (!sender.isOp()) {
            ErrorCheck.noPermissions(sender);
        }

        Warriors.getInstance().setTestMode(!Warriors.getInstance().isTestMode());
    }

    @Override
    public SubCommand[] getChilds() {
        return new SubCommand[0];
    }
}
