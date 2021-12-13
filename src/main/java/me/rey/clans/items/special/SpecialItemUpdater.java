package me.rey.clans.items.special;

import me.rey.clans.Tribes;
import me.rey.clans.items.special.data.SpecialItemStatusChangeEvent;
import me.rey.clans.items.special.epics.EpicItem;
import me.rey.clans.items.special.epics.MysticHoeListener;
import me.rey.clans.items.special.epics.PrimordialAxeListener;
import me.rey.clans.items.special.epics.RunedPickaxeListener;
import me.rey.clans.items.special.legends.*;
import me.rey.clans.items.special.miscs.DebugStickListener;
import me.rey.clans.items.special.miscs.DebugWeaponListener;
import me.rey.clans.items.special.miscs.MiscItem;
import me.rey.core.players.User;
import me.rey.core.utils.Activatable;
import me.rey.core.utils.UtilFile;
import me.rey.core.utils.UtilTool;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class SpecialItemUpdater implements Listener, Activatable {

    private final Tribes plugin;
    private final Map<UUID, Boolean> raining;
    public Map<LegendaryItem.LegendType, LegendaryItem> legends;
    public Map<EpicItem.EpicType, EpicItem> epics;
    public Map<MiscItem.MiscType, MiscItem> miscs;
    public Map<SpecialItem, Boolean> specialItemStatus;
    BukkitTask updater;
    BukkitRunnable statusChecker;
    BukkitTask statusCheckingTask;
    private File toolSettings;

    public SpecialItemUpdater(final Tribes plugin) {
        this.plugin = plugin;
        this.raining = new HashMap<>();

        this.legends = new HashMap<>();
        this.epics = new HashMap<>();
        this.miscs = new HashMap<>();
        this.specialItemStatus = new HashMap<>();
    }

    @Override
    public void onEnable() {
        this.legends.put(LegendaryItem.LegendType.ALLIGATORS_TOOTH, new AlligatorsToothListener(this.plugin));
        this.legends.put(LegendaryItem.LegendType.GIANTS_BROADSWORD, new GiantsBroadswordListener(this.plugin));
        this.legends.put(LegendaryItem.LegendType.HYPERAXE, new HyperaxeListener(this.plugin));
        this.legends.put(LegendaryItem.LegendType.KNIGHTS_GREATLANCE, new KnightsGreatlanceListener(this.plugin));
        this.legends.put(LegendaryItem.LegendType.MAGNETIC_MAUL, new MagneticMaulListener(this.plugin));
        this.legends.put(LegendaryItem.LegendType.MERIDIAN_SCEPTER, new MeridianScepterListener(this.plugin));
        this.legends.put(LegendaryItem.LegendType.SCYTHE_OF_THE_FALLEN_LORD, new ScytheOfTheFallenLordListener(this.plugin));
        this.legends.put(LegendaryItem.LegendType.WINDBLADE, new WindbladeListener(this.plugin));

        this.epics.put(EpicItem.EpicType.MYSTIC_HOE, new MysticHoeListener(this.plugin));
        this.epics.put(EpicItem.EpicType.PRIMORDIAL_AXE, new PrimordialAxeListener(this.plugin));
        this.epics.put(EpicItem.EpicType.RUNED_PICKAXE, new RunedPickaxeListener(this.plugin));

        this.miscs.put(MiscItem.MiscType.DEBUG_STICK, new DebugStickListener(this.plugin));
        this.miscs.put(MiscItem.MiscType.DEBUG_WEAPON, new DebugWeaponListener(this.plugin));

        final Map<String, Object> defaults = new HashMap<>();
        for (final SpecialItem item : this.getAllSpecialItems()) {
            defaults.put(item.getItemId(), true);
        }

        final File folder = UtilFile.getFolder("data", this.plugin.getPlugin().getDataFolder());
        this.toolSettings = UtilFile.getYamlFile("specialitems.yml", folder, defaults);
        final YamlConfiguration toolSettingsYml = YamlConfiguration.loadConfiguration(this.toolSettings);
        for (final String key : toolSettingsYml.getKeys(false)) {
            final SpecialItem item = this.getSpecialItemById(key);
            if (item == null) {
                continue;
            }
            try {
                this.specialItemStatus.put(item, toolSettingsYml.getBoolean(key, true));
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
        try {
            toolSettingsYml.save(this.toolSettings);
        } catch (final IOException e) {
            e.printStackTrace();
        }

        this.updater = new BukkitRunnable() {
            @Override
            public void run() {
                for (final LegendaryItem item : SpecialItemUpdater.this.legends.values()) {
                    item.update();
                }
                for (final EpicItem item : SpecialItemUpdater.this.epics.values()) {
                    item.update();
                }
                for (final MiscItem item : SpecialItemUpdater.this.miscs.values()) {
                    item.update();
                }
            }
        }.runTaskTimer(this.plugin.getPlugin(), 2, 0);

        this.statusChecker = new BukkitRunnable() {
            @Override
            public void run() {
                for (final Player player : Bukkit.getOnlinePlayers()) {
                    for (int i = 0; i < player.getInventory().getSize(); i++) {
                        for (final LegendaryItem legend : SpecialItemUpdater.this.legends.values()) {
                            if (!legend.isOfArchetype(player.getInventory().getItem(i), legend.getLegendType())) {
                                continue;
                            }
                            final ItemStack stack = player.getInventory().getItem(i);
                            final Integer disabledFromItemByte = (Integer) UtilTool.getNBTTag(stack, "ClansDisabled", UtilTool.NBTTagType.INT);
                            final Boolean disabledFromItem = disabledFromItemByte != null ? disabledFromItemByte == 1 : null;

                            if (!SpecialItemUpdater.this.specialItemStatus.getOrDefault(legend, true) && disabledFromItem == SpecialItemUpdater.this.specialItemStatus.getOrDefault(legend, true)) {
                                new User(player).sendMessageWithPrefix("Tribes", "The &s" + legend.getDisplayName() + "&r in your inventory has been disabled by a staff member!");
                                ItemStack clone = legend.getItems().getRight().stack;

                                clone.setAmount(stack.getAmount());
                                final Integer durability = (Integer) UtilTool.getNBTTag(stack, "ClansDurability", UtilTool.NBTTagType.INT);
                                final Integer maxDurability = (Integer) UtilTool.getNBTTag(stack, "ClansMaxDurability", UtilTool.NBTTagType.INT);

                                clone = UtilTool.setNBTTag(clone, "ClansDisabled", 1, UtilTool.NBTTagType.BYTE);

                                if (durability != null) {
                                    clone = UtilTool.setNBTTag(clone, "ClansDurability", durability, UtilTool.NBTTagType.INT);
                                }
                                if (maxDurability != null) {
                                    clone = UtilTool.setNBTTag(clone, "ClansMaxDurability", maxDurability, UtilTool.NBTTagType.INT);
                                }
                                player.getInventory().setItem(i, clone);
                            } else if (SpecialItemUpdater.this.specialItemStatus.getOrDefault(legend, true) && disabledFromItem == SpecialItemUpdater.this.specialItemStatus.getOrDefault(legend, true)) {
                                new User(player).sendMessageWithPrefix("Tribes", "The &s" + legend.getDisplayName() + "&r in your inventory has been re-enabled by a staff member!");
                                ItemStack clone = legend.getItems().getLeft().stack;

                                clone.setAmount(stack.getAmount());
                                final Integer durability = (Integer) UtilTool.getNBTTag(stack, "ClansDurability", UtilTool.NBTTagType.INT);
                                final Integer maxDurability = (Integer) UtilTool.getNBTTag(stack, "ClansMaxDurability", UtilTool.NBTTagType.INT);

                                clone = UtilTool.setNBTTag(clone, "ClansDisabled", 0, UtilTool.NBTTagType.BYTE);

                                if (durability != null) {
                                    clone = UtilTool.setNBTTag(clone, "ClansDurability", durability, UtilTool.NBTTagType.INT);
                                }
                                if (maxDurability != null) {
                                    clone = UtilTool.setNBTTag(clone, "ClansMaxDurability", maxDurability, UtilTool.NBTTagType.INT);
                                }
                                player.getInventory().setItem(i, clone);
                            }
                        }

                        for (final EpicItem epic : SpecialItemUpdater.this.epics.values()) {
                            if (!epic.isOfArchetype(player.getInventory().getItem(i), epic.getEpicType())) {
                                continue;
                            }
                            final ItemStack stack = player.getInventory().getItem(i);
                            final Integer disabledFromItemByte = (Integer) UtilTool.getNBTTag(stack, "ClansDisabled", UtilTool.NBTTagType.INT);
                            final Boolean disabledFromItem = disabledFromItemByte != null ? disabledFromItemByte == 1 : null;

                            if (!SpecialItemUpdater.this.specialItemStatus.getOrDefault(epic, true) && disabledFromItem == SpecialItemUpdater.this.specialItemStatus.getOrDefault(epic, true)) {
                                new User(player).sendMessageWithPrefix("Tribes", "The &s" + epic.getDisplayName() + "&r in your inventory has been disabled by a staff member!");
                                ItemStack clone = epic.getItems().getRight().stack;

                                clone.setAmount(stack.getAmount());
                                final Integer durability = (Integer) UtilTool.getNBTTag(stack, "ClansDurability", UtilTool.NBTTagType.INT);
                                final Integer maxDurability = (Integer) UtilTool.getNBTTag(stack, "ClansMaxDurability", UtilTool.NBTTagType.INT);

                                clone = UtilTool.setNBTTag(clone, "ClansDisabled", 1, UtilTool.NBTTagType.BYTE);

                                if (durability != null) {
                                    clone = UtilTool.setNBTTag(clone, "ClansDurability", durability, UtilTool.NBTTagType.INT);
                                }
                                if (maxDurability != null) {
                                    clone = UtilTool.setNBTTag(clone, "ClansMaxDurability", maxDurability, UtilTool.NBTTagType.INT);
                                }
                                player.getInventory().setItem(i, clone);
                            } else if (SpecialItemUpdater.this.specialItemStatus.getOrDefault(epic, true) && disabledFromItem == SpecialItemUpdater.this.specialItemStatus.getOrDefault(epic, true)) {
                                new User(player).sendMessageWithPrefix("Tribes", "The &s" + epic.getDisplayName() + "&r in your inventory has been re-enabled by a staff member!");
                                ItemStack clone = epic.getItems().getLeft().stack;

                                clone.setAmount(stack.getAmount());
                                final Integer durability = (Integer) UtilTool.getNBTTag(stack, "ClansDurability", UtilTool.NBTTagType.INT);
                                final Integer maxDurability = (Integer) UtilTool.getNBTTag(stack, "ClansMaxDurability", UtilTool.NBTTagType.INT);

                                clone = UtilTool.setNBTTag(clone, "ClansDisabled", 0, UtilTool.NBTTagType.BYTE);

                                if (durability != null) {
                                    clone = UtilTool.setNBTTag(clone, "ClansDurability", durability, UtilTool.NBTTagType.INT);
                                }
                                if (maxDurability != null) {
                                    clone = UtilTool.setNBTTag(clone, "ClansMaxDurability", maxDurability, UtilTool.NBTTagType.INT);
                                }
                                player.getInventory().setItem(i, clone);
                            }
                        }

                        for (final MiscItem misc : SpecialItemUpdater.this.miscs.values()) {
                            if (!misc.isOfArchetype(player.getInventory().getItem(i), misc.getMiscType())) {
                                continue;
                            }
                            final ItemStack stack = player.getInventory().getItem(i);
                            final Integer disabledFromItemByte = (Integer) UtilTool.getNBTTag(stack, "ClansDisabled", UtilTool.NBTTagType.INT);
                            final Boolean disabledFromItem = disabledFromItemByte != null ? disabledFromItemByte == 1 : null;

                            if (!SpecialItemUpdater.this.specialItemStatus.getOrDefault(misc, true) && disabledFromItem == SpecialItemUpdater.this.specialItemStatus.getOrDefault(misc, true)) {
                                new User(player).sendMessageWithPrefix("Tribes", "The &s" + misc.getDisplayName() + "&r in your inventory has been disabled by a staff member!");
                                ItemStack clone = misc.getItems().getRight().stack;

                                clone.setAmount(stack.getAmount());
                                final Integer durability = (Integer) UtilTool.getNBTTag(stack, "ClansDurability", UtilTool.NBTTagType.INT);
                                final Integer maxDurability = (Integer) UtilTool.getNBTTag(stack, "ClansMaxDurability", UtilTool.NBTTagType.INT);

                                clone = UtilTool.setNBTTag(clone, "ClansDisabled", 1, UtilTool.NBTTagType.BYTE);

                                if (durability != null) {
                                    clone = UtilTool.setNBTTag(clone, "ClansDurability", durability, UtilTool.NBTTagType.INT);
                                }
                                if (maxDurability != null) {
                                    clone = UtilTool.setNBTTag(clone, "ClansMaxDurability", maxDurability, UtilTool.NBTTagType.INT);
                                }
                                player.getInventory().setItem(i, clone);
                            } else if (SpecialItemUpdater.this.specialItemStatus.getOrDefault(misc, true) && disabledFromItem == SpecialItemUpdater.this.specialItemStatus.getOrDefault(misc, true)) {
                                new User(player).sendMessageWithPrefix("Tribes", "The &s" + misc.getDisplayName() + "&r in your inventory has been re-enabled by a staff member!");
                                ItemStack clone = misc.getItems().getLeft().stack;

                                clone.setAmount(stack.getAmount());
                                final Integer durability = (Integer) UtilTool.getNBTTag(stack, "ClansDurability", UtilTool.NBTTagType.INT);
                                final Integer maxDurability = (Integer) UtilTool.getNBTTag(stack, "ClansMaxDurability", UtilTool.NBTTagType.INT);

                                clone = UtilTool.setNBTTag(clone, "ClansDisabled", 0, UtilTool.NBTTagType.BYTE);

                                if (durability != null) {
                                    clone = UtilTool.setNBTTag(clone, "ClansDurability", durability, UtilTool.NBTTagType.INT);
                                }
                                if (maxDurability != null) {
                                    clone = UtilTool.setNBTTag(clone, "ClansMaxDurability", maxDurability, UtilTool.NBTTagType.INT);
                                }
                                player.getInventory().setItem(i, clone);
                            }
                        }
                    }
                }
            }
        };
        this.statusCheckingTask = this.statusChecker.runTaskTimer(this.plugin.getPlugin(), 2, 200);

        Bukkit.getPluginManager().registerEvents(this, this.plugin.getPlugin());
    }

    @Override
    public void onDisable() {

    }

    public List<SpecialItem> getAllSpecialItems() {
        final List<SpecialItem> items = new ArrayList<>();
        items.addAll(this.legends.values());
        items.addAll(this.epics.values());
        items.addAll(this.miscs.values());
        return items;
    }

    public SpecialItem getSpecialItemById(final String itemId) {
        for (final SpecialItem item : this.getAllSpecialItems()) {
            if (item.getItemId().equals(itemId)) {
                return item;
            }
        }
        return null;
    }

    public boolean isDisabled(final SpecialItem item) {
        return !this.specialItemStatus.getOrDefault(item, true);
    }

    public void setDisabled(final SpecialItem item, final boolean disabled) {
        final YamlConfiguration toolSettingsYml = YamlConfiguration.loadConfiguration(this.toolSettings);
        toolSettingsYml.set(item.getItemId(), !disabled);
        try {
            toolSettingsYml.save(this.toolSettings);
        } catch (final IOException e) {
            e.printStackTrace();
        }

        this.specialItemStatus.put(item, !disabled);
        Bukkit.getPluginManager().callEvent(new SpecialItemStatusChangeEvent(item, !disabled));
    }

    @EventHandler
    private void onStatusChange(final SpecialItemStatusChangeEvent event) {
        this.specialItemStatus.put(event.item, event.status);
        this.statusChecker.run();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onWeatherChange(final WeatherChangeEvent event) {
        if (event.isCancelled()) {
            return;
        }
        this.raining.put(event.getWorld().getUID(), event.toWeatherState());
    }

    public boolean isRaining(final World world) {
        return this.raining.getOrDefault(world.getUID(), false);
    }
}
