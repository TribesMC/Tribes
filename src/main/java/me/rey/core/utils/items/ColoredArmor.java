package me.rey.core.utils.items;

import me.rey.core.gui.Item;
import me.rey.core.utils.enums.ArmorPiece;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

public class ColoredArmor extends Item {

    private final Color color;

    public ColoredArmor(ArmorPiece piece, Color color) {
        super(piece.equals(ArmorPiece.BOOTS) ?
                Material.LEATHER_BOOTS : piece.equals(ArmorPiece.LEGGINGS) ?
                Material.LEATHER_LEGGINGS : piece.equals(ArmorPiece.CHESTPLATE) ?
                Material.LEATHER_CHESTPLATE : Material.LEATHER_HELMET);

        this.color = color;
    }

    public ColoredArmor(ArmorPiece piece, int red, int green, int blue) {
        super(piece.equals(ArmorPiece.BOOTS) ?
                Material.LEATHER_BOOTS : piece.equals(ArmorPiece.LEGGINGS) ?
                Material.LEATHER_LEGGINGS : piece.equals(ArmorPiece.CHESTPLATE) ?
                Material.LEATHER_CHESTPLATE : Material.LEATHER_HELMET);

        this.color = Color.fromBGR(red, green, blue);
    }

    @Override
    public ItemStack get() {
        ItemStack larmor = super.get();
        LeatherArmorMeta lam = (LeatherArmorMeta)larmor.getItemMeta();
        lam.setColor(this.color);
        larmor.setItemMeta(lam);
        return larmor;
    }

}
