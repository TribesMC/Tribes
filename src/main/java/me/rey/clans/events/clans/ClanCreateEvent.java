package me.rey.clans.events.clans;

import org.bukkit.entity.Player;

import me.rey.clans.clans.Clan;
import me.rey.clans.events.clans.ClanEvent.ClanPlayerEvent;

public class ClanCreateEvent extends ClanPlayerEvent {

	public ClanCreateEvent(Clan clan, Player issuer) {
		super(clan, issuer);
	}

}
