package me.rey.clans.commands.staff.loggers;

import me.rey.clans.clans.ClansRank;
import me.rey.clans.commands.*;
import me.rey.clans.enums.CommandType;
import me.rey.core.utils.Text;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;

public class LoggerHelp extends SubCommand {

	public LoggerHelp() {
		super("help", "Displays a list of available commands", "/loggers help", ClansRank.NONE, CommandType.HELP, false);
		this.setStaff(true);
	}

	@Override
	public void build(ClansCommand source, CommandSender sender, String[] args) {
		
		sender.sendMessage(Text.format(this.commandType().getName(), "Commands List:"));
		
		for (SubCommand subCommand : source.getChilds()) {
			if (!subCommand.displayHelp()) continue;
			ChatColor c = subCommand.requiredRank().getColor();
			sender.sendMessage(c + Text.color(String.format("%s &7%s",
					subCommand.usage(),
					subCommand.description()
					)));
		}
	}

	@Override
	public SubCommand[] getChilds() {
		return new SubCommand[] {};
	}
	

}
