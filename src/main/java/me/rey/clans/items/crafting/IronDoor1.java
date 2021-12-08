package me.rey.clans.items.crafting;

import me.rey.core.gui.Item;
import org.bukkit.Material;

import me.rey.clans.items.crafting.CraftingRecipe.IExtraCraft;

public class IronDoor1 extends CraftingRecipe implements IExtraCraft {

	public IronDoor1() {
		super(new Item(Material.IRON_DOOR).setAmount(1).get());
	}

	@Override
	public CraftingRecipe init() {
		this.shape(" II", " II", " II");
		this.setIngredient('I', Material.WOOD);
		
		return this;
	}

}
