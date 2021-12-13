package me.rey.clans.commands.staff;

import me.rey.clans.Tribes;
import me.rey.clans.clans.ClansRank;
import me.rey.clans.commands.ClansCommand;
import me.rey.clans.commands.SubCommand;
import me.rey.clans.enums.CommandType;
import me.rey.clans.features.incognito.IncognitoManager;
import me.rey.core.players.User;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SpectatorObserve extends ClansCommand {

    Map<UUID, GameMode> oldGamemodes;

    public SpectatorObserve() {
        super("cmo", "Sends you into spectator mode", "/cmo [Player]", ClansRank.NONE, CommandType.STAFF, true);
        super.setStaff(true);
        oldGamemodes = new HashMap<>();
    }

    //todo this doesnt vanish people yet

    @Override
    public void run(CommandSender sender, String[] args) {
        Player player;
        if (args != null && args.length > 0) {
            player = Bukkit.getPlayer(args[0]);
        } else {
            player = (Player) sender;
        }

        if (player.getGameMode() != GameMode.SPECTATOR) {
            player.setGameMode(GameMode.SPECTATOR);
            new User(player).sendMessageWithPrefix("Observe", "You have entered spectator observe mode!");
            Tribes.getInstance().getIncognito().setIncognitoType(player, IncognitoManager.IncognitoType.CMO);
        } else {
            player.setGameMode(oldGamemodes.getOrDefault(player.getUniqueId(), Bukkit.getServer().getDefaultGameMode()));
            new User(player).sendMessageWithPrefix("Observe", "You have left spectator observe mode!");
            Tribes.getInstance().getIncognito().removeIncognitoType(player);
        }
    }

    @Override
    public SubCommand[] getChilds() {
        return new SubCommand[0];
    }
}
