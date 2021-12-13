package me.rey.clans.features.incognito;

import me.rey.core.utils.Activatable;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class IncognitoManager implements Activatable {
    //todo
    Map<UUID, IncognitoType> map;

    public IncognitoManager() {
        map = new HashMap<>();
    }

    @Override
    public void onEnable() {

    }

    @Override
    public void onDisable() {
        map.clear();
        map = null;
    }

    public boolean isIncognito(Player player) {
        return map.containsKey(player.getUniqueId());
    }

    public IncognitoType getIncognitoType(Player player) {
        return map.get(player.getUniqueId());
    }

    public void setIncognitoType(Player player, IncognitoType type) {
        map.put(player.getUniqueId(), type);
    }

    public void removeIncognitoType(Player player) {
        map.remove(player.getUniqueId());
    }

    public enum IncognitoType {
        O,
        CMO
    }
}
