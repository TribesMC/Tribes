package me.rey.core.utils;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

public class ItemUtils {

    public static boolean compareLeatherArmor(ItemStack a, ItemStack b) {
        try {
            LeatherArmorMeta meta1 = (LeatherArmorMeta)a.getItemMeta();
            LeatherArmorMeta meta2 = (LeatherArmorMeta)b.getItemMeta();

            if(meta1.getColor().asRGB() == meta2.getColor().asRGB())
                return true;

        } catch (Exception e) {
            if(a == null || b == null)
                return false;
            if(a.getType() != b.getType())
                return false;
            return true;

        }
        return false;
    }

    public static boolean compareNames(ItemStack a, ItemStack b, boolean ignoreColor, boolean ignoreCase) {
        if (a.hasItemMeta() != b.hasItemMeta()) return false;
        if (a.getItemMeta().hasDisplayName() != b.getItemMeta().hasDisplayName()) return false;
        if (!a.getItemMeta().hasDisplayName()) return true;

        String name1 = a.getItemMeta().getDisplayName(), name2 = a.getItemMeta().getDisplayName();

        if (ignoreColor) {
            name1 = ChatColor.stripColor(name1);
            name2 = ChatColor.stripColor(name2);
        }

        return ignoreCase ? name1.equalsIgnoreCase(name2) : name1.equals(name2);
    }

}
