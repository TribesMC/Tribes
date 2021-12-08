package me.rey.clans.features;

import me.rey.Main;
import me.rey.clans.Tribes;
import me.rey.clans.clans.Clan;
import me.rey.clans.database.SQLManager;
import me.rey.clans.events.clans.ClanTerritoryUnclaimEvent;
import me.rey.clans.events.clans.ClanTerritoryUnclaimEvent.UnclaimReason;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class EnergyHandler extends BukkitRunnable {

    /*
     *  CURRENT MAX ENERGY: 10,000
     *  ENERGY DECREMENT PER MINUTE: -1
     *  ENERGY DECREMENT PER HOUR: -60
     *  ENERGY DECREMENT PER DAY: 1440
     *  ENERGY DECREMENT PER WEEK: ~10,000
     *
     */

    private final int intervalMinutes = 1;
    private final int decrement = 1;
    private final SQLManager sql;

    public EnergyHandler(final SQLManager sql) {
        this.sql = sql;
    }

    public void start() {
        this.runTaskTimer(JavaPlugin.getPlugin(Main.class), 0, (this.intervalMinutes * 60) * 20);
    }

    @Override
    public void run() {

        for (final Clan clan : Tribes.clans) {
            final Clan toDecrease = clan;
            if (toDecrease == null) {
                continue;
            }
            if (toDecrease.isServerClan()) {
                continue;
            }
            if (toDecrease.getEnergy() <= 0) {
                continue;
            }
            if (toDecrease.getTerritory().size() == 0) {
                continue;
            }

            toDecrease.setEnergy(toDecrease.getEnergy() - this.decrement);
            this.sql.saveClan(toDecrease);

            if (toDecrease.getEnergy() <= 0) {
                /*
                 * EVENT HANDLING
                 */
                final ClanTerritoryUnclaimEvent event = new ClanTerritoryUnclaimEvent(toDecrease, null, toDecrease.getTerritory(), UnclaimReason.ENERGY, true);
                Bukkit.getServer().getPluginManager().callEvent(event);

                toDecrease.unclaimAll();
                toDecrease.announceToClan("Your Clan Energy has &qDEPLETED&r!");

                this.sql.saveClan(toDecrease);
            }

        }
    }

}
