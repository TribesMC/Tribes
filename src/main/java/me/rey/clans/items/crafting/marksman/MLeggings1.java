package me.rey.clans.items.crafting.marksman;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import me.rey.clans.items.crafting.CraftingRecipe;
import me.rey.core.classes.ClassType;

public class MLeggings1 extends CraftingRecipe {

	public MLeggings1() {
		super(new ItemStack(ClassType.CHAIN.getLeggings().get()));
	}

	@Override
	public CraftingRecipe init() {
		this.shape("GIG", "I I", "G G");
		this.setIngredient('G', Material.GOLD_INGOT);
		this.setIngredient('I', Material.IRON_INGOT);
		
		return this;
	}
	
}
