package me.rey.clans.events;

import me.rey.clans.Tribes;
import me.rey.clans.clans.ClansPlayer;
import me.rey.clans.events.clans.PlayerTerritoryChangeEvent;
import me.rey.clans.siege.Siege;
import me.rey.core.events.customevents.ability.AbilityUseEvent;
import me.rey.core.players.User;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.spigotmc.event.entity.EntityMountEvent;

import java.util.HashMap;
import java.util.UUID;

public class ServerEvents implements Listener {

    final float foodLossMultiplier = 0.1F;
    HashMap<UUID, Double> foodLevel = new HashMap<>();

    @EventHandler
    public void onFoodLevelChange(final FoodLevelChangeEvent e) {
        if (!(e.getEntity() instanceof Player)) {
            return;
        }

        final Player p = (Player) e.getEntity();
        if (!this.foodLevel.containsKey(p.getUniqueId())) {
            this.foodLevel.put(p.getUniqueId(), (double) e.getFoodLevel());
            return;
        }

        final double foodBefore = this.foodLevel.get(p.getUniqueId());
        final int foodNow = e.getFoodLevel();
        if (foodNow > foodBefore) {
            return;
        }

        e.setCancelled(true);
        final double newFood = foodBefore - this.foodLossMultiplier;
        this.foodLevel.replace(p.getUniqueId(), newFood);
        e.setFoodLevel((int) Math.ceil(newFood));
    }

    @EventHandler
    public void onInteract(final PlayerInteractEvent e) {
        final Player p = e.getPlayer();

        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (e.getItem() != null && e.getItem().getType() == Material.BOAT) {
                new User(e.getPlayer()).sendMessageWithPrefix("Boat", "You cannot use boats!");

                p.setItemInHand(null);
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onTerritoryChange(final PlayerTerritoryChangeEvent e) {
        final ClansPlayer cp = new ClansPlayer(e.getPlayer());

        final boolean allowFlight = e.getPlayer().getAllowFlight();
        final boolean flying = e.getPlayer().isFlying();
        if (e.getTo() != null && !e.getTo().compare(cp.getClan()) && (e.getPlayer().getGameMode().equals(GameMode.SURVIVAL) || e.getPlayer().getGameMode().equals(GameMode.ADVENTURE))) {
            if (cp.hasClan() && cp.getClan().isSiegingOther()) {
                for (final Siege siege : cp.getClan().getClansSiegedBySelf()) {
                    if (siege.getClanSieged().compare(e.getTo())) {
                        return;
                    }
                }
            }

            e.getPlayer().setGameMode(GameMode.ADVENTURE);
        } else if (e.getPlayer().getGameMode().equals(GameMode.ADVENTURE)) {
            e.getPlayer().setGameMode(GameMode.SURVIVAL);
        }
        e.getPlayer().setAllowFlight(allowFlight);
        e.getPlayer().setFlying(flying);
    }

    @EventHandler
    public void onBowShoot(final EntityShootBowEvent e) {
        if (e.getEntityType() != EntityType.PLAYER) {
            return;
        }

        final Player p = (Player) e.getEntity();

        if (p.isInsideVehicle()) {
            e.setCancelled(true);
            new User(p).sendMessageWithPrefix("Mount", "You cannot shoot while mounted!");
        }
    }

    @EventHandler
    public void onAbilityUseOnMount(final AbilityUseEvent e) {
        if (e.getPlayer().isInsideVehicle()) {
            e.setCancelled(true);
            new User(e.getPlayer()).sendMessageWithPrefix("Mount", "You cannot use abilities while mounted!");
        }
    }

    @EventHandler
    public void onMount(final EntityMountEvent e) {
        if (!(e.getEntity() instanceof Player)) {
            return;
        }

        if (new User((Player) e.getEntity()).isInCombat() && e.getMount().getType() != EntityType.PLAYER) {
            final Player p = (Player) e.getEntity();
            new User(p).sendMessageWithPrefix("Mount", "You cannot mount an entity while in combat!");
            e.setCancelled(true);
        }
    }


    @EventHandler
    public void onRedstoneLightup(final BlockRedstoneEvent e) {
        e.setNewCurrent(0);
    }

    @EventHandler
    private void onBlockChange(final EntityChangeBlockEvent event) {
        if (event.getBlock().getType() != Material.AIR && event.getBlock().getType().isBlock()) {
            return;
        }
        Bukkit.getScheduler().runTask(Tribes.getInstance().getPlugin(), () -> {
            if (event.getBlock().getType() != Material.SAND && event.getBlock().getType() != Material.GRAVEL) {
                return;
            }
            event.getBlock().getWorld().playEffect(event.getBlock().getLocation(), Effect.STEP_SOUND, event.getBlock().getType(), 20);
            event.getBlock().getWorld().getBlockAt(event.getBlock().getLocation()).setType(Material.AIR);
        });
    }
}
