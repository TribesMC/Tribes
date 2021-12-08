package me.rey.clans.items.special.legends;

import me.rey.clans.Tribes;
import me.rey.clans.items.special.ClansTool;
import me.rey.core.events.customevents.combat.DamagedByEntityEvent;
import me.rey.core.players.User;
import me.rey.core.utils.UtilEnt;
import me.rey.core.utils.UtilPacket;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.*;

public class HyperaxeListener extends LegendaryItem {

    // CHANGES \\
    // Hyper rush now starts at swiftness 2
    // Hyper rush now immediately expires once held item switched off of hyper axe
    // Normal hyper axe hit speed is now 1.54 per second and 2.5 with hyper rush

    private final Set<UUID> hypedPlayers;
    private final Map<UUID, Long> cooldown;
    private final Map<LivingEntity, Integer> toRemove;

    public HyperaxeListener(final Tribes plugin) {
        super(plugin, "HYPERAXE", LegendType.HYPERAXE, ClansTool.HYPERAXE, ClansTool.HYPERAXE_DISABLED);
        this.hypedPlayers = new HashSet<>();
        this.cooldown = new HashMap<>();
        this.toRemove = new HashMap<>();
    }

    @Override
    public void update() {
        if (this.updater.isDisabled(this)) {
            return;
        }
        final List<LivingEntity> removalQueue = new ArrayList<>();

        for (final LivingEntity entity : this.toRemove.keySet()) {
            if (entity.isDead()) {
                continue;
            }

            if (this.toRemove.get(entity) > 0) {
                final int index = this.toRemove.getOrDefault(entity, 1);
                this.toRemove.put(entity, index - 1);
            } else {
                entity.setMaximumNoDamageTicks(20);
                if (this.toRemove.containsKey(entity)) {
                    removalQueue.add(entity);
                }
            }
        }

        removalQueue.clear();

        for (final UUID uuid : this.hypedPlayers) {
            final Player player = Bukkit.getPlayer(uuid);
            if (!this.isOfArchetype(player.getItemInHand(), this.legend)) {
                player.removePotionEffect(PotionEffectType.SPEED);
                this.hypedPlayers.remove(uuid);
            }
        }

        for (final UUID uuid : this.cooldown.keySet()) {
            final Player player = Bukkit.getPlayer(uuid);

            if (player == null || !player.isOnline()) {
                this.cooldown.remove(uuid);
            } else if ((System.currentTimeMillis() - this.cooldown.get(uuid)) / 1000L > 15L) {
                this.cooldown.remove(uuid);
                new User(player).sendMessageWithPrefix("Hyper Axe", "You can use &sHyper Rush&r!");
                if (this.isOfArchetype(player.getItemInHand(), this.legend)) {
                    UtilPacket.sendActionBarMessage(player, ChatColor.GREEN + "" + ChatColor.BOLD + "Hyper Rush Recharged");
                }
                player.playSound(player.getLocation(), Sound.NOTE_PLING, 5.0f, 1.0f);
            } else {
                if (!this.isOfArchetype(player.getItemInHand(), this.legend)) {
                    return;
                }
                final double x = 16.0 - Math.pow(10.0, -1.0) * ((double) (System.currentTimeMillis() - this.cooldown.get(player.getUniqueId())) / 100L);
                final double divide = (System.currentTimeMillis() - this.cooldown.get(uuid)) / 16000.0;
                final String[] doubleParse = Double.toString(x).replace('.', '-').split("-");
                final String concat = doubleParse[0] + "." + doubleParse[1].charAt(0);
                UtilPacket.displayProgress(ChatColor.BOLD + "Hyper Rush", divide, ChatColor.WHITE + " " + concat + " seconds", false, player);
            }
        }
    }

    @EventHandler
    private void onDamage(final EntityDamageEvent event) {
        if (this.updater.isDisabled(this)) {
            return;
        }
        if (event.isCancelled() || !(event.getEntity() instanceof LivingEntity)) {
            return;
        }

        if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) {

            final LivingEntity entity = (LivingEntity) event.getEntity();
            if (this.toRemove.containsKey(entity)) {
                entity.setMaximumNoDamageTicks(20);
                this.toRemove.remove(entity);
            }

        }
    }

    @EventHandler
    private void onDamageByEntity(final DamagedByEntityEvent event) {
        if (this.updater.isDisabled(this)) {
            return;
        }
        if (!(event.getDamager() instanceof Player)) {
            return;
        }

        final Player player = (Player) event.getDamager();

        if (this.plugin.getPvpTimer().getPvpTimer(player) > 0) {
            return;
        }
        final LivingEntity damagee = event.getDamagee();
        final ItemStack item = player.getItemInHand();

        if (this.isOfArchetype(item, this.legend)) {

            if (damagee instanceof Player) {
                if (((Player) event.getDamagee()).getGameMode() == GameMode.CREATIVE || ((Player) event.getDamagee()).getGameMode() == GameMode.SPECTATOR) {
                    return;
                }
            }

            if (this.hypedPlayers.contains(event.getDamager().getUniqueId())) {
                damagee.setMaximumNoDamageTicks(8);
            } else {
                damagee.setMaximumNoDamageTicks(13);
            }
            damagee.setVelocity(new Vector(0, 0.12, 0));

            this.toRemove.put(damagee, 7);
            UtilEnt.damage(4, "Hyper Axe", event.getDamagee(), event.getDamager());
            return;
        }

        if (this.toRemove.containsKey(damagee)) {
            damagee.setMaximumNoDamageTicks(20);
            this.toRemove.remove(damagee);
        }
    }

    @EventHandler
    private void onInteract(final PlayerInteractEvent event) {
        if (this.updater.isDisabled(this)) {
            return;
        }
        final Player player = event.getPlayer();
        final ItemStack item = player.getItemInHand();
        if ((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            if (this.isOfArchetype(item, this.legend)) {
                if (player.getLocation().getBlock().isLiquid()) {
                    new User(player).sendMessageWithPrefix("Hyper Axe", "You cannot use &sHyper Rush &r in water!");
                    return;
                }
                if (this.cooldown.containsKey(player.getUniqueId())) {
                    final double x = 16.0 - Math.pow(10.0, -1.0) * ((double) (System.currentTimeMillis() - this.cooldown.get(player.getUniqueId())) / 100L);
                    final String[] doubleParse = Double.toString(x).replace('.', '-').split("-");
                    final String concat = doubleParse[0] + "." + doubleParse[1].charAt(0);
                    new User(player).sendMessageWithPrefix("Hyper Axe", "You cannot use &sHyper Rush &r for another &s" + concat + " &rseconds!");
                    return;
                }
                this.cooldown.put(player.getUniqueId(), System.currentTimeMillis());
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 1));
                this.hypedPlayers.add(player.getUniqueId());
                Bukkit.getScheduler().runTaskLater(this.plugin.getPlugin(), () -> this.hypedPlayers.remove(player.getUniqueId()), 200);
                new User(player).sendMessageWithPrefix("Hyper Axe", "You used &sHyper Rush &r!");
            }
        }
    }

    @EventHandler
    private void onDeath(final EntityDeathEvent event) {
        if (this.updater.isDisabled(this)) {
            return;
        }
        final LivingEntity entity = event.getEntity();

        if (entity instanceof Player) {
            final Player player = (Player) entity;
            if (this.hypedPlayers.contains(player.getUniqueId())) {
                player.removePotionEffect(PotionEffectType.SPEED);
                this.hypedPlayers.remove(player.getUniqueId());
            }
        }

        if (this.toRemove.containsKey(entity)) {
            entity.setMaximumNoDamageTicks(20);
        }
        this.toRemove.remove(entity);
    }

    @EventHandler
    private void onPlayerQuit(final PlayerQuitEvent event) {
        event.getPlayer().setMaximumNoDamageTicks(20);
        if (this.hypedPlayers.contains(event.getPlayer().getUniqueId())) {
            event.getPlayer().removePotionEffect(PotionEffectType.SPEED);
            this.hypedPlayers.remove(event.getPlayer().getUniqueId());
        }
        this.cooldown.remove(event.getPlayer().getUniqueId());
        this.toRemove.remove(event.getPlayer());
    }

    @Override
    public void serverShutdown() {
        this.hypedPlayers.clear();
        this.cooldown.clear();
        this.toRemove.clear();
    }
}
