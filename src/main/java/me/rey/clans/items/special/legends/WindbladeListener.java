package me.rey.clans.items.special.legends;

import me.rey.clans.Tribes;
import me.rey.clans.items.special.ClansTool;
import me.rey.core.events.customevents.combat.DamagedByEntityEvent;
import me.rey.core.players.User;
import me.rey.core.utils.UtilEnt;
import me.rey.core.utils.UtilPacket;
import me.rey.core.utils.UtilParticle;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WindbladeListener extends LegendaryItem {
    private final Map<UUID, BukkitTask> chargeBlocker;
    private final Map<UUID, Float> charges;
    private final Map<UUID, Float> smoother;
    private final Map<UUID, Vector> vectors;

    public WindbladeListener(final Tribes plugin) {
        super(plugin, "WINDBLADE", LegendType.WINDBLADE, ClansTool.WINDBLADE, ClansTool.WINDBLADE_DISABLED);
        this.chargeBlocker = new HashMap<>();
        this.charges = new HashMap<>();
        this.smoother = new HashMap<>();
        this.vectors = new HashMap<>();
    }

    @Override
    public void update() {
        if (this.updater.isDisabled(this)) {
            return;
        }
        for (final Player player : Bukkit.getOnlinePlayers()) {
            if (this.isOfArchetype(player.getItemInHand(), this.legend)) {
                this.onUpdate(player);
            } else {
                this.remove(player, false);
            }
        }
    }

    @Override
    public void serverShutdown() {
        this.chargeBlocker.clear();
        this.charges.clear();
        this.smoother.clear();
        this.vectors.clear();
    }

    @EventHandler
    private void onDamage(final DamagedByEntityEvent event) {
        if (this.updater.isDisabled(this)) {
            return;
        }
        if (event.getDamagee() instanceof Player) {
            if (this.plugin.getPvpTimer().getPvpTimer((Player) event.getDamagee()) > 0) {
                return;
            }
        }
        if (event.getDamager() instanceof Player) {
            final Player player = (Player) event.getDamager();
            if (this.isOfArchetype(player.getItemInHand(), this.legend)) {
                UtilEnt.damage(4.0, "Windblade", event.getDamagee(), event.getDamager());
            }
        }
    }

    @EventHandler
    private void onFallDamage(final EntityDamageEvent event) {
        if (this.updater.isDisabled(this)) {
            return;
        }
        if (event.getEntity() instanceof Player) {
            final Player player = (Player) event.getEntity();

            if (event.getCause() != EntityDamageEvent.DamageCause.FALL) {
                return;
            }

            if (this.isOfArchetype(player.getItemInHand(), this.legend)) {
                event.setCancelled(true);
                if (event.getDamage() > 5 || player.getHealth() <= 12) {
                    new User(player).sendMessageWithPrefix("Windblade", "Your &sWindblade &rblocked &s" + event.getDamage() + " &rHP from falling from that height!");
                }
                for (final Player observer : Bukkit.getOnlinePlayers()) {
                    if (observer.getLocation().distance(player.getLocation()) < 64.0) {
                        UtilParticle.showParticle(observer, player.getLocation().add(0, 0.2, 0), 0, 0.012f, 0.2f, 0.012f, 0.1f, 10);
                    }
                }
            }
        }
    }

    public void onUpdate(final Player player) {
        if (this.updater.isDisabled(this)) {
            return;
        }
        if (!this.charges.containsKey(player.getUniqueId())) {
            this.charges.put(player.getUniqueId(), 0.0f);
        }
        if (player.isOnGround() && !this.chargeBlocker.containsKey(player.getUniqueId())) {
            this.charge(player);
        }
        if (!this.vectors.containsKey(player.getUniqueId())) {
            this.vectors.put(player.getUniqueId(), null);
        }
        if (!this.smoother.containsKey(player.getUniqueId())) {
            this.smoother.put(player.getUniqueId(), 0.0f);
        } else if (this.smoother.get(player.getUniqueId()) != null && this.smoother.get(player.getUniqueId()) > 0.0f) {
            if (this.vectors.get(player.getUniqueId()) != null) {
                this.charges.put(player.getUniqueId(), (float) Math.max(0.0, this.getCharge(player) - 0.02));
                player.setVelocity(this.vectors.get(player.getUniqueId()));
            }
        } else {
            player.setAllowFlight(player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR);
        }

        this.smoother.put(player.getUniqueId(), (float) Math.max(0.0, this.smoother.get(player.getUniqueId()) - 0.5));
        if (this.getCharge(player) == 0.0f) {
            UtilPacket.displayProgress(null, 0.0, null, false, player);
        } else {
            UtilPacket.displayProgress(null, this.getCharge(player), null, false, player);
        }
    }

    public void charge(final Player player) {
        if (!this.charges.containsKey(player.getUniqueId())) {
            this.charges.put(player.getUniqueId(), 0.0f);
        }
        float charge = this.charges.get(player.getUniqueId());
        charge = (float) Math.min(1.0, charge + 0.01);
        this.charges.put(player.getUniqueId(), charge);
        UtilPacket.displayProgress(null, charge, null, false, player);
    }

    public float getCharge(final Player player) {
        if (!this.charges.containsKey(player.getUniqueId())) {
            this.charges.put(player.getUniqueId(), 0.0f);
        }
        return this.charges.get(player.getUniqueId());
    }

    @EventHandler
    private void onInteract(final PlayerInteractEvent event) {
        if (this.updater.isDisabled(this)) {
            return;
        }
        final Player player = event.getPlayer();
        if ((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) && this.isOfArchetype(player.getItemInHand(), this.legend)) {
            final BukkitTask toCancel = this.chargeBlocker.get(event.getPlayer().getUniqueId());
            if (toCancel != null) {
                toCancel.cancel();
            }

            this.chargeBlocker.put(event.getPlayer().getUniqueId(), new BukkitRunnable() {
                @Override
                public void run() {
                    WindbladeListener.this.chargeBlocker.remove(event.getPlayer().getUniqueId());
                }
            }.runTaskLater(this.plugin.getPlugin(), 20));

            if (player.getLocation().getBlock().isLiquid()) {
                new User(player).sendMessageWithPrefix("Windblade", "You cannot fly in water!");
                return;
            }

            if (this.getCharge(player) <= 0.0f) {
                return;
            }

            this.windLaunch(player);
        }
    }

    @EventHandler
    private void onToggleFlight(final PlayerToggleFlightEvent event) {
        if (this.updater.isDisabled(this)) {
            return;
        }
        if ((event.getPlayer().getGameMode() == GameMode.SURVIVAL || event.getPlayer().getGameMode() == GameMode.ADVENTURE) && this.isOfArchetype(event.getPlayer().getItemInHand(), this.legend)) {
            if (this.smoother.get(event.getPlayer().getUniqueId()) == null) {
                return;
            }
            if (this.smoother.get(event.getPlayer().getUniqueId()) == 0.0f) {
                return;
            }
            event.setCancelled(true);
        }
    }

    private void windLaunch(final Player player) {
        if (this.updater.isDisabled(this)) {
            return;
        }
        final Vector vector = player.getLocation().getDirection();
        if (Double.isNaN(vector.getX()) || Double.isNaN(vector.getY()) || Double.isNaN(vector.getZ()) || vector.length() == 0.0) {
            return;
        }
        vector.normalize().multiply(0.70);
        player.setVelocity(vector);
        for (final Player observer : Bukkit.getOnlinePlayers()) {
            if (observer.getLocation().distance(player.getLocation()) < 64.0) {
                UtilParticle.showParticle(observer, player.getLocation().add(0, 0.2, 0), 0, 0.012f, 0.2f, 0.012f, 0.1f, 4);
            }
        }

        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin.getPlugin(), () -> {
            if (player.isOnGround()) {
                player.playSound(player.getLocation(), Sound.LAVA_POP, 13.0f, 2.0f);
            }
            for (final Player observer : Bukkit.getOnlinePlayers()) {
                if (observer.getLocation().distance(player.getLocation()) < 64.0) {
                    UtilParticle.showParticle(observer, player.getLocation().add(0, 0.2, 0), 0, 0.012f, 0.2f, 0.012f, 0.1f, 4);
                }
            }
            player.setVelocity(vector);
        }, 2L);

        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin.getPlugin(), () -> {
            if (player.isOnGround()) {
                player.playSound(player.getLocation(), Sound.LAVA_POP, 13.0f, 2.0f);
            }
            for (final Player observer : Bukkit.getOnlinePlayers()) {
                if (observer.getLocation().distance(player.getLocation()) < 64.0) {
                    UtilParticle.showParticle(observer, player.getLocation().add(0, 0.2, 0), 0, 0.012f, 0.2f, 0.012f, 0.1f, 4);
                }
            }
            player.setVelocity(vector);
        }, 3L);

        this.vectors.put(player.getUniqueId(), vector);
        if (!this.smoother.containsKey(player.getUniqueId())) {
            this.smoother.put(player.getUniqueId(), 0.0f);
        }
        this.smoother.put(player.getUniqueId(), Math.min(5.0f, this.smoother.get(player.getUniqueId()) + 1.0f));
        if (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE) {
            player.setAllowFlight(true);
        }
        player.getWorld().playSound(player.getLocation(), Sound.FIZZ, 1.2f, 1.5f);
    }

    private void remove(final Player player, final boolean logged) {
        if (logged) {
            this.charges.remove(player.getUniqueId());
        }
        this.vectors.remove(player.getUniqueId());
        if (this.smoother.containsKey(player.getUniqueId())) {
            if (player.getGameMode() == GameMode.SPECTATOR || player.getGameMode() == GameMode.CREATIVE) {
                player.setAllowFlight(true);
                this.smoother.remove(player.getUniqueId());
            } else {
                player.setAllowFlight(false);
            }
            this.smoother.remove(player.getUniqueId());
        }
    }

    @EventHandler
    private void onPlayerDeath(final PlayerDeathEvent player) {
        this.remove(player.getEntity(), true);
    }

    @EventHandler
    private void onPlayerQuit(final PlayerQuitEvent event) {
        this.chargeBlocker.remove(event.getPlayer().getUniqueId());
        this.charges.remove(event.getPlayer().getUniqueId());
        this.smoother.remove(event.getPlayer().getUniqueId());
        this.vectors.remove(event.getPlayer().getUniqueId());
    }
}
