package me.rey.clans.commands.staff.loggers;

import me.rey.clans.Tribes;
import me.rey.clans.clans.ClansPlayer;
import me.rey.clans.clans.ClansRank;
import me.rey.clans.commands.ClansCommand;
import me.rey.clans.commands.SubCommand;
import me.rey.clans.enums.CommandType;
import me.rey.core.players.User;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Stop extends SubCommand {
    public Stop() {
        super("stop", "Stops combat loggers from spawning", "/loggers stop", ClansRank.NONE, CommandType.STAFF, true);
        this.setStaff(true);
    }

    @Override
    public void build(ClansCommand source, CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) return;
        Player player = (Player) sender;
        User user = new User(player);
        if (!Tribes.getInstance().getCombatLogger().isDoCombatLoggers()) {
            user.sendMessageWithPrefix("Loggers", "Combat loggers are already turned off!");
            return;
        }

        Tribes.getInstance().getCombatLogger().toggleCombatLoggers(false);
        for (Player player1 : Bukkit.getOnlinePlayers()) {
            new User(player1).sendMessageWithPrefix("Tribes", "Combat loggers have been turned off by a staff member, you will no longer drop a combat logger if you log out. All current combat loggers have been removed!");
        }
        user.sendMessageWithPrefix("Loggers", "Successfully turned combat loggers off!");
    }

    @Override
    public SubCommand[] getChilds() {
        return new SubCommand[0];
    }
}
