package me.rey.core.classes.abilities.shaman.sword;

import me.rey.core.Warriors;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.effects.ParticleEffect;
import me.rey.core.effects.SoundEffect;
import me.rey.core.packets.ActionBar;
import me.rey.core.players.User;
import me.rey.core.utils.*;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftFirework;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

public class StaticLazer extends Ability {

    private static HashMap<Player, StaticLazerObject> activeLazers = new HashMap<>();

    public StaticLazer() {
        super(502, "Static Lazer", ClassType.GREEN, AbilityType.SWORD, 1, 5, 0.00, Arrays.asList(
                "Hold Block to charge static electricity.",
                "Release Block to fire static lazer.",
                "",
                "Charges <variable>24+8*l</variable> (+8) % per Second",
                "",
                "Deals <variable>6+l</variable> (+1) damage and travels up to",
                "<variable>15+5*l</variable> (+5) blocks.",
                "",
                "Energy: 24 per Second",
                "Recharge: <variable>11-0.5*l</variable> (-0.5) Seconds"
        ));

        this.setSound(null, 0f);
    }

    @Override
    protected boolean execute(User u, Player p, int level, Object... conditions) {

        float cooldown = 11F - 0.5F * level;

        if (new User(p).getEnergy() >= cooldown) {
            if (!activeLazers.containsKey(p)) {
                activeLazers.put(p, new StaticLazerObject(this, p, level, cooldown));
            }
        } else {
            if (activeLazers.containsKey(p)) {
                activeLazers.get(p).fire();
            }

            activeLazers.remove(p);
            return false;
        }

        setCooldownCanceled(true);
        return true;
    }

    static class StaticLazerObject {

        private final Player owner;
        private final Ability ability;
        private final float chargeIncrement, damage, maxDistance;
        private float chargePercentage, ticks;

        private boolean isDestroyed = false;

        protected StaticLazerObject(Ability ability, Player owner, float level, float cooldown) {
            this.chargeIncrement = 24 + (8 * level);
            this.damage = 6 + level;
            this.maxDistance = 15 + 5 * level;
            this.owner = owner;
            this.ability = ability;

            new BukkitRunnable() {
                @Override
                public void run() {
                    new User(owner).consumeEnergy(24 / 20F);
                    tick();

                    if (isDestroyed) {
                        this.cancel();
                        return;
                    }

                    boolean hasEnergy = new User(owner).getEnergy() >= 24;
                    if (!owner.isBlocking() || !hasEnergy) {

                        if (!hasEnergy) {
                            ability.sendEnergyError(owner);
                        }

                        activeLazers.remove(owner);
                        fire();
                        ability.sendUsedMessageToPlayer(owner, ability.getName());
                        ability.setCooldown(cooldown);
                        ability.applyCooldown(owner);
                    }
                }
            }.runTaskTimer(Warriors.getInstance().getPlugin(), 0L, 1L);
        }

        public void fire() {
            isDestroyed = true;

            final Location fireLocation = owner.getEyeLocation().clone().add(0, -0.5, 0);
            Location explodeLocation = fireLocation.clone();
            for (double i = 0; i < maxDistance; i += 0.2) {
                Location toSet = explodeLocation.clone().add(owner.getLocation().clone().getDirection().normalize().multiply(i));

                final Collection<Entity> nearby = toSet.getWorld().getNearbyEntities(toSet, 0.5F, 0.5F, 0.5F);
                nearby.remove(owner);
                if (UtilBlock.solid(toSet.getBlock()) || !nearby.isEmpty()) {
                    break;
                }

                explodeLocation = toSet;
            }

            new SoundEffect(Sound.ZOMBIE_REMEDY, 1.5F).setVolume(1F).play(fireLocation);
            makeParticlesBetween(fireLocation, explodeLocation);

            Firework firework = explodeLocation.getWorld().spawn(explodeLocation, Firework.class);
            final FireworkEffect fe = FireworkEffect.builder().flicker(false).withColor(Color.WHITE).with(FireworkEffect.Type.BURST).trail(false).build();
            FireworkMeta data = firework.getFireworkMeta();
            data.clearEffects();
            data.setPower(1);
            data.addEffect(fe);
            firework.setFireworkMeta(data);
            ((CraftFirework) firework).getHandle().expectedLifespan = 1;

            for (Entity entity : UtilLoc.getEntitiesInCircle(explodeLocation, 6)) {
                if (!(entity instanceof LivingEntity) || entity == owner) {
                    continue;
                }
                final LivingEntity le = (LivingEntity) entity;
                double distance = (le.getLocation().distance(explodeLocation));

                UtilEnt.damage(damage * chargePercentage, ability.getName(), le, owner);
                UtilVelocity.velocity(
                        le,
                        owner,
                        le.getLocation().clone().subtract(explodeLocation.clone()).toVector(),
                        0.5 + (distance * (0.1)),
                        1.0,
                        false,
                        0.1,
                        0.05,
                        0.2,
                        true
                );
            }
        }

        private void tick() {
            if (ticks % 2 == 0) {
                owner.playSound(owner.getLocation(), Sound.CREEPER_HISS, 1F, 0.25F + 2.5F * chargePercentage);
            }

            new ActionBar(
                    new ChargingBar(ChargingBar.ACTIONBAR_BARS, chargePercentage * 100).getBarString()
                            .replace(ChatColor.GREEN.toString(), ChatColor.YELLOW.toString())
                            .replace(ChatColor.RED.toString(), ChatColor.WHITE.toString()))
                    .send(owner);

            chargePercentage = Math.min(1F, chargePercentage + (chargeIncrement / 100F) / 20F);
            ticks++;
        }
        private void makeParticlesBetween(Location init, Location loc) {
            Vector pvector = Utils.getDirectionBetweenLocations(init.clone(), loc.clone());
            for(double i = 1; i <= init.distance(loc); i += 0.2) {
                pvector.multiply(i);
                init.add(pvector);
                Location toSpawn = init.clone();
                toSpawn.setY(toSpawn.getY() + 0.5);
                new ParticleEffect.ColoredParticle(255, 255, 0).play(toSpawn);
                init.subtract(pvector);
                pvector.normalize();
            }
        }
    }

}
