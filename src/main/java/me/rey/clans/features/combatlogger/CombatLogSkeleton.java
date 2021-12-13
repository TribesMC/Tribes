package me.rey.clans.features.combatlogger;

import me.rey.clans.Tribes;
import me.rey.clans.clans.Clan;
import me.rey.clans.clans.ClansPlayer;
import me.rey.clans.events.CombatLoggerDespawnEvent;
import me.rey.clans.events.CombatLoggerKillEvent;
import me.rey.clans.events.clans.ClanWarpointEvent;
import me.rey.clans.features.hologram.Hologram;
import me.rey.clans.utils.UtilTime;
import me.rey.core.utils.UtilEntity;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.metadata.FixedMetadataValue;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CombatLogSkeleton {
    private final CombatLogPlayerInfo playerInfo;
    private final String userDataPath;
    private final long endingTime;
    private final long duration;
    private final CombatLoggerDurationHandler handler;
    private final double spawnHealth;
    private final boolean creative;

    private long spawnDate;
    private LivingEntity skeleton;
    private Hologram hologram;

    public int getEntityId() {
        return skeleton.getEntityId();
    }

    public CombatLogSkeleton(Player player, String userDataPath, long duration) {
        this.creative = player.getGameMode() == GameMode.CREATIVE;
        this.playerInfo = new CombatLogPlayerInfo(player);
        this.userDataPath = userDataPath;
        this.duration = duration;
        this.handler = null;

        Clan clan;
        hologram = new Hologram(Tribes.getInstance().getHologramManager(), player.getEyeLocation().add(0, 1, 0), "Quitting in 30 seconds!", playerInfo.getPrefix() + "" + ChatColor.GOLD + ((clan = Tribes.getInstance().getClan(playerInfo.getClanUniqueId())) != null ? clan.getName() + " " : "") + ChatColor.YELLOW + playerInfo.getPlayerName());
        spawnDate = 0;
        endingTime = System.currentTimeMillis() + duration;
        spawnHealth = player.getHealth();
        hologram.start();
    }

    public CombatLogSkeleton(Player player, String userDataPath, CombatLoggerDurationHandler handler) {
        this.creative = player.getGameMode() == GameMode.CREATIVE;
        this.playerInfo = new CombatLogPlayerInfo(player);
        this.userDataPath = userDataPath;
        this.duration = -1;
        this.handler = handler;

        Clan clan;
        hologram = new Hologram(Tribes.getInstance().getHologramManager(), player.getEyeLocation().add(0, 1, 0), "Quitting in 30 seconds!", playerInfo.getPrefix() + "" + ChatColor.GOLD + ((clan = Tribes.getInstance().getClan(playerInfo.getClanUniqueId())) != null ? clan.getName() + " " : "") + ChatColor.YELLOW + playerInfo.getPlayerName());
        spawnDate = 0;
        endingTime = -1;
        spawnHealth = player.getHealth();
        hologram.start();
    }

    public void onDeath(Entity killer) {
        Location location = skeleton.getLocation();
        World world = location.getWorld();

        File file = new File(world.getWorldFolder(), "playerdata/" + playerInfo.getUniqueId().toString() + ".dat");
        if (!file.delete()) {
            // it says this even if it successfully deletes :D??!!?
//            Text.debug("Failed to delete '" + world.getWorldFolder() + "/playerdata/" + playerInfo.getUniqueId().toString() + ".dat'");
        }

        if (killer != null) {
            String killerName = "Unknown";

            if (killer instanceof Player) {
                killerName = killer.getName();
            } else {
                UtilEntity.EntityInfo info = UtilEntity.getEntityByType(killer.getType());
                if (info != null) {
                    killerName = info.getName();
                }
            }

            try {
                DataOutputStream stream = new DataOutputStream(new FileOutputStream(userDataPath + "DEATH_" + playerInfo.getUniqueId().toString() + ".dat"));

                stream.writeLong(System.currentTimeMillis());
                stream.writeInt(killerName.length());
                stream.writeBytes(killerName);

                stream.close();
            } catch (IOException e) {
                System.out.println("FATAL ERROR while trying to create player death lock for " + playerInfo.getPlayerName() + ", meaning " + playerInfo.getPlayerName() + " will not be informed that they died next time they log in.");
            }

            for (Player ocp : Bukkit.getOnlinePlayers()) {
                ClansPlayer cp = new ClansPlayer(ocp);
                cp.sendMessageWithPrefix("Death", ChatColor.YELLOW + playerInfo.getPlayerName() + ChatColor.GRAY + " was killed by " + ChatColor.YELLOW + killerName + ChatColor.GRAY + " while combat logged!");
            }

            if (killer instanceof Player) {
                Bukkit.getPluginManager().callEvent(new CombatLoggerKillEvent(this, (Player) killer));
                Clan loggerClan = Tribes.getInstance().getClan(playerInfo.getClanUniqueId());
                Clan attackerClan = new ClansPlayer((Player) killer).getClan();
                if (loggerClan != null && attackerClan != null) {
                    long loggerClanWarpoints = loggerClan.getWarpointsOnClan(attackerClan.getUniqueId());
                    long attackerClanWarpoints = attackerClan.getWarpointsOnClan(loggerClan.getUniqueId());

                    ClanWarpointEvent warpointEvent = new ClanWarpointEvent(attackerClan, loggerClan, 1);
                    Bukkit.getPluginManager().callEvent(warpointEvent);
                    if (!warpointEvent.isCancelled()) {
                        long newLoggerClanWarpoints = loggerClanWarpoints - warpointEvent.getKillerWarpoints();
                        long newAttackerClanWarpoints = attackerClanWarpoints + warpointEvent.getKillerWarpoints();
                        loggerClan.setWarpoint(attackerClan.getUniqueId(), newLoggerClanWarpoints);
                        attackerClan.setWarpoint(loggerClan.getUniqueId(), newAttackerClanWarpoints);
                        Tribes.getInstance().getSQLManager().saveClan(loggerClan);
                        Tribes.getInstance().getSQLManager().saveClan(attackerClan);

                        ChatColor loggerColor = newLoggerClanWarpoints >= 10 ? ChatColor.DARK_RED : ChatColor.YELLOW;
                        ChatColor attackerColor = newAttackerClanWarpoints >= 10 ? ChatColor.DARK_RED : ChatColor.YELLOW;
                        String sLost = newLoggerClanWarpoints > 0 ? "+" : "";
                        String sWon = newAttackerClanWarpoints > 0 ? "+" : "";

                        loggerClan.announceToClan("&9(!) &7Your clan has &qLOST &ra War Point to &s" + attackerColor + attackerClan.getName() + " &7(" + loggerColor + sLost + newLoggerClanWarpoints + "&7).", false);
                        attackerClan.announceToClan("&9(!) &7Your clan has &wGAINED &ra War Point on &s" + loggerColor + loggerClan.getName() + " &7(" + attackerColor + sWon + newAttackerClanWarpoints + "&7).", false);
                    }
                }
            }
        }

        playerInfo.dropItems(location);
    }

    public void update() {
        //todo find a way to make text editable per player
        Clan clan;
        List<String> lines = new ArrayList<>();
        if (handler != null && handler.getQuitMessages() != null) {
            lines.addAll(Arrays.asList(handler.getQuitMessages()));
        } else {
            lines.add("Quitting in " + UtilTime.convert(Math.max(endingTime - System.currentTimeMillis(), 0), 0, UtilTime.getBestUnit(Math.max(endingTime - System.currentTimeMillis(), 0))) + " " + UtilTime.getBestUnit(Math.max(endingTime - System.currentTimeMillis(), 0)).name().toLowerCase() + "!");
        }
        lines.add(playerInfo.getPrefix() + "" + ChatColor.GOLD + ((clan = Tribes.getInstance().getClan(playerInfo.getClanUniqueId())) != null ? clan.getName() + " " : "") + ChatColor.YELLOW + playerInfo.getPlayerName());
        hologram.setText(lines.toArray(new String[0]));
    }

    public long getDuration() {
        return duration;
    }

    public double getSpawnHealth() {
        return spawnHealth;
    }

    public long getEndingTime() {
        return endingTime;
    }

    public long getSpawnDate() {
        return spawnDate;
    }

    public String getUserDataPath() {
        return userDataPath;
    }

    public boolean isAlive() {
        return skeleton != null && !skeleton.isDead();
    }

    public long getAliveDuration() {
        return System.currentTimeMillis() - spawnDate;
    }

    public void spawn() {
        if (skeleton != null) despawn();

        skeleton = spawnNpc(getPlayer());
        spawnDate = System.currentTimeMillis();
    }

    public void despawn() {
        Bukkit.getPluginManager().callEvent(new CombatLoggerDespawnEvent(this));
        if (skeleton != null) {
            skeleton.remove();
            skeleton = null;
            hologram.stop();
            hologram = null;
        }
    }

    public void remove() {
        if (hologram != null) {
            hologram.stop();
            hologram = null;
        }
    }

    public CombatLogPlayerInfo getPlayerInfo() {
        return playerInfo;
    }

    public Player getPlayer() {
        return playerInfo.getPlayer();
    }

    public boolean matchesPlayer(Player player) {
        return playerInfo.getPlayerName().equalsIgnoreCase(player.getName());
    }

    private LivingEntity spawnNpc(Player player) {
        Location spawnLoc = player.getLocation();
        Skeleton skeleton = player.getWorld().spawn(spawnLoc, Skeleton.class);
        skeleton.setMetadata("CombatLogSkeleton", new FixedMetadataValue(Tribes.getInstance().getPlugin(), player.getUniqueId().toString()));
        skeleton.teleport(spawnLoc);
        skeleton.setHealth(spawnHealth);
        UtilEntity.stupidify(skeleton, true);
        UtilEntity.anchor(skeleton);

        skeleton.getEquipment().setHelmet(player.getInventory().getHelmet());
        skeleton.getEquipment().setChestplate(player.getInventory().getChestplate());
        skeleton.getEquipment().setLeggings(player.getInventory().getLeggings());
        skeleton.getEquipment().setBoots(player.getInventory().getBoots());
        skeleton.getEquipment().setItemInHand(player.getItemInHand());

        hologram.setFollowEntity(skeleton);

        return skeleton;
    }

    public boolean wasCreative() {
        return creative;
    }

    public CombatLoggerDurationHandler getHandler() {
        return handler;
    }
}
