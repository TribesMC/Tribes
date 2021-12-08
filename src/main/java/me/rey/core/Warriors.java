package me.rey.core;

import me.rey.Module;
import me.rey.core.classes.ClassCondition;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.assassin.axe.Dash;
import me.rey.core.classes.abilities.assassin.axe.Flash;
import me.rey.core.classes.abilities.assassin.bow.BlindingArrow;
import me.rey.core.classes.abilities.assassin.bow.Disarm;
import me.rey.core.classes.abilities.assassin.bow.Premonition;
import me.rey.core.classes.abilities.assassin.passive_a.SmokeBomb;
import me.rey.core.classes.abilities.assassin.passive_b.BlitzStrikes;
import me.rey.core.classes.abilities.assassin.passive_b.SickeningStrikes;
import me.rey.core.classes.abilities.assassin.sword.Evade;
import me.rey.core.classes.abilities.bandit.axe.Blink;
import me.rey.core.classes.abilities.bandit.axe.Leap;
import me.rey.core.classes.abilities.bandit.passive_a.BruteForce;
import me.rey.core.classes.abilities.bandit.passive_a.Recall;
import me.rey.core.classes.abilities.bandit.passive_a.Scream;
import me.rey.core.classes.abilities.bandit.passive_b.Backstab;
import me.rey.core.classes.abilities.bandit.passive_b.RapidSuccession;
import me.rey.core.classes.abilities.bandit.sword.Daze;
import me.rey.core.classes.abilities.bandit.sword.Vortex;
import me.rey.core.classes.abilities.brute.axe.Adrenaline;
import me.rey.core.classes.abilities.brute.axe.Takedown;
import me.rey.core.classes.abilities.brute.passive_a.Bloodlust;
import me.rey.core.classes.abilities.brute.passive_a.Intimidation;
import me.rey.core.classes.abilities.brute.passive_a.Stampede;
import me.rey.core.classes.abilities.brute.passive_b.Colossus;
import me.rey.core.classes.abilities.brute.passive_b.CripplingBlow;
import me.rey.core.classes.abilities.brute.passive_b.Overwhelm;
import me.rey.core.classes.abilities.brute.sword.IronHook;
import me.rey.core.classes.abilities.brute.sword.Throw;
import me.rey.core.classes.abilities.knight.axe.Charge;
import me.rey.core.classes.abilities.knight.axe.HoldPosition;
import me.rey.core.classes.abilities.knight.axe.ShieldSmash;
import me.rey.core.classes.abilities.knight.passive_b.Advantage;
import me.rey.core.classes.abilities.knight.passive_b.FatalBlow;
import me.rey.core.classes.abilities.knight.passive_b.Recover;
import me.rey.core.classes.abilities.knight.passive_b.Revenge;
import me.rey.core.classes.abilities.knight.sword.Immunity;
import me.rey.core.classes.abilities.knight.sword.Thrust;
import me.rey.core.classes.abilities.mage.axe.FireBlast;
import me.rey.core.classes.abilities.mage.axe.IcePrison;
import me.rey.core.classes.abilities.mage.axe.LightningOrb;
import me.rey.core.classes.abilities.mage.passive_a.ArcticZone;
import me.rey.core.classes.abilities.mage.passive_a.Void;
import me.rey.core.classes.abilities.mage.passive_b.MagmaBlade;
import me.rey.core.classes.abilities.mage.passive_b.NullBlade;
import me.rey.core.classes.abilities.mage.passive_c.EnergyPool;
import me.rey.core.classes.abilities.mage.passive_c.EnergyRegeneration;
import me.rey.core.classes.abilities.mage.sword.Blaze;
import me.rey.core.classes.abilities.mage.sword.SnowFlurry;
import me.rey.core.classes.abilities.mage.sword.bolt.Bolt;
import me.rey.core.classes.abilities.marksman.bow.GrapplingArrow;
import me.rey.core.classes.abilities.marksman.sword.Escape;
import me.rey.core.classes.abilities.shaman.axe.Fissure;
import me.rey.core.classes.abilities.shaman.axe.Overgrown;
import me.rey.core.classes.abilities.shaman.axe.Synthesis;
import me.rey.core.classes.abilities.shaman.passive_a.Aromatherapy;
import me.rey.core.classes.abilities.shaman.passive_a.Thorns;
import me.rey.core.classes.abilities.shaman.spade.Miasma;
import me.rey.core.classes.abilities.shaman.spade.Paralysis;
import me.rey.core.classes.abilities.shaman.spade.Tornado;
import me.rey.core.classes.abilities.shaman.sword.StaticLazer;
import me.rey.core.classes.abilities.shaman.sword.Tremor;
import me.rey.core.classes.conditions.*;
import me.rey.core.combat.DamageHandler;
import me.rey.core.commands.Equip;
import me.rey.core.commands.Skill;
import me.rey.core.database.SQLManager;
import me.rey.core.events.*;
import me.rey.core.items.Consumable;
import me.rey.core.items.Glow;
import me.rey.core.items.ThrowingItem;
import me.rey.core.items.custom.Cobweb;
import me.rey.core.items.custom.MushroomSoup;
import me.rey.core.items.custom.WaterBottle;
import me.rey.core.players.PlayerRunnableHandler;
import me.rey.core.players.combat.PlayerHitCache;
import me.rey.core.pvp.Build;
import me.rey.core.utils.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.*;
import java.util.logging.Logger;

/*
 * This is the official Warriors plugin
 *
 * @author	Rey, Kenchh
 * @version 1.0.0
 * @since	2020-01-01
 */
public class Warriors extends Module {

    // Cache
    public static ArrayList<Ability> abilityCache;
    public static ArrayList<Consumable> consumableCache;
    public static List<ThrowingItem> throwingCache;
    public static ArrayList<ClassCondition> classConditions;
    public static Map<Player, ClassType> userCache;
    public static Map<UUID, HashMap<ClassType, Build[]>> buildCache;
    public static PlayerHitCache hitCache;
    public static boolean isTestMode;
    //Config
    public static boolean deathMessagesEnabled;
    static Warriors instance;
    private final Logger logger = this.getPlugin().getLogger();
    PluginManager pm = Bukkit.getServer().getPluginManager();
    private SQLManager sql;

    public Warriors(final JavaPlugin plugin) {
        super("Warriors", plugin);
    }

    /*
     * Return Main class instance
     */
    public static Warriors getInstance() {
        return instance;
    }

    /*
     * Called whenever the plugin is ENABLED.
     */
    @Override
    protected void onEnable() {
        instance = this;
        isTestMode = false;

        this.initDatabase();

        this.registerCommands();
        this.registerListeners();
        this.registerConsumables();
        this.registerThrowingItems();

        new PlayerRunnableHandler(this.getPlugin());
        new ClassHandler();

        userCache = new HashMap<>();
        this.initAbilityCache();
        this.initConditionCache();

        this.registerEnchantments();

        // HUNGER RATES
        deathMessagesEnabled = this.getPlugin().getConfig().getBoolean("kill-death-messages");
        this.logger.warning("Search for any errors in CONSOLE, they may be fatal to player gameplay");
        buildCache = this.getSQLManager().loadAllBuilds();

        ClassType.scheduleChecks();
    }

    /*
     * Called whenever the plugin is DISABLED.
     */
    @Override
    protected void onDisable() {

        userCache.clear();
        userCache = null;

        buildCache.clear();
        buildCache = null;


        this.sql.onDisable();
    }

    private void initDatabase() {
        this.sql = new SQLManager(this);

        Text.log("=====================================");
        Text.log("");
        Text.log("&5&lMySQL database connected!");
        Text.log("&f&lWarriors now has access to all player data");
        Text.log("");
        Text.log("=====================================");
    }

    public SQLManager getSQLManager() {
        return this.sql;
    }

    public PlayerHitCache getHitCache() {
        if (hitCache == null) {
            hitCache = new PlayerHitCache(this);
        }
        return hitCache;
    }

    /*
     * Register commands
     * FORMAT: this.getCommand("cmd").setExecutor(new CommandExecutorClass());
     */
    public void registerCommands() {
        this.getPlugin().getCommand("skill").setExecutor(new Skill());
        this.getPlugin().getCommand("equip").setExecutor(new Equip());
    }

    /*
     * Register listeners
     * FORMAT: pm.registerEvents(new ListenerClass(), this);
     */
    public void registerListeners() {
        this.pm.registerEvents(new ClassEditorClickEvent(), this.getPlugin());
        this.pm.registerEvents(new BuildHandler(), this.getPlugin());
        this.pm.registerEvents(new PlayerDeathHandler(), this.getPlugin());
        this.pm.registerEvents(new DurabilityChangeEvent(), this.getPlugin());
        this.pm.registerEvents(new DamageHandler(), this.getPlugin());
        this.pm.registerEvents(new PlayerInteractChecker(), this.getPlugin());
        this.pm.registerEvents(new ServerEvents(), this.getPlugin());
    }

    /*
     * Register Consumables
     */
    public void registerConsumables() {
        consumableCache = new ArrayList<>(Arrays.asList(
                new MushroomSoup(),
                new WaterBottle()
        ));

        for (final Consumable cur : consumableCache) {
            this.pm.registerEvents(cur, this.getPlugin());
        }
    }

    /*
     * Random Throwing Items
     */
    public void registerThrowingItems() {
        throwingCache = new ArrayList<>(Arrays.asList(
                new Cobweb()
        ));

        for (final ThrowingItem cur : throwingCache) {
            this.pm.registerEvents(cur, this.getPlugin());
            Text.log(String.format("Successfully loaded throwing item [%s]",
                    cur.getName()));
        }
    }

    /*
     * Initialize all ability listeners
     */
    public void initConditionCache() {
        classConditions = new ArrayList<>(Arrays.asList(
                // SHAMAN
                new ThePowerOfNature(),
                // NINJA
                new Vigour(),
                // MAGE
                new SummoningDarkEnergy(),
                // MARKSMAN
                // KNIGHT
                new Balanced(),
                // BRUTE
                // BANDIT
                new Lightweight()
        ));

        for (final ClassCondition condition : classConditions) {
            Bukkit.getPluginManager().registerEvents(condition, this.getPlugin());
            ClassCondition.registerCondition(condition);
        }
    }

    /*
     * Initialize all ability listeners
     */
    public void initAbilityCache() {
        abilityCache = new ArrayList<>(Arrays.asList(
                //BANDIT
                new BlindingArrow(),
                new Blink(),
                new BruteForce(),
                new Disarm(),
                //new HiddenAssault(),
                new Leap(),
                new Recall(),
                new RapidSuccession(),
                new Scream(),
                new SmokeBomb(),
                new Vortex(),
                new Daze(),

                //ASSASSIN
                new Backstab(),
                new BlitzStrikes(),
                new Dash(),
                new Evade(),
                new Flash(),
                new Premonition(),
                new SickeningStrikes(),

                //MAGE
                new ArcticZone(),
                new Blaze(),
                new EnergyRegeneration(),
                new EnergyPool(),
                new FireBlast(),
                new Fissure(),
                new IcePrison(),
                new LightningOrb(),
                new Bolt(),
                new MagmaBlade(),
                new Void(),
                new SnowFlurry(),

                //SHAMAN
                new Tremor(),
                new StaticLazer(),
                new Aromatherapy(),
                new Miasma(),
                new NullBlade(),
                new Overgrown(),
                new Paralysis(),
                new Synthesis(),
                new Thorns(),
                new Tornado(),

                //BERSERKER

                //MARKSMAN
                new Escape(),
                new GrapplingArrow(),

                //KNIGHT
                new Advantage(),
                new Charge(),
                new FatalBlow(),
                new HoldPosition(),
                new Immunity(),
                new Recover(),
                new Revenge(),
                new ShieldSmash(),
                new Thrust(),

                //BRUTE
                new Adrenaline(),
                new Bloodlust(),
                new CripplingBlow(),
                new Colossus(),
                new IronHook(),
                new Takedown(),
                new Stampede(),
                new Intimidation(),
                new Overwhelm(),
                new Throw()
        ));

        for (final Ability ability : abilityCache) {
            Bukkit.getPluginManager().registerEvents(ability, this.getPlugin());
            Text.log(String.format("Successfully loaded ability [%s] [ID: %s] [Class: %s]",
                    ability.getName(),
                    ability.getIdLong(),
                    ability.getClassType().getName()));
        }
    }

    public void registerAbility(final Ability ability) {
        if (abilityCache == null) {
            abilityCache = new ArrayList<>();
        }

        for (final Ability query : this.getAbilitiesInCache()) {
            if (query.getIdLong() == ability.getIdLong()) {
                Text.log("&4&lFAILED TO LOAD ABILITY " + ability.getName());
                this.getPlugin().getPluginLoader().disablePlugin(this.getPlugin());
                throw new AbilityIdentifierException("An ability already has ID: " + ability.getIdLong());
            }
        }

        abilityCache.add(ability);
        Bukkit.getPluginManager().registerEvents(ability, this.getPlugin());
    }

    public ArrayList<Ability> getAbilitiesInCache() {
        return abilityCache != null ? abilityCache : new ArrayList<>();
    }

    public ArrayList<Ability> getClassAbilities(final ClassType classType) {
        final ArrayList<Ability> abilities = new ArrayList<>();
        for (final Ability a : this.getAbilitiesInCache()) {
            if (a.getClassType().equals(classType)) {
                abilities.add(a);
            }
        }
        return abilities;
    }

    private void registerEnchantments() {

        try {

            final Field f = Enchantment.class.getDeclaredField("acceptingNew");
            f.setAccessible(true);
            f.set(null, true);
        } catch (final Exception e) {
            e.printStackTrace();
        }

        try {
            final Glow glow = new Glow(255);
            Enchantment.registerEnchantment(glow);
        } catch (final IllegalArgumentException e) {

        } catch (final Exception e) {
            e.printStackTrace();
        }

    }

    public boolean isTestMode() {
        return isTestMode;
    }

    public void setTestMode(final boolean testMode) {
        isTestMode = testMode;
        Bukkit.broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + ChatColor.MAGIC + "00" + ChatColor.RED + "" + ChatColor.BOLD + " THIS SERVER IS " + (testMode ? "NOW" : "NO LONGER") + " IN TEST MODE " + ChatColor.MAGIC + "00");
    }
}
