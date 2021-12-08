package me.rey.clans.items.special.legends;

import me.rey.clans.Tribes;
import me.rey.clans.items.special.ClansTool;
import me.rey.core.events.customevents.combat.DamagedByEntityEvent;
import me.rey.core.players.User;
import me.rey.core.utils.LineVisualizer;
import me.rey.core.utils.UtilEnt;
import me.rey.core.utils.UtilParticle;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GiantsBroadswordListener extends LegendaryItem {

    // CHANGES \\
    // Regeneration to caster buffed to 5
    // Now heals all allies and clanmates in a radius of 5 blocks with regeneration 4
    // Particle effects are linked between players from the caster to see who is using the legend
    // Legend caster has more heart particles than the others being healed
    // Docile magic particles volume doubled

    List<UUID> healFreeze;

    public GiantsBroadswordListener(final Tribes plugin) {
        super(plugin, "GIANTS_BROADSWORD", LegendType.GIANTS_BROADSWORD, ClansTool.GIANTS_BROADSWORD, ClansTool.GIANTS_BROADSWORD_DISABLED);
        this.healFreeze = new ArrayList<>();
    }

    @Override
    public void update() {
        if (this.updater.isDisabled(this)) {
            return;
        }
        for (final Player player : Bukkit.getOnlinePlayers()) {
            final ItemStack itemInHand = player.getItemInHand();
            if (!this.isOfArchetype(itemInHand, this.legend)) {
                return;
            }

            final Location location = player.getLocation().clone().add(0.0, 1.0, 0.0);
            for (final Player observer : Bukkit.getOnlinePlayers()) {
                UtilParticle.showParticle(observer, location, 10, .4f, .4f, .4f, 0.005f, 1);
            }
        }
    }

    @Override
    public void serverShutdown() {
        this.healFreeze.clear();
        this.healFreeze = null;
    }

    @EventHandler
    private void onInteract(final PlayerInteractEvent event) {
        if (this.updater.isDisabled(this)) {
            return;
        }
        final Player player = event.getPlayer();
        if (!event.hasItem()) {
            return;
        }
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (!this.isOfArchetype(event.getItem(), this.legend)) {
                return;
            }

            if (this.plugin.getPvpTimer().getPvpTimer(event.getPlayer()) > 0) {
                new User(event.getPlayer()).sendMessageWithPrefix("Giants Broadsword", "You cannot use this weapons ability with an active PVP timer!");
                return;
            }

            new BukkitRunnable() {
                @Override
                public void run() {
                    GiantsBroadswordListener.this.healFreeze.add(player.getUniqueId());
                    for (final Player nearby : Bukkit.getOnlinePlayers()) {
                        //todo temp patch
                        if (!new User(player).getTeam().contains(nearby)) {
                            continue;
                        }
                        if (nearby.getLocation().distance(player.getLocation()) <= 5) {
                            nearby.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20, player.equals(nearby) ? 3 : 2));
                            nearby.setFoodLevel(nearby.getFoodLevel() + 1);

                            for (final Player observer : Bukkit.getOnlinePlayers()) {
                                if (observer.getLocation().distance(player.getLocation()) > 50) {
                                    continue;
                                }
                                if (nearby.equals(player)) {
                                    continue;
                                }

                                final LineVisualizer points = new LineVisualizer(10, player.getLocation().add(0, 1.5, 0).toVector(), nearby.getLocation().add(0, 1.5, 0).toVector());
                                for (final Vector vector : points.points) {
                                    final Location particleLoc = vector.toLocation(observer.getWorld());
                                    UtilParticle.showParticle(observer, particleLoc, 3, 0, 0, 0, 0, 1, 0);
                                }

                                UtilParticle.showParticle(observer, nearby.getLocation().add(0, 2, 0), 34, .1f, .4f, .1f, 0.05f, 1);
                            }
                        }
                    }

                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20, 255));
                    player.getWorld().playSound(player.getLocation(), Sound.LAVA_POP, 13.0f, 2.0f);
                    for (final Player observer : Bukkit.getOnlinePlayers()) {
                        if (observer.getLocation().distance(player.getLocation()) > 50) {
                            continue;
                        }
                        UtilParticle.showParticle(observer, player.getLocation().add(0, 2, 0), 34, .4f, .4f, .4f, 0.05f, 3);
                    }

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (!player.isOnline()) {
                                return;
                            }
                            player.getWorld().playSound(player.getLocation(), Sound.LAVA_POP, 13.0f, 2.0f);
                        }
                    }.runTaskLater(GiantsBroadswordListener.this.plugin.getPlugin(), 3);

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (!player.isOnline()) {
                                return;
                            }
                            player.getWorld().playSound(player.getLocation(), Sound.LAVA_POP, 13.0f, 2.0f);
                            GiantsBroadswordListener.this.healFreeze.remove(player.getUniqueId());
                        }
                    }.runTaskLater(GiantsBroadswordListener.this.plugin.getPlugin(), 5);

                }
            }.runTask(this.plugin.getPlugin());
        }
    }

    @EventHandler
    private void onDamage(final DamagedByEntityEvent event) {
        if (this.updater.isDisabled(this)) {
            return;
        }
        if (!(event.getDamager() instanceof Player)) {
            return;
        }
        final Player player = (Player) event.getDamager();
        if (!this.isOfArchetype(player.getItemInHand(), this.legend)) {
            return;
        }
        if (this.plugin.getPvpTimer().getPvpTimer(player) > 0) {
            return;
        }
        if (this.healFreeze.contains(player.getUniqueId())) {
            return;
        }
        UtilEnt.damage(10.0, "Giants Broadsword", event.getDamagee(), event.getDamager());
    }

    @EventHandler
    private void onHitAnimation(final PlayerAnimationEvent event) {
        if (this.updater.isDisabled(this)) {
            return;
        }
        final ItemStack itemInHand = event.getPlayer().getItemInHand();
        if (this.isOfArchetype(itemInHand, this.legend)) {
            if (!this.healFreeze.contains(event.getPlayer().getUniqueId())) {
                return;
            }
            if (event.getAnimationType() != PlayerAnimationType.ARM_SWING) {
                return;
            }
            event.setCancelled(true);
        }
    }

    @EventHandler
    private void onPlayerQuit(final PlayerQuitEvent event) {
        this.healFreeze.remove(event.getPlayer().getUniqueId());
    }
}
