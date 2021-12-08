package me.rey.core.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.rey.core.players.User;

public class Skill implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		if((sender instanceof Player) && command.getName().equalsIgnoreCase("skill")) {
			Player p;
			User u;
			if (args != null && args.length > 0) {
				p = Bukkit.getPlayer(args[0]);
				u = new User(p);
				if (p == null || !p.isValid() || !p.isOnline()) {
					new User((Player) sender).sendMessageWithPrefix("Skill", "Could not find player by the name &s" + args[0] + "&r!");
					return true;
				}
			} else {
				p = (Player) sender;
				u = new User(p);
			}
			u.sendListingClassSkills(u.getWearingClass());
		}
		
		
		return true;
	}

}
