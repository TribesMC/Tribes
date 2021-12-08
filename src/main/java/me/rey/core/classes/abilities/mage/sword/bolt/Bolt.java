package me.rey.core.classes.abilities.mage.sword.bolt;

import me.rey.core.Warriors;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.classes.abilities.IConstant;
import me.rey.core.events.customevents.update.UpdateEvent;
import me.rey.core.gui.Item;
import me.rey.core.packets.ActionBar;
import me.rey.core.players.User;
import me.rey.core.utils.ChargingBar;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class Bolt extends Ability implements IConstant {

    private static final String EMPTY_STACK = "○○", CHARGED_STACK = "●●";
    private static final float energyCostPerTick = 45f / 20f, reductionInCost = 2f / 20f;
    private final HashMap<UUID, BoltProfile> bolts = new HashMap<>();
    private final HashMap<UUID, Integer> stacks = new HashMap<>();
    /*
     * Each player has a long value,
     * which is the amount of ticks until
     * their stack should decrement by 1
     */
    private final HashMap<UUID, Long> stackDecayCooldown = new HashMap<>();
    private final List<UUID> cooldown = new ArrayList<>();

    public Bolt() {
        super(201, "Bolt", ClassType.GOLD, AbilityType.SWORD, 1, 5, 0.0, Arrays.asList(
                "Charge up an electric beam, piercing and dealing ",
                "2 plus 0.5 per stack to all enemies hit by it.",
                "Enemies affected by this bolt are also shocked",
                "for 2 seconds.",
                "",
                "Every time an enemy is hit, a stack is gained up to ",
                "a maximum of 5 stacks.",
                "",
                "When a beam doesn't connect with any target, your",
                "stacks will decay over time and will disappear",
                "upon missing a fired bolt.",
                "",
                "Energy: <variable>45-l*2</variable> (-2) per Second"
        ));

        this.setIgnoresCooldown(true);
        this.setEnergyCost(energyCostPerTick, reductionInCost);
    }

    @Override
    protected boolean execute(final User u, final Player p, final int level, final Object... conditions) {
        this.setEnergyCost(0, 0);

        if ((conditions != null && conditions.length == 1 && conditions[0] != null && conditions[0] instanceof UpdateEvent) || this.cooldown.contains(p.getUniqueId())) {

            // Ensures the maximum amount of stacks a player can have is 5
            final int maxStacks = Math.min(2 + level, 5);

            /*
             * If the player is not currently charging a bolt attack
             * and they have a Sword ToolType in their hand,
             * display the current stack the player has
             */
            if (!this.bolts.containsKey(p.getUniqueId())
                    && this.matchesAbilityTool(this.match(p.getItemInHand() == null ? new Item(Material.AIR).get() : p.getItemInHand()))) {
                this.showStacks(maxStacks, p);
            }

            final long now = System.currentTimeMillis();
            final long stackDecay = this.stackDecayCooldown.getOrDefault(p.getUniqueId(), now);
            final int stacks = this.stacks.getOrDefault(p.getUniqueId(), 0);

            if (now - stackDecay >= 1000L) {
                if (stacks >= 1) {
                    this.stacks.put(p.getUniqueId(), stacks - 1);
                    this.stackDecayCooldown.put(p.getUniqueId(), now);
                } else {
                    this.stackDecayCooldown.remove(p.getUniqueId());
                }
            }

            if (!p.isBlocking()) {
                return false;
            }
        }

        if (this.bolts.containsKey(p.getUniqueId()) || this.cooldown.contains(p.getUniqueId())) {
            return false;
        }

        BoltProfile bolt = this.bolts.get(p.getUniqueId());
        if (bolt == null) {
            bolt = new BoltProfile(p, u, level);
            this.bolts.put(p.getUniqueId(), bolt);
        }

        if (bolt.stopcharge) {
            return false;
        }

        this.cooldown.add(p.getUniqueId());

        final BoltProfile finalBolt = bolt;

        new BukkitRunnable() {
            @Override
            public void run() {
                if (p.isBlocking()) {
                    if (finalBolt.charge < finalBolt.maxcharge && u.getEnergy() > (energyCostPerTick - level * reductionInCost)) {
                        finalBolt.charge++;
                        p.playSound(p.getLocation(), Sound.CREEPER_HISS, 1F, 0.25F + 2.5F * (float) (finalBolt.charge / finalBolt.maxcharge));
                        new ActionBar(new ChargingBar(ChargingBar.ACTIONBAR_BARS, finalBolt.maxcharge - finalBolt.charge + 1, finalBolt.maxcharge).
                                getBarString().replace(ChatColor.GREEN.toString(), ChatColor.YELLOW.toString()).replace(ChatColor.RED.toString(), ChatColor.WHITE.toString())).send(p);

                        u.consumeEnergy(energyCostPerTick - (level * reductionInCost));

                    } else {
                        finalBolt.shoot();

                        Bolt.this.startCooldown(p);

                        Bolt.this.bolts.remove(p.getUniqueId());
                        this.cancel();
                    }
                } else {
                    finalBolt.shoot();

                    Bolt.this.startCooldown(p);

                    Bolt.this.bolts.remove(p.getUniqueId());
                    this.cancel();
                }
            }
        }.runTaskTimer(Warriors.getInstance().getPlugin(), 1L, 1L);

        return false;
    }

    private void startCooldown(final Player p) {
        new BukkitRunnable() {
            @Override
            public void run() {
                p.playSound(p.getLocation(), Sound.NOTE_PLING, 1F, 2F);
                Bolt.this.cooldown.remove(p.getUniqueId());
            }
        }.runTaskLater(Warriors.getInstance().getPlugin(), (long) (0.5 * 20));
    }

    /**
     * Displays the players 'stacks' on their action bar
     *
     * @param maxStacks the max number of stacks the player can have at their current level
     * @param p         the player to display the action bar to
     */
    private void showStacks(final int maxStacks, final Player p) {
        if (p == null || this.stacks == null) {
            return;
        }

        final StringBuilder builder = new StringBuilder();
        final int playerStack = this.stacks.getOrDefault(p.getUniqueId(), 0);

        for (int i = 1; i < maxStacks; i++) {
            builder.append(this.getColorFromStack(i).toString())
                    .append((playerStack >= i) ? CHARGED_STACK : EMPTY_STACK);
        }

        builder.append(this.getColorFromStack(maxStacks - 1).toString())
                .append((playerStack >= maxStacks - 1) ? CHARGED_STACK : EMPTY_STACK);

        for (int i = maxStacks - 1; i > 0; i--) {
            builder.append(this.getColorFromStack(i).toString())
                    .append((playerStack >= i) ? CHARGED_STACK : EMPTY_STACK);
        }

        new ActionBar(builder.toString().trim()).send(p);
    }

    private ChatColor getColorFromStack(final int amount) {
        switch (amount) {
            case 4:
                return ChatColor.DARK_RED;
            case 3:
                return ChatColor.RED;
            case 2:
                return ChatColor.GOLD;
            case 1:
                return ChatColor.YELLOW;
            default:
                return ChatColor.GRAY;
        }
    }

    class BoltProfile {

        final double hitbox = 0.7;
        final double maxcharge = 1.2 * 20;
        final double particledensity = 3;
        final double maxDistanceUntilCurve = 0.5;
        final double maxTravelDistance = 15;
        final double baseDamage = 2;
        final double damagePerLevel = 0;
        final double damagePerStack = 0.5;
        final double shockSeconds = 2;
        Player shooter;
        User user;
        Location origin;
        int level;
        Bolt bolt;
        double charge = 0;
        boolean stopcharge = false;
        int maxstacks;

        public BoltProfile(final Player shooter, final User u, final int level) {
            this.shooter = shooter;
            this.user = u;
            this.level = level;
            this.maxstacks = Math.min(level + 2, 5);
            this.bolt = Bolt.this;
        }

        public void shoot() {

            final Location loc = this.shooter.getEyeLocation();
            this.origin = loc.clone().add(loc.getDirection().multiply(0.5));
            this.stopcharge = true;

            new ElectricBolt(this, Bolt.this.stacks, Bolt.this.stackDecayCooldown, false, this.origin);

            /* Visuals */
            new ElectricBolt(this, Bolt.this.stacks, Bolt.this.stackDecayCooldown, true, this.origin);

        }


    }

}
