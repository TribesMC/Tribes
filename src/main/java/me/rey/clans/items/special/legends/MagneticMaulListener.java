package me.rey.clans.items.special.legends;

import me.rey.clans.Tribes;
import me.rey.clans.items.special.ClansTool;
import me.rey.core.events.customevents.combat.DamagedByEntityEvent;
import me.rey.core.players.User;
import me.rey.core.utils.LineVisualizer;
import me.rey.core.utils.UtilEnt;
import me.rey.core.utils.UtilPacket;
import me.rey.core.utils.UtilParticle;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MagneticMaulListener extends LegendaryItem {

    // CHANGES \\
    // Range buffed from 7 to 12
    // Changed how particles work reducing particle-induced lag
    // Damaging entities with this weapon no longer brings them towards you, instead gives them smaller knockback
    // Recharge delay increased to 2 seconds
    // Recharge rate decreased from 13% per second to 10% per second
    // Maul can no longer pull holograms, NPCs and armor stands in certain circumstances

    private final Map<UUID, BukkitTask> chargeBlocker;
    private final Map<UUID, Float> charges;
    private final Map<UUID, Float> smoother;

    public MagneticMaulListener(final Tribes plugin) {
        super(plugin, "MAGNETIC_MAUL", LegendType.MAGNETIC_MAUL, ClansTool.MAGNETIC_MAUL, ClansTool.MAGNETIC_MAUL_DISABLED);
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
            final ItemStack item = player.getItemInHand();

            boolean contains = false;
            for (final ItemStack stack : player.getInventory().getContents()) {
                if (stack == null) {
                    continue;
                }
                if (stack.isSimilar(this.legend.getTool().stack)) {
                    contains = true;
                    break;
                }
            }

            if (!contains) {
                this.charges.remove(player.getUniqueId());
            }

            final Float smoothed;
            if ((smoothed = this.smoother.get(player.getUniqueId())) != null) {
                if (smoothed > 0.6f && this.charges.getOrDefault(player.getUniqueId(), 0f) >= 0.15f) {
                    for (final Entity entity : player.getNearbyEntities(12.0, 12.0, 12.0)) {
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
                            if (this.plugin.getPvpTimer().getPvpTimer(prey) > 0) {
                                continue;
                            }
                        }

                        if (!this.getLookingAt(player, entity)) {
                            continue;
                        }

                        for (final Player observer : Bukkit.getOnlinePlayers()) {
                            if (observer.getLocation().distance(player.getLocation()) > 50) {
                                continue;
                            }

                            final LineVisualizer points = new LineVisualizer((int) (12 - player.getLocation().distance(entity.getLocation())), player.getLocation().add(0, 1.5, 0).toVector(), entity.getLocation().add(0, 1.5, 0).toVector());
                            for (final Vector vector : points.points) {
                                final Location particleLoc = vector.toLocation(observer.getWorld());
                                UtilParticle.showParticle(observer, particleLoc, 10, 0.1f, 0.1f, 0.1f, 0, 1, 0);
                            }
                        }

                        final Vector vec = player.getLocation().toVector().subtract(entity.getLocation().toVector()).normalize();
                        final Vector newVec = vec.multiply(0.7);
                        final Vector useCharge = newVec.multiply(1.2).multiply(Math.min(0.34, Math.max(this.getCharge(player), 0.23)));
                        entity.setVelocity(useCharge);
                    }
                }
            }

            if (!this.isOfArchetype(item, this.legend)) {
                this.smoother.remove(player.getUniqueId());
                return;
            }

            this.onUpdate(player);
        }
    }

    @Override
    public void serverShutdown() {
        for (final Map.Entry<UUID, BukkitTask> task : this.chargeBlocker.entrySet()) {
            task.getValue().cancel();
        }
        this.chargeBlocker.clear();
        this.charges.clear();
        this.smoother.clear();
    }

    @EventHandler
    private void onClick(final PlayerInteractEvent event) {
        if (this.updater.isDisabled(this)) {
            return;
        }
        if ((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) && this.isOfArchetype(event.getItem(), this.legend)) {
            if (this.plugin.getPvpTimer().getPvpTimer(event.getPlayer()) > 0) {
                new User(event.getPlayer()).sendMessageWithPrefix("Magnetic Maul", "You cannot use this weapons ability with an active PVP timer!");
                return;
            }

            final BukkitTask toCancel = this.chargeBlocker.get(event.getPlayer().getUniqueId());
            if (toCancel != null) {
                toCancel.cancel();
            }

            this.chargeBlocker.put(event.getPlayer().getUniqueId(), new BukkitRunnable() {
                @Override
                public void run() {
                    MagneticMaulListener.this.chargeBlocker.remove(event.getPlayer().getUniqueId());
                }
            }.runTaskLater(this.plugin.getPlugin(), 40));

            this.smoother.put(event.getPlayer().getUniqueId(), Math.min(5.0f, this.smoother.get(event.getPlayer().getUniqueId()) + 2.0f));
        }
    }

    public void charge(final Player player) {
        float charge = this.charges.getOrDefault(player.getUniqueId(), 0.0f);
        charge = (float) Math.min(1.0, charge + 0.01);
        this.charges.put(player.getUniqueId(), charge);
        UtilPacket.displayProgress(null, charge, null, false, player);
    }

    public float getCharge(final Player player) {
        return this.charges.getOrDefault(player.getUniqueId(), 0.0f);
    }

    private boolean getLookingAt(final Player player, final Entity entity) {
        final Location eye = player.getLocation();
        final Vector toEntity = entity.getLocation().toVector().subtract(eye.toVector());
        return toEntity.normalize().dot(eye.getDirection()) > 0.7;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onDamage(final DamagedByEntityEvent event) {
        if (this.updater.isDisabled(this)) {
            return;
        }
        if (event.isCancelled()) {
            return;
        }

        if (event.getDamager() instanceof Player) {
            final Player player = (Player) event.getDamager();

            if (!this.isOfArchetype(player.getItemInHand(), this.legend)) {
                return;
            }

            if (event.getDamagee() instanceof Player) {
                if (this.plugin.getPvpTimer().getPvpTimer((Player) event.getDamagee()) > 0) {
                    return;
                }
                if (((Player) event.getDamagee()).getGameMode() == GameMode.CREATIVE || ((Player) event.getDamagee()).getGameMode() == GameMode.SPECTATOR) {
                    return;
                }
            }
            event.getDamagee().setVelocity(new Vector(0, 0.3, 0));
            UtilEnt.damage(8.0, "Magnetic Maul", event.getDamagee(), event.getDamager());
        }
    }

    public void onUpdate(final Player player) {
        if (this.updater.isDisabled(this)) {
            return;
        }
        if (!this.charges.containsKey(player.getUniqueId()) && this.isOfArchetype(player.getItemInHand(), this.legend)) {
            this.charges.put(player.getUniqueId(), 0.0f);
        }
        if (!this.smoother.containsKey(player.getUniqueId()) && this.isOfArchetype(player.getItemInHand(), this.legend)) {
            this.smoother.put(player.getUniqueId(), 0.0f);
        }

        if (this.smoother.get(player.getUniqueId()) == 0.0f) {// || charges.get(player.getUniqueId()) <= 0.13) {
            if (!this.chargeBlocker.containsKey(player.getUniqueId())) {
                this.charge(player);
            }
        } else if (this.smoother.get(player.getUniqueId()) != 0.0f) {
            this.charges.put(player.getUniqueId(), (float) Math.max(0.0, this.getCharge(player) - 0.017));
        }

        if (this.isOfArchetype(player.getItemInHand(), this.legend)) {
            this.smoother.put(player.getUniqueId(), (float) Math.max(0.0, this.smoother.get(player.getUniqueId()) - 0.5));
            UtilPacket.displayProgress(null, this.getCharge(player), null, false, player);
        }
    }

    @EventHandler
    private void onPlayerQuit(final PlayerQuitEvent event) {
        final BukkitTask toCancel = this.chargeBlocker.get(event.getPlayer().getUniqueId());
        if (toCancel != null) {
            toCancel.cancel();
        }
        this.chargeBlocker.remove(event.getPlayer().getUniqueId());
        this.charges.remove(event.getPlayer().getUniqueId());
        this.smoother.remove(event.getPlayer().getUniqueId());
    }
}