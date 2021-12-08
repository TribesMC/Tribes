package me.rey.clans.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.rey.clans.clans.ClansPlayer;
import me.rey.clans.clans.ClansRank;
import me.rey.clans.enums.CommandType;

public class Focus extends ClansCommand {
	
	public Focus() {
		super("focus", "Focus a player", "/focus <Player>", ClansRank.NONE, CommandType.FEATURE, true);
	}

	@Override
	public void run(CommandSender sender, String[] args) {
		if(args.length != 1) {
			this.sendUsageError(this.usage());
			return;
		}
		
		ClansPlayer cp = new ClansPlayer((Player) sender);
		
		Player toFocus = Bukkit.getServer().getPlayer(args[0]);
		if(toFocus == null || !toFocus.isOnline() || toFocus == (Player) sender) {
			this.sendMessageWithPrefix("Error", "Invalid player!");
			return;
		}
		
		
		cp.unfocus();
		cp.focus(toFocus);
		this.sendMessageWithPrefix("Focus", "You are now focusing &s" + toFocus.getName() + "&r.");
	}

	@Override
	public SubCommand[] getChilds() {
		return new SubCommand[] {};
	}
	
}
