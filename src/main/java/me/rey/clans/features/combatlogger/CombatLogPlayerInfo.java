package me.rey.clans.features.combatlogger;

import me.rey.clans.clans.ClansPlayer;
import me.rey.clans.utils.UtilText;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class CombatLogPlayerInfo {
    private final String playerName, prefix;
    private final UUID playerUuid, clanUuid;
    private final Set<ItemStack> items;

    public CombatLogPlayerInfo(Player player) {
        playerName = player.getName();
        prefix = UtilText.getPrefix(player);
        playerUuid = player.getUniqueId();
        clanUuid = new ClansPlayer(player).getClan() != null ? new ClansPlayer(player).getClan().getUniqueId() : null;
        items = fetchItems(player.getInventory());
    }

    public void dropItems(Location location) {
        World world = location.getWorld();
        for (ItemStack item : items) {
            world.dropItemNaturally(location, item);
        }
    }

    public String getPlayerName()
    {
        return playerName;
    }

    public String getPrefix() {
        return prefix;
    }

    public Set<ItemStack> getItems()
    {
        return items;
    }

    public UUID getUniqueId()
    {
        return playerUuid;
    }

    public UUID getClanUniqueId() {
        return clanUuid;
    }

    public Player getPlayer()
    {
        return Bukkit.getPlayerExact(playerName);
    }

    private Set<ItemStack> fetchItems(PlayerInventory inventory) {
        Set<ItemStack> items = new HashSet<>();

        addItems(items, inventory.getArmorContents());
        addItems(items, inventory.getContents());

        return items;
    }

    private void addItems(Set<ItemStack> items, ItemStack[] itemsToAdd) {
        for (ItemStack item : itemsToAdd) {
            if (item == null) return;
            if (item.getType() == Material.AIR) return;
            items.add(item);
        }
    }
}
