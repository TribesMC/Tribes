package me.rey.core.classes.abilities.knight.passive_b;

import me.rey.core.Warriors;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.classes.abilities.IDamageTrigger;
import me.rey.core.events.customevents.combat.DamageEvent;
import me.rey.core.events.customevents.combat.DamagedByEntityEvent;
import me.rey.core.players.User;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Revenge extends Ability implements IDamageTrigger.IPlayerDamagedByEntity, IDamageTrigger.IPlayerDamagedEntity {

    private static final HashMap<Player, BukkitTask> toDealDMG = new HashMap<>();
    private static final Set<Player> onCooldown = new HashSet<>();
    private final double cooldownSecs = 5D;

    public Revenge() {
        super(344, "Revenge", ClassType.IRON, AbilityType.PASSIVE_B, 1, 3, 0.00, Arrays.asList(
                "After taking damage from an enemy,",
                "your next melee attack will deal <variable>1.5+(0.5*l)</variable>",
                "extra damage if you hit within <variable>1.5+(0.5*l)</variable> Seconds."
        ));
        this.setIgnoresCooldown(true);
    }

    @Override
    protected boolean execute(final User u, final Player p, final int level, final Object... conditions) {

        if (conditions[0] instanceof DamageEvent) {

            if (!toDealDMG.containsKey(p)) {
                return false;
            }

            final DamageEvent e = (DamageEvent) conditions[0];
            final double extraDamage = 1.5 + 0.5 * level;
            e.addMod(extraDamage);

            toDealDMG.remove(p);
            onCooldown.add(p);
            new BukkitRunnable() {
                @Override
                public void run() {
                    onCooldown.remove(p);
                }
            }.runTaskLater(Warriors.getInstance().getPlugin(), (int) Math.round(20 * this.cooldownSecs));

            return true;
        }

        if (conditions[0] instanceof DamagedByEntityEvent) {
            final DamagedByEntityEvent e = (DamagedByEntityEvent) conditions[0];
            if (!(e.getDamager() instanceof Player) || onCooldown.contains(e.getDamagee())) {
                return false;
            }

            final BukkitTask task = new BukkitRunnable() {
                @Override
                public void run() {
                    toDealDMG.remove(e.getDamagee());
                }
            }.runTaskLater(Warriors.getInstance().getPlugin(), (int) Math.round(20 * (1.5D + (0.5 * level))));

            if (toDealDMG.containsKey(e.getDamagee())) {
                toDealDMG.get(e.getDamagee()).cancel();
                toDealDMG.replace(e.getDamagee(), task);
            } else {
                toDealDMG.put(e.getDamagee(), task);
            }

            return false;
        }

        return false;
    }

}
