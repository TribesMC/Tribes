package me.rey.core.classes.abilities;

import me.rey.core.Warriors;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.AbilityType.EventType;
import me.rey.core.classes.abilities.IConstant.IDroppable;
import me.rey.core.classes.abilities.IConstant.ITogglable;
import me.rey.core.classes.abilities.IDamageTrigger.IPlayerDamagedByEntity;
import me.rey.core.classes.abilities.IDamageTrigger.IPlayerDamagedEntity;
import me.rey.core.effects.Effect;
import me.rey.core.effects.repo.Silence;
import me.rey.core.energy.IEnergyEditor;
import me.rey.core.enums.AbilityFail;
import me.rey.core.enums.State;
import me.rey.core.events.customevents.ability.AbilityFailEvent;
import me.rey.core.events.customevents.ability.AbilityUseEvent;
import me.rey.core.events.customevents.ability.BowAbilityHitEvent;
import me.rey.core.events.customevents.ability.BowAbilityShootEvent;
import me.rey.core.events.customevents.combat.DamageEvent;
import me.rey.core.events.customevents.combat.DamagedByEntityEvent;
import me.rey.core.events.customevents.update.EnergyUpdateEvent;
import me.rey.core.events.customevents.update.UpdateEvent;
import me.rey.core.gui.Item;
import me.rey.core.packets.ActionBar;
import me.rey.core.players.User;
import me.rey.core.players.combat.PlayerHit;
import me.rey.core.pvp.Build;
import me.rey.core.pvp.ToolType;
import me.rey.core.pvp.ToolType.HitType;
import me.rey.core.utils.ChargingBar;
import me.rey.core.utils.Cooldown;
import me.rey.core.utils.Text;
import me.rey.core.utils.UtilBlock;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Ability extends Cooldown implements Listener {

    static List<UUID> abilityArrows = new ArrayList<>();
    private final String USED;
    // Cooldowns
    private final Map<UUID, Double> tempMaxCooldowns;
    private final String name;
    private final AbilityType abilityType;
    private final ClassType classType;
    private final String[] description;
    private final int maxLevel;
    private final int tokenCost;
    private final long id;
    private final double resetCooldown;
    protected boolean skipCooldownCheck;
    protected String MAIN = "&7", VARIABLE = "&a", SECONDARY = "&e";
    // Dependant
    private Set<UUID> playersEnabled;
    private int tempDefaultLevel;
    private boolean cooldownCanceled, ignoresCooldown, inLiquid, whileSlowed, inAir, whileSilenced;
    private double cooldown;
    private double energyCost;
    private double energyReductionPerLevel;

    public Ability(final long id, final String name, final ClassType classType, final AbilityType abilityType, final int tokenCost, final int maxLevel, final double cooldown, final List<String> description) {
        super(Warriors.getInstance().getPlugin(), Text.format(name, "You can use &a" + name + "&7."), true);

        this.USED = Text.format(name, "You can use &a%s&7.");

        this.name = name;
        this.classType = classType;
        this.abilityType = abilityType;
        this.maxLevel = maxLevel;
        this.cooldown = cooldown;
        this.resetCooldown = cooldown;
        this.inLiquid = false;
        this.whileSlowed = true;
        this.inAir = true;
        this.cooldownCanceled = false;
        this.skipCooldownCheck = false;
        this.id = id;
        this.tokenCost = tokenCost;
        this.energyCost = 0.00;
        this.description = new String[description.size()];
        this.tempMaxCooldowns = new HashMap<>();
        this.whileSilenced = false;

        int index = 0;
        for (final String s : description) {
            this.description[index] = s;
            index++;
        }

        if (this instanceof ITogglable) {
            this.playersEnabled = new HashSet<>();
        }

    }

    public boolean run(final Player p, final ToolType toolType, final boolean messages, final Object... conditions) {
        return this.run(true, false, p, toolType, messages, conditions);
    }

    public boolean run(final boolean useEnergy, final boolean ignoreEvents, final Player p, final ToolType toolType, final boolean messages, final Object... conditions) {
        final User user = new User(p);
        if (user.getWearingClass() == null || !(user.getWearingClass().equals(this.getClassType()))) {
            return false;
        }

        Build b = user.getSelectedBuild(this.getClassType());
        if (b == null) {
            b = this.getClassType().getDefaultBuild();
        }
        if (b.getAbility(this.getAbilityType()) == null || b.getAbility(this.getAbilityType()).getIdLong() != this.getIdLong()) {
            return false;
        }

        int level = b.getAbilityLevel(this.getAbilityType());
        final double baseEnergyCost = this.energyCost;

        /*
         * BOOSTER WEAPONS
         */
        if (this.getAbilityType().supportsBoosters() && toolType != null && this.matchesAbilityTool(toolType) && toolType.isBooster()) {
            level = Math.min(this.getMaxLevel() + 1, level + 2);
        }

        AbilityFailEvent event = null;

        // WHILE SILENCED
        if (!this.whileSilenced && Effect.hasEffect(Silence.class, p) && (Silence.silencedAbilities.contains(this.getAbilityType()) || this instanceof ITogglable)) {
            if (!(conditions != null && conditions.length == 1 && (conditions[0] instanceof UpdateEvent || conditions[0] instanceof DamageEvent
                    || conditions[0] instanceof DamagedByEntityEvent || conditions[0] instanceof EnergyUpdateEvent)) || this instanceof ITogglable) {
                event = new AbilityFailEvent(AbilityFail.SILENCED, p, this, level);
                Bukkit.getServer().getPluginManager().callEvent(event);

                if (!event.isCancelled()) {
                    if (this instanceof ITogglable) {
                        ((ITogglable) this).off(p);
                        this.toggle(p, State.DISABLED);
                    } else {
                        Silence.SOUND.play(p);
                        this.sendAbilityMessage(p, "You are &asilenced&7.");
                    }
                    return false;
                }
            }
        }

        // WHILE COOLDOWN
        if (this.hasCooldown(p) && !this.skipCooldownCheck && !this.ignoresCooldown && !this.cooldownCanceled) {
            event = new AbilityFailEvent(AbilityFail.COOLDOWN, p, this, level);
            Bukkit.getServer().getPluginManager().callEvent(event);

            if (!event.isCancelled()) {
                if (!event.isMessageCancelled() && messages) {
                    this.sendCooldownMessage(p);
                }

                return false;
            }
        }

        // IN LIQUID
        if (p.getLocation().getBlock() != null && p.getLocation().getBlock().isLiquid() && !this.inLiquid) {

            event = new AbilityFailEvent(AbilityFail.LIQUID, p, this, level);
            Bukkit.getServer().getPluginManager().callEvent(event);

            if (!event.isCancelled()) {
                final String source = p.getLocation().getBlock().getType().name().toLowerCase().contains("water") ? "water" : "lava";
                if (messages && !event.isMessageCancelled()) {
                    new User(p).sendMessageWithPrefix(this.getName(), String.format("You cannot use &a" + this.getName() + "&7 in %s.", source));
                }
                return false;
            }
        }

        // WHILE SLOWED
        if (user.hasPotionEffect(PotionEffectType.SLOW) && !this.whileSlowed) {

            event = new AbilityFailEvent(AbilityFail.SLOWED, p, this, level);
            Bukkit.getServer().getPluginManager().callEvent(event);

            if (!event.isCancelled()) {
                if (messages && !event.isMessageCancelled()) {
                    new User(p).sendMessageWithPrefix(this.getName(), "You cannot use &a" + this.getName() + "&7 while slowed.");
                }
                return false;
            }
        }

        // IN THE AIR
        if (!((Entity) p).isOnGround() && !this.inAir) {

            event = new AbilityFailEvent(AbilityFail.AIR, p, this, level);
            Bukkit.getServer().getPluginManager().callEvent(event);

            if (!event.isCancelled()) {
                if (messages && !event.isMessageCancelled()) {
                    new User(p).sendMessageWithPrefix(this.getName(), "You cannot use &a" + this.getName() + "&7 in the air.");
                }
                return false;
            }
        }

        // CALLING ABILITY EVENT
        if (!ignoreEvents) {
            final AbilityUseEvent abilityEvent = new AbilityUseEvent(p, this, level);
            Bukkit.getServer().getPluginManager().callEvent(abilityEvent);
            if (abilityEvent.isCancelled()) {
                return false;
            }
        }

        if (useEnergy) {
            if (user.getEnergy() < this.energyCost - this.energyReductionPerLevel * level) {

                if (messages) {
                    this.sendEnergyError(p);
                }

                if (this instanceof ITogglable) {
                    this.playersEnabled.remove(p.getUniqueId());
                    ((ITogglable) this).off(p);
                }
                return false;
            }
        }

        this.setSound(Sound.NOTE_PLING, 2.0F);
        final boolean success = this.execute(user, p, level, conditions);

        this.applyCooldown(p);

        final double toConsume = this.energyCost - this.energyReductionPerLevel * level;
        if (useEnergy && toConsume > 0) {
            user.consumeEnergy(toConsume);
        }

        this.setEnergyCost(baseEnergyCost, this.energyReductionPerLevel);
        return success;
    }

    protected abstract boolean execute(User u, final Player p, final int level, Object... conditions);

    public String[] getDescription(int level) {
        final String[] desc = this.description.clone();
        final boolean selected = level <= 0;
        level = level <= 0 ? 1 : level;

        for (int i = 0; i < desc.length; i++) {
            String s = desc[i];
            if (s == null) {
                continue;
            }

            //ADDING <VARIABLE> </VARIABLE> COLORS AND CALCULATING
            final Pattern p2 = Pattern.compile("(?<=\\<variable\\>)(\\s*.*\\s*)(?=\\<\\/variable\\>)");
            final Matcher m2 = p2.matcher(s);
            int finds = 0;
            while (m2.find()) {
                final String match = m2.group(finds).replaceAll("\\s+", "").toLowerCase().replaceAll("l", level + "");
                final String result = String.format("%.1f", Text.eval(this.getName(), match));

                s = s.replace(m2.group(), result.replace(".0", ""));
                finds++;
            }

            // EDITING in VARIABLES INSIDE ()
            final Pattern p1 = Pattern.compile("\\(.*?\\)");
            final Matcher m1 = p1.matcher(s);
            while (m1.find()) {
                final String match = m1.group().subSequence(1, m1.group().length() - 1).toString();

                s = selected ? s.replace(" (" + match + ")", "") : s.replace("(" + match + ")", this.MAIN + "(<secondary>" + match + "<main>)");
            }

            if (selected) {
                desc[i] = Text.color(this.MAIN + s.replaceAll("<main>", this.MAIN).replaceAll("</variable>", this.MAIN)
                        .replaceAll("<variable>", this.VARIABLE).replaceAll("<secondary>", this.SECONDARY));
            } else {
                desc[i] = Text.color(this.MAIN + s.replaceAll("<main>", this.MAIN).replaceAll("</variable>", this.MAIN)
                        .replaceAll("<variable>", this.SECONDARY).replaceAll("<secondary>", this.VARIABLE));
            }

        }

        return desc;
    }

    public String[] getDescription() {
        return this.getDescription(-1);
    }

    public void applyCooldown(final Player p) {
        if (!this.ignoresCooldown && !this.cooldownCanceled) {
            this.setCooldownForPlayer(p, this.cooldown);
            this.tempMaxCooldowns.put(p.getUniqueId(), this.cooldown);
        }

        this.resetCooldown();
        this.setCooldownCanceled(false);
    }

    public int getSkillTokenCost() {
        return this.tokenCost;
    }

    public void setWhileSilenced(final boolean silence) {
        this.whileSilenced = silence;
    }

    public void setIgnoresCooldown(final boolean ignore) {
        this.ignoresCooldown = ignore;
    }

    private void resetCooldown() {
        this.cooldown = this.resetCooldown;
    }

    public long getIdLong() {
        return this.id;
    }

    public String getId() {
        return this.getIdLong() + "";
    }

    public String getName() {
        return this.name;
    }

    public double getCooldown() {
        return this.cooldown;
    }

    public void setCooldown(final double time) {
        this.cooldown = time;
    }

    public ClassType getClassType() {
        return this.classType;
    }

    public AbilityType getAbilityType() {
        return this.abilityType;
    }

    public void sendEnergyError(final Player p) {
        new User(p).sendMessageWithPrefix("Error", String.format("You don't have enough energy to use &a%s&7!", this.getName()));
    }

    public int getMaxLevel() {
        return this.maxLevel;
    }

    public int getTempDefaultLevel() {
        final int temp = this.tempDefaultLevel;
        this.tempDefaultLevel = 0;
        return temp;
    }

    public Ability setTempDefaultLevel(final int level) {
        this.tempDefaultLevel = level;
        return this;
    }

    public boolean sendUsedMessageToPlayer(final Player p, final String name) {
        new User(p).sendMessageWithPrefix(this.getName(), "You used &a" + name + "&7.");
        return true;
    }

    public boolean sendReadyMessageToPlayer(final Player p, final String name) {
        new User(p).sendMessage(this.USED.replace("%s", name));
        return true;
    }

    public boolean sendAbilityMessage(final LivingEntity p, final String text) {
        if (text == null) {
            return false;
        }
        p.sendMessage(Text.format(this.getName(), text));
        return true;
    }

    public void setInLiquid(final boolean inLiquid) {
        this.inLiquid = inLiquid;
    }

    public void setWhileSlowed(final boolean whileSlowed) {
        this.whileSlowed = whileSlowed;
    }

    public void setWhileInAir(final boolean inAir) {
        this.inAir = inAir;
    }

    public void setCooldownCanceled(final boolean canceled) {
        this.cooldownCanceled = canceled;
    }

    public void setSkipCooldownCheck(final boolean canceled) {
        this.skipCooldownCheck = canceled;
    }

    public void setEnergyCost(final double energyCost, final double energyReductionPerLevel) {
        this.energyCost = energyCost;
        this.energyReductionPerLevel = energyReductionPerLevel;
    }

    public Set<UUID> getEnabledPlayers() {
        return this.playersEnabled;
    }

    protected void sendCooldownMessage(final Player p) {
        new User(p).sendMessageWithPrefix(this.getName(), "You cannot use &a" + this.getName() + "&7 for &a" + this.getPlayerCooldown(p) + " &7seconds.");
    }

    public void toggle(final Player player, final State state) {
        new User(player).sendMessageWithPrefix(this.getName(), this.getName() + ": " + state.getName());
        if (state == State.DISABLED) {
            this.playersEnabled.remove(player.getUniqueId());
        } else {
            this.playersEnabled.add(player.getUniqueId());
        }
    }

    /*
     * UPDATING ENERGY
     */
    @EventHandler
    public void onEnergyUpdate(final EnergyUpdateEvent e) {
        if (!(this instanceof IEnergyEditor) || !(this instanceof IConstant)) {
            return;
        }
        if (!(new User(e.getPlayer()).isUsingAbility(this))) {
            return;
        }

        this.run(true, true, e.getPlayer(), null, true, e);
    }

    /*
     * DAMAGE EVENT TRIGGER
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamage(final DamageEvent e) {
        if (!(new User(e.getDamager()).isUsingAbility(this))) {
            return;
        }
        if (!(this instanceof IPlayerDamagedEntity)) {
            return;
        }
        if (!e.getHitType().equals(HitType.MELEE)) {
            return;
        }

        if (!e.isCancelled()) {
            final boolean success = this.run(false, true, e.getDamager(), null, false, e);

            if (!(e.getDamagee() instanceof Player)) {
                return;
            }
            if (!success) {
                return;
            }
            final PlayerHit hit = new PlayerHit((Player) e.getDamagee(), (Player) e.getDamager(), e.getDamage(), null);
            hit.setCause("&a" + this.getName());
            e.setHit(hit);
        }
    }

    /*
     * DAMAGE BY ENTITY EVENT TRIGGER
     */
    @EventHandler
    public void onDamageByEntity(final DamagedByEntityEvent e) {
        if (!(new User(e.getDamagee()).isUsingAbility(this))) {
            return;
        }
        if (!(this instanceof IPlayerDamagedByEntity)) {
            return;
        }
        if (!e.getHitType().equals(HitType.MELEE)) {
            return;
        }

        if (!e.isCancelled()) {
            final boolean success = this.run(false, true, e.getDamagee(), null, false, e);

            if (!(e.getDamager() instanceof Player)) {
                return;
            }
            if (!success) {
                return;
            }
            final PlayerHit hit = new PlayerHit((Player) e.getDamager(), (Player) e.getDamagee(), e.getDamage(), null);
            hit.setCause("&a" + this.getName());
            e.setHit(hit);
        }
    }

    /*
     * CONSTANT PASSIVES
     */
    @EventHandler
    public void onUpdate(final UpdateEvent e) {
        if (!(this instanceof IConstant) || this instanceof IEnergyEditor) {
            return;
        }
        this.setMessage(null);

        for (final Player p : Bukkit.getOnlinePlayers()) {
            if (this instanceof ITogglable && this.playersEnabled.contains(p.getUniqueId()) && !new User(p).isUsingAbility(this)) {
                ((ITogglable) this).off(p);
                this.toggle(p, State.DISABLED);
                continue;
            }

            if (!(new User(p).isUsingAbility(this))) {
                continue;
            }

            boolean messages = true;
            if (this instanceof ITogglable && !this.playersEnabled.contains(p.getUniqueId())) {
                continue;
            }
            if (this instanceof IConstant && !(this instanceof ITogglable)) {
                messages = false;
            }

            final ToolType toolType = this.findBooster(p);
            final boolean success = this.run(true, this instanceof IConstant && !(this instanceof ITogglable), p, toolType, messages, e);
            if (!success && this instanceof ITogglable && this.playersEnabled.contains(p.getUniqueId())) {
                ((ITogglable) this).off(p);
                this.toggle(p, State.DISABLED);
            }
        }
    }

    /*
     * DROPPABLE/TOGGLEABLE ITEMS
     */
    @EventHandler
    public void onDropEvent(final PlayerDropItemEvent e) {
        if (!(new User(e.getPlayer()).isUsingAbility(this))) {
            return;
        }
        if (this instanceof ITogglable) {

            final ItemStack holding = e.getItemDrop().getItemStack();
            if (this.match(holding) == null) {
                return;
            }

            e.setCancelled(true);

            // TOGGLING STATE AND SAVING
            final UUID uuid = e.getPlayer().getUniqueId();
            State newState = this.playersEnabled.contains(uuid) ? State.DISABLED : State.ENABLED;


            // RUNNING IF ENABLED
            boolean success = true;
            if (newState == State.ENABLED) {
                success = this.run(e.getPlayer(), this.match(holding), true, e);
            }

            if (!success) {
                return;
            }

            if (this.playersEnabled.contains(e.getPlayer().getUniqueId())) {
                newState = State.DISABLED;
                this.toggle(e.getPlayer(), newState);
                ((ITogglable) this).off(e.getPlayer());
            } else {
                newState = State.ENABLED;
                this.toggle(e.getPlayer(), newState);
                ((ITogglable) this).on(e.getPlayer());
            }

        } else if (this instanceof IDroppable) {

			/*
			FIX FOR ANY ITEM IN INVENTORY DUPE GLITCH
			 */
            final InventoryView view = e.getPlayer().getOpenInventory();
            if (view != null && view.getTopInventory().getType() != InventoryType.CRAFTING) {
                return;
            }

            final ItemStack holding = e.getItemDrop().getItemStack();
            if (this.match(holding) == null) {
                return;
            }

            e.setCancelled(true);

            this.run(e.getPlayer(), this.match(holding), true);
        }
    }

    /*
     * BOW SHOOT EVENT
     */

    /*
     * BOW ABILITIES / PREPARABLE / ON SHOT
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onBowHit(final DamageEvent e) {
        if (!(this instanceof IBowPreparable)) {
            return;
        }
        if (!e.getHitType().equals(HitType.ARCHERY)) {
            return;
        }
        if (!new User(e.getDamager()).isUsingAbility(this)) {
            return;
        }
        if (!((IBowPreparable) this).hasShot(e.getDamager())) {
            return;
        }

        if (e.isCancelled()) {
            return;
        }

        final Player hitter = e.getDamager();
        final LivingEntity hit = e.getDamagee();

        if (!e.isCancelled()) {
            final String messageForShooter = "You hit " + this.SECONDARY + hit.getName() + this.MAIN + " with " + this.VARIABLE + this.getName() + this.MAIN + ".";
            final String messageForDamagee = this.SECONDARY + hitter.getName() + this.MAIN + " hit you with " + this.VARIABLE + this.getName() + this.MAIN + ".";
            final int level = new User(hitter).getSelectedBuild(this.getClassType()).getAbilityLevel(this.getAbilityType());

            final Vector velocity = e.getOriginalEvent().getDamager().getVelocity();
            final BowAbilityHitEvent event = new BowAbilityHitEvent(hitter, this, level, hit, hit.getLocation(), velocity, messageForShooter, messageForDamagee);
            Bukkit.getServer().getPluginManager().callEvent(event);

            if (!event.isCancelled()) {

                this.setCooldownCanceled(true);
                ((IBowPreparable) this).unshoot(e.getDamager());
                this.run(true, true, e.getDamager(), this.findBooster(e.getDamager()), true, event);

                if (!(e.getDamagee() instanceof Player)) {
                    return;
                }
                final PlayerHit playerHit = new PlayerHit((Player) e.getDamagee(), (Player) e.getDamager(), e.getDamage(), null);
                playerHit.setCause("&a" + this.getName());
                e.setHit(playerHit);

                this.sendAbilityMessage(hitter, event.getMessageForShooter());
                this.sendAbilityMessage(hit, event.getMessageForDamagee());
                hit.getWorld().playSound(hit.getLocation(), Sound.BLAZE_BREATH, 2.5F, 2.0F);

            } else {
                e.setCancelled(true);
            }

        }

    }

    @EventHandler
    public void onBowShoot(final EntityShootBowEvent e) {
        if (!(this instanceof IBowPreparable)) {
            return;
        }
        if (!(e.getEntity() instanceof Player)) {
            return;
        }
        if (!new User((Player) e.getEntity()).isUsingAbility(this)) {
            return;
        }
        if (!((IBowPreparable) this).isPrepared((Player) e.getEntity())) {
            return;
        }

        final Player shooter = (Player) e.getEntity();

        final int level = new User(shooter).getSelectedBuild(this.getClassType()).getAbilityLevel(this.getAbilityType());
        final String message = "You fired " + this.VARIABLE + this.getName() + this.MAIN + ".";

        final BowAbilityShootEvent event = new BowAbilityShootEvent(shooter, this, level, message);
        Bukkit.getServer().getPluginManager().callEvent(event);

        if (!event.isCancelled() && !e.isCancelled()) {
            ((IBowPreparable) this).unprepare((Player) e.getEntity());
            ((IBowPreparable) this).shoot((Player) e.getEntity());
            abilityArrows.add(e.getProjectile().getUniqueId());

            if (this instanceof IBowPreparable.IBowEvents) {
                this.run(true, true, shooter, this.findBooster(shooter), true, event);
            }
            this.sendAbilityMessage((Player) e.getEntity(), event.getShootMessage());
        }

    }

    @EventHandler
    public void projectileHit(final ProjectileHitEvent e) {
        if (!(this instanceof IBowPreparable)) {
            return;
        }
        if (!(e.getEntity() instanceof Arrow)) {
            return;
        }
        if (!(((Arrow) e.getEntity()).getShooter() instanceof Player)) {
            return;
        }

        final Player shooter = (Player) ((Arrow) e.getEntity()).getShooter();
        final Arrow arrow = (Arrow) e.getEntity();
        if (!new User(shooter).isUsingAbility(this)) {
            return;
        }

        if (!abilityArrows.contains(arrow.getUniqueId())) {
            return;
        }

        final Ability self = this;
        final int level = new User(shooter).getSelectedBuild(this.getClassType()).getAbilityLevel(this.getAbilityType());
        final Vector velocity = arrow.getVelocity();
        new BukkitRunnable() {
            @Override
            public void run() {
                if (((IBowPreparable) self).hasShot(shooter) && (arrow.isOnGround() || arrow.isDead())) {

                    final String message = "You missed " + Ability.this.VARIABLE + Ability.this.getName() + Ability.this.MAIN + ".";
                    final BowAbilityHitEvent event = new BowAbilityHitEvent(shooter, self, level, null, arrow.getLocation(), velocity, message, "");
                    Bukkit.getServer().getPluginManager().callEvent(event);

                    if (!event.isCancelled()) {
                        ((IBowPreparable) self).unshoot(shooter);
                        abilityArrows.remove(arrow.getUniqueId());

                        if (self instanceof IBowPreparable.IBowEvents) {
                            Ability.this.run(true, true, shooter, Ability.this.findBooster(shooter), true, event);
                        }

                        Ability.this.sendAbilityMessage(shooter, event.getMessageForShooter());
                    }

                }
            }
        }.runTaskLater(Warriors.getInstance().getPlugin(), 2);
    }

    /*
     * RUN ABILITIES
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEvent(final PlayerInteractEvent e) {
        this.runCheck(e.getAction(), e.getPlayer(), e.getItem(), e.getClickedBlock(), e);
    }

//	@EventHandler (priority = EventPriority.HIGHEST)
//	public void onEvent(PlayerInteractEntityEvent e) {
//
//		this.runCheck(Action.RIGHT_CLICK_AIR, e.getPlayer(), e.getPlayer().getItemInHand(), null, e);
//	}


    /*
     * CHECK TO RUN
     */
    private void runCheck(final Action action, final Player p, final ItemStack hold, final Block clicked, final Event e) {

        final boolean isAir = clicked == null || action.equals(Action.RIGHT_CLICK_AIR) || action.equals(Action.LEFT_CLICK_AIR);
        final boolean isUsable = clicked != null && UtilBlock.usableBlocks().contains(clicked.getType());

        if (isUsable && !isAir) {
            return;
        }

        // RIGHT CLICK ABILITIES
        if (this.getAbilityType().getEventType().equals(EventType.RIGHT_CLICK)
                && (action.equals(Action.RIGHT_CLICK_AIR) || action.equals(Action.RIGHT_CLICK_BLOCK))) {

            final Material item = hold == null ? Material.AIR : hold.getType();

            final ToolType match = this.match(item);
            if (match == null) {
                return;
            }

            this.run(p, match, true);
            return;
        }

        // LEFT CLICK ABILITIES
        if ((this.getAbilityType().getEventType().equals(EventType.LEFT_CLICK) || this instanceof IBowPreparable)
                && (action.equals(Action.LEFT_CLICK_AIR) || action.equals(Action.LEFT_CLICK_BLOCK))) {
            final Material item = hold == null ? Material.AIR : hold.getType();
            final ToolType match = this.match(item);
            if (match == null) {
                return;
            }

            if (this instanceof IBowPreparable) {
                final boolean success = this.run(p, match, true, e);
                if (success) {
                    this.sendAbilityMessage(p, "You have prepared " + this.VARIABLE + this.getName() + "&r.");
                    p.getWorld().playSound(p.getLocation(), Sound.BLAZE_BREATH, 2.5F, 2.0F);
                }
            } else {
                this.run(p, match, true);
            }

            return;
        }

    }
    // end

    protected ToolType match(final Material item) {
        ToolType toolType = null;
        for (final ToolType type : this.getAbilityType().getToolTypes()) {
            if (type.getType().equals(item)) {
                toolType = type;
            }
        }

        return toolType;
    }

    protected ToolType match(final ItemStack item) {
        return this.match(item.getType());
    }

    protected ToolType findBooster(final Player p) {
        for (final ItemStack item : p.getInventory().getContents()) {
            if (item != null && this.match(item) != null && this.matchesAbilityTool(this.match(item)) && this.match(item).isBooster()) {
                return this.match(item);
            }
        }

        return null;
    }

    /*
     * CLEAR ENABLED PLAYERS ON LOG OFF
     */
    @EventHandler
    public void onLeave(final PlayerQuitEvent e) {
        if (this instanceof ITogglable && this.playersEnabled.contains(e.getPlayer().getUniqueId())) {
            ((ITogglable) this).off(e.getPlayer());
            this.playersEnabled.remove(e.getPlayer().getUniqueId());
        }
    }

    public boolean matchesAbilityTool(final ToolType holdType) {

        // Checking if this ability's tools match with the player's held tool
        boolean pass = false;
        for (final ToolType available : this.getAbilityType().getToolTypes()) {
            if (available.equals(holdType)) {
                pass = true;
            }
        }


        // If the ability tool doesn't match, then we continue to the next player
        return pass;
    }

    /*
     * ACTION BAR COOLDOWN DISPLAY
     */
    @EventHandler
    public void onActionBarCooldown(final UpdateEvent e) {
        // Checked for only SWORD & AXE abilities
        if (!this.getAbilityType().equals(AbilityType.SWORD) && !this.getAbilityType().equals(AbilityType.AXE) && !this.getAbilityType().equals(AbilityType.SPADE)) {
            return;
        }

        for (final Player p : Bukkit.getOnlinePlayers()) {
            final User u = new User(p);
            if (!u.isUsingAbility(this)) {
                continue;
            }


            if (!this.matchesAbilityTool(this.match(p.getItemInHand() == null ? new Item(Material.AIR).get() : p.getItemInHand()))) {
                return;
            }

            // Getting cooldown variables
            final double maxCooldown = this.tempMaxCooldowns.containsKey(p.getUniqueId()) ? this.tempMaxCooldowns.get(p.getUniqueId()) : this.getCooldown();
            final double pCooldown = this.getPlayerCooldown(p);

            // Canceling if the cooldown is ignored or the player has 0 cooldown
            if (this.ignoresCooldown || pCooldown == 0) {
                continue;
            }

            final String READY;
            if (pCooldown <= 0.1) {
                READY = ChatColor.GREEN + ChatColor.BOLD.toString() + "READY!";
                new ActionBar(Text.color("&f&l" + this.getName() + " " + READY)).send(p);
                continue;
            } else {
                READY = "&f" + pCooldown + " Seconds";
            }

            ActionBar.getChargingBar(this.getName(), new ChargingBar(ChargingBar.ACTIONBAR_BARS, pCooldown, maxCooldown), READY).send(p);
        }
    }

}
