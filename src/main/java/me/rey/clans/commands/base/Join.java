package me.rey.clans.commands.base;

import me.rey.clans.Tribes;
import me.rey.clans.clans.Clan;
import me.rey.clans.clans.ClansPlayer;
import me.rey.clans.clans.ClansRank;
import me.rey.clans.commands.ClansCommand;
import me.rey.clans.commands.SubCommand;
import me.rey.clans.enums.CommandType;
import me.rey.clans.events.clans.ClanJoinEvent;
import me.rey.clans.events.clans.ClanJoinEvent.JoinReason;
import me.rey.clans.utils.ErrorCheck;
import me.rey.clans.utils.References;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Join extends SubCommand {

    public static final Map<UUID, Long> cooldowns = new HashMap<>();

    public Join() {
        super("join", "Join a Clan", "/c join <Clan>", ClansRank.NONE, CommandType.CLAN, true);
    }

    @Override
    public void build(final ClansCommand source, final CommandSender sender, final String[] args) {
        if (args.length != 1) {
            this.sendUsage();
            return;
        }

        final ClansPlayer cp = new ClansPlayer((Player) sender);
        if (cp.hasClan()) {
            ErrorCheck.hasClan(sender);
            return;
        }

        final HashMap<UUID, ArrayList<UUID>> players = Invite.players;
        final Clan toJoin = Tribes.getInstance().getClan(args[0]);
        if (toJoin == null) {
            ErrorCheck.clanNotExist(sender);
            return;
        }

        final ArrayList<UUID> invites = players.get(toJoin.getUniqueId());
        if (invites == null || invites.isEmpty() || !invites.contains(cp.getUniqueId())) {
            this.sendMessageWithPrefix("Error", "You have not been invited to join Clan &s" + toJoin.getName() + "&r.");
            return;
        }

        if (cooldowns.containsKey(cp.getUniqueId())) {
            final double cooldownTimeLeft = References.CLAN_JOIN_COOLDOWN_SECONDS - (System.currentTimeMillis() - cooldowns.get(cp.getUniqueId())) / 1000D;

            if (cooldownTimeLeft <= 0) {
                cooldowns.remove(cp.getUniqueId());
            } else {
                final String timeLeft;

                if (cooldownTimeLeft > (60 * 60)) {
                    timeLeft = String.format("%.1f hours", cooldownTimeLeft / 60 / 60D);
                } else if (cooldownTimeLeft > 60) {
                    timeLeft = String.format("%.1f minutes", cooldownTimeLeft / 60);
                } else {
                    timeLeft = String.format("%.1f seconds", cooldownTimeLeft);
                }

                cp.sendMessageWithPrefix("Error", String.format("You cannot join a clan for another &g%s&7.", timeLeft));
                return;
            }

        }

        Invite.players.get(toJoin.getUniqueId()).remove(cp.getUniqueId());

        if (toJoin.hasMaxMembers()) {
            this.sendMessageWithPrefix("Error", "The clan you tried to join is already full!");
            return;
        }

        this.sendMessageWithPrefix("Tribe", "You have joined Clan &s" + toJoin.getName() + "&r.");
        toJoin.addPlayer(cp.getUniqueId(), ClansRank.RECRUIT);
        this.sql().saveClan(toJoin);
        toJoin.announceToClan("&s" + cp.getPlayer().getName() + " &rjoined your Clan!", cp);

        /*
         * EVENT HANDLING
         */
        final ClanJoinEvent event = new ClanJoinEvent(toJoin, cp.getPlayer(), JoinReason.INVITE);
        Bukkit.getServer().getPluginManager().callEvent(event);
    }

    @Override
    public SubCommand[] getChilds() {
        return new SubCommand[]{};
    }

}
