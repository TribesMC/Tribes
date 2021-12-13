package me.rey.clans.features.combatlogger;

import me.rey.clans.Tribes;
import me.rey.clans.clans.Clan;
import me.rey.clans.clans.ClansPlayer;
import me.rey.clans.events.CombatLoggerDamageEvent;
import me.rey.clans.events.CombatLoggerSpawnEvent;
import me.rey.clans.siege.Siege;
import me.rey.clans.utils.UtilTime;
import me.rey.core.Warriors;
import me.rey.core.events.customevents.combat.DamageEvent;
import me.rey.core.events.customevents.combat.MobDeathEvent;
import me.rey.core.events.customevents.update.UpdateEvent;
import me.rey.core.packets.Title;
import me.rey.core.players.User;
import me.rey.core.utils.Activatable;
import me.rey.core.utils.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.*;

public class CombatLogger implements Activatable, Listener {
    public static final String userDir = Bukkit.getWorlds().get(0).getWorldFolder().getPath() + File.separator + ".." + File.separator + "TRIBES_USER_DATA" + File.separator;

    private boolean doCombatLoggers;
    private Set<CombatLogSkeleton> skeletons;
    private final List<String> flying;

    public CombatLogger() {
        doCombatLoggers = true;
        skeletons = new HashSet<>();
        flying = new ArrayList<>();
        File file;
        if (!(file = new File(CombatLogger.userDir)).mkdir()) {
            Text.debug("Unable to create combat logger dir at " + file.getPath());
        }
    }

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, Tribes.getInstance().getPlugin());
    }

    @Override
    public void onDisable() {
        for (CombatLogSkeleton skeleton : skeletons) {
            skeleton.despawn();
        }
        skeletons.clear();
        skeletons = null;
    }

    /*
     * Detecting if a skeleton should be dropped
     */

    public void onPlayerQuit(Player player) {
        boolean isSafeLog = false;

        if (!doCombatLoggers) return;

        CombatLoggerSpawnEvent event = new CombatLoggerSpawnEvent(player);
        ClansPlayer cp = new ClansPlayer(player);
        if (cp.isInSafeZone() && cp.isInCombat()) {
            isSafeLog = true;
        }

        if (flying.contains(player.getName())) {
            flying.remove(player.getName());
            return;
        }

        //todo frozen, panicking, restarting, raids,

        if (Tribes.getInstance().getPvpTimer().getPvpTimer(player) > 0) {
            isSafeLog = true;
        }

        if (Tribes.getInstance().getIncognito().isIncognito(player)) {
            isSafeLog = true;
        }

        event.setCancelled(isSafeLog);
        Bukkit.getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            Clan clan = cp.getClan();
            Clan territory = Tribes.getInstance().getClanFromTerritory(player.getLocation().getChunk());
            if (clan != null && clan.isBeingSieged()) {
                spawnSkeleton(player, new CombatLoggerDurationHandler() {
                    @Override
                    public boolean shouldDestroy() {
                        return !clan.isBeingSieged();
                    }

                    @Override
                    public String[] getQuitMessages() {
                        ChatColor color;
                        long time = System.currentTimeMillis() / 100;
                        int val = (int) (time % 10);
                        if (val < 5) {
                            color = ChatColor.RED;
                        } else {
                            color = ChatColor.WHITE;
                        }

                        return new String[] {
                                color + "" + ChatColor.BOLD + "" + ChatColor.MAGIC + "||" + color + "" + ChatColor.BOLD + " ACTIVE BESIEGE " + ChatColor.MAGIC + "||",
                                color + clan.getClansSiegingSelf().get(0).getRemainingString(System.currentTimeMillis())
                        };
                    }
                });
            } else if (cp.isInCombat()) {
                spawnSkeleton(player, 45000);
            } else if (!cp.isInSafeZone() && (clan != null && territory != null && !territory.getUniqueId().equals(clan.getUniqueId()))) {
                spawnSkeleton(player, 30000);
            }
        }
    }

    public void onPlayerJoin(Player player) {
        if (hasSkeleton(player)) {
            despawnSkeleton(player);
        }

        File deathFile = new File(userDir + "DEATH_" + player.getUniqueId().toString() + ".dat");

        if (!deathFile.exists()) return;
        try {
            DataInputStream stream = new DataInputStream(new FileInputStream(deathFile));

            final long time = stream.readLong();
            final int length = stream.readInt();
            final StringBuilder killerName = new StringBuilder();

            for (int i = 0; i < length; i++) {
                killerName.append((char) stream.readByte());
            }

            stream.close();

            Bukkit.getScheduler().runTaskLater(Tribes.getInstance().getPlugin(), () -> {

            }, 15);
            // do tips like mineplex does?
            new User(player).sendMessageWithPrefix("SafeLog", "You were killed by &s" + killerName + " &rwhen you logged out! This happened about &s" + UtilTime.convert(System.currentTimeMillis() - time, 0, UtilTime.getBestUnit(System.currentTimeMillis() - time)) + " " + UtilTime.getBestUnit(System.currentTimeMillis() - time).name().toLowerCase() + " &rago.");
            new Title(ChatColor.RED + "Offline Death", "Log out in a safer place next time!", 15, 80, 40).send(player);

            if (!deathFile.delete()) {
                Text.log("Unable to delete " + deathFile.getPath());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        onPlayerQuit(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void flyCheck(PlayerKickEvent event) {
        if (event.getPlayer().getItemInHand() != null && event.getPlayer().getItemInHand().getItemMeta() != null && (ChatColor.GOLD + "Windblade").equals(event.getPlayer().getItemInHand().getItemMeta().getDisplayName())) {
            if (event.getReason().contains("flying is not enabled")) {
                flying.add(event.getPlayer().getName());
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerKicked(PlayerKickEvent event) {
        if (event.isCancelled()) return;
        if (event.getPlayer().getItemInHand() != null && event.getPlayer().getItemInHand().getItemMeta() != null && (ChatColor.GOLD + "Windblade").equals(event.getPlayer().getItemInHand().getItemMeta().getDisplayName())) {
            if (event.getReason().contains("flying is not enabled")) return;
        }

        onPlayerQuit(event.getPlayer());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        onPlayerJoin(event.getPlayer());
    }

    /*
     * Skeleton stuff
     */

    public void spawnSkeleton(Player player, long duration) {
        if (hasSkeleton(player)) return;
        CombatLogSkeleton skeleton = new CombatLogSkeleton(player, userDir, duration);
        skeleton.spawn();
        skeletons.add(skeleton);
    }

    public void spawnSkeleton(Player player, CombatLoggerDurationHandler handler) {
        if (hasSkeleton(player)) return;
        CombatLogSkeleton skeleton = new CombatLogSkeleton(player, userDir, handler);
        skeleton.spawn();
        skeletons.add(skeleton);
    }

    @EventHandler
    private void killSkeletons(PlayerJoinEvent event) {
        for (World world : Bukkit.getWorlds()) {
            for (LivingEntity entity : world.getLivingEntities()) {
                if (!entity.hasMetadata("CombatLogSkeleton")) return;
                if (!entity.getMetadata("CombatLogSkeleton").get(0).asString().equals(entity.getUniqueId().toString())) return;
                entity.remove();
            }
        }
    }

    public void despawnSkeleton(Player player) {
        CombatLogSkeleton skeleton = getSkeleton(player);
        if (skeleton == null) return;
        skeleton.despawn();
        skeletons.remove(skeleton);
    }

    public boolean hasSkeleton(Player player) {
        return getSkeleton(player) != null;
    }

    public CombatLogSkeleton getSkeleton(Player player) {
        for (CombatLogSkeleton skeleton : skeletons) {
            if (!skeleton.matchesPlayer(player)) return null;
            return skeleton;
        }
        return null;
    }

    @EventHandler
    private void onChunkUnload(ChunkUnloadEvent event) {
        for (Entity entity : event.getChunk().getEntities()) {
            for (CombatLogSkeleton skeleton : skeletons) {
                if (entity.getEntityId() != skeleton.getEntityId()) return;
                event.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void onEntityDeath(MobDeathEvent event) {
        CombatLogSkeleton skeleton = getSkeleton(event.getEntity());
        if (skeleton == null) return;
        skeleton.onDeath(event.getKiller());
        event.getDrops().clear();
    }

    @EventHandler
    private void onCombatLoggerDamage(CombatLoggerDamageEvent event) {
        if (Tribes.getInstance().getPvpTimer().getPvpTimer(event.getDamager()) > 0) {
            new ClansPlayer(event.getDamager()).sendMessageWithPrefix("Combat", "You cannot attack this combat logger with an active PVP timer!");
            event.setCancelled(true);
            return;
        }
        if (event.getCombatLogger().wasCreative()) {
            new ClansPlayer(event.getDamager()).sendMessageWithPrefix("Combat", "You cannot attack this combat logger because it appears that the player was in creative mode when they left!");
            event.setCancelled(true);
            return;
        }
        if (event.getDamager() != null) {
            Clan clan = new ClansPlayer(event.getDamager()).getClan();
            if (clan != null) {
                if (clan.isInClan(event.getCombatLogger().getPlayerInfo().getUniqueId(), true)) {
                    new ClansPlayer(event.getDamager()).sendMessageWithPrefix("Combat", "You cannot attack this combat logger because it belongs to one of your clan mates!");
                    event.setCancelled(true);
                    return;
                }
                if (clan.isAlliedTo(Tribes.getInstance().getClan(event.getCombatLogger().getPlayerInfo().getClanUniqueId()))) {
                    new ClansPlayer(event.getDamager()).sendMessageWithPrefix("Combat", "You cannot attack this combat logger because it belongs to one of your allies!");
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    private void onEntityDamaged(DamageEvent event) {
        if (!(event.getDamagee() instanceof Skeleton)) return;
        CombatLogSkeleton skeleton = getSkeleton(event.getDamagee());
        if (skeleton == null) return;
        if (event.getDamager() == null) return;
        CombatLoggerDamageEvent event1 = new CombatLoggerDamageEvent(skeleton, event.getDamager());
        Bukkit.getPluginManager().callEvent(event1);
        if (!event1.isCancelled()) {
            Warriors.getInstance().getHitCache().startCombatTimer(event.getDamager());
            event.setKnockbackMult(0);
            event.getDamager().playSound(event.getDamager().getLocation(), Sound.HURT_FLESH, 1, 1);
        } else {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityIgnite(EntityCombustEvent event) {
        if (!isSkeleton(event.getEntity())) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if (event.getType() == UpdateEvent.TickType.TICK) {
            for (CombatLogSkeleton skeleton : skeletons) {
                skeleton.update();
            }
        }

        if (event.getType() == UpdateEvent.TickType.SECOND) {
            Iterator<CombatLogSkeleton> iterator = skeletons.iterator();

            while (iterator.hasNext()) {
                CombatLogSkeleton skeleton = iterator.next();

                if (Bukkit.getPlayer(skeleton.getPlayerInfo().getPlayerName()) != null) {
                    skeleton.despawn();
                    iterator.remove();
                }

                if (!doCombatLoggers) {
                    skeleton.despawn();
                    iterator.remove();
                }

                if (!skeleton.isAlive()) {
                    skeleton.remove();
                    iterator.remove();
                } else if (skeleton.getHandler() == null && skeleton.getAliveDuration() > skeleton.getDuration()) {
                    skeleton.despawn();
                    iterator.remove();
                } else if (skeleton.getHandler() != null && skeleton.getHandler().shouldDestroy()) {
                    skeleton.despawn();
                    iterator.remove();
                }
            }
        }
    }

    private boolean isSkeleton(Entity entity) {
        return getSkeleton(entity) != null;
    }

    private CombatLogSkeleton getSkeleton(Entity entity) {
        return getSkeleton(entity.getEntityId());
    }

    private CombatLogSkeleton getSkeleton(int entityId) {
        for (CombatLogSkeleton skeleton : skeletons) {
            if (skeleton.getEntityId() != entityId) return null;
            return skeleton;
        }

        return null;
    }

    public void toggleCombatLoggers(boolean toggle) {
        doCombatLoggers = toggle;
    }

    public boolean isDoCombatLoggers() {
        return doCombatLoggers;
    }

    public void cleanup() {
        Iterator<CombatLogSkeleton> iterator = skeletons.iterator();

        while (iterator.hasNext()) {
            CombatLogSkeleton skeleton = iterator.next();
            skeleton.despawn();
            iterator.remove();
        }
    }
}
