package me.rey.clans.commands.staff;

import me.rey.clans.Tribes;
import me.rey.clans.clans.ClansPlayer;
import me.rey.clans.clans.ClansRank;
import me.rey.clans.commands.ClansCommand;
import me.rey.clans.commands.SubCommand;
import me.rey.clans.enums.CommandType;
import me.rey.clans.features.punishments.Punishment;
import me.rey.clans.features.punishments.gui.PunishWipeMenu;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PunishWipe extends ClansCommand {

    public PunishWipe() {
        super("punishwipe", "Wipes a punishment from the database removing its existence", "/punishwipe <id>", ClansRank.NONE, CommandType.STAFF, true);
        this.addAlias("pwipe");
        this.setStaff(true);
    }

    @Override
    public void run(final CommandSender sender, final String[] args) {
        ClansPlayer player = new ClansPlayer((Player) sender);

        if (args == null || args.length <= 0) {
            player.sendMessageWithPrefix("Punish", "No punishment ID provided! Usage: &s/punishwipe <id>");
        } else {
            int id;
            try {
                id = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                player.sendMessageWithPrefix("Punish", "&s" + args[0] + "&r is not a valid number!");
                return;
            }
            Punishment punishment = Tribes.getInstance().getSQLManager().getPunishmentById(id);
            if (punishment == null) {
                player.sendMessageWithPrefix("Punish", "Could not find a punishment with the ID: " + args[0] + "&r!");
                return;
            }
            PunishWipeMenu gui = new PunishWipeMenu(player.getPlayer(), punishment);
            gui.setup();
            gui.open(player.getPlayer());
        }
    }

    @Override
    public SubCommand[] getChilds() {
        return new SubCommand[0];
    }

}