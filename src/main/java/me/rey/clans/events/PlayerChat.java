package me.rey.clans.events;

import me.rey.clans.utils.UtilText;
import me.rey.core.utils.Text;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import me.rey.clans.clans.Clan;
import me.rey.clans.clans.ClanRelations;
import me.rey.clans.clans.ClansPlayer;
import me.rey.clans.commands.AllyChat;
import me.rey.clans.commands.ClanChat;

public class PlayerChat implements Listener {
	
	@EventHandler
	public void onChat(AsyncPlayerChatEvent e) {
		if(e.isCancelled()) return;
		e.setCancelled(true);
		
		String prefix = "Chat";
		final String defPrefix = prefix;
		if(ClanChat.inChat.contains(e.getPlayer().getUniqueId()) || AllyChat.inChat.contains(e.getPlayer().getUniqueId())) {
			Clan self = new ClansPlayer(e.getPlayer()).getClan();
			if(self == null) {
				ClanChat.inChat.remove(e.getPlayer().getUniqueId());
				AllyChat.inChat.remove(e.getPlayer().getUniqueId());	
			}
			
			else if(AllyChat.inChat.contains(e.getPlayer().getUniqueId())) {
				self.shoutToRelation(ClanRelations.ALLY, e.getPlayer(), e.getMessage());
				prefix = "Ally Chat";
			}
			
			else if(ClanChat.inChat.contains(e.getPlayer().getUniqueId())) {
				self.shoutToRelation(ClanRelations.SELF, e.getPlayer(), e.getMessage());
				prefix = "Clan Chat";
			}
			
		}
		
		Player p = e.getPlayer();

		if(defPrefix.equals(prefix)) {
			for(Player online : Bukkit.getOnlinePlayers()) {
				ClansPlayer toSend = new ClansPlayer(online);
				toSend.getPlayer().sendMessage(Text.color(UtilText.getPrefix(e.getPlayer()) + UtilText.formatClanColors(p, online) + " &f") + e.getMessage());
			}
		}
		
		UtilText.echo(prefix, p.getName() + ": " + e.getMessage());
	}

}
