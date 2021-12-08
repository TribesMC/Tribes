package me.rey.clans.items.special.legends;

import me.rey.clans.Tribes;
import me.rey.clans.items.special.ClansTool;
import me.rey.core.events.customevents.combat.DamagedByEntityEvent;
import me.rey.core.players.User;
import me.rey.core.utils.UtilEnt;
import me.rey.core.utils.UtilEntity;
import me.rey.core.utils.UtilPacket;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.*;

public class KnightsGreatlanceListener extends LegendaryItem {
    // CHANGES \\
    // Duration increased to 10 seconds
    // Recharge delay increased to 2 seconds
    // Speed buff increased to speed 20
    // Recharge rate decreased to 10% per second
    // Entity bash radius increased to 1.5 blocks
    // Increased upward velocity of Charge bash when entity is on the floor
    // Charge damage increased to 8
    // Charge cooldown increased to 12

    private final Set<UUID> cooldown;
    private final Map<UUID, BukkitTask> chargeBlocker;
    private final Map<UUID, Float> charges;
    private final Map<UUID, Float> smoother;

    public KnightsGreatlanceListener(final Tribes plugin) {
        super(plugin, "KNIGHTS_GREATLANCE", LegendType.KNIGHTS_GREATLANCE, ClansTool.KNIGHTS_GREATLANCE, ClansTool.KNIGHTS_GREATLANCE_DISABLED);
        this.cooldown = new HashSet<>();
        this.chargeBlocker = new HashMap<>();
        this.charges = new HashMap<>();
        this.smoother = new HashMap<>();
    }

    @Override
    public void update() {
        if (this.updater.isDisabled(this)) {
            return;
        }
        for (final Player player : Bukkit.getOnlinePlayers()) {
            if (this.isOfArchetype(player.getItemInHand(), this.legend)) {
                this.onUpdate(player);
            }
        }
    }

    @Override
    public void serverShutdown() {
        this.cooldown.clear();
        this.chargeBlocker.clear();
        this.charges.clear();
        this.smoother.clear();
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
                    KnightsGreatlanceListener.this.chargeBlocker.remove(event.getPlayer().getUniqueId());
                }
            }.runTaskLater(this.plugin.getPlugin(), 40));

            if (player.getLocation().getBlock().isLiquid()) {
                new User(player).sendMessageWithPrefix("Knight's Greatlance", "You cannot charge in water!");
                return;
            }

            if (this.getCharge(player) <= 0.0f) {
                return;
            }

            this.doSpeed(player);
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
        if (!this.isOfArchetype(player.getItemInHand(), this.legend)) {
            return;
        }
        UtilEnt.damage(8.0, "Knights Greatlance", event.getDamagee(), event.getDamager());
    }

    private void onUpdate(final Player player) {
        if (this.updater.isDisabled(this)) {
            return;
        }
        if (!this.charges.containsKey(player.getUniqueId())) {
            this.charges.put(player.getUniqueId(), 0.0f);
        }
        if (!this.chargeBlocker.containsKey(player.getUniqueId())) {
            this.charge(player);
        }
        if (!this.smoother.containsKey(player.getUniqueId())) {
            this.smoother.put(player.getUniqueId(), 0.0f);
        } else if (this.smoother.get(player.getUniqueId()) != null && this.smoother.get(player.getUniqueId()) > 0.0f) {
            this.charges.put(player.getUniqueId(), (float) Math.max(0.0, this.getCharge(player) - 0.01));
        }

        this.smoother.put(player.getUniqueId(), (float) Math.max(0.0, this.smoother.get(player.getUniqueId()) - 0.5));
        if (this.getCharge(player) == 0.0f) {
            UtilPacket.displayProgress(null, 0.0, null, false, player);
        } else {
            UtilPacket.displayProgress(null, this.getCharge(player), null, false, player);
        }
    }

    public float getCharge(final Player player) {
        if (this.updater.isDisabled(this)) {
            return 0.0f;
        }
        if (!this.charges.containsKey(player.getUniqueId())) {
            this.charges.put(player.getUniqueId(), 0.0f);
        }
        return this.charges.get(player.getUniqueId());
    }

    public void charge(final Player player) {
        if (this.updater.isDisabled(this)) {
            return;
        }
        if (!this.charges.containsKey(player.getUniqueId())) {
            this.charges.put(player.getUniqueId(), 0.0f);
        }
        float charge = this.charges.get(player.getUniqueId());
        charge = (float) Math.min(1.0, charge + 0.005);
        this.charges.put(player.getUniqueId(), charge);
        UtilPacket.displayProgress(null, charge, null, false, player);
    }

    private void doSpeed(final Player player) {
        if (this.updater.isDisabled(this)) {
            return;
        }

        if (this.plugin.getPvpTimer().getPvpTimer(player) > 0) {
            new User(player).sendMessageWithPrefix("Knights Greatlance", "You cannot use this weapons ability with an active PVP timer!");
            return;
        }

        if (!this.smoother.containsKey(player.getUniqueId())) {
            this.smoother.put(player.getUniqueId(), 0.0f);
        }

        boolean bashed = false;
        if (!this.cooldown.contains(player.getUniqueId())) {
            for (final Entity entity : player.getNearbyEntities(1.5, 1.5, 1.5)) {
                if (bashed) {
                    continue;
                }
                if (entity == player) {
                    continue;
                }
                if (entity.isDead()) {
                    continue;
                }
                if (!(entity instanceof LivingEntity)) {
                    continue;
                }
                if (entity instanceof ArmorStand) {
                    continue;
                }

                if (entity instanceof Player) {
                    final Player prey = (Player) entity;
                    if (!player.isOnline()) {
                        continue;
                    }
                    if (!player.canSee(prey)) {
                        continue;
                    }
                }

                bashed = true;
                this.bash(player, (LivingEntity) entity);
            }
        }

        this.smoother.put(player.getUniqueId(), Math.min(5.0f, this.smoother.get(player.getUniqueId()) + 1.0f));
        player.getWorld().playSound(player.getLocation(), Sound.FIZZ, 1.2f, 1.5f);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 5, 19));
    }

    private void bash(final Entity attacker, final LivingEntity entity) {
        if (this.updater.isDisabled(this)) {
            return;
        }
        if (attacker instanceof Player) {
            this.cooldown.add(attacker.getUniqueId());
            Bukkit.getScheduler().runTaskLater(this.plugin.getPlugin(), () -> {
                if (!((Player) attacker).isOnline()) {
                    return;
                }
                this.cooldown.remove(attacker.getUniqueId());
                new User((Player) attacker).sendMessageWithPrefix("Knight's Greatlance", "You can use &sCharge&r!");
            }, 240);
        }

        if (entity instanceof Player) {
            if (this.plugin.getPvpTimer().getPvpTimer((Player) entity) > 0) {
                return;
            }
        }

        Vector knockback = entity.getLocation().toVector().subtract(attacker.getLocation().toVector()).setY(0).normalize();
        knockback.multiply(5);
        knockback.setY(0.5);
        if (entity.isOnGround()) {
            knockback = knockback.add(new Vector(0, 0.3, 0));
        }
        entity.setFallDistance(0);
        entity.setVelocity(knockback);
        UtilEnt.damage(8.0, "Greatlance Charge", entity, (LivingEntity) attacker);

        if (attacker instanceof Player) {
            new User((Player) attacker).sendMessageWithPrefix("Knight's Greatlance", "You hit &s" + this.getEntityName(entity) + " &rwith &sCharge&r!");
        }
        if (entity instanceof Player) {
            new User((Player) entity).sendMessageWithPrefix("Clans", "&s" + this.getEntityName(attacker) + " &rhit you with &sCharge&r!");
        }
    }

    public String getEntityName(final Entity entity) {
        final String name;
        if (entity instanceof Player) {
            name = "&s" + entity.getName() + "&r";
        } else {
            if (entity.isCustomNameVisible()) {
                name = "&s" + entity.getCustomName() + "&r";
            } else {
                final UtilEntity.EntityInfo info = UtilEntity.getEntityByType(entity.getType());
                if (info != null) {
                    name = info.reference + " &s" + info.name + "&r";
                } else {
                    name = "&ssomething&r";
                }
            }
        }
        return name;
    }

    @EventHandler
    private void onPlayerQuit(final PlayerQuitEvent event) {
        this.cooldown.remove(event.getPlayer().getUniqueId());
        this.chargeBlocker.remove(event.getPlayer().getUniqueId());
        this.charges.remove(event.getPlayer().getUniqueId());
        this.smoother.remove(event.getPlayer().getUniqueId());
    }
}
