package me.rey.clans.commands.staff.loggers;

import me.rey.clans.clans.ClansRank;
import me.rey.clans.commands.ClansCommand;
import me.rey.clans.commands.SubCommand;
import me.rey.clans.enums.CommandType;
import org.bukkit.command.CommandSender;

public class Loggers extends ClansCommand {
    public Loggers() {
        super("loggers", "Base combat logger command", "/loggers <command>", ClansRank.NONE, CommandType.STAFF, true);
        this.setStaff(true);
    }

    @Override
    public void run(CommandSender sender, String[] args) {
        if (args == null || args.length <= 0) {
            new LoggerHelp().run(this, sender, args);
        }
    }

    @Override
    public SubCommand[] getChilds() {
        return new SubCommand[]{
                new LoggerHelp(),
                new Stop(),
                new Start(),
                new Clear()
        };
    }
}
