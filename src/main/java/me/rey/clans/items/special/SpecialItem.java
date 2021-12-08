package me.rey.clans.items.special;

import org.bukkit.inventory.ItemStack;

public interface SpecialItem {
    String getItemId();
    String getName();
    String getDisplayName();
    ItemStack getItemStack();
    Class<? extends SpecialItem> getType();
}
