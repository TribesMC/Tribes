package me.rey.clans.commands.base;

import me.rey.clans.Tribes;
import me.rey.clans.clans.Clan;
import me.rey.clans.clans.ClansPlayer;
import me.rey.clans.clans.ClansRank;
import me.rey.clans.clans.ServerClan;
import me.rey.clans.commands.ClansCommand;
import me.rey.clans.commands.SubCommand;
import me.rey.clans.enums.CommandType;
import me.rey.clans.events.clans.ClanCreateEvent;
import me.rey.clans.utils.ErrorCheck;
import me.rey.clans.utils.UtilText;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class Create extends SubCommand {

    private final List<String> badNames = Arrays.asList(
            "unclaim",
            "delete",
            "leave",
            "disband"
    ); // There's probably a better place for this to be

    public Create() {
        super("create", "Create a Clan", "/c create <Clan>", ClansRank.NONE, CommandType.CLAN, true);
    }

    @Override
    public void build(final ClansCommand source, final CommandSender sender, final String[] args) {

        if (args.length != 1) {
            this.sendUsage();
            return;
        }

        if (new ClansPlayer((Player) sender).hasClan()) {
            ErrorCheck.hasClan(sender);
            return;
        }

        final String name = args[0];
        if (name.length() < 3) {
            this.sendMessageWithPrefix("Error", "Your Clan name must be at least 3 characters long!");
            return;
        } else if (name.length() > 10) {
            this.sendMessageWithPrefix("Error", "Your Clan name is too long!");
            return;
        }

        if (this.sql().clanExists(name)) {
            this.sendMessageWithPrefix("Error", "A clan with that name already exists!");
            return;
        }

        for (final ServerClan type : ServerClan.values()) {
            if (type.getName().equalsIgnoreCase(args[0])) {
                this.sendMessageWithPrefix("Error", "Invalid clan name!");
                return;
            }
        }

        if (!UtilText.isAlphanumeric(args[0])) {
            this.sendMessageWithPrefix("Error", "Invalid clan name!");
            return;
        }

        for (final SubCommand sc : source.getChilds()) {
            if (sc.command().equalsIgnoreCase(args[0])) {
                this.sendMessageWithPrefix("Error", "Invalid clan name!");
                return;
            }
        }

        for (final String badName : this.badNames) {
            if (badName.equalsIgnoreCase(args[0])) {
                this.sendMessageWithPrefix("Error", "You cannot create a clan with that name!");
            }
        }

        final UUID uuid = UUID.randomUUID();
        this.sql().createClan(uuid, name, ((Player) sender));
        this.sendMessageWithPrefix("Tribe", String.format("You have created Clan &s%s&r.", name));

        /*
         * EVENT HANDLING
         */
        final Clan self = Tribes.getInstance().getClan(uuid);
        final ClanCreateEvent event = new ClanCreateEvent(self, (Player) sender);
        Bukkit.getServer().getPluginManager().callEvent(event);
    }

    @Override
    public SubCommand[] getChilds() {
        return new SubCommand[]{};
    }

}
