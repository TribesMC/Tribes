package me.rey.clans.features.hologram;

import me.rey.clans.Tribes;
import me.rey.core.events.customevents.update.UpdateEvent;
import me.rey.core.utils.Activatable;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class HologramManager implements Activatable, Listener {

    //todo this should primarily use packets instead of the bukkit api
    private List<Hologram> holograms = new ArrayList<>();

    public HologramManager() {

    }

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, Tribes.getInstance().getPlugin());
    }

    @Override
    public void onDisable() {
        holograms.clear();

    }

    void addHologram(Hologram hologram) {
        holograms.add(hologram);
    }

    void removeHologram(Hologram hologram) {
        holograms.remove(hologram);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onUpdate(UpdateEvent event) {
        if (event.getType() != UpdateEvent.TickType.TICK) return;
        if (holograms.isEmpty()) return;

        Iterator<Hologram> holoIterator = holograms.iterator();

        while (holoIterator.hasNext()) {
            Hologram holo = holoIterator.next();

            if ((holo.getMaxLifetime() != -1 && System.currentTimeMillis() - holo.getStartTime() > holo.getMaxLifetime()) || !Bukkit.getWorlds().contains(holo.getLocation().getWorld())) {
                holoIterator.remove();
                holo.stop();
            } else {
                if (holo.getEntityFollowing() != null) {
                    Entity following = holo.getEntityFollowing();

                    if (holo.isRemoveOnAttachmentDeath() && !following.isValid()) {
                        holoIterator.remove();
                        holo.stop();
                        continue;
                    }

                    if (!holo.relativeToEntity.equals(following.getLocation().subtract(holo.getLocation()).toVector())) {
                        // And we do this so in the rare offchance it changes by a decimal. It doesn't start turning wonky.
                        Vector vec = holo.relativeToEntity.clone();
                        holo.setLocation(following.getLocation().add(holo.relativeToEntity));
                        holo.relativeToEntity = vec;

                        continue;
                    }
                }

                List<Player> canSee = holo.getNearbyPlayers();

                Iterator<Player> players = holo.getPlayersTracking().iterator();

                while (players.hasNext()) {
                    Player player = players.next();

                    if (!canSee.contains(player)) {
                        players.remove();
                        if (player.getWorld() == holo.getLocation().getWorld()) {
//                            UtilPlayer.sendPacket(player, hologram.getDestroyPacket());
                        }
                    }
                }

                for (Player player : canSee) {
                    if (!holo.getPlayersTracking().contains(player)) {
                        holo.getPlayersTracking().add(player);

//                        UtilPlayer.sendPacket(player, hologram.getSpawnPackets(player));
                    }
                }
            }
        }
    }

//    public void handle(PacketInfo packetInfo)
//    {
//        PacketPlayInUseEntity packetPlayIn = (PacketPlayInUseEntity) packetInfo.getPacket();
//
//        for (Hologram hologram : holograms)
//        {
//            if (!hologram.isEntityId(packetPlayIn.a))
//                continue;
//
//            if (hologram.getInteraction() != null)
//            {
//                hologram.getInteraction().onClick(packetInfo.getPlayer(),
//                        packetPlayIn.action == EnumEntityUseAction.ATTACK ? ClickType.LEFT : ClickType.RIGHT);
//            }
//
//            break;
//        }
//    }
}
