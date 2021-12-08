package me.rey.clans.items.crafting;

import me.rey.clans.Tribes;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.MaterialData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class CraftingRecipe extends ShapedRecipe implements Listener {

    public static Map<ItemStack, List<CraftingRecipe>> recipes = new HashMap<ItemStack, List<CraftingRecipe>>();
    private final Map<Character, ItemStack> exactIntegrients = new HashMap<Character, ItemStack>();


    public CraftingRecipe(final ItemStack result) {
        super(result);
        Bukkit.getServer().getPluginManager().registerEvents(this, Tribes.getInstance().getPlugin());
    }

    public static List<CraftingRecipe> getRecipes(final ItemStack item) {
        return CraftingRecipe.recipes.get(item);
    }

    public abstract CraftingRecipe init();

    @Override
    public CraftingRecipe setIngredient(final char key, final Material ingredient) {
        super.setIngredient(key, ingredient);
        this.exactIntegrients.put(key, new ItemStack(ingredient));
        return this;
    }

    @Override
    public CraftingRecipe setIngredient(final char key, final MaterialData ingredient) {
        super.setIngredient(key, ingredient);
        this.exactIntegrients.put(key, new ItemStack(ingredient.getItemType(), 1, ingredient.getData()));
        return this;
    }

    @Override
    public CraftingRecipe setIngredient(final char key, final Material ingredient, final int raw) {
        super.setIngredient(key, ingredient, raw);
        this.exactIntegrients.put(key, new ItemStack(ingredient, 1, (short) raw));
        return this;
    }

    public CraftingRecipe setIngredient(final char key, final ItemStack item) {
        super.setIngredient(key, item.getType(), item.getDurability());
        this.exactIntegrients.put(key, item);
        return this;
    }

    public boolean equals(final ItemStack[] matrix) {
        final String[] shape = super.getShape();
        for (int y = 0; y < shape.length; y++) {
            final String line = shape[y];
            for (int x = 0; x < line.length(); x++) {
                final char c = line.charAt(x);
                final int i = y * line.length() + x;
                final ItemStack item0 = matrix[i];
                final ItemStack item1 = this.exactIntegrients.get(c);
                if (item0 != null && item1 != null) {
                    if (item0.getType() != item1.getType() || item0.getDurability() != item1.getDurability() || !item0.getItemMeta().equals(item1.getItemMeta())) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public void register() {
        Bukkit.addRecipe(this);
        final ItemStack result = super.getResult();
        List<CraftingRecipe> list = CraftingRecipe.recipes.get(result);
        if (list == null) {
            list = new ArrayList<CraftingRecipe>();
        }
        if (!list.contains(this)) {
            list.add(this);
            CraftingRecipe.recipes.put(result, list);
        }
    }

    @EventHandler
    public void process(final PrepareItemCraftEvent event) {
        final CraftingInventory inv = event.getInventory();
        final ItemStack result = inv.getResult();

        if (!(this instanceof IExtraCraft)) {
            for (final ItemStack found : recipes.keySet()) {
                if (!(this instanceof IExtraCraft) && found != null && result != null && found.getType().equals(result.getType())) {
                    inv.setResult(null);
                }
            }
        }

        final List<CraftingRecipe> recipes = CraftingRecipe.getRecipes(result);
        if (recipes != null) {
            for (final CraftingRecipe recipe : recipes) {
                if (recipe.equals(inv.getMatrix())) {
                    inv.setResult(recipe.getResult());
                    return;
                }
            }
        }
    }

    public interface IExtraCraft {

    }
}
