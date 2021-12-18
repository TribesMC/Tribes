package me.rey.clans.commands.staff;

import me.rey.clans.Tribes;
import me.rey.clans.clans.ClansPlayer;
import me.rey.clans.clans.ClansRank;
import me.rey.clans.commands.ClansCommand;
import me.rey.clans.commands.SubCommand;
import me.rey.clans.enums.CommandType;
import me.rey.clans.features.punishments.Punishment;
import me.rey.clans.features.punishments.gui.PunishHistoryMenu;
import me.rey.clans.features.punishments.gui.PunishMenu;
import me.rey.clans.features.punishments.gui.PunishWipeMenu;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PunishHistory extends ClansCommand {

    public PunishHistory() {
        super("punishhistory", "Shows an unchangeable punishment history of a player", "/punishhistory <player>", ClansRank.NONE, CommandType.STAFF, true);
        this.addAlias("ph");
        this.setStaff(true);
    }

    @Override
    public void run(final CommandSender sender, final String[] args) {
        ClansPlayer player = new ClansPlayer((Player) sender);
        OfflinePlayer target;

        if (args == null || args.length <= 0) {
            target = player.getPlayer();
        } else {
            target = Bukkit.getOfflinePlayer(args[0]);
        }
        PunishHistoryMenu gui = new PunishHistoryMenu(player.getPlayer(), target, Tribes.getInstance().getPunishmentManager().getPunishments(target.getUniqueId()));
        gui.setup();
        gui.open(player.getPlayer());
    }

    @Override
    public SubCommand[] getChilds() {
        return new SubCommand[0];
    }

}