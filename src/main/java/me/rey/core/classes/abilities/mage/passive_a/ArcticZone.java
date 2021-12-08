package me.rey.core.classes.abilities.mage.passive_a;

import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.classes.abilities.IConstant;
import me.rey.core.classes.abilities.IDamageTrigger;
import me.rey.core.classes.abilities.mage.sword.Blaze;
import me.rey.core.energy.EnergyHandler;
import me.rey.core.enums.State;
import me.rey.core.events.customevents.ability.AbilityInteractEvent;
import me.rey.core.events.customevents.update.UpdateEvent;
import me.rey.core.players.User;
import me.rey.core.utils.UtilLoc;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class ArcticZone extends Ability implements IConstant, IConstant.ITogglable, IDamageTrigger.IPlayerDamagedEntity {

    private static final PotionEffect slownessEffect = new PotionEffect(PotionEffectType.SLOW, 2, 0),
            selfResistance = new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 2, 1),
            teamResistance = new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 2, 0);
    private static final double energyPerSecond = 12;

    private EnergyHandler handler = new EnergyHandler();
    public Map<UUID, Set<FrozenBlockWrapper>> frozenBlocks = new HashMap<>();

    public ArcticZone() {
        super(232, "Arctic Zone", ClassType.GOLD, AbilityType.PASSIVE_A, 1, 3, 0.00, Arrays.asList(
                "Freeze the ground by radiating an arctic circle",
                "",
                "You receive Resistance II and give your allies",
                "Resistance I when in zone.",
                "",
                "Enemies attacked in zone receive Slowness I until",
                "they are outside the circle.",
                "",
                "The zone has a radius of <variable>2+l</variable> blocks.",
                "",
                "Energy: <variable>12-l</variable> Per Second."
        ));
        this.setEnergyCost(energyPerSecond / 20F, 1F / 20F);
        this.setIgnoresCooldown(true);
        this.setInLiquid(true);
    }

    @Override
    protected boolean execute(User u, Player p, int level, Object... conditions) {
        Object arg = conditions[0];
        double radius = level + 2;

        if(arg instanceof UpdateEvent) {

            // Consuming energy
            if(!this.getEnabledPlayers().contains(p.getUniqueId()))
                return false;

            handler.togglePauseEnergy(State.ENABLED, p.getUniqueId());

            for(Location loc : UtilLoc.circleLocations(p.getLocation(), radius, 12)) {
                loc.getWorld().spigot().playEffect(loc, Effect.SNOW_SHOVEL, 0, 0, 0, 0, 0, 1, 0, 30);
            }

            /* Giving user Resistance II */
            p.getWorld().playSound(p.getLocation(), Sound.AMBIENCE_RAIN, 0.2f, 0.5f);
            p.addPotionEffect(selfResistance);

            /*
             * Apply slowness affect to all entities in radius
             * and give team resistance to team members
             */
            final Set<Entity> entitiesInRadius = UtilLoc.getEntitiesInCircle(p.getLocation(), radius);
            for(Entity ent : entitiesInRadius) {
                if(ent instanceof ArmorStand) continue;
                if(ent instanceof LivingEntity) {
                    LivingEntity livingEnt = (LivingEntity) ent;

                    if(livingEnt instanceof Player) {
                        if(livingEnt.equals(p))
                            continue;

                        if(u.getTeam().contains(livingEnt)) {
                            livingEnt.addPotionEffect(teamResistance);
                            continue;
                        }
                    }

                    AbilityInteractEvent aie = new AbilityInteractEvent((LivingEntity) ent, p, ArcticZone.this, level);
                    Bukkit.getPluginManager().callEvent(aie);
                    if (!aie.isCancelled()) {
                        livingEnt.addPotionEffect(slownessEffect);
                    }
                }
            }

            /*
             * Freezing the water
             * blocksInRadius is increased by 1 block from the radius of the particles
             * FrozenBlockWrapper allows identification of flowing water blocks
             */
            final Location iceLocation = p.getLocation().clone().add(0D, -1.0D, 0D);
            final Set<Block> blocksInRadius = UtilLoc.getBlocksInRadius(iceLocation, radius + 1.0D, 1).keySet();
            final Set<FrozenBlockWrapper> frozenBlocks = this.frozenBlocks.getOrDefault(p.getUniqueId(), new HashSet<>());

            Iterator<FrozenBlockWrapper> iterator = frozenBlocks.iterator();
            while(iterator.hasNext()) {
                FrozenBlockWrapper fb = iterator.next();
                if(!blocksInRadius.contains(fb.block) && fb.canDecay()) {
                    iterator.remove();
                    fb.block.setType(fb.source ? Material.STATIONARY_WATER : Material.AIR);
                }
            }

            // Checks if the player is submerged in water
            // If not, create new ice blocks
            boolean isPlayerInWater = isSubmerged(p);
            if(!isPlayerInWater) {
                for (Block b : blocksInRadius) {
                    if (isWater(b.getType())) {
                        frozenBlocks.add(new FrozenBlockWrapper(b, isSource(b)));
                        b.setType(Material.ICE);
                    }
                }
            }

            this.frozenBlocks.put(p.getUniqueId(), frozenBlocks);
        }
        return true;
    }

    @Override
    public boolean off(Player p) {
        handler.togglePauseEnergy(State.DISABLED, p.getUniqueId());

        final Set<FrozenBlockWrapper> frozenBlocks = this.frozenBlocks.getOrDefault(p.getUniqueId(), new HashSet<>());
        for(FrozenBlockWrapper fb : frozenBlocks) {
            fb.block.setType(fb.source ? Material.STATIONARY_WATER : Material.AIR);
        }

        this.frozenBlocks.remove(p.getUniqueId());
        return true;
    }

    @Override
    public boolean on(Player p) {
        handler.togglePauseEnergy(State.ENABLED, p.getUniqueId());
        return true;
    }

    /**
     * Checks if the player is fully submerged in water
     *
     * @param player the player to check
     * @return {@code true} if the player is submerged
     */
    private boolean isSubmerged(Player player) {
        return isWater(player.getWorld().getBlockAt(player.getLocation().clone().add(0, 1, 0)).getType());
    }

    /**
     * Checks if the block is a source water
     *
     * @param b the block to check
     * @return {@code true} if it is a source water block
     */
    @SuppressWarnings("deprecation")
    private boolean isSource(Block b) {
        return (b.getType() == Material.STATIONARY_WATER || b.getType() == Material.WATER) && b.getData() == 0;
    }

    private boolean isWater(Material mat) {
        return (mat == Material.STATIONARY_WATER || mat == Material.WATER);
    }

    static class FrozenBlockWrapper {
        final Block block;
        final boolean source;
        final long initialTime;

        public FrozenBlockWrapper(Block block, boolean source) {
            this.block = block;
            this.source = source;
            this.initialTime = System.currentTimeMillis();
        }

        boolean canDecay() {
            return System.currentTimeMillis() - initialTime > 2500; // after 2.5 seconds
        }
    }
}
