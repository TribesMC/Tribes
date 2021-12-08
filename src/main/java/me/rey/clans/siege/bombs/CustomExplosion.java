package me.rey.clans.siege.bombs;

import me.rey.clans.Tribes;
import me.rey.clans.clans.Clan;
import me.rey.clans.clans.ClansPlayer;
import me.rey.clans.events.ClaimProtection;
import me.rey.clans.events.clans.PlayerEditClaimEvent;
import me.rey.clans.events.clans.PlayerEditClaimEvent.ClaimPermission;
import me.rey.clans.events.clans.PlayerEditClaimEvent.EditAction;
import me.rey.clans.items.Placeable;
import me.rey.clans.siege.Siege;
import me.rey.clans.siege.SiegeTriggerEvent;
import me.rey.core.effects.ParticleEffect;
import me.rey.core.effects.SoundEffect;
import me.rey.core.utils.UtilEnt;
import me.rey.core.utils.UtilLoc;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import java.util.*;

public class CustomExplosion implements Listener {

    public static final double C4_RADIUS = 4D;
    public static final double BOMB_RADIUS = 5D;
    public static HashMap<UUID, HashMap<UUID, Explodable>> ACTIVE_BOMBS = new HashMap<>();
    public static Bomb BOMB;
    public static Bomb2 C4;
    private final Set<Material> BYPASS_BLOCKS = new HashSet<>(Arrays.asList(
            Material.BEACON,
            Material.CHEST,
            Material.TRAPPED_CHEST,
            Material.FURNACE,
            Material.DISPENSER
    ));

    private final String cause;
    private final double damage;
    private final double radius;
    private final boolean instant;

    public CustomExplosion(final String cause, final double damage, final boolean instant, final double radius) {
        this.cause = cause;
        this.damage = damage;
        this.radius = radius;
        this.instant = instant;

        Bukkit.getServer().getPluginManager().registerEvents(this, Tribes.getInstance().getPlugin());
    }

    public static HashMap<UUID, Explodable> getActiveBombs(final Clan clan) {
        return ACTIVE_BOMBS.containsKey(clan.getUniqueId()) ? ACTIVE_BOMBS.get(clan.getUniqueId()) : new HashMap<>();
    }

    public void blow(final Clan clan, final Player placer, final Location location, final Block[] toRemove) {

        for (final Block b : toRemove) {
            b.setType(Material.AIR);
        }

        /*
         * EXPLOSION
         */
        final HashMap<Block, Double> blocks = UtilLoc.getBlocksInRadius(location, this.radius);
        for (final Block b : blocks.keySet()) {

            if (Bomb2.unbreakable.contains(b) || this.BYPASS_BLOCKS.contains(b.getType()) || Bomb.unbreakable.contains(b)) {
                continue;
            }

            if (b.getType().equals(Material.SMOOTH_BRICK) && !this.instant && b.getData() != (byte) 2) {
                b.setData((byte) 2);
            } else {
                b.setType(Material.AIR);
            }
        }

        final Iterator<Entity> ents = UtilLoc.getEntitiesInCircle(location, this.radius).iterator();
        while (ents.hasNext()) {

            final Entity next = ents.next();

            if (!(next instanceof LivingEntity)) {
                continue;
            }

            final LivingEntity ent = (LivingEntity) next;

            this.kb(ent, location);
            UtilEnt.damage(this.damage, this.cause, ent, placer);
        }

        new ParticleEffect(Effect.EXPLOSION_LARGE).play(location);
        new SoundEffect(Sound.EXPLODE, 2F).play(location);

        final HashMap<UUID, Explodable> active = CustomExplosion.getActiveBombs(clan);
        active.remove(Explodable.isInSiegerTerritory(clan, placer, location).getUniqueId());
        if (!active.isEmpty()) {
            CustomExplosion.ACTIVE_BOMBS.put(clan.getUniqueId(), active);
        } else {
            CustomExplosion.ACTIVE_BOMBS.remove(clan.getUniqueId());
        }
    }

    private void kb(final LivingEntity ent, final Location from) {
        final Vector vec = ent.getLocation().toVector().subtract(from.toVector());
        vec.multiply(0.2F);

        ent.setVelocity(vec.normalize());
    }

    @EventHandler
    public void onForm(final EntityChangeBlockEvent e) {
        if (e.getEntity().getType().equals(EntityType.FALLING_BLOCK)) {
            e.getBlock().setType(Material.AIR);
        }
    }

    @EventHandler
    public void onExplode(final BlockExplodeEvent e) {
        if (Bomb2.unbreakable.contains(e.getBlock()) || Bomb.unbreakable.contains(e.getBlock())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPrime(final ExplosionPrimeEvent e) {
        if (e.getEntity() instanceof TNTPrimed) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onExplosion(final EntityExplodeEvent e) {
        e.setYield(0F);
        e.setCancelled(true);
    }

    @EventHandler
    public void onInteract(final PlayerInteractEvent e) {
        if (e.getClickedBlock() != null && e.getClickedBlock().getType().equals(Material.TNT) && e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            e.setCancelled(true);
        }
    }

    public static abstract class Explodable extends Placeable {

        private Clan onTerritory = null, whoPlaced = null;

        public static Clan isInSiegerTerritory(final Clan sieger, final Player p, final Location location) {

            final Clan clan = Tribes.getInstance().getClanFromTerritory(location.getChunk());
            if (clan == null && ClaimProtection.isNearOtherClaim(p, location.getBlock()) == null) {
                return null;
            }

            for (final Siege siege : sieger.getClansSiegedBySelf()) {
                if (siege.getClanSieged().compare(clan)) {
                    return siege.getClanSieged();
                }
            }

            return ClaimProtection.isNearOtherClaim(p, location.getBlock());
        }

        public Clan getClanOnTerritory() {
            return this.onTerritory;
        }

        public Clan getClanWhoPlaced() {
            return this.whoPlaced;
        }

        public abstract void start(Player player, Location location);

        @Override
        protected void place(final Player player, final Location location) {
            final ClansPlayer cp = new ClansPlayer(player);
            if (!cp.hasClan() || !cp.getClan().isSiegingOther()) {
                cp.sendMessageWithPrefix("Siege", "You aren't sieging anyone!");
                return;
            }

            if (isInSiegerTerritory(cp.getClan(), player, location) == null && ClaimProtection.isNearOtherClaim(player, location.getBlock()) == null) {
                cp.sendMessageWithPrefix("Siege", "You can only place TNT in territories from clans you're sieging!");
                return;
            }

            this.onTerritory = Tribes.getInstance().getClanFromTerritory(location.getChunk());
            this.whoPlaced = cp.getClan();

            this.start(player, location);
            final HashMap<UUID, Explodable> active = CustomExplosion.getActiveBombs(cp.getClan());
            active.put(isInSiegerTerritory(cp.getClan(), player, location).getUniqueId(), this);
            CustomExplosion.ACTIVE_BOMBS.put(cp.getClan().getUniqueId(), active);

        }

        @EventHandler
        public void onEditClaim(final PlayerEditClaimEvent e) {
            if (!e.getAction().equals(EditAction.PLACE)) {
                return;
            }
            if (!SiegeTriggerEvent.isInSiegerTerritory(e.getPlayer(), e.getBlockToReplace())) {
                return;
            }

            final String name = e.getItemInHand() != null && e.getItemInHand().hasItemMeta() && e.getItemInHand().getItemMeta().hasDisplayName()
                    ? e.getItemInHand().getItemMeta().getDisplayName()
                    : "N/A";
            if (!this.getItem().getName().equals(name)) {
                return;
            }

            e.setPermission(ClaimPermission.ALLOW);
        }


    }

}
