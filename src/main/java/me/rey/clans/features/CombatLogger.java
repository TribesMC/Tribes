package me.rey.clans.features;

import me.rey.clans.Tribes;
import me.rey.clans.clans.Clan;
import me.rey.clans.clans.ClansPlayer;
import me.rey.clans.database.SQLManager;
import me.rey.clans.events.CombatLoggerAlter;
import me.rey.clans.events.clans.ClanWarpointEvent;
import me.rey.clans.utils.NMSUtil;
import me.rey.core.events.customevents.combat.DamageEvent;
import me.rey.core.utils.Text;
import org.bukkit.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class CombatLogger implements Listener {

    private static final Map<UUID, CombatLoggerInfo> info;

    static {
        info = new HashMap<>();
    }

    private final SQLManager sql = Tribes.getInstance().getSQLManager();
    private CombatLoggerInfo clInfo;

    public CombatLogger(final Player player) {
        Bukkit.getPluginManager().registerEvents(this, Tribes.getInstance().getPlugin());

        this.clInfo = new CombatLoggerInfo(player);
        info.put(player.getUniqueId(), this.clInfo);

        if (this.clInfo.type == CombatLoggerType.SAFEZONE_SAFE) {
            return;
        }

        this.clInfo.location.setYaw(0);
        this.clInfo.location.setPitch(0);
        final Skeleton skeleton = (Skeleton) player.getWorld().spawnEntity(this.clInfo.location, EntityType.SKELETON);
        this.clInfo.skeleton = skeleton;
        skeleton.setMetadata("COMBATLOGGER", new FixedMetadataValue(Tribes.getInstance().getPlugin(), this.clInfo));
        skeleton.setSkeletonType(Skeleton.SkeletonType.NORMAL);

        skeleton.getEquipment().clear();
        skeleton.getEquipment().setHelmet(this.clInfo.armor[3]);
        skeleton.getEquipment().setChestplate(this.clInfo.armor[2]);
        skeleton.getEquipment().setLeggings(this.clInfo.armor[1]);
        skeleton.getEquipment().setBoots(this.clInfo.armor[0]);
        skeleton.getEquipment().setHelmetDropChance(0);
        skeleton.getEquipment().setChestplateDropChance(0);
        skeleton.getEquipment().setLeggingsDropChance(0);
        skeleton.getEquipment().setBootsDropChance(0);
        if (this.clInfo.heldItem.getType() != Material.AIR) {
            skeleton.getEquipment().setItemInHand(this.clInfo.heldItem);
        } else {
            skeleton.getEquipment().getItemInHand().setType(Material.AIR);
            skeleton.getEquipment().getItemInHand().setAmount(0);
        }
        skeleton.getEquipment().setItemInHandDropChance(0);

        try {
            final Object handle = skeleton.getClass().getMethod("getHandle").invoke(skeleton);
            final Constructor<?> nbtCompound = Objects.requireNonNull(NMSUtil.getNmsClass("NBTTagCompound")).getConstructor();
            final Object nbtTagCompound = nbtCompound.newInstance();
            handle.getClass().getMethod("c", nbtTagCompound.getClass()).invoke(handle, nbtTagCompound);

            nbtTagCompound.getClass().getMethod("setByte", String.class, byte.class).invoke(nbtTagCompound, "NoAI", (byte) 1);
            handle.getClass().getMethod("f", nbtTagCompound.getClass()).invoke(handle, nbtTagCompound);
        } catch (final IllegalAccessException | InvocationTargetException | NoSuchMethodException | InstantiationException e) {
            e.printStackTrace();
        }

        skeleton.teleport(this.clInfo.location, PlayerTeleportEvent.TeleportCause.PLUGIN);
        skeleton.setMaxHealth(player.getMaxHealth());
        skeleton.setHealth(player.getHealth());

        final ArmorStand stand = (ArmorStand) player.getWorld().spawnEntity(this.clInfo.location, EntityType.ARMOR_STAND);
        this.clInfo.stand = stand;
        stand.setArms(false);
        stand.setBasePlate(false);
        stand.setGravity(false);
        stand.setSmall(true);
        stand.setVisible(false);
        stand.setCustomNameVisible(true);

        final ArmorStand stand1 = (ArmorStand) player.getWorld().spawnEntity(this.clInfo.location, EntityType.ARMOR_STAND);
        this.clInfo.stand1 = stand1;
        stand1.setArms(false);
        stand1.setBasePlate(false);
        stand1.setGravity(false);
        stand1.setSmall(false);
        stand1.setVisible(false);
        stand1.setCustomNameVisible(true);
        stand1.setCustomName(ChatColor.YELLOW + this.clInfo.player.getName());

        for (final Player player1 : Bukkit.getOnlinePlayers()) {
            new ClansPlayer(player1).sendMessageWithPrefix("Combat", ChatColor.YELLOW + player.getName() + ChatColor.GRAY + " logged out while in combat!");
        }

        skeleton.setPassenger(stand);
        this.clInfo.placedAt = System.currentTimeMillis();
        this.clInfo.placedAt = System.currentTimeMillis();

        new BukkitRunnable() {
            long ticks = 0;
            boolean besiegeValueRed = false;

            @Override
            public void run() {
                if (CombatLogger.this.clInfo.player.isOnline()) {
                    skeleton.remove();
                    stand.remove();
                    stand1.remove();
                    CombatLogger.this.clInfo.disabled = true;
                    final Player onlinePlayer = (Player) CombatLogger.this.clInfo.player;
                    onlinePlayer.teleport(CombatLogger.this.clInfo.location, PlayerTeleportEvent.TeleportCause.PLUGIN);
                    onlinePlayer.setHealth(skeleton.getHealth());
                    this.cancel();
                    return;
                }
                if (skeleton.isDead() || skeleton.getHealth() <= 0) {
                    this.cancel();
                    return;
                }

                switch (CombatLogger.this.clInfo.type) {
                    case SAFE:
                    case UNSAFE:
                        if (System.currentTimeMillis() >= CombatLogger.this.clInfo.placedUntil) {
                            skeleton.remove();
                            stand.remove();
                            stand1.remove();
                            CombatLogger.this.clInfo.disabled = true;
                            this.cancel();
                            return;
                        } else {
                            try {
                                final String str = Long.toString(CombatLogger.this.clInfo.placedUntil - System.currentTimeMillis());
                                final double time = (double) Integer.parseInt(str.substring(0, str.length() - 2)) / 10;
                                stand.setCustomName(String.format("Combat logger expires in %s seconds!", time));
                            } catch (final Exception ignored) {
                            }
                        }
                        break;
                    case BESIEGE:
                        if (CombatLogger.this.clInfo.clan.isBeingSieged() || CombatLogger.this.clInfo.clan.isSiegingOther()) {
                            if (this.ticks > 0 && this.ticks % 10 == 0) {
                                this.besiegeValueRed = !this.besiegeValueRed;
                            }
                            stand.setCustomName((this.besiegeValueRed ? ChatColor.RED : ChatColor.WHITE) + "" + ChatColor.BOLD + "" + ChatColor.MAGIC + "||" + (this.besiegeValueRed ? ChatColor.RED : ChatColor.WHITE) + "" + ChatColor.BOLD + " ACTIVE BESIEGE " + ChatColor.MAGIC + "||");
                            break;
                        } else {
                            skeleton.remove();
                            stand.remove();
                            stand1.remove();
                            CombatLogger.this.clInfo.disabled = true;
                            this.cancel();
                            return;
                        }
                }

                skeleton.setVelocity(new Vector(0, 0, 0));
                stand.setVelocity(new Vector(0, 0, 0));
                stand1.setVelocity(new Vector(0, 0, 0));
                if (skeleton.getLocation() != CombatLogger.this.clInfo.location) {
                    skeleton.teleport(CombatLogger.this.clInfo.location);
                }
                if (stand1.getLocation() != skeleton.getLocation()) {
                    stand1.teleport(skeleton.getLocation());
                }
                this.ticks++;
            }
        }.runTaskTimer(Tribes.getInstance().getPlugin(), 0, 1);
    }

    @EventHandler
    private void onPluginStop(final PluginDisableEvent e) {
        for (final Map.Entry<UUID, CombatLoggerInfo> entry : info.entrySet()) {
            entry.getValue().skeleton.remove();
            entry.getValue().stand.remove();
            entry.getValue().stand1.remove();
            info.remove(entry.getKey(), entry.getValue());
        }
    }

    @EventHandler
    private void onEntityBurn(final EntityCombustEvent e) {
        if (e.getEntity().hasMetadata("COMBATLOGGER")) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    private void onPlayerJoin(final PlayerJoinEvent e) {
        final File file;
        final YamlConfiguration yml = YamlConfiguration.loadConfiguration(file = this.getLogged());
        final List<String> uuids = yml.getStringList("looted");
        if (uuids.contains(e.getPlayer().getUniqueId().toString())) {
            e.getPlayer().getInventory().clear();
            e.getPlayer().teleport(e.getPlayer().getWorld().getSpawnLocation());
            e.getPlayer().getEquipment().setHelmet(new ItemStack(Material.AIR));
            e.getPlayer().getEquipment().setChestplate(new ItemStack(Material.AIR));
            e.getPlayer().getEquipment().setLeggings(new ItemStack(Material.AIR));
            e.getPlayer().getEquipment().setBoots(new ItemStack(Material.AIR));
            e.getPlayer().setHealth(e.getPlayer().getMaxHealth());
            e.getPlayer().setFoodLevel(20);
            new ClansPlayer(e.getPlayer()).sendMessageWithPrefix("Death", "Your combat logger was killed last time you left!");

            uuids.remove(e.getPlayer().getUniqueId().toString());
            yml.set("looted", uuids);
            this.saveFile(yml, file);
        }
    }

    @EventHandler
    private void onEntityDeath(final DamageEvent e) {
        if (e.getDamage() > e.getDamagee().getHealth()) {
            if (e.getDamagee().hasMetadata("COMBATLOGGER")) {
                e.setCancelled(true);
                for (final Map.Entry<UUID, CombatLoggerInfo> entry : info.entrySet()) {
                    if (entry.getValue().skeleton.equals(e.getDamagee())) {
                        if (this.clInfo.disabled) {
                            return;
                        }

                        this.addLootedPlayer(this.clInfo.player);

                        this.clInfo.disabled = true;
                        entry.getValue().skeleton.setHealth(0);
                        entry.getValue().skeleton.getWorld().playSound(entry.getValue().skeleton.getLocation(), Sound.SKELETON_DEATH, 1f, 1f);
                        entry.getValue().stand.remove();
                        entry.getValue().stand1.remove();

                        ClanWarpointEvent wpEvent = null;

                        if (e.getDamager() != null) {
                            final ClansPlayer player = new ClansPlayer(e.getDamager());

                            for (final Player ocp : Bukkit.getOnlinePlayers()) {
                                final ClansPlayer cp = new ClansPlayer(ocp);
                                cp.sendMessageWithPrefix("Death", ChatColor.YELLOW + this.clInfo.player.getName() + ChatColor.GRAY + " was killed by " + ChatColor.YELLOW + player.getPlayer().getName() + ChatColor.GRAY + " while combat logged!");
                            }

                            if (player.getClan() == null || this.clInfo.clan == null || entry.getValue().clan == null) {
                                final CombatLoggerAlter cla = new CombatLoggerAlter(entry.getKey(), entry.getValue(), e.getDamager(), null, CombatLoggerAlter.CombatLoggerEventType.KILLED);
                                Bukkit.getServer().getPluginManager().callEvent(cla);
                                this.clInfo = cla.getInfo();
                                this.dropItems();
                                return;
                            }
                            if (player.getClan().equals(entry.getValue().clan)) {
                                final CombatLoggerAlter cla = new CombatLoggerAlter(entry.getKey(), entry.getValue(), e.getDamager(), null, CombatLoggerAlter.CombatLoggerEventType.KILLED);
                                Bukkit.getServer().getPluginManager().callEvent(cla);
                                this.clInfo = cla.getInfo();
                                this.dropItems();
                                return;
                            }

                            final Clan winning = player.getClan();
                            final long wps;
                            winning.setWarpoint(this.clInfo.clan.getUniqueId(), wps = winning.getWarpointsOnClan(this.clInfo.clan.getUniqueId()) + 1);
                            wpEvent = new ClanWarpointEvent(winning, this.clInfo.clan, wps);
                            this.sql.saveClan(winning);
                        }

                        if (wpEvent != null) {
                            final CombatLoggerAlter cla = new CombatLoggerAlter(entry.getKey(), entry.getValue(), e.getDamager(), wpEvent, CombatLoggerAlter.CombatLoggerEventType.KILLED);
                            Bukkit.getServer().getPluginManager().callEvent(cla);
                            Bukkit.getServer().getPluginManager().callEvent(wpEvent);
                            this.clInfo = cla.getInfo();
                            this.dropItems();

                            if (!wpEvent.isCancelled()) {
                                final long lost = wpEvent.getKilled().getWarpointsOnClan(wpEvent.getClan().getUniqueId());
                                final long won = wpEvent.getClan().getWarpointsOnClan(wpEvent.getKilled().getUniqueId());
                                final ChatColor color = lost <= -10 || lost >= 10 ? ChatColor.DARK_RED : ChatColor.YELLOW;
                                final String sLost = lost > 0 ? "+" : "";
                                final String sWon = won > 0 ? "+" : "";

                                wpEvent.getKilled().setWarpoint(wpEvent.getClan().getUniqueId(), lost);
                                wpEvent.getClan().setWarpoint(wpEvent.getKilled().getUniqueId(), won);

                                wpEvent.getKilled().setWarpoint(wpEvent.getClan().getUniqueId(), wpEvent.getKilled().getWarpoints().get(wpEvent.getClan().getUniqueId()) - 1);
                                wpEvent.getClan().announceToClan("&9(!) &7Your clan has &wGAINED &ra War Point on &s" + color + wpEvent.getKilled().getName() + " &7(" + color + sWon + won + "&7).", false);
                                wpEvent.getKilled().announceToClan("&9(!) &7Your clan has &qLOST &ra War Point to &s" + color + wpEvent.getClan().getName() + " &7(" + color + sLost + (lost - 1) + "&7).", false);

                                this.sql.saveClan(wpEvent.getClan());
                                this.sql.saveClan(wpEvent.getKilled());
                            }
                        } else {
                            final CombatLoggerAlter cla = new CombatLoggerAlter(entry.getKey(), entry.getValue(), e.getDamager(), null, CombatLoggerAlter.CombatLoggerEventType.KILLED);
                            Bukkit.getServer().getPluginManager().callEvent(cla);
                            this.clInfo = cla.getInfo();
                            this.dropItems();
                        }
                    }
                }
            }
        }
    }

    private void dropItems() {
        for (final ItemStack stack : this.clInfo.inventory) {
            if (stack != null && stack.getType() != Material.AIR) {
                this.clInfo.location.getWorld().dropItemNaturally(this.clInfo.location.clone().add(0, new Random().nextInt(4 - 1) + 1, 0), stack);
            }
        }
        for (final ItemStack stack : this.clInfo.armor) {
            if (stack != null && stack.getType() != Material.AIR) {
                this.clInfo.location.getWorld().dropItemNaturally(this.clInfo.location.clone().add(0, new Random().nextInt(4 - 1) + 1, 0), stack);
            }
        }
    }

    public void addLootedPlayer(final OfflinePlayer player) {
        final File file;
        final YamlConfiguration yml = YamlConfiguration.loadConfiguration(file = this.getLogged());
        final List<String> uuids = yml.getStringList("looted");
        uuids.add(player.getUniqueId().toString());
        yml.set("looted", uuids);
        this.saveFile(yml, file);
    }

    private File getLogged() {
        final File file = new File(Tribes.getInstance().getPlugin().getDataFolder(), "combatLogger.yml");
        final YamlConfiguration yml;
        if (!file.exists()) {
            try {
                Text.log("Unable to find '" + file.getName() + "', creating a new one!");
                if (!file.createNewFile()) {
                    Text.log("There was an unknown error whilst trying to create '" + file.getName() + "'. If the file has been created in " + file.getParentFile().getPath() + ", you can ignore this. If it hasn't and restarting your server does not fix this, please contact the plugin developer via DM.");
                } else {
                    Text.log("Created '" + file.getName() + "' successfully!");
                }
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }

        yml = YamlConfiguration.loadConfiguration(file);
        yml.set("looted", yml.getStringList("looted"));

        this.saveFile(yml, file);
        return file;
    }

    private void saveFile(final YamlConfiguration yml, final File file) {
        try {
            yml.save(file);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    private enum CombatLoggerType {
        SAFEZONE_SAFE(0),
        SAFE(30),
        UNSAFE(45),
        BESIEGE(-1);

        int time;

        CombatLoggerType(final int time) {
            this.time = time;
        }
    }

    public static class CombatLoggerInfo {
        public OfflinePlayer player;
        public Clan clan;
        public ItemStack[] inventory;
        public ItemStack[] armor;
        public Location location;
        public ItemStack heldItem;
        public Skeleton skeleton;
        public ArmorStand stand;
        public ArmorStand stand1;
        public CombatLoggerType type;
        public boolean disabled;
        public long placedAt;
        public long placedUntil;

        public CombatLoggerInfo(final Player player) {
            this.player = player;
            this.clan = new ClansPlayer(player).getClan();
            this.inventory = player.getInventory().getContents();
            this.armor = player.getInventory().getArmorContents();
            this.location = player.getLocation();
            this.heldItem = player.getItemInHand();
            this.disabled = false;

            if (Tribes.getInstance().getPvpTimer().getPvpTimer(player) <= 0) {
                this.type = CombatLoggerType.SAFEZONE_SAFE;
                this.placedUntil = System.currentTimeMillis();
            }

            final Clan territory = Tribes.getInstance().getClanFromTerritory(this.location.getChunk());
            if (this.clan != null) {
                if (this.clan.isBeingSieged() || this.clan.isSiegingOther()) {
                    this.type = CombatLoggerType.BESIEGE;
                    this.placedUntil = -1;
                } else {
                    if (territory != null && (territory.getName().equalsIgnoreCase("Shops") || territory.getName().equalsIgnoreCase("Spawn"))) {
                        if (new ClansPlayer(player).isInCombat()) {
                            this.type = CombatLoggerType.UNSAFE;
                            this.placedUntil = System.currentTimeMillis() + 45000;
                        } else {
                            this.type = CombatLoggerType.SAFEZONE_SAFE;
                            this.placedUntil = System.currentTimeMillis();
                        }
                    } else {
                        if (new ClansPlayer(player).isInCombat()) {
                            this.type = CombatLoggerType.UNSAFE;
                            this.placedUntil = System.currentTimeMillis() + 45000;
                        } else {
                            this.type = CombatLoggerType.SAFE;
                            this.placedUntil = System.currentTimeMillis() + 30000;
                        }
                    }
                }
            } else {
                if (territory != null && (territory.getName().equalsIgnoreCase("Shops") || territory.getName().equalsIgnoreCase("Spawn"))) {
                    if (new ClansPlayer(player).isInCombat()) {
                        this.type = CombatLoggerType.UNSAFE;
                        this.placedUntil = System.currentTimeMillis() + 45000;
                    } else {
                        this.type = CombatLoggerType.SAFEZONE_SAFE;
                        this.placedUntil = System.currentTimeMillis();
                    }
                } else {
                    if (new ClansPlayer(player).isInCombat()) {
                        this.type = CombatLoggerType.UNSAFE;
                        this.placedUntil = System.currentTimeMillis() + 45000;
                    } else {
                        this.type = CombatLoggerType.SAFE;
                        this.placedUntil = System.currentTimeMillis() + 30000;
                    }
                }
            }
        }

        @Override
        public String toString() {
            final Map<String, Object> map = new HashMap<>();
            map.put("player", this.player);
            map.put("clan", this.clan);
            map.put("inventory", this.inventory);
            map.put("armor", this.armor);
            map.put("location", this.location);
            map.put("heldItem", this.heldItem);
            map.put("skeleton", this.skeleton);
            map.put("stand", this.stand);
            map.put("type", this.type);
            map.put("disabled", this.disabled);
            return map.toString();
        }
    }
}
