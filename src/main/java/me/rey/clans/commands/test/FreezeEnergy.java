package me.rey.clans.commands.test;

import me.rey.clans.clans.ClansRank;
import me.rey.clans.commands.ClansCommand;
import me.rey.clans.commands.SubCommand;
import me.rey.clans.enums.CommandType;
import me.rey.core.Warriors;
import me.rey.core.events.customevents.update.EnergyConsumeEvent;
import me.rey.core.events.customevents.update.EnergyReplenishEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class FreezeEnergy extends ClansCommand implements Listener {

    Set<UUID> frozens;

    public FreezeEnergy() {
        super("fenergy", "Stops energy degeneration", "/fenergy [player]", ClansRank.NONE, CommandType.TEST, true);
        super.setTest(true);
        this.frozens = new HashSet<>();
        Bukkit.getPluginManager().registerEvents(this, Warriors.getInstance().getPlugin());
    }

    @Override
    public void run(final CommandSender sender, final String[] args) {
        final Player player;
        if (args != null && args.length > 0) {
            player = Bukkit.getPlayer(args[0]);
        } else {
            player = (Player) sender;
        }

        if (this.frozens.contains(player.getUniqueId())) {
            this.frozens.remove(player.getUniqueId());
            if (player.equals(sender)) {
                Bukkit.broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + ChatColor.MAGIC + "00" + ChatColor.RED + " " + ChatColor.BOLD + player.getName() + " has unfrozen their energy! " + ChatColor.MAGIC + "00");
            } else {
                Bukkit.broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + ChatColor.MAGIC + "00" + ChatColor.RED + " " + ChatColor.BOLD + sender.getName() + " has unfrozen " + player.getName() + "'s energy! " + ChatColor.MAGIC + "00");
            }
        } else {
            this.frozens.add(player.getUniqueId());
            if (player.equals(sender)) {
                Bukkit.broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + ChatColor.MAGIC + "00" + ChatColor.RED + " " + ChatColor.BOLD + player.getName() + " has frozen their energy! " + ChatColor.MAGIC + "00");
            } else {
                Bukkit.broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + ChatColor.MAGIC + "00" + ChatColor.RED + " " + ChatColor.BOLD + sender.getName() + " has frozen " + player.getName() + "'s energy! " + ChatColor.MAGIC + "00");
            }
        }
    }

    @Override
    public SubCommand[] getChilds() {
        return new SubCommand[0];
    }

    @EventHandler
    private void onManaReplenish(final EnergyReplenishEvent event) {
        if (this.frozens.contains(event.getPlayer().getUniqueId()) && Warriors.isTestMode) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    private void onManaConsume(final EnergyConsumeEvent event) {
        if (this.frozens.contains(event.getPlayer().getUniqueId()) && Warriors.isTestMode) {
            event.setCancelled(true);
        }
    }
}
