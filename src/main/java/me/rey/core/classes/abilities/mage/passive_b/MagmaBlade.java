package me.rey.core.classes.abilities.mage.passive_b;

import me.rey.core.Warriors;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.classes.abilities.IDamageTrigger.IPlayerDamagedEntity;
import me.rey.core.events.customevents.combat.DamageEvent;
import me.rey.core.players.User;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

public class MagmaBlade extends Ability implements IPlayerDamagedEntity {

    private final ArrayList<UUID> cooldowns;
    private final double igniteSeconds = 2;
    private final double damageToFire = 1;

    public MagmaBlade() {
        super(241, "Magma Blade", ClassType.GOLD, AbilityType.PASSIVE_B, 1, 3, 0.00, Arrays.asList(
                "Your sword deals an additional",
                "<variable>1</variable> damage to burning opponents,",
                "but also extinguishes them.", "",
                "When the opponent is not in fire,",
                "you ignite them for <variable>1</variable> second with",
                "a <variable>5.5-(0.5*l)</variable> (-0.5) second cooldown."
        ));

        this.cooldowns = new ArrayList<>();
        this.setIgnoresCooldown(true);
        this.setInLiquid(true);
    }

    @Override
    protected boolean execute(final User u, final Player p, final int level, final Object... conditions) {
        final DamageEvent e = (DamageEvent) conditions[0];

        if (e.getDamagee().getFireTicks() <= 0 && !this.cooldowns.contains(p.getUniqueId())) {
            e.getDamagee().setFireTicks((int) (this.igniteSeconds * 20));
            this.cooldowns.add(p.getUniqueId());

            final double cooldownToRemove = 5.5 - (0.5 * level);

            new BukkitRunnable() {
                @Override
                public void run() {
                    MagmaBlade.this.cooldowns.remove(p.getUniqueId());
                }
            }.runTaskLater(Warriors.getInstance().getPlugin(), (int) (cooldownToRemove * 20));
            return true;
        }

        if (e.getDamagee().getFireTicks() > 0) {
            e.addMod(this.damageToFire);
        }
        return true;
	}


}
