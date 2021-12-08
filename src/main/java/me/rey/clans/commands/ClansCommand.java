package me.rey.clans.commands;

import me.rey.Main;
import me.rey.clans.Tribes;
import me.rey.clans.clans.ClansPlayer;
import me.rey.clans.clans.ClansRank;
import me.rey.clans.database.SQLManager;
import me.rey.clans.enums.CommandType;
import me.rey.clans.utils.ErrorCheck;
import me.rey.core.Warriors;
import me.rey.core.utils.Text;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class ClansCommand implements TabCompleter, CommandExecutor {

    private final String command, description;
    private final ClansRank requiredRank;
    private final CommandType commandType;
    private final boolean isPlayerExclusive;
    private final String usage;
    private final SQLManager sql = Tribes.getInstance().getSQLManager();
    private final List<String> aliases = new ArrayList<>();
    private CommandSender sender;
    private boolean isForStaff, isTestOnly;

    public ClansCommand(final String command, final String description, final String usage, final ClansRank requiredRank, final CommandType commandType, final boolean isPlayerExclusive) {
        this.command = command;
        this.description = description;
        this.requiredRank = requiredRank;
        this.commandType = commandType;
        this.isPlayerExclusive = isPlayerExclusive;
        this.usage = usage;
        this.isForStaff = false;
        this.isTestOnly = false;

        JavaPlugin.getPlugin(Main.class).getCommand(command).setExecutor(this);
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        if (!cmd.getName().equalsIgnoreCase(this.command())) {
            return true;
        }
        if (this.isPlayerExclusive() && !(sender instanceof Player)) {
            return ErrorCheck.playerOnly(sender);
        }

        if (this.isStaff() && sender instanceof Player) {

            if (!sender.isOp()) {
                return ErrorCheck.noPermissions(sender);
            }
        }

        if ((sender instanceof Player) && this.requiredRank != ClansRank.NONE) {
            final ClansPlayer cp = new ClansPlayer((Player) sender);
            if (cp.getClan() == null) {
                return ErrorCheck.noClan(sender);
            }

            if (cp.getClan() == null || cp.getClan().getPlayerRank(cp.getUniqueId()).getPower() < this.requiredRank().getPower()) {
                return ErrorCheck.incorrectRank(sender, this.requiredRank);
            }
        }

        if (this.isTestOnly && !Warriors.getInstance().isTestMode()) {
            if (!sender.isOp()) {
                return ErrorCheck.noPermissions(sender);
            }
            return ErrorCheck.notInTestMode(sender);
        }

        this.setSender(sender);

        if ((this.getChilds() != null && this.getChilds().length != 0) && args.length > 0) {

            for (final SubCommand argument : this.getChilds()) {
                if (argument.command().equalsIgnoreCase(args[0]) || argument.hasAlias(args[0])) {
                    argument.run(this, sender, Arrays.copyOfRange(args, 1, args.length));
                    return true;
                }
            }

        }

        this.run(sender, args);

        return true;
    }

    @Override
    public List<String> onTabComplete(final CommandSender commandSender, final Command command, final String s, final String[] strings) {
        return null;
    }

    public abstract void run(CommandSender sender, String[] args);

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
        if (this.sender == null) {
            return;
        }
        this.sender.sendMessage(Text.format(prefix, message));
    }

    public void sendMessage(final String message) {
        if (this.sender == null) {
            return;
        }
        this.sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }


    public boolean isStaff() {
        return this.isForStaff;
    }

    public void setStaff(final boolean staff) {
        this.isForStaff = staff;
    }

    public boolean isTest() {
        return this.isTestOnly;
    }

    public void setTest(final boolean test) {
        this.isTestOnly = test;
    }

    public void addAlias(final String alias) {
        if (!this.aliases.contains(alias)) {
            this.aliases.add(alias);
        }
    }

    public boolean hasAlias(final String alias) {
        return this.aliases.contains(alias);
    }

}
