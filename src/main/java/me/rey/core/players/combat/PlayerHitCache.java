package me.rey.core.players.combat;

import me.rey.core.Warriors;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;

public class PlayerHitCache implements Listener {

    public static final int COMBAT_TIMER = 15;
    private final Warriors plugin;
    private final HashMap<Player, ArrayList<PlayerHit>> playerHitCache;
    private final HashMap<Player, CombatTimer> combatTimers;

    public PlayerHitCache(final Warriors plugin) {
        this.plugin = plugin;
        this.playerHitCache = new HashMap<>();
        this.combatTimers = new HashMap<>();
    }

    public ArrayList<PlayerHit> getPlayerCache(final Player player) {
        if (!this.playerHitCache.containsKey(player)) {
            return new ArrayList<>();
        }
        return this.playerHitCache.get(player);
    }

    public void addToPlayerCache(final Player target, final PlayerHit hit) {
        final ArrayList<PlayerHit> currentHits = this.getPlayerCache(target);

        // Adding current damage if the damager is the same in other hits
        boolean isAlreadyIn = false;
        for (int i = 0; i < currentHits.size(); i++) {
            final PlayerHit query = currentHits.get(i);

            if (query.getDamager().equals(hit.getDamager())) {
                query.addDamage(hit.getDamage());
                query.setCause(hit.getCause());
                query.setTimeIssued(hit.getTimeIssued());
                isAlreadyIn = true;
            }
        }

        // saving
        if (!isAlreadyIn) {
            currentHits.add(hit);
            this.updateCache(target, currentHits);
        }

        new BukkitRunnable() {

            @Override
            public void run() {

                for (int i = 0; i < PlayerHitCache.this.getPlayerCache(target).size(); i++) {
                    final PlayerHit query = PlayerHitCache.this.getPlayerCache(target).get(i);

                    if (query.getDamager().equals(hit.getDamager()) && query.getDamage() == hit.getDamage()) {
                        final ArrayList<PlayerHit> clone = PlayerHitCache.this.getPlayerCache(target);
                        clone.remove(i);
                        PlayerHitCache.this.updateCache(target, clone);
                        this.cancel();
                        return;
                    }
                }

                this.cancel();

            }

        }.runTaskLater(this.plugin.getPlugin(), COMBAT_TIMER * 20);


    }

    public void startCombatTimer(final Player target) {
        if (this.hasCombatTimer(target) && this.getCombatTimer(target).getRemaining(System.currentTimeMillis()) > 0) {
            this.getCombatTimer(target).cancel();
        }
        this.combatTimers.put(target, new CombatTimer(target));
        final CombatTimer start = this.getCombatTimer(target).init();

        new BukkitRunnable() {

            @Override
            public void run() {
                if (PlayerHitCache.this.getCombatTimer(target) != null && PlayerHitCache.this.getCombatTimer(target).getTimeIssued() == start.getTimeIssued()) {
                    PlayerHitCache.this.combatTimers.remove(target);
                }
                this.cancel();

            }

        }.runTaskLater(this.plugin.getPlugin(), COMBAT_TIMER * 20);

    }

    public CombatTimer getCombatTimer(final Player target) {
        return this.hasCombatTimer(target) ? this.combatTimers.get(target) : null;
    }

    public boolean hasCombatTimer(final Player target) {
        return this.combatTimers.containsKey(target);
    }

    private void updateCache(final Player target, final ArrayList<PlayerHit> cache) {
        this.playerHitCache.put(target, cache);
    }

    public void clearPlayerCache(final Player target) {
        this.playerHitCache.remove(target);
    }

    public void stopCombatTimer(final Player target) {
        final CombatTimer timer = this.combatTimers.remove(target);
        if (timer != null) {
            timer.cancel();
        }
    }

    public PlayerHit getLastBlow(final Player target) {
        final ArrayList<PlayerHit> hits = this.getPlayerCache(target);
        for (int i = hits.size() - 1; i >= 0; i--) {
            final PlayerHit query = hits.get(i);
            if (!query.isCausedByPlayer()) {
                continue;
            }
            return query;
        }
        return null;
    }

    public int getAssists(final Player player) {
        int assists = 0;
        for (final PlayerHit hit : this.getPlayerCache(player)) {
            if (!hit.isCausedByPlayer()) {
                continue;
            }

            assists++;
        }

        return assists > 0 ? assists - 1 : 0;
    }

}
