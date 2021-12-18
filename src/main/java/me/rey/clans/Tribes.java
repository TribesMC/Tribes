package me.rey.clans;

import me.rey.Module;
import me.rey.clans.clans.Clan;
import me.rey.clans.clans.WarriorsTeamHandler;
import me.rey.clans.commands.*;
import me.rey.clans.commands.base.Base;
import me.rey.clans.commands.staff.*;
import me.rey.clans.commands.staff.loggers.Loggers;
import me.rey.clans.commands.test.FreezeEnergy;
import me.rey.clans.commands.test.TestMode;
import me.rey.clans.currency.PickupBalance;
import me.rey.clans.database.SQLManager;
import me.rey.clans.database.local.LocalSQLiteManager;
import me.rey.clans.events.*;
import me.rey.clans.features.*;
import me.rey.clans.features.combatlogger.CombatLogger;
import me.rey.clans.features.hologram.HologramManager;
import me.rey.clans.features.incognito.IncognitoManager;
import me.rey.clans.features.punishments.PunishmentManager;
import me.rey.clans.gui.ConfirmationGUI;
import me.rey.clans.items.crafting.*;
import me.rey.clans.items.crafting.marksman.*;
import me.rey.clans.items.special.SpecialItemUpdater;
import me.rey.clans.playerdisplay.PlayerInfo;
import me.rey.clans.shops.ShopEvents;
import me.rey.clans.siege.SiegeTriggerEvent;
import me.rey.clans.siege.bombs.Bomb;
import me.rey.clans.siege.bombs.Bomb2;
import me.rey.clans.siege.bombs.CustomExplosion;
import me.rey.clans.utils.UtilFocus;
import me.rey.clans.worldevents.ClansEvents;
import me.rey.core.utils.Text;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public class Tribes extends Module {

    public static HashMap<UUID, UUID> adminFakeClans;
    public static Set<String> safeZoneCoords;
    public static ArrayList<Clan> clans;
    public static HashMap<UUID, HashMap<String, Object>> playerdata;
    private static Tribes instance;
    public HashMap<Chunk, UUID> territory;
    public HashMap<Chunk, Long> territoryCooldowns;
    PluginManager pm = Bukkit.getPluginManager();
    List<ClansCommand> commands;
    private SQLManager sql;
    private LocalSQLiteManager localSqlite;
    private ServerParser stp;
    private PlayerInfo info;
    private PvpTimer pvpTimer;
    private SpecialItemUpdater specialItemUpdater;
    private CombatLogger combatLogger;
    private IncognitoManager incognito;
    private HologramManager hologramManager;
    private PunishmentManager punishmentManager;

    public Tribes(final JavaPlugin plugin) {
        super("Tribes", plugin);
    }

    public static Tribes getInstance() {
        return instance;
    }

    /*
     * Called on plugin enable
     */
    @Override
    protected void onEnable() {
        instance = this;
        adminFakeClans = new HashMap<>();

        this.initDatabase();
        this.localSqlite = new LocalSQLiteManager();

        this.stp = new ServerParser();
        this.stp.init();
        this.pm.registerEvents(this.stp, this.getPlugin());

        safeZoneCoords = this.getSQLManager().getSafeZones();
        this.territory = new HashMap<>();
        this.territory.putAll(this.getSQLManager().loadTerritories());
        this.territoryCooldowns = new HashMap<>();
        clans = this.getSQLManager().getClans();
        playerdata = this.getSQLManager().getAllPlayerData();

        this.specialItemUpdater = new SpecialItemUpdater(this);
        this.specialItemUpdater.onEnable();

        this.registerCommands();
        this.registerListeners();

        new StaffChatGateway();

        /*
         * ENERGY HANDLER
         */
        final EnergyHandler energyHandler = new EnergyHandler(this.getSQLManager());
        energyHandler.start();

        /*
         * SERVER CLANS
         */
        this.getSQLManager().loadServerClans();

        /*
         * RECIPES
         */
        new MHelmet1().init().register();
        new MHelmet2().init().register();
        new MChestplate1().init().register();
        new MChestplate2().init().register();
        new MLeggings1().init().register();
        new MLeggings2().init().register();
        new MBoots1().init().register();
        new MBoots2().init().register();

        new PowerSword().init().register();
        new PowerAxe().init().register();
        new BoosterSword().init().register();
        new BoosterAxe().init().register();

        new IronDoor1().init().register();
        new IronTrapDoor1().init().register();


        /*
         * SCOREBOARD
         */
        this.info = new PlayerInfo();
        this.pm.registerEvents(this.info, this.getPlugin());
        for (final Player online : Bukkit.getOnlinePlayers()) {
            this.info.setupSidebar(online);
        }

        /*
         * NAMETAGS
         */
        new BukkitRunnable() {
            @Override
            public void run() {
                Tribes.this.info.updateNameTagsForAll();
                Tribes.this.info.updateTabListForAll();
            }
        }.runTaskTimerAsynchronously(this.getPlugin(), 0, 5);

        this.pm.registerEvents(new WarriorsTeamHandler(), this.getPlugin());

        /*
         * WORLD EVENTS
         */
        final ClansEvents worldEvents = new ClansEvents();
        worldEvents.register();
        this.pm.registerEvents(worldEvents, this.getPlugin());

        /*
         * TNT
         */
        CustomExplosion.BOMB = new Bomb();
        CustomExplosion.C4 = new Bomb2();

        /* Setting difficulty to easy, because hunger too much xd */
        for (final World w : Bukkit.getServer().getWorlds()) {
            w.setDifficulty(Difficulty.EASY);
        }

        /*
         * COMBAT LOGGER
         */

        punishmentManager = new PunishmentManager();
        punishmentManager.onEnable();

        incognito = new IncognitoManager();
        incognito.onEnable();

        combatLogger = new CombatLogger();
        combatLogger.onEnable();

        hologramManager = new HologramManager();
        hologramManager.onEnable();
    }

    /*
     * Called on plugin disable
     */
    @Override
    protected void onDisable() {

        /* Resetting all fields ores in cache */
        Fields.fieldsOres.forEach(Fields.FieldsOre::replaceForcefully);

        hologramManager.onDisable();
        combatLogger.onDisable();
        incognito.onDisable();
        punishmentManager.onDisable();

        this.sql.onDisable();
    }

    private SQLManager initDatabase() {
        this.sql = new SQLManager(this);

        Text.log("=====================================");
        Text.log("");
        Text.log("&a&lMySQL database connected!");
        Text.log("&f&lClans now has access to all player data");
        Text.log("");
        Text.log("=====================================");

        return this.sql;
    }

    public SQLManager getSQLManager() {
        return this.sql;
    }

    public LocalSQLiteManager getLocalSQLiteManager() {
        return this.localSqlite;
    }

    public PvpTimer getPvpTimer() {
        return this.pvpTimer;
    }

    /*
     * Command registration
     * getCommand("command").setExector(new Class());
     */
    public void registerCommands() {
        this.commands = new ArrayList<>(Arrays.asList(
                new Base(),
                new ClanChat(),
                new AllyChat(),
                new Focus(),
                new Unfocus(),
                new Kill(),
                new PvpTimerCmd(),
                new PvpEnable(),
                new SpectatorObserve(),
                new FreezeEnergy(),
                new TestMode(),
                new Legendary(),
                new Loggers(),
                new Punish(),
                new PunishWipe(),
                new PunishHistory()
        ));
    }


    /*
     * Listener registration
     * PluginManager#registerEvents(new Class(), this);
     */
    public void registerListeners() {
        this.pm.registerEvents(new PlayerChat(), this.getPlugin());
        this.pm.registerEvents(new PlayerJoin(), this.getPlugin());
        this.pm.registerEvents(new PlayerDeath(), this.getPlugin());
        this.pm.registerEvents(new TerritoryChange(), this.getPlugin());
        this.pm.registerEvents(new ClaimProtection(), this.getPlugin());
        this.pm.registerEvents(new CombatBaseRelation(), this.getPlugin());
        this.pm.registerEvents(new ShopEvents(), this.getPlugin());
        this.pm.registerEvents(new SiegeTriggerEvent(), this.getPlugin());
        this.pm.registerEvents(new UtilFocus(), this.getPlugin());
        this.pm.registerEvents(new ServerEvents(), this.getPlugin());
        this.pm.registerEvents(new ConfirmationGUI(), this.getPlugin());
        this.pm.registerEvents(new PickupBalance(), this.getPlugin());
        this.pm.registerEvents(new Fields(), this.getPlugin());
        this.pm.registerEvents(this.pvpTimer = new PvpTimer(), this.getPlugin());
    }

    public World getClansWorld() {
        return Bukkit.getServer().getWorld(this.getPlugin().getConfig().getString("clans-world"));
    }

    public Clan getClanFromTerritory(final Chunk c) {

        for (final Chunk chunk : this.territory.keySet()) {
            if (c.getX() == chunk.getX() && c.getZ() == chunk.getZ()) {

                final UUID uuid = this.territory.get(chunk);
                if (!this.getSQLManager().clanExists(uuid)) {
                    this.territory.remove(chunk);
                    return null;
                }

                return this.getClan(uuid);
            }
        }

        return null;
    }

    public boolean removeTerritory(final Chunk c) {
        if (this.getClanFromTerritory(c) == null) {
            return false;
        }

        for (final Chunk chunk : ((HashMap<Chunk, UUID>) this.territory.clone()).keySet()) {
            if (c.getX() == chunk.getX() && c.getZ() == chunk.getZ()) {
                this.territory.remove(chunk);
                return true;
            }
        }

        return false;
    }

    public void updateTerritoryCooldownCache() {
        for (final Map.Entry<Chunk, Long> entry : this.territoryCooldowns.entrySet()) {
            if (System.currentTimeMillis() > entry.getValue()) {
                this.territoryCooldowns.remove(entry.getKey(), entry.getValue());
            }
        }
    }

    public Clan getClan(final UUID uuid) {
        for (final Clan c : clans) {
            if (c.getUniqueId().equals(uuid)) {
                return c;
            }
        }
        return null;
    }

    public Clan getClan(final String name) {
        for (final Clan c : clans) {
            if (c.getName().equalsIgnoreCase(name)) {
                return c;
            }
        }
        return null;
    }

    public ServerParser getServerParser() {
        return this.stp;
    }

    public SpecialItemUpdater getSpecialItemUpdater() {
        return this.specialItemUpdater;
    }

    public boolean isInSafezone(final Location loc) {
        final String self = String.format("%s;%s;%s", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        return Tribes.safeZoneCoords.contains(self);
    }

    public CombatLogger getCombatLogger() {
        return combatLogger;
    }

    public IncognitoManager getIncognito() {
        return incognito;
    }

    public HologramManager getHologramManager() {
        return hologramManager;
    }

    public PunishmentManager getPunishmentManager() {
        return punishmentManager;
    }
}
