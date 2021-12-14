package me.rey.clans.commands.staff;

import me.rey.clans.clans.ClansPlayer;
import me.rey.clans.clans.ClansRank;
import me.rey.clans.commands.ClansCommand;
import me.rey.clans.commands.SubCommand;
import me.rey.clans.enums.CommandType;
import me.rey.clans.features.punishments.gui.PunishMenu;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Punish extends ClansCommand {

    public Punish() {
        super("punish", "Main command for the punishment system", "/punish <player> <reason>", ClansRank.NONE, CommandType.STAFF, true);
        this.addAlias("p");
        this.setStaff(true);
    }

    @Override
    public void run(final CommandSender sender, final String[] args) {
        ClansPlayer player = new ClansPlayer((Player) sender);

        if (args == null || args.length <= 0) {
            player.sendMessageWithPrefix("Punish", "No player or reason provided! Usage: &s/punish <player> <reason>");
            return;
        } else if (args.length == 1) {
            player.sendMessageWithPrefix("Punish", "No reason provided! Usage: &s/punish <player> <reason>");
            return;
        } else {
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);

            List<String> reasonArgs = new ArrayList<>(Arrays.asList(args));
            reasonArgs.remove(0);
            StringBuilder builder = new StringBuilder();
            for (String reasonArg : reasonArgs) {
                builder.append(reasonArg).append(" ");
            }
            builder.delete(builder.length() - 1, builder.length());
            PunishMenu gui = new PunishMenu(player.getPlayer(), target, builder.toString());
            gui.setup();
            gui.open(player.getPlayer());
        }
    }

    @Override
    public SubCommand[] getChilds() {
        return new SubCommand[0];
    }

}