package me.rey.clans.utils;

import me.rey.core.utils.Text;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.rey.clans.clans.Clan;
import me.rey.clans.clans.ClanRelations;
import me.rey.clans.clans.ClansPlayer;
import me.rey.clans.clans.ClansRank;

public class ErrorCheck {

	public static boolean playerOnly(CommandSender sender) {
		sender.sendMessage(Text.color("&cOnly players can use this command!"));
		return true;
	}

	public static boolean inexistentPlayer(CommandSender sender) {
		sender.sendMessage(Text.color("&cCould not find player by that name!"));
		return true;
	}
	
	public static boolean incorrectRank(CommandSender sender, ClansRank correctRank) {
		sender.sendMessage(Text.format("Tribe", String.format("Only %s%ss+&r can do this!", correctRank.getColor(), correctRank.getName())));
		return true;
	}
	
	public static boolean noClan(CommandSender sender) {
		sender.sendMessage(Text.format("Error", "You are not in a clan!"));
		return true;
	}
	
	public static boolean hasClan(CommandSender sender) {
		sender.sendMessage(Text.format("Error", "You are already in a clan!"));
		return true;
	}
	
	public static boolean clanNotExist(CommandSender sender) {
		sender.sendMessage(Text.format("Error", "That clan does not exist!"));
		return true;
	}
	
	public static boolean playerOffline(CommandSender sender) {
		sender.sendMessage(Text.format("Error", "That player is not online!"));
		return true;
	}
	
	public static boolean specifiedNotInClan(CommandSender sender) {
		sender.sendMessage(Text.format("Error", "That player is not in your clan!"));
		return true;
	}
	
	public static boolean playerNotOurank(CommandSender sender) {
		sender.sendMessage(Text.format("Error", "You do not outrank this player!"));
		return true;
	}
	
	public static boolean actionSelf(CommandSender sender, String action) {
		sender.sendMessage(Text.format("Error", "You cannot " + action + " yourself!"));
		return true;
	}

	public static boolean isLeader(CommandSender sender) {
		sender.sendMessage(Text.format("Error", "You are the leader!"));
		return true;
	}
	
	public static boolean invalidRank(CommandSender sender) {
		sender.sendMessage(Text.format("Error", "That rank is invalid!"));
		return true;
	}
	
	public static boolean noPermissions(CommandSender sender) {
		sender.sendMessage(Text.format("Permission", "You do not have permission to run this command."));
		return true;
	}

	public static boolean notInTestMode(CommandSender sender) {
		sender.sendMessage(Text.format("Error", "The server needs to be in test mode for you to be able to use that command!"));
		return true;
	}
	
	public static boolean invalidNumber(CommandSender sender) {
		sender.sendMessage(Text.format("Error", "That number is invalid!"));
		return true;
	}

	public static String getInClaimMessage(ClansPlayer self, Clan owner) {
		ChatColor color = self.hasClan() ? owner.getClanRelation(self.getClan().getUniqueId()).getPlayerColor() : ClanRelations.NEUTRAL.getPlayerColor();
		return "You cannot do that while in territory of " + color + owner.getName() + "&r.";
	}

	public static String getNearClaimMessage(ClansPlayer self, Clan owner) {
		ChatColor color = self.hasClan() ? owner.getClanRelation(self.getClan().getUniqueId()).getPlayerColor() : ClanRelations.NEUTRAL.getPlayerColor();
		return "You cannot build so close to " + color + owner.getName() + "&r.";
	}
	
}
