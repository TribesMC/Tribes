package me.rey.clans.commands.base;

import me.rey.Main;
import me.rey.clans.clans.Clan;
import me.rey.clans.clans.ClansPlayer;
import me.rey.clans.clans.ClansRank;
import me.rey.clans.commands.ClansCommand;
import me.rey.clans.commands.SubCommand;
import me.rey.clans.enums.CommandType;
import me.rey.clans.utils.ErrorCheck;
import me.rey.core.utils.Text;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class Invite extends SubCommand {

    public static HashMap<UUID, ArrayList<UUID>> players = new HashMap<>();
    private final int inviteExpiresSeconds = 60;

    public Invite() {
        super("invite", "Invite a Player to your Tribe", "/c invite <Player>", ClansRank.ADMIN, CommandType.CLAN, true);
    }

    @Override
    public void build(final ClansCommand source, final CommandSender sender, final String[] args) {
        if (args.length != 1) {
            this.sendUsage();
            return;
        }

        final ClansPlayer cp = new ClansPlayer((Player) sender);
        final Player playerToInvite = Bukkit.getServer().getPlayer(args[0]);
        if (playerToInvite == null || !playerToInvite.isOnline()) {
            ErrorCheck.playerOffline(sender);
            return;
        }

        if (playerToInvite.getUniqueId() == ((Player) sender).getUniqueId()) {
            ErrorCheck.actionSelf(sender, "invite");
            return;
        }

        final Clan toInvite = new ClansPlayer((Player) sender).getClan();
        if (toInvite.hasMaxMembers()) {
            this.sendMessageWithPrefix("Error", "Your tribe is already full!");
            return;
        }

        final ArrayList<UUID> currentInvites = players.get(toInvite.getUniqueId());
        if (currentInvites != null && currentInvites.contains(playerToInvite.getUniqueId())) {
            this.sendMessageWithPrefix("Error", "That player has already been invited!");
            return;
        }

        new ClansPlayer(playerToInvite).sendMessageWithPrefix(
                "Tribe", "&s" + cp.getPlayer().getName() + " &rhas invited you to join &s" + toInvite.getName() + "&r.");

        /* Click message */
        final ComponentBuilder msg = new ComponentBuilder(Text.format("Tribe", "&rClick &e&lHERE &rto join &rtheir tribe."));
        final BaseComponent[] clickmsg = msg.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/c join " + toInvite.getName())).create();
        playerToInvite.spigot().sendMessage(clickmsg);

        final ArrayList<UUID> cache = players.get(toInvite.getUniqueId()) == null ? new ArrayList<UUID>() : players.get(toInvite.getUniqueId());
        cache.add(playerToInvite.getUniqueId());
        players.put(toInvite.getUniqueId(), cache);

        new BukkitRunnable() {

            @Override
            public void run() {
                final ArrayList<UUID> newCurrent = players.get(toInvite.getUniqueId());
                if (newCurrent != null && !newCurrent.isEmpty() && newCurrent.contains(playerToInvite.getUniqueId())) {
                    players.get(toInvite.getUniqueId()).remove(playerToInvite.getUniqueId());
                    this.cancel();
                    return;
                }
            }

        }.runTaskLater(JavaPlugin.getPlugin(Main.class), this.inviteExpiresSeconds * 20);

        toInvite.announceToClan("&s" + cp.getPlayer().getName() + " &rhas invited &s" + playerToInvite.getName() + " &rto your tribe!");
    }

    @Override
    public SubCommand[] getChilds() {
        return new SubCommand[]{};
    }
}
