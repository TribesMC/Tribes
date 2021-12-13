package me.rey.clans.commands.staff.loggers;

import me.rey.clans.Tribes;
import me.rey.clans.clans.ClansRank;
import me.rey.clans.commands.ClansCommand;
import me.rey.clans.commands.SubCommand;
import me.rey.clans.enums.CommandType;
import me.rey.core.players.User;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Clear extends SubCommand {
    public Clear() {
        super("clear", "Clears all spawned combat loggers", "/loggers clear", ClansRank.NONE, CommandType.STAFF, true);
        this.setStaff(true);
    }

    @Override
    public void build(ClansCommand source, CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) return;
        Player player = (Player) sender;
        User user = new User(player);
        Tribes.getInstance().getCombatLogger().cleanup();
        user.sendMessageWithPrefix("Loggers", "Removed all spawned combat loggers!");
    }

    @Override
    public SubCommand[] getChilds() {
        return new SubCommand[0];
    }
}
