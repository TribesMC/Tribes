package me.rey.clans.items.special.legends;

import me.rey.clans.Tribes;
import me.rey.clans.items.special.ClansTool;
import me.rey.clans.items.special.data.SpecialItemStatusChangeEvent;
import me.rey.core.events.customevents.combat.DamagedByEntityEvent;
import me.rey.core.players.User;
import me.rey.core.utils.UtilEnt;
import me.rey.core.utils.UtilEntity;
import me.rey.core.utils.UtilPacket;
import me.rey.core.utils.UtilParticle;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class MeridianScepterListener extends LegendaryItem {

    // CHANGES \\
    // Travels a maximum of 60 blocks
    // Stops travelling to player if they get 50+ blocks away
    // Damage massively dropped to 6
    // Changed the way messages from the scepter are phrased
    // Blindness dropped to 1.25 seconds from 2.5 seconds
    // Blast shots now hit immediately
    // Upward range increased to 30 from 15
    // No-target speed slightly decreased below walking speed
    // Meridian scepter now has a melee damage amount of 4
    // If a player who has launched blasts is killed, all active blasts are destroyed
    // Blast with no target no longer has a slightly upward velocity

    private final Set<ScepterBlast> scepterBlasts;
    private final Map<UUID, Long> cooldown;

    public MeridianScepterListener(final Tribes plugin) {
        super(plugin, "MERIDIAN_SCEPTER", LegendType.MERIDIAN_SCEPTER, ClansTool.MERIDIAN_SCEPTER, ClansTool.MERIDIAN_SCEPTER_DISABLED);
        this.scepterBlasts = new HashSet<>();
        this.cooldown = new HashMap<>();
    }

    @Override
    public void update() {
        if (this.updater.isDisabled(this)) {
            return;
        }
        if (!this.scepterBlasts.isEmpty()) {
            // to avoid cme's
            final Set<ScepterBlast> concurrentCopy = new HashSet<>(this.scepterBlasts);
            for (final ScepterBlast shot : concurrentCopy) {
                if (!shot.arrowCast.isDead() && !shot.gone) {
                    shot.update();
                }
            }
        }
        for (final UUID uuid : new HashMap<>(this.cooldown).keySet()) {
            final Player player = Bukkit.getPlayer(uuid);
            if ((System.currentTimeMillis() - this.cooldown.get(uuid)) / 1000L > 1L) {
                this.cooldown.remove(uuid);
                if (!this.isOfArchetype(player.getItemInHand(), this.legend)) {
                    continue;
                }
                UtilPacket.sendActionBarMessage(player, ChatColor.GREEN + "" + ChatColor.BOLD + "Scepter Blast Recharged");
            } else {
                if (!this.isOfArchetype(player.getItemInHand(), this.legend)) {
                    continue;
                }
                final double x = 2.0 - Math.pow(10.0, -1.0) * ((double) (System.currentTimeMillis() - this.cooldown.get(player.getUniqueId())) / 100L);
                final double divide = (System.currentTimeMillis() - this.cooldown.get(uuid)) / 2000.0;
                final String[] doubleParse = Double.toString(x).replace('.', '-').split("-");
                final String concat = doubleParse[0] + "." + doubleParse[1].charAt(0);
                UtilPacket.displayProgress(ChatColor.BOLD + "Scepter Blast", divide, ChatColor.WHITE + " " + concat + " Seconds", false, player);
            }
        }
    }

    @Override
    public void serverShutdown() {
        for (final ScepterBlast sb : this.scepterBlasts) {
            sb.cleanup();
        }
        this.scepterBlasts.clear();
        this.cooldown.clear();
    }

    @EventHandler
    private void onInteract(final PlayerInteractEvent event) {
        if (this.updater.isDisabled(this)) {
            return;
        }
        final Player player = event.getPlayer();
        final ItemStack item = player.getItemInHand();
        if ((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            if (!this.isOfArchetype(item, this.legend)) {
                return;
            }
            if (this.plugin.getPvpTimer().getPvpTimer(event.getPlayer()) > 0) {
                new User(event.getPlayer()).sendMessageWithPrefix("Meridian Scepter", "You cannot use this weapons ability with an active PVP timer!");
                return;
            }
            if (this.cooldown.containsKey(player.getUniqueId())) {
                return;
            }
            if (player.getLocation().getBlock().isLiquid()) {
                new User(player).sendMessageWithPrefix("Meridian Scepter", "You cannot use &sScepter Blast &rin water!");
                return;
            }

            this.cooldown.put(player.getUniqueId(), System.currentTimeMillis());
            final ScepterBlast sb = new ScepterBlast(this.plugin, this, event.getPlayer());
            this.scepterBlasts.add(sb);
        }
    }

    @EventHandler
    private void onProjectileHit(final ProjectileHitEvent event) {
        if (this.updater.isDisabled(this)) {
            return;
        }
        if (event.getEntity() instanceof Arrow) {
            final Arrow arrow = (Arrow) event.getEntity();
            for (final ScepterBlast shot : this.scepterBlasts) {
                if (shot.arrowCast == arrow) {
                    final Location arrowLoc = shot.arrowCast.getLocation();
                    final Vector arrowVec = shot.arrowCast.getVelocity();
                    final Location blockLocation = new Location(shot.arrowCast.getWorld(), arrowLoc.getX() + arrowVec.getX(), arrowLoc.getY() + arrowVec.getY(), arrowLoc.getZ() + arrowVec.getZ());

                    final Block hitBlock = arrowLoc.getWorld().getBlockAt(blockLocation);
                    if (hitBlock == null || hitBlock.getType() == Material.AIR) {
                        continue;
                    }

                    final Material type = hitBlock.getType();
                    if (type == Material.STATIONARY_LAVA || type == Material.STATIONARY_WATER || type == Material.WATER || type == Material.LAVA) {
                        return;
                    }

                    arrow.teleport(new Location(arrow.getWorld(), 0.0, -10.0, 0.0));

                    if (shot.toRemove) {
                        continue;
                    }
                    shot.toRemove = true;
                }
            }
        }
    }

    @EventHandler
    private void onDeath(final EntityDeathEvent event) {
        if (this.updater.isDisabled(this)) {
            return;
        }
        if (event.getEntity() instanceof Arrow) {
            final Arrow arrow = (Arrow) event.getEntity();
            for (final ScepterBlast shot : this.scepterBlasts) {
                if (shot.arrowCast == arrow) {
                    if (shot.toRemove) {
                        return;
                    }
                    shot.toRemove = true;
                }
            }
        }

        for (final ScepterBlast sb : this.scepterBlasts) {
            if (sb.firer.equals(event.getEntity())) {
                sb.toRemove = true;
            }
        }
    }

    @EventHandler
    private void onDamage(final DamagedByEntityEvent event) {
        if (this.updater.isDisabled(this)) {
            return;
        }
        if (event.isCancelled()) {
            if (event.getOriginalEvent().getDamager() instanceof Arrow) {
                final Arrow arrow = (Arrow) event.getOriginalEvent().getDamager();
                for (final ScepterBlast shot : this.scepterBlasts) {
                    if (shot.arrowCast == arrow) {
                        arrow.teleport(new Location(arrow.getWorld(), 0.0, -10.0, 0.0));
                        shot.toRemove = true;
                    }
                }
            }
            return;
        }
        if (event.getOriginalEvent().getDamager() instanceof Arrow) {
            final Arrow arrow = (Arrow) event.getOriginalEvent().getDamager();
            final LivingEntity hitEntity = event.getDamagee();
            if (hitEntity.isDead()) {
                return;
            }
            for (final ScepterBlast shot : this.scepterBlasts) {
                if (shot.arrowCast == arrow) {
                    if (shot.firer == hitEntity) {
                        event.setCancelled(true);
                    } else {
                        arrow.teleport(new Location(arrow.getWorld(), 0.0, -10.0, 0.0));

                        if (hitEntity instanceof Player) {
                            if (this.plugin.getPvpTimer().getPvpTimer((Player) hitEntity) > 0) {
                                return;
                            }
                        }

                        if (shot.firer instanceof Player) {
                            new User((Player) shot.firer).sendMessageWithPrefix("Meridian Scepter", "You hit " + this.getEntityName(hitEntity) + " with &sScepter Blast&r!");
                        }
                        if (hitEntity instanceof Player) {
                            new User((Player) hitEntity).sendMessageWithPrefix("Clans", "&s" + this.getEntityName(shot.firer) + "&r hit you with a &sScepter Blast&r!");
                        }

                        event.setCancelled(true);

                        shot.toRemove = true;

                        hitEntity.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 0));
                        shot.arrowCast.getWorld().strikeLightningEffect(hitEntity.getLocation());
                        UtilEnt.damage(8.0, "Meridian Scepter", hitEntity, event.getDamager());
                    }
                }
            }
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
            }

            if (event.getDamagee() instanceof LivingEntity) {
                if (event.getDamagee() instanceof Player && (((Player) event.getDamagee()).getGameMode() == GameMode.CREATIVE || ((Player) event.getDamagee()).getGameMode() == GameMode.SPECTATOR)) {
                    return;
                }
                UtilEnt.damage(5.0, "Scepter Bolt", event.getDamagee(), event.getDamager());
            }
        }
    }

    @EventHandler
    private void onPlayerQuit(final PlayerQuitEvent event) {
        for (final ScepterBlast sb : this.scepterBlasts) {
            if (sb.firer.equals(event.getPlayer())) {
                sb.toRemove = true;
            }
        }
        this.cooldown.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    private void onStatusChange(final SpecialItemStatusChangeEvent event) {
        if (!event.item.equals(this)) {
            return;
        }
        if (event.status) {
            return;
        }
        for (final ScepterBlast sb : this.scepterBlasts) {
            sb.cleanup();
        }
        this.scepterBlasts.clear();
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

    public static class ScepterBlast {

        public boolean toRemove, gone;
        public Entity firer;
        public Arrow arrowCast;
        public Location startPoint;
        public long spawnedAt;
        public Vector vector;
        UUID uuid;
        Tribes plugin;
        MeridianScepterListener listener;
        LivingEntity target = null;

        private boolean disabled = false;

        public ScepterBlast(final Tribes plugin, final MeridianScepterListener listener, final Entity firer) {
            this.toRemove = false;
            this.gone = false;
            this.uuid = UUID.randomUUID();
            this.plugin = plugin;
            this.listener = listener;
            this.firer = firer;
            this.arrowCast = this.hideArrowCast(((LivingEntity) firer).launchProjectile(Arrow.class, firer.getLocation().getDirection().multiply(0.5)));
            this.startPoint = firer.getLocation();
            this.arrowCast.setVelocity(firer.getLocation().getDirection().multiply(0.55));
            this.vector = this.arrowCast.getVelocity().clone().multiply(0.55);
            this.spawnedAt = System.currentTimeMillis();
        }

        public void update() {
            if (this.disabled) {
                return;
            }
            final long timeAlive = (System.currentTimeMillis() - this.spawnedAt) / 1000;
            if (timeAlive > 10 || this.arrowCast.getTicksLived() > 240 || this.startPoint.distance(this.arrowCast.getLocation()) > 60 || this.arrowCast.getVelocity() == null || this.arrowCast.getVelocity().equals(new Vector(0, 0, 0))) {
                return;
            }

            for (final Player player : Bukkit.getOnlinePlayers()) {
                for (int i = 0; i < 10; i++) {
                    if (!player.isOnline()) {
                        return;
                    }
                    if (this.toRemove) {
                        return;
                    }
                    if (this.gone) {
                        return;
                    }
                    UtilParticle.showParticle(player, this.arrowCast.getLocation(), 30, 0.77f, 0.0f, 1.0f, 1.0f, 0, 1);
                }
            }

            if (this.target == null) {
                if (!this.toRemove) {
                    if (this.vector == null) {
                        this.arrowCast.setVelocity(this.arrowCast.getVelocity());
                    } else {
                        this.arrowCast.setVelocity(this.vector);
                    }
                    this.searchForTarget();
                }
            } else if (!this.target.isDead() && this.target.hasLineOfSight(this.arrowCast) && this.target.getLocation().distance(this.arrowCast.getLocation()) < 50) {
                if (!this.toRemove) {
                    final Vector toTarget = this.target.getEyeLocation().clone().subtract(this.arrowCast.getLocation()).toVector();
                    final Vector dirVelocity = this.arrowCast.getVelocity().clone().normalize();
                    final Vector dirToTarget = toTarget.clone().normalize();
                    final Vector newDir = dirVelocity.clone().add(dirToTarget.clone());
                    newDir.normalize();
                    final Vector newVelocity = newDir.clone().multiply(0.4);
                    this.arrowCast.setVelocity(newVelocity);
                }
            } else if (!this.toRemove) {
                this.target = null;
            }

            if (this.toRemove) {
                this.listener.scepterBlasts.remove(this);
                this.arrowCast.remove();
                this.gone = true;
                this.toRemove = false;
                this.disabled = true;
            }
        }

        public void cleanup() {
            this.toRemove = true;
        }

        public UUID getUniqueId() {
            return this.uuid;
        }

        private Arrow hideArrowCast(final Arrow arrow) {
            try {
                final Constructor<?> constructor = UtilPacket.getClassNMS("PacketPlayOutEntityDestroy").getConstructor(int[].class);
                final int[] array = {
                        arrow.getEntityId()
                };
                final Object packet = constructor.newInstance((Object) array);
                for (final Player player : Bukkit.getOnlinePlayers()) {
                    UtilPacket.sendPacket(player, packet);
                }
            } catch (final NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
            return arrow;
        }

        private void searchForTarget() {
            final Map<LivingEntity, Double> entities = new HashMap<>();
            LivingEntity lowest = null;
            for (final Entity possibleTarget : this.arrowCast.getNearbyEntities(30.0, 15.0, 30.0)) {
                if (possibleTarget instanceof LivingEntity) {
                    if (possibleTarget == this.firer) {
                        continue;
                    }
                    if (possibleTarget instanceof Arrow) {
                        continue;
                    }
                    if (possibleTarget instanceof ArmorStand) {
                        continue;
                    }

                    if (possibleTarget instanceof Player) {
                        final Player player = (Player) possibleTarget;
                        if (this.plugin.getPvpTimer().getPvpTimer((Player) possibleTarget) > 0) {
                            continue;
                        }
                        if (this.firer instanceof Player) {
                            if (!((Player) this.firer).canSee(player)) {
                                continue;
                            }
                        }
                        if (player.getGameMode() == GameMode.CREATIVE) {
                            continue;
                        }
                        if (player.getGameMode() == GameMode.SPECTATOR) {
                            continue;
                        }
                    }
                    if (possibleTarget.isDead()) {
                        continue;
                    }
                    if (this.arrowCast.getLocation().distance(possibleTarget.getLocation()) > 40.0) {
                        continue;
                    }

                    final LivingEntity livingEntity = (LivingEntity) possibleTarget;
                    entities.put(livingEntity, Math.abs(livingEntity.getLocation().distance(this.arrowCast.getLocation())));
                }
            }
            for (final LivingEntity possibleLowest : entities.keySet()) {
                if (lowest == null || entities.get(possibleLowest) <= entities.get(lowest)) {
                    lowest = possibleLowest;
                }
            }
            this.target = lowest;
        }
    }
}
