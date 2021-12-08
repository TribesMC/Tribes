package me.rey.clans.commands.base;

import me.rey.clans.gui.ConfirmationGUI;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.rey.clans.clans.Clan;
import me.rey.clans.clans.ClansPlayer;
import me.rey.clans.clans.ClansRank;
import me.rey.clans.commands.ClansCommand;
import me.rey.clans.commands.SubCommand;
import me.rey.clans.enums.CommandType;
import me.rey.clans.events.clans.ClanDisbandEvent;
import me.rey.clans.events.clans.ClanDisbandEvent.DisbandReason;

public class Disband extends SubCommand {

	public Disband() {
		super("disband", "Disband your Clan", "/c disband", ClansRank.LEADER, CommandType.CLAN, true);
	}

	@Override
	public void build(ClansCommand source, CommandSender sender, String[] args) {

		/* CONFIRMATION GUI */
		((Player) sender).openInventory(new ConfirmationGUI("Disband your clan?").getInv());

	}

	@Override
	public SubCommand[] getChilds() {
		return new SubCommand[] {};
	}

}
