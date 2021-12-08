package me.rey.core.classes.conditions;

import me.rey.core.Warriors;
import me.rey.core.classes.ClassCondition;
import me.rey.core.classes.ClassType;
import me.rey.core.players.User;
import org.bukkit.Effect;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ArcaneRepair extends ClassCondition {

    private final Set<UUID> inCombat;
    private final int delay = 8;
    private final int regenInterval = 5;
    private final int maxHearts = 3;
    private final double heartsPerRegen = 1;

    public ArcaneRepair() {
        super(ClassType.GOLD, "Arcane Repair", Arrays.asList(
                "After being hit by an enemy you regain",
                "the lost health up to 3 hearts over time."
        ));

        this.inCombat = new HashSet<>();
    }

    @Override
    protected void execute(final User u, final Player p) {
        if (u.isInCombat()) {
            this.inCombat.add(u.getUniqueId());
        } else if (!u.isInCombat() && this.inCombat.contains(u.getUniqueId())) {
            this.inCombat.remove(u.getUniqueId());

            final double timerInterval = 0.1;
            new BukkitRunnable() {

                double seconds = 0;
                int regened = 0;

                @Override
                public void run() {
                    if (u.isInCombat() || this.regened == ArcaneRepair.this.maxHearts || p.getHealth() == p.getMaxHealth()) {
                        this.cancel();
                        return;
                    }

                    this.seconds = Double.parseDouble(String.format("%.1f", this.seconds));

                    if ((double) ArcaneRepair.this.delay <= (double) this.seconds) {
                        for (int i = 0; i < (ArcaneRepair.this.maxHearts * ArcaneRepair.this.regenInterval); i += ArcaneRepair.this.regenInterval) {
                            if (this.seconds - ArcaneRepair.this.delay != (double) i) {
                                continue;
                            }
                            this.regened++;

                            if (!p.isDead()) {
                                p.setHealth(Math.min(20, p.getHealth() + (ArcaneRepair.this.heartsPerRegen * 2)));
                                p.getWorld().spigot().playEffect(p.getEyeLocation(), Effect.HEART, 0, 0, 0F, 0F, 0.5F, 0F, 1, 10);
                            }
                        }
                    }

                    this.seconds = this.seconds + timerInterval;
                }

            }.runTaskTimer(Warriors.getInstance().getPlugin(), 0, (int) (timerInterval * 20));

        }

    }

}
