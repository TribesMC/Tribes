package me.rey.core.events;

import me.rey.core.Warriors;
import me.rey.core.effects.Effect;
import me.rey.core.events.customevents.combat.DeathEvent;
import me.rey.core.packets.ActionBar;
import me.rey.core.players.User;
import me.rey.core.players.combat.PlayerHit;
import me.rey.core.players.combat.PlayerHitCache;
import me.rey.core.utils.Text;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

import java.util.ArrayList;

public class PlayerDeathHandler implements Listener {

    PlayerHitCache cache = Warriors.getInstance().getHitCache();

    @EventHandler
    public void onPlayerDeath(final org.bukkit.event.entity.PlayerDeathEvent e) {

        final Player player = e.getEntity();

        /* AUTO-RESPAWN */
        Bukkit.getScheduler().scheduleSyncDelayedTask(Warriors.getInstance().getPlugin(), () -> {
            Effect.clearAllEffects(player, null);

            if (player.getVehicle() != null) {
                player.getVehicle().eject();
            }
            player.spigot().respawn();
        }, 1L);

        if (Warriors.deathMessagesEnabled) {

            final int assists = this.cache.getAssists(player);
            final ArrayList<PlayerHit> playerCache = this.cache.getPlayerCache(player);
            final PlayerHit lastBlow = this.cache.getLastBlow(player);

            e.setDeathMessage(null);
            final DeathEvent deathEvent = new DeathEvent(player, lastBlow, assists, player.getLastDamageCause());
            Bukkit.getServer().getPluginManager().callEvent(deathEvent);

            if (!deathEvent.isDeathMessageCanceled()) {
                Bukkit.broadcastMessage(Text.color(deathEvent.getDeathMessage().get()));
            }

            // SENIDNG THEM THEIR DEATH SUMMARY
            int index = 1;
            for (final PlayerHit hit : playerCache) {
                final String cause = hit.hasCause() ? hit.getCause() : null;

                new User(player).sendMessage(String.format("&2#%s: &e%s &7[&e%s&7] &7[&a%s&7] &7[&a%s Seconds Prior&7]&r", index, hit.getDamager(),
                        String.format("%.1f", Math.min(999, hit.getDamage())), cause == null ? "None" : cause, hit.getLongAgo(System.currentTimeMillis())));

                index++;
            }

        }

        this.cache.clearPlayerCache(player);
        new User(player).stopCombatTimer();
        player.setVelocity(new Vector(0, 0, 0));
        new ActionBar(Text.color("&r")).send(player);

    }


}
