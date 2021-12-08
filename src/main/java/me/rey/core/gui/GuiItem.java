package me.rey.core.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public abstract class GuiItem {

    private ItemStack itemStack;
    private Item item;

    public GuiItem(Item item){
        this.itemStack = item.get();
        this.item = item;
    }

    public GuiItem(ItemStack item) {
        this.itemStack = item;
        this.item = new Item(item.getType());
    }

    public ItemStack get() {
        return itemStack;
    }

    public Item getFromItem() {
        return item;
    }

    public abstract void onUse(Player player, ClickType type, int slot);

}