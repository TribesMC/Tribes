package me.rey.core.packets;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class Nametag extends Packets {

	private Player tag;
	private String text, team;
	
	public Nametag(Player tag, String team, String text) {
		this.tag = tag;
		this.text = text;
		this.team = team;
	}

	@Override
	public void send(LivingEntity entity) {
		Player p = (Player) entity;
		Scoreboard sb = getPlayerScoreboard(p);
		
		Team team = sb.getTeam(this.team) == null ? sb.registerNewTeam(this.team) : sb.getTeam(this.team);
		if(!team.getEntries().contains(tag.getName())) {
			team.addEntry(tag.getName());
		}
		
		team.setPrefix(text);
		p.setScoreboard(sb);
	}
	
	private Scoreboard getPlayerScoreboard(Player p) {
		if(p.getScoreboard() == null)
			p.setScoreboard(Bukkit.getServer().getScoreboardManager().getNewScoreboard());
		return p.getScoreboard();
	}

}
