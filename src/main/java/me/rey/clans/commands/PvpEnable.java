package me.rey.clans.commands;

import me.rey.clans.Tribes;
import me.rey.clans.clans.ClansRank;
import me.rey.clans.enums.CommandType;
import me.rey.clans.features.PvpTimer;
import me.rey.clans.utils.UtilTime;
import me.rey.core.utils.UtilText;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PvpEnable extends ClansCommand {

    private final static Map<UUID, String> confirming;

    static {
        confirming = new HashMap<>();
    }

    public PvpEnable() {
        super("pvpenable", "Allows a player to turn their PVP timer off", "/pvpenable", ClansRank.NONE, CommandType.FEATURE, true);
    }

    @Override
    public void run(final CommandSender sender, final String[] args) {
        final Player player = (Player) sender;

        if (Tribes.getInstance().getPvpTimer().getPvpTimer(player) <= 0) {
            this.sendMessageWithPrefix("PVP Timer", "You do not have an active PVP timer!");
            return;
        }

        if (!PvpTimer.getRemovables().getOrDefault(player.getUniqueId(), true)) {
            this.sendMessageWithPrefix("PVP Timer", "You cannot remove this PVP timer! You need to wait for it to end or ask a staff member to remove it for you.  (There is currently " + ChatColor.YELLOW + UtilTime.convert(Tribes.getInstance().getPvpTimer().getLivePvpTimer(player), 0, UtilTime.getBestUnit(Tribes.getInstance().getPvpTimer().getLivePvpTimer(player))) + " " + UtilTime.getBestUnit(Tribes.getInstance().getPvpTimer().getLivePvpTimer(player)).name().toLowerCase() + ChatColor.GRAY + " of your current PVP timer left)");
            return;
        }

        if (confirming.containsKey(player.getUniqueId())) {
            Tribes.getInstance().getPvpTimer().removePvpTimer(player, true);
            this.sendMessageWithPrefix("PVP Timer", "You have removed your PVP timer! You can now fight with other players!");
        } else {
            final String session = UtilText.randomString(16);
            confirming.put(player.getUniqueId(), session);
            this.sendMessageWithPrefix("PVP Timer", "Are you sure you'd like to remove your PVP timer? This will allow other players to attack you! (There is currently " + ChatColor.YELLOW + UtilTime.convert(Tribes.getInstance().getPvpTimer().getLivePvpTimer(player), 0, UtilTime.getBestUnit(Tribes.getInstance().getPvpTimer().getLivePvpTimer(player))) + " " + UtilTime.getBestUnit(Tribes.getInstance().getPvpTimer().getLivePvpTimer(player)).name().toLowerCase() + ChatColor.GRAY + " of your current PVP timer left)");
            this.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "Use the command again to confirm!");
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!player.isOnline()) {
                        this.cancel();
                    }

                    if (!confirming.getOrDefault(player.getUniqueId(), "").equals(session)) {
                        this.cancel();
                    }

                    PvpEnable.this.sendMessageWithPrefix("PVP Timer", "PVP timer removal confirmation expired, you will be re-prompted to confirm once you try to remove your PVP timer again.");
                    confirming.remove(player.getUniqueId());
                }
            }.runTaskLater(Tribes.getInstance().getPlugin(), 2400);
        }
    }

    @Override
    public SubCommand[] getChilds() {
        return new SubCommand[]{};
    }
}
