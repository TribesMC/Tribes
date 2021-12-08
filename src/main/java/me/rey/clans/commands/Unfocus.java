package me.rey.clans.commands;

import me.rey.clans.clans.ClansPlayer;
import me.rey.clans.clans.ClansRank;
import me.rey.clans.enums.CommandType;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Unfocus extends ClansCommand {

	public Unfocus() {
		super("unfocus", "Unfocus a player", "/unfocus", ClansRank.NONE, CommandType.FEATURE, true);
	}

	@Override
	public void run(CommandSender sender, String[] args) {
		
		ClansPlayer cp = new ClansPlayer((Player) sender);
		if(cp.hasFocus())
			this.sendMessageWithPrefix("Focus", "You are no longer focusing &s" + cp.getFocus().getName() + "&r.");
		else
			this.sendMessageWithPrefix("Focus", "You are not focusing anybody.");
		cp.unfocus();

	}

	@Override
	public SubCommand[] getChilds() {
		return new SubCommand[] {};
	}
	
}
