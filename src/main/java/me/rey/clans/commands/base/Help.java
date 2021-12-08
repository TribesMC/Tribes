package me.rey.clans.commands.base;

import java.util.Arrays;
import java.util.List;

import me.rey.clans.commands.*;
import me.rey.core.utils.Text;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import me.rey.clans.clans.ClansRank;
import me.rey.clans.enums.CommandType;

public class Help extends SubCommand {

	public Help() {
		super("help", "Displays a list of available commands", "/c help", ClansRank.NONE, CommandType.HELP, false);
	}

	@Override
	public void build(ClansCommand source, CommandSender sender, String[] args) {
		
		sender.sendMessage(Text.format(this.commandType().getName(), "Commands List:"));
		
		for(SubCommand subCommand : source.getChilds()) {
			if(!subCommand.displayHelp()) continue;
			ChatColor c = subCommand.requiredRank().getColor();
			sender.sendMessage(c + Text.color(String.format("%s &7%s",
					subCommand.usage(),
					subCommand.description()
					)));
		}
		
		
		List<ClansCommand> extraCommands = Arrays.asList(new AllyChat(), new ClanChat(), new Focus(), new Unfocus());
		extraCommands.forEach((cmd) -> {
			ChatColor c = cmd.requiredRank().getColor();
			sender.sendMessage(c + Text.color(String.format("%s &7%s",
					cmd.usage(),
					cmd.description()
					)));
		});
	}

	@Override
	public SubCommand[] getChilds() {
		return new SubCommand[] {};
	}
	

}
