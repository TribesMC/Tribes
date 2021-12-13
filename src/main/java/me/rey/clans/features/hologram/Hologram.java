package me.rey.clans.features.hologram;

import com.google.common.collect.Lists;
import me.rey.core.utils.UtilEntity;
import me.rey.core.utils.UtilPacket;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.util.Vector;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class Hologram {
    private Object destroy;

    private final List<Integer> entities = new ArrayList<>();
    private Entity attachment;
    private final HologramManager hologramManager;
    private String[] text = new String[0];

    private Vector lastMove;
    private Location loc;
    private boolean noDestroyPacket = true;
    private boolean noSpawnPackets = true;
    private Object[] packets;
    private final Set<UUID> playerRules = new HashSet<>();
    private final List<Player> trackingPlayers = new ArrayList<>();
    private boolean removeOnAttachmentDeath;
    private HologramTarget status = HologramTarget.BLACKLIST;
    private int dist = 70;
    protected Vector relativeToEntity;
    private HologramInteraction interaction;

    private final long maxLifetime;
    private long startTime;

    public Hologram(HologramManager hologramManager, Location location, String... text) {
        this(hologramManager, location, -1L, text);
    }

    public Hologram(HologramManager hologramManager, Location location, long maxLifetime, String... text) {
        this.hologramManager = hologramManager;
        loc = location.clone();
        this.maxLifetime = maxLifetime;
        setText(text);
    }

    public Hologram setInteraction(HologramInteraction interact) {
        interaction = interact;
        return this;
    }

    public HologramInteraction getInteraction() {
        return interaction;
    }

    public Hologram addPlayer(Player player) {
        return addPlayer(player.getUniqueId());
    }

    public Hologram addPlayer(UUID player) {
        playerRules.add(player);
        return this;
    }

    public Hologram addPlayers(Collection<Player> players) {
        for (Player player : players) {
            addPlayer(player);
        }
        return this;
    }

    public boolean containsPlayer(Player player) {
        return playerRules.contains(player.getUniqueId());
    }

    protected Object getDestroyPacket() {
        if (noDestroyPacket) {
            makeDestroyPacket();
            noDestroyPacket = false;
        }

        return destroy;
    }

    public Entity getEntityFollowing() {
        return attachment;
    }

    public HologramTarget getHologramTarget() {
        return status;
    }

    public Location getLocation() {
        return loc.clone();
    }

    public List<Player> getNearbyPlayers() {
        List<Player> nearbyPlayers = new ArrayList<>();

        for (Player player : getLocation().getWorld().getPlayers()) {
            if (!isVisible(player)) continue;
            nearbyPlayers.add(player);
        }

        return nearbyPlayers;
    }

    public List<Player> getPlayersTracking() {
        return trackingPlayers;
    }

    public void checkSpawnPackets() {
        if (!noSpawnPackets) return;
        makeSpawnPackets();
        noSpawnPackets = false;
    }

    public String[] getText() {
        return Lists.reverse(Arrays.asList(text)).toArray(new String[0]);
    }

    public int getViewDistance() {
        return dist;
    }

    public boolean isActive() {
        return lastMove != null;
    }

    public boolean isRemoveOnAttachmentDeath() {
        return removeOnAttachmentDeath;
    }

    public boolean isVisible(Player player) {
        if (getLocation().getWorld() != player.getWorld()) return false;
        if (getHologramTarget() == HologramTarget.BLACKLIST && containsPlayer(player)) return false;
        if (getHologramTarget() == HologramTarget.WHITELIST && !containsPlayer(player)) return false;
        return !(getLocation().distanceSquared(player.getLocation()) > Math.pow(getViewDistance(), 2));
    }

    private void makeDestroyPacket() {
        int[] entityIds = new int[entities.size()];

        for (int i = 0; i < entities.size(); i++) {
            entityIds[i] = entities.get(i);
        }

        try {
            destroy = UtilPacket.getClassNMS("PacketPlayOutEntityDestroy").getConstructor(int[].class).newInstance((Object) entityIds);
        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void makeSpawnPackets() {
        packets = new Object[text.length];

        if (entities.size() < text.length) {
            noDestroyPacket = true;

            for (int i = entities.size(); i < text.length; i++) {
                entities.add(UtilEntity.getNewEntityId(true));
            }
        } else {
            noDestroyPacket = true;

            while (entities.size() > text.length) {
                entities.remove(text.length);
            }
        }
        for (int textRow = 0; textRow < text.length; textRow++) {
            packets[textRow] = makeSpawnPacket(textRow, entities.get(textRow), text[textRow]);
        }
    }

    private Object makeSpawnPacket(int textRow, int entityId, String lineOfText) {
        try {
            Object packet = UtilPacket.getClassNMS("PacketPlayOutSpawnEntityLiving").getConstructor().newInstance();
            Object watcher = UtilPacket.getClassNMS("DataWatcher").getConstructor(UtilPacket.getClassNMS("Entity")).newInstance((Object) null);

            UtilPacket.getAndSetField("a", packet, entityId);
            UtilPacket.getAndSetField("b", packet, 30);
            UtilPacket.getAndSetField("c", packet, (int) (getLocation().getX() * 32));
            UtilPacket.getAndSetField("d", packet, (int) ((getLocation().getY() - 2.1 + ((double) textRow * 0.31)) * 32));
            UtilPacket.getAndSetField("e", packet, (int) (getLocation().getZ() * 32));
            UtilPacket.getAndSetField("l", packet, watcher);

            watcher.getClass().getMethod("a", int.class, Object.class).invoke(watcher, 0, (byte) 32);
            watcher.getClass().getMethod("a", int.class, Object.class).invoke(watcher, 2, lineOfText);
            watcher.getClass().getMethod("a", int.class, Object.class).invoke(watcher, 3, (byte) 1);

            return packet;
        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Hologram removePlayer(Player player) {
        return removePlayer(player.getUniqueId());
    }

    public Hologram removePlayer(UUID player) {
        playerRules.remove(player);
        return this;
    }

    public Hologram setFollowEntity(Entity entityToFollow) {
        attachment = entityToFollow;

        if (entityToFollow != null) {
            relativeToEntity = loc.clone().subtract(entityToFollow.getLocation()).toVector();
        }

        return this;
    }

    public Hologram setHologramTarget(HologramTarget newTarget) {
        status = newTarget;
        return this;
    }

    public Hologram setLocation(Location newLocation) {
        noSpawnPackets = true;

        Location oldLocation = getLocation();
        loc = newLocation.clone();

        if (getEntityFollowing() != null) {
            relativeToEntity = loc.clone().subtract(getEntityFollowing().getLocation()).toVector();
        }

        if (isActive()) {
            List<Player> canSee = getNearbyPlayers();
            Iterator<Player> players = trackingPlayers.iterator();

            while (players.hasNext()) {
                Player player = players.next();
                if (!canSee.contains(player)) {
                    players.remove();

                    if (player.getWorld() == getLocation().getWorld()) {
                        UtilPacket.sendPacket(player, getDestroyPacket());
                    }
                }
            }

            players = canSee.iterator();
            checkSpawnPackets();

            while (players.hasNext()) {
                Player player = players.next();

                if (trackingPlayers.contains(player)) continue;
                trackingPlayers.add(player);
                players.remove();

                UtilPacket.sendPacket(player, packets);
            }

            if (canSee.isEmpty()) return this;
            lastMove.add(new Vector(
                    newLocation.getX() - oldLocation.getX(),
                    newLocation.getY() - oldLocation.getY(),
                    newLocation.getZ() - oldLocation.getZ()
            ));

            int x;
            int z;

            Object[] packets = new Packet[text.length];

            int i = 0;

            x = (int) Math.floor(32 * newLocation.getX());
            z = (int) Math.floor(32 * newLocation.getZ());

            lastMove = new Vector(newLocation.getX() - (x / 32D), 0, newLocation.getZ() - (z / 32D));

            for (Integer entityId : entities) {
                try {
                    packets[i] = UtilPacket.getClassNMS("PacketPlayOutEntityTeleport").getConstructor(int.class, int.class, int.class, int.class, byte.class, byte.class, boolean.class).newInstance(entityId, x, (int) Math.floor((oldLocation.getY() + (-2.1) + ((double) i * 0.31)) * 32), z, (byte) 0, (byte) 0, false);
                    i++;
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }

            for (Player player : canSee) {
                for (Object packet : packets) {
                    UtilPacket.sendPacket(player, packet);
                }
            }
        }

        return this;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getMaxLifetime() {
        return maxLifetime;
    }

    public Hologram setRemoveOnAttachmentDeath() {
        removeOnAttachmentDeath = true;
        return this;
    }

    public boolean isEntityId(int entityId) {
        return entities.contains(entityId);
    }

    public Hologram setText(String... newLines) {
        String[] newText = new String[newLines.length];

        for (int i = 0; i < newText.length; i++) {
            newText[i] = newLines[newText.length - (i + 1)];
        }

        if (Arrays.equals(newText, text)) return this;

        if (isActive()) {
            int[] destroy = new int[0];

            List<Object> packets = new ArrayList<>();

            if (text.length != newText.length) {
                noDestroyPacket = true;
            }

            for (int i = 0; i < Math.max(text.length, newText.length); i++) {
                if (i >= text.length) {
                    int entityId = UtilEntity.getNewEntityId(true);
                    entities.add(entityId);

                    packets.add(makeSpawnPacket(i, entityId, newText[i]));
                } else if (i >= newText.length) {// If less lines than previously
                    // Remove entity id and send destroy packets
                    Integer entityId = entities.remove(newText.length);

                    destroy = Arrays.copyOf(destroy, destroy.length + 1);
                    destroy[destroy.length - 1] = entityId;
                } else if (!newText[i].equals(text[i])) {
                    // Send update metadata packets
                    Integer entityId = entities.get(i);

                    try {
                        Object watcher = UtilPacket.getClassNMS("DataWatcher").getConstructor(UtilPacket.getClassNMS("Entity")).newInstance((Object) null);

                        watcher.getClass().getMethod("a", int.class, Object.class).invoke(watcher, 0, (byte) 32);
                        watcher.getClass().getMethod("a", int.class, Object.class).invoke(watcher, 2, newText[i]);
                        watcher.getClass().getMethod("a", int.class, Object.class).invoke(watcher, 3, (byte) 1);

                        Object packet = UtilPacket.getClassNMS("PacketPlayOutEntityMetadata").getConstructor(int.class, UtilPacket.getClassNMS("DataWatcher"), boolean.class).newInstance(entityId, watcher, true);
                        packets.add(packet);
                    } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException | InstantiationException e) {
                        e.printStackTrace();
                    }
                }
            }

            if (destroy.length > 0) {
                packets.add(new PacketPlayOutEntityDestroy(destroy));
            }

            for (Player player : trackingPlayers) {
                for (Object packet : packets) {
                    UtilPacket.sendPacket(player, packet);
                }
            }
        }

        text = newText;
        makeSpawnPackets();

        return this;
    }

    public Hologram setViewDistance(int newDistance) {
        dist = newDistance;
        return setLocation(getLocation());
    }

    public Hologram start() {
        if (isActive()) return this;
        startTime = System.currentTimeMillis();

        hologramManager.addHologram(this);
        trackingPlayers.addAll(getNearbyPlayers());

        sendPackets();

        lastMove = new Vector();

        return this;
    }

    private void sendPackets() {
        checkSpawnPackets();

        for (Player player : trackingPlayers) {
            for (Object packet : packets) {
                UtilPacket.sendPacket(player, packet);
            }
        }
    }

    public Object[] getSpawnPackets(Player player) {
        checkSpawnPackets();

        return packets;
    }

    public Hologram stop() {
        if (!isActive()) return this;
        hologramManager.removeHologram(this);

        for (Player player : trackingPlayers) {
            UtilPacket.sendPacket(player, getDestroyPacket());
        }

        trackingPlayers.clear();
        lastMove = null;

        return this;
    }

    public enum HologramTarget {
        BLACKLIST, WHITELIST
    }

    public interface HologramInteraction {
        void onClick(Player player, ClickType type);
    }
}
