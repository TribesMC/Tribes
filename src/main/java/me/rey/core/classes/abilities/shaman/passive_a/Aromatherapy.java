package me.rey.core.classes.abilities.shaman.passive_a;

import com.google.common.collect.ImmutableMap;
import me.rey.core.Warriors;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.classes.abilities.IConstant;
import me.rey.core.classes.abilities.IConstant.ITogglable;
import me.rey.core.classes.abilities.IDamageTrigger.IPlayerDamagedEntity;
import me.rey.core.effects.ParticleEffect;
import me.rey.core.effects.SoundEffect;
import me.rey.core.energy.EnergyHandler;
import me.rey.core.enums.State;
import me.rey.core.events.customevents.block.CustomBlockPlaceEvent.PlaceCause;
import me.rey.core.events.customevents.combat.CustomDamageEvent;
import me.rey.core.events.customevents.combat.DamageEvent;
import me.rey.core.events.customevents.update.UpdateEvent;
import me.rey.core.gui.Item;
import me.rey.core.packets.Title;
import me.rey.core.players.User;
import me.rey.core.utils.ChargingBar;
import me.rey.core.utils.UtilLoc;
import me.rey.core.utils.Utils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.Map.Entry;

public class Aromatherapy extends Ability implements IConstant, ITogglable, IPlayerDamagedEntity {

    final static double minHeartsToTransfer = 7.0D;
    final static double blossomCooldown = 20.0D;
    final String BLOSSOM = "Blossom";
    private final Map<Material, Integer> FAKE_BLOCKS_FLORA = ImmutableMap.of(
            Material.LONG_GRASS, 1,
            Material.RED_ROSE, 0,
            Material.YELLOW_FLOWER, 0
    );
    private final List<Item> FAKE_ITEMS_TO_SPAWN = new ArrayList<>(Arrays.asList(
            new Item(Material.YELLOW_FLOWER),
            new Item(Material.RED_ROSE),
            new Item(Material.LONG_GRASS).setDurability(1),
            new Item(Material.DOUBLE_PLANT),
            new Item(Material.DOUBLE_PLANT).setDurability(2),
            new Item(Material.DOUBLE_PLANT).setDurability(3),
            new Item(Material.RED_ROSE).setDurability(1)
    ));
    private final EnergyHandler handler = new EnergyHandler();
    private final HashMap<Player, Integer> ticks = new HashMap<>();
    private final HashMap<Player, Integer> blossomCharge = new HashMap<>();
    private final HashMap<Player, Set<LivingEntity>> healing = new HashMap<Player, Set<LivingEntity>>();
    private final Set<Player> onBlossomCooldown = new HashSet<>();
    private final Set<Player> usingBlossom = new HashSet<>();
    private final Set<Player> usingAromatherapy = new HashSet<>();
    public HashMap<Player, HashMap<Block, Object[]>> toRestore = new HashMap<>();

    public Aromatherapy() {
        super(531, "Aromatherapy", ClassType.GREEN, AbilityType.PASSIVE_A, 1, 3, 0.00, Arrays.asList(
                "Transfer <variable>1+(0.5*l)</variable> (+0.5) health to your nearest ally",
                "under " + minHeartsToTransfer + " hearts. When activated, you will",
                "gain Regeneration 1 and lifesteal",
                "upon hitting enemies for <variable>0.2+(0.2*l)</variable> (+0.2) health",
                "",
                "Holding shift for <variable>8-l</variable> (-1) Seconds will enable Blossom.",
                "Blossom gives Regeneration 2 to all nearby allies",
                "and gives the user a Regeneration 3 and Speed 1",
                "effect. Blossom disables all buffs from",
                "Aromatherapy",
                "",
                "Health can only be transfered above 7 hearts.",
                "Blossom can only be used below 7 hearts.",
                "",
                "Aromatherapy Energy: <variable>10-l</variable> (-1)",
                "Blossom Recharge: 20 Seconds"
        ));

        this.setIgnoresCooldown(true);
        this.setEnergyCost(1, 0);
    }

    @Override
    public boolean off(final Player p) {
        if (this.usingAromatherapy.contains(p)) {
            p.removePotionEffect(PotionEffectType.REGENERATION);
            this.handler.togglePauseEnergy(State.DISABLED, p.getUniqueId());
            return false;
        }

        this.ticks.remove(p);
        return true;
    }

    @Override
    public boolean on(final Player p) {
        if (this.usingBlossom.contains(p)) {
            this.sendAbilityMessage(p, "You cannot use &g" + this.getName() + "&r with &g" + this.BLOSSOM + "&r enabled!");
            this.toggle(p, State.DISABLED);
            this.ticks.remove(p);
            return false;
        }

        this.handler.togglePauseEnergy(State.ENABLED, p.getUniqueId());
        p.removePotionEffect(PotionEffectType.REGENERATION);
        p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20 * 100000, 0, false, false));
        this.reset(p);
        return true;
    }

    @Override
    protected boolean execute(final User u, final Player p, final int level, final Object... conditions) {
        final Object arg = conditions[0];

        if (arg != null) {

            if (arg instanceof UpdateEvent) {

                if (!this.toRestore.containsKey(p)) {
                    this.toRestore.put(p, new HashMap<Block, Object[]>());
                }

                /*
                 * AROMATHERAPY
                 */

                // Consuming energy
                if (!this.getEnabledPlayers().contains(p.getUniqueId())) {
                    this.usingAromatherapy.remove(p);
                    return false;
                }

                this.usingAromatherapy.add(p);
                this.handler.togglePauseEnergy(State.ENABLED, p.getUniqueId());

                // Checking for the seconds
                final int ticksPassed = this.ticks.get(p);
                final double intervalSeconds = 1;

                // Spawning effects/items
                if (ticksPassed % (intervalSeconds * 20 / 4) == 0) {
                    // Particles on player
                    if (ticksPassed > (intervalSeconds * 20 / 2)) {
                        this.spawnFlora(p, p.getLocation(), level, 2 + level);
                    }

                    // Items
                    final double secondsToDespawn = 1.5;
                    final me.rey.core.items.Throwable flower = new me.rey.core.items.Throwable(this.FAKE_ITEMS_TO_SPAWN.get(new Random().nextInt(this.FAKE_ITEMS_TO_SPAWN.size())), false);
                    flower.drop(p.getLocation(), true);

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            flower.destroy();
                        }
                    }.runTaskLaterAsynchronously(Warriors.getInstance().getPlugin(), (int) (20 * secondsToDespawn));
                }

                // Sucessfully healed
                if (ticksPassed >= intervalSeconds * 20) {
                    final double healthToTransfer = 1 + (0.5 * level);
                    final double radius = 6 + (3 * level);

                    final Iterator<Entity> nearby = p.getNearbyEntities(radius, 5, radius).iterator();
                    if (p.getHealth() / 2D >= minHeartsToTransfer) {
                        while (nearby.hasNext()) {
                            final Entity next = nearby.next();
                            if (!(next instanceof LivingEntity)) {
                                continue;
                            }

                            final LivingEntity ent = (LivingEntity) next;
                            if (this.getPlayerHealing(p).contains(ent)) {
                                continue;
                            }

                            // are teamed
                            if (u.getTeam().contains(ent)) {
                                if (p == ent || p.getHealth() < minHeartsToTransfer * 2) {
                                    continue;
                                }
                                if (ent.getHealth() + healthToTransfer > ent.getMaxHealth()) {
                                    continue;
                                }


                                final int secondsIntervalTicks = (int) (20 * 0.1);
                                final Location initial = p.getEyeLocation().clone();
                                this.addToHealing(p, ent);
                                new BukkitRunnable() {

                                    final double particleSeparation = 0.2, hitbox = 0.3, offset = 0.8;
                                    final Location heartLoc = initial.clone();

                                    @Override
                                    public void run() {
                                        if (ent == null || ent.isDead() || (ent instanceof Player && !((Player) ent).isOnline())) {
                                            this.cancel();
                                            return;
                                        }

                                        if (ent.getLocation().distance(initial) > radius) {
                                            this.cancel();
                                            return;
                                        }

                                        final Vector pvector = Utils.getDirectionBetweenLocations(this.heartLoc, ent.getLocation());
                                        pvector.multiply(this.particleSeparation);
                                        this.heartLoc.add(pvector);
                                        final Location toSpawn = this.heartLoc.clone();
                                        toSpawn.setY(toSpawn.getY() + this.offset);

                                        new ParticleEffect(Effect.HEART).play(toSpawn);

                                        if (toSpawn.clone().subtract(new Location(toSpawn.getWorld(), 0, this.offset, 0)).distance(ent.getLocation()) <= this.hitbox) {
                                            if (p.getHealth() >= minHeartsToTransfer * 2) {
                                                ent.setHealth(Math.min(ent.getHealth() + healthToTransfer, ent.getMaxHealth()));
                                                p.setHealth(Math.max(0, Math.min(p.getMaxHealth(), p.getHealth() - healthToTransfer)));
                                            }
                                            Aromatherapy.this.removeFromHealing(p, ent);
                                            this.cancel();
                                            return;
                                        }

                                        pvector.normalize();

                                    }
                                }.runTaskTimerAsynchronously(Warriors.getInstance().getPlugin(), 0, secondsIntervalTicks);

                            }

                        }
                    } else {

                    }

                    this.reset(p);
                }

                // Consume energy
                final int energyPerSecond = 10 - level;
                this.setEnergyCost(energyPerSecond / 20D, 0);

                // Updating their in the ticks map
                this.ticks.replace(p, this.ticks.get(p) + 1);

            }


            if (arg instanceof DamageEvent) {
                if (this.onBlossomCooldown.contains(p)) {
                    return false;
                }

                final LivingEntity damager = ((CustomDamageEvent) arg).getDamager();
                if (!(damager instanceof Player) || !this.getEnabledPlayers().contains(((Player) damager).getUniqueId())) {
                    return false;
                }

                final double lifesteal = 0.2 + (0.2 * level);
                damager.setHealth(Math.min(damager.getHealth() + lifesteal, damager.getMaxHealth()));
            }

        }

        return true;
    }

    /*
     * BLOSSOM
     */
    @EventHandler
    public void onBlossom(final UpdateEvent e) {
        for (final Player p : Bukkit.getOnlinePlayers()) {

            if (!new User(p).isUsingAbility(this)) {
                continue;
            }

            if (this.onBlossomCooldown.contains(p)) {
                continue;
            }

            if (!this.toRestore.containsKey(p)) {
                this.toRestore.put(p, new HashMap<Block, Object[]>());
            }

            if (p.isSneaking() && p.getHealth() < minHeartsToTransfer * 2) {
                this.blossomCharge.putIfAbsent(p, 0);
                this.blossomCharge.replace(p, this.blossomCharge.get(p) + 1);
            } else {
                if (this.blossomCharge.containsKey(p)) {
                    new SoundEffect(Sound.NOTE_PLING, 0.5F).play(p);
                }
                this.blossomCharge.remove(p);
            }

            if (!this.blossomCharge.containsKey(p)) {
                continue;
            }

            final int level = new User(p).getSelectedBuild(this.getClassType()).getAbilityLevel(this.getAbilityType());
            final int selfCharge = this.blossomCharge.get(p);
            final double chargeSeconds = 8 - level;
            final double neededCharge = chargeSeconds * 20;
            final double floraRadius = 6 + level / 2;

            if (selfCharge <= neededCharge) {
                // DISPLAY COOLDOWN
                final double percentage = selfCharge * 100 / neededCharge;

                final int bars = ChargingBar.TITLE_BARS;
                final ChargingBar bar = new ChargingBar(bars, percentage);
                Title.getChargingBar("", bar).send(p);

                final double x = (double) ((selfCharge / 20D) * 100 / chargeSeconds);
                final double y = x % 5;
                if (y == 0) {
                    new SoundEffect(Sound.NOTE_PLING, 0.1F * bar.getChargeBars()).play(p);
                }

                // PARTICLES
                final ParticleEffect happy = new ParticleEffect(Effect.HAPPY_VILLAGER).setOffset(0.4F, 1F, 0.4F).setParticleCount(5);
                happy.play(p.getLocation());

                // FLORA SPAWN
                this.spawnFlora(p, p.getLocation(), level, floraRadius);
            } else {

                this.blossomCharge.remove(p);

                // Particles
                final double particleInterval = 0.4;
                new BukkitRunnable() {
                    final double radiusIncrement = 0.8;
                    final int maxCircles = 3;
                    double radius = 0;

                    @Override
                    public void run() {
                        this.radius += this.radiusIncrement;
                        final ArrayList<Location> circle = UtilLoc.circleLocations(p.getLocation(), this.radius, 1);
                        for (final Location point : circle) {
                            new ParticleEffect(Effect.SNOW_SHOVEL).play(point);
                        }

                        if (this.radius != 0 && this.radius > this.radiusIncrement * this.maxCircles) {
                            this.cancel();
                            return;
                        }
                    }
                }.runTaskTimer(Warriors.getInstance().getPlugin(), 0, (int) Math.round(20 * particleInterval));

                this.sendUsedMessageToPlayer(p, this.BLOSSOM);
                new SoundEffect(Sound.AMBIENCE_THUNDER, 2.0F).play(p.getLocation());
                new SoundEffect(Sound.CREEPER_HISS, 0.5F).play(p.getLocation());

                this.onBlossomCooldown.add(p);
                this.usingBlossom.add(p);

                new BukkitRunnable() {

                    @Override
                    public void run() {
                        Aromatherapy.this.usingBlossom.remove(p);
                    }

                }.runTaskLater(Warriors.getInstance().getPlugin(), 20L * (3 + level));

                new BukkitRunnable() {

                    @Override
                    public void run() {
                        Aromatherapy.this.onBlossomCooldown.remove(p);
                        Aromatherapy.this.sendReadyMessageToPlayer(p, Aromatherapy.this.BLOSSOM);
                    }

                }.runTaskLater(Warriors.getInstance().getPlugin(), (int) (20 * blossomCooldown));

                this.off(p);
                this.toggle(p, State.DISABLED);

                /*
                 * BUFFS
                 */
                p.removePotionEffect(PotionEffectType.REGENERATION);
                p.removePotionEffect(PotionEffectType.SPEED);
                p.addPotionEffects(Arrays.asList(
                        new PotionEffect(PotionEffectType.SPEED, 20 * (3 + level), 0, false, false),
                        new PotionEffect(PotionEffectType.REGENERATION, 20 * (3 + level), 2, false, false)
                ));

                final Iterator<Entity> ents = UtilLoc.getEntitiesInCircle(p.getLocation(), floraRadius).iterator();
                while (ents.hasNext()) {
                    final Entity found = ents.next();
                    if (!(found instanceof Player)) {
                        continue;
                    }

                    if (new User(p).getTeam().contains((Player) found)) {
                        ((LivingEntity) found).addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20 * (3 + level), 1, false, false));
                    }

                }

            }

        }
    }

    private void reset(final Player p) {
        if (this.ticks.containsKey(p)) {
            this.ticks.replace(p, 0);
        } else {
            this.ticks.put(p, 0);
        }
    }

    private void spawnFlora(final Player p, final Location loc, final int level, final double radius) {
        final HashMap<Block, Double> near = UtilLoc.getBlocksInRadius(loc, radius + 0.5D);

        for (final Block b : near.keySet()) {
            final double offset = near.get(b);

            // Is in radius
            if (offset < radius + 0.2D) {
                // Checking if grass & if above is air
                if (!b.getType().equals(Material.GRASS)) {
                    continue;
                }

                if (new Random().nextInt(5) < 1) {
                    continue;
                }
                this.SetBlock(p, b.getRelative(BlockFace.UP), loc.getBlock(), level);
            }

        }

    }

    public void SetBlock(final Player p, final Block freeze, final Block mid, final int level) {
        if (freeze == null || !freeze.getType().equals(Material.AIR)) {
            return;
        }

        final double time = 2;

        final List<Entry<Material, Integer>> entries = new ArrayList<>(this.FAKE_BLOCKS_FLORA.entrySet());
        final int index = new Random().nextInt(entries.size() + 5);
        final Entry<Material, Integer> item = entries.get(index > entries.size() - 1 ? 0 : index);
        this.restoreLater(p, freeze, item.getKey(), item.getValue(), time);
    }

    public void restoreLater(final Player p, final Block block, final Material toReplace, final int data, final double time) {

        final Material type = block == null ? Material.AIR : block.getType();
        final Object[] array = new Object[2];
        array[0] = type;
        array[1] = block.getData();

        final HashMap<Block, Object[]> self = this.toRestore.get(p);
        self.put(block, array);

        me.rey.core.utils.UtilBlock.replaceBlock(PlaceCause.ABILITY, block, toReplace, (byte) data);

        this.toRestore.replace(p, self);

        new BukkitRunnable() {

            @Override
            public void run() {
                Aromatherapy.this.replaceBlock(p, block);
            }

        }.runTaskLater(Warriors.getInstance().getPlugin(), (int) (time * 20));
    }

    private void replaceBlock(final Player p, final Block block) {
        if (!this.toRestore.containsKey(p)) {
            return;
        }

        final HashMap<Block, Object[]> self = this.toRestore.get(p);
        if (!self.containsKey(block)) {
            return;
        }

        final Object[] objects = self.get(block);
        self.remove(block);

        me.rey.core.utils.UtilBlock.replaceBlock(PlaceCause.ABILITY, block, (Material) objects[0], (byte) objects[1]);

        this.toRestore.replace(p, self);
        if (self.isEmpty()) {
            this.toRestore.remove(p);
        }
    }

    @EventHandler
    public void onBlockBreak(final BlockBreakEvent e) {
        for (final Player keys : this.toRestore.keySet()) {
            for (final Block b : this.toRestore.get(keys).keySet()) {
                if (b.equals(e.getBlock())) {
                    e.setCancelled(true);
                    return;
                }
            }
        }
    }

    private Set<LivingEntity> getPlayerHealing(final Player p) {
        return this.healing.containsKey(p) ? this.healing.get(p) : new HashSet<LivingEntity>();
    }

    private void addToHealing(final Player p, final LivingEntity le) {
        final Set<LivingEntity> cur = this.getPlayerHealing(p);
        cur.add(le);

        if (!this.healing.containsKey(p)) {
            this.healing.put(p, cur);
        } else {
            this.healing.replace(p, cur);
        }
    }

    private void removeFromHealing(final Player p, final LivingEntity le) {
        final Set<LivingEntity> cur = this.getPlayerHealing(p);
        cur.remove(le);

        if (!this.healing.containsKey(p)) {
            this.healing.put(p, cur);
        } else {
            this.healing.replace(p, cur);
        }
    }

}
