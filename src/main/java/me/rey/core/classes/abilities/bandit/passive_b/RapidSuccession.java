package me.rey.core.classes.abilities.bandit.passive_b;

import me.rey.core.Warriors;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.classes.abilities.IDamageTrigger;
import me.rey.core.events.customevents.combat.DamageEvent;
import me.rey.core.players.User;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

public class RapidSuccession extends Ability implements IDamageTrigger.IPlayerDamagedEntity {

    private static final HashMap<Player, String> hits = new HashMap<>();
    private final double INCREASE = 1;
    private final double EXPIRE = 3.00;

    public RapidSuccession() {
        super(142, "Rapid Succession", ClassType.BLACK, AbilityType.PASSIVE_B, 1, 2, 0.00, Arrays.asList(
                "Your damage gets boosted by",
                "a factor of 1 every time you",
                "hit your enemy.", "",
                "It does not stack over enemies.",
                "and has a maximum of <variable>l*1</variable> (+1) damage.",
                "The boost expires after 3 seconds",
                "out of combat."
        ));

        this.setIgnoresCooldown(true);
    }

    @Override
    protected boolean execute(final User u, final Player p, final int level, final Object... conditions) {
        final DamageEvent e = (DamageEvent) conditions[0];
        final UUID found = e.getDamagee().getUniqueId();

        if (hits.containsKey(p)) {
            final String[] KeyAndValue = hits.get(p).split(";");
            final UUID stacked = UUID.fromString(KeyAndValue[0]);
            final double dmg = Double.parseDouble(KeyAndValue[1]);

            if (found.equals(stacked)) {
                e.addMod(dmg);
                this.addToHits(p, found, Math.min(dmg + 1, level));
            } else {
                this.addToHits(p, found, this.INCREASE);
            }

        } else {
            this.addToHits(p, found, this.INCREASE);
        }

        return true;
    }

    private void addToHits(final Player p, final UUID uuid, final double dmg) {
        final String s = uuid.toString() + ";" + dmg;
        final BukkitTask remove = new BukkitRunnable() {

            @Override
            public void run() {
                if (hits.containsKey(p)) {
                    hits.remove(p, s + ";" + this.getTaskId());
                }
            }
        }.runTaskLater(Warriors.getInstance().getPlugin(), (int) (20 * this.EXPIRE));
        final String copy = s + ";" + remove.getTaskId();

        if (hits.containsKey(p)) {
            hits.replace(p, copy);
        } else {
            hits.put(p, copy);
        }
    }

}
