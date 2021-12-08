package me.rey.core.classes.abilities.mage.axe;

import me.rey.core.Warriors;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.effects.repo.Shock;
import me.rey.core.events.customevents.ability.AbilityInteractEvent;
import me.rey.core.gui.Item;
import me.rey.core.items.Throwable;
import me.rey.core.players.User;
import me.rey.core.utils.UtilEnt;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

public class LightningOrb extends Ability {

    final double secondsToStrike = 1.7;

    /* Throwing the item */
    final double throwBaseV = 0.5;
    final double throwChargeV = 0.25;
    final double throwLevelMultiplier = 0.1;

    public LightningOrb() {
        super(213, "Lightning Orb", ClassType.GOLD, AbilityType.AXE, 1, 3, 0.00, Arrays.asList(
                "Throw an orb that strikes lightning",
                "on all nearby players within a radius",
                "of <variable>3.5+(0.5*l)</variable> blocks.",
                "",
                "Applies Slowness 2 for 4 seconds to",
                "players and deals <variable>5+l</variable> damage.",
                "",
                "Energy: <variable>56-(l*3)</variable> (-3)",
                "Recharge: <variable>12-l</variable>(-1) Seconds"
        ));
        this.setEnergyCost(56, 3);
    }

    @Override
    protected boolean execute(final User u, final Player p, final int level, final Object... conditions) {

        final Throwable lorb = new Throwable(new Item(Material.EYE_OF_ENDER), false);
        final Vector vec = (p.getLocation().getDirection().normalize()
                .multiply(this.throwBaseV + (this.throwChargeV) * (1 + level * this.throwLevelMultiplier))
                .setY(p.getLocation().getDirection().getY() + 0.2));
        lorb.fire(p.getEyeLocation(), vec);

        new BukkitRunnable() {

            int ticks = 0;

            @Override
            public void run() {
                boolean wasDirect = Throwable.checkForEntityCollision(lorb, 0.1, 0.1, 0.1) != null;

                if (wasDirect) {
                    final Set<LivingEntity> ents = Throwable.checkForEntityCollision(lorb, 0.1, 0.1, 0.1);
                    if (ents.iterator().next().equals(p)) {
                        wasDirect = !wasDirect;
                    }
                }

                if (wasDirect || this.ticks >= (LightningOrb.this.secondsToStrike * 20D)) {
                    LightningOrb.this.strikeNearby(p, level, lorb.getEntityitem());

                    lorb.destroy();
                    this.cancel();
                    return;
                }

                this.ticks++;
            }

        }.runTaskTimer(Warriors.getInstance().getPlugin(), 0, 2);

        this.setCooldown(12 - level);
        return true;
    }

    private void strikeNearby(final Player responsible, final int level, final org.bukkit.entity.Item item) {
        final PotionEffect slow = new PotionEffect(PotionEffectType.SLOW, 2 * 4, 1, false, false);
        final double radius = 3.5 + (0.5 * level);

        final Iterator<Entity> nearby = item.getNearbyEntities(radius, radius, radius).iterator();
        while (nearby.hasNext()) {
            final Entity found = nearby.next();
            if (!(found instanceof LivingEntity)) {
                continue;
            }

            final LivingEntity ent = (LivingEntity) found;

            final User user = new User(responsible);
            if (!user.getTeam().contains(ent)) {
                final AbilityInteractEvent aie = new AbilityInteractEvent(ent, responsible, LightningOrb.this, level);
                Bukkit.getPluginManager().callEvent(aie);
                if (!aie.isCancelled()) {
                    ent.addPotionEffect(slow);
                    new Shock().apply(ent, 2.0D);

                    ent.getWorld().strikeLightningEffect(ent.getLocation());
                    UtilEnt.damage(5 + level, this.getName(), ent, responsible);
                }
            }

        }
    }

}
