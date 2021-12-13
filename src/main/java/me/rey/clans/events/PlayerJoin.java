package me.rey.clans.events;

import me.rey.clans.Tribes;
import me.rey.clans.playerdisplay.PlayerInfo;
import me.rey.parser.Text;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerJoin implements Listener {

    PlayerInfo info = new PlayerInfo();

    @EventHandler
    public void onJoin(final PlayerJoinEvent e) {
        if (e.getPlayer().getGameMode() == GameMode.SPECTATOR) {
            e.getPlayer().setGameMode(Bukkit.getServer().getDefaultGameMode());
        }
        e.setJoinMessage(Text.color("&8Join> &7" + e.getPlayer().getName()));
		this.info.setupSidebar(e.getPlayer());
		this.info.updateNameTagsForAll();
        Tribes.getInstance().getPvpTimer().loadPlayer(e.getPlayer());

        Tribes.getInstance().getSQLManager().setPlayerData(e.getPlayer().getUniqueId(), "name", e.getPlayer().getName());
        Tribes.getInstance().getLocalSQLiteManager().loadPlayer(e.getPlayer());
    }

    @EventHandler
    public void onQuit(final PlayerQuitEvent e) {
        Tribes.getInstance().getPvpTimer().unloadPlayer(e.getPlayer());
        e.setQuitMessage(Text.color("&8Quit> &7" + e.getPlayer().getName()));
    }

}
