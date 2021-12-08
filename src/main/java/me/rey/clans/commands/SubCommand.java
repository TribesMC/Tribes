package me.rey.clans.commands;

import me.rey.clans.Tribes;
import me.rey.clans.clans.ClansPlayer;
import me.rey.clans.clans.ClansRank;
import me.rey.clans.database.SQLManager;
import me.rey.clans.enums.CommandType;
import me.rey.clans.utils.ErrorCheck;
import me.rey.core.utils.Text;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;

public abstract class SubCommand {

    private final String command, description;
    private final ClansRank requiredRank;
    private final CommandType commandType;
    private final boolean isPlayerExclusive;
    private final String usage;
    CommandSender sender;
    private boolean displayHelp, isForStaff;
    private final ArrayList<String> aliases = new ArrayList<String>();
    private final SQLManager sql = Tribes.getInstance().getSQLManager();

    public SubCommand(final String command, final String description, final String usage, final ClansRank requiredRank, final CommandType commandType, final boolean isPlayerExclusive) {
        this.command = command;
        this.description = description;
        this.requiredRank = requiredRank;
        this.commandType = commandType;
        this.isPlayerExclusive = isPlayerExclusive;
        this.usage = usage;
        this.displayHelp = true;
        this.isForStaff = false;
    }

    public void run(final ClansCommand source, final CommandSender sender, final String[] args) {
        if (this.isPlayerExclusive && !(sender instanceof Player)) {
			return;
		}

        if (this.isStaff() && sender instanceof Player) {

            if (!sender.isOp()) {
                ErrorCheck.noPermissions(sender);
                return;
            }
        }

        if ((sender instanceof Player) && this.requiredRank != ClansRank.NONE) {
            final ClansPlayer cp = new ClansPlayer((Player) sender);
            if (cp.getClan() == null) {
                ErrorCheck.noClan(sender);
                return;
            }

            final ClansRank rank = cp.getClan().getPlayerRank(cp.getUniqueId());
            if (cp.getClan() == null || rank.getPower() < this.requiredRank().getPower()) {
                ErrorCheck.incorrectRank(sender, this.requiredRank);
                return;
            }
        }

        this.setSender(sender);

        if ((this.getChilds() != null && this.getChilds().length != 0) && args.length > 0) {

            for (final SubCommand argument : this.getChilds()) {
                if (argument.command().equalsIgnoreCase(args[0]) || argument.hasAlias(args[0])) {
                    argument.run(source, sender, Arrays.copyOfRange(args, 1, args.length));
                    return;
                }
            }

        }

        this.build(source, sender, args);
    }

    public abstract void build(ClansCommand source, CommandSender sender, String[] args);

    public abstract SubCommand[] getChilds();

    public String command() {
        return this.command;
    }

    public String description() {
        return this.description;
    }

    public ClansRank requiredRank() {
        return this.requiredRank;
    }

    public CommandType commandType() {
        return this.commandType;
    }

    public boolean isPlayerExclusive() {
        return this.isPlayerExclusive;
    }

    public void sendUsage() {
        this.sendUsageError(this.usage());
    }

    public String usage() {
        return this.usage;
    }

    public SQLManager sql() {
        return this.sql;
    }

    public void setSender(final CommandSender sender) {
        this.sender = sender;
    }

    public void sendUsageError(final String usage) {
		this.sendMessageWithPrefix("Error", "Incorrect Usage! Usage: &e" + usage);
    }

    public void sendMessageWithPrefix(final CommandType type, final String message) {
		this.sendMessageWithPrefix(type.getName(), message);
    }

    public void sendMessageWithPrefix(final String prefix, final String message) {
		this.sendMessage(Text.format(prefix, message));
    }

    public void sendMessage(final String message) {
        if (this.sender == null) {
			return;
		}
		this.sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

    public void setDisplayOnHelp(final boolean display) {
        this.displayHelp = display;
    }

    public boolean displayHelp() {
        return this.displayHelp;
    }

    public boolean isStaff() {
        return this.isForStaff;
    }

    public void setStaff(final boolean staff) {
        this.isForStaff = staff;
    }


    public void addAlias(final String alias) {
        if (this.aliases.contains(alias) == false) {
			this.aliases.add(alias);
        }
    }

    public boolean hasAlias(final String alias) {
        if (this.aliases.contains(alias)) {
            return true;
        }
        return false;
    }


}