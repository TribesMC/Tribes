package me.rey.core.gui;

import me.rey.core.items.Glow;
import me.rey.core.utils.Text;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Item {

    private boolean glow;
    private Material material;
    private int data;
    private int amount = 1;
    private int durability = 0;
    private String name;
    private List<String> lore;
    private ItemStack item;
    private Map<Enchantment, Integer> enchantements;


    public Item(Material material){
        this.material = material;
        this.lore = new ArrayList<>();
        this.enchantements = new HashMap<Enchantment, Integer>();
        this.glow = false;
    }

    public Item(ItemStack item){
        this.item = item;
        this.amount = item.getAmount();
        this.durability = item.getDurability();
        this.material = item.getType();
        this.lore = item.hasItemMeta() && item.getItemMeta().hasLore() ? item.getItemMeta().getLore() : new ArrayList<>();
        this.enchantements = item.getEnchantments();
        this.name = item.hasItemMeta() && item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : null;
        this.glow = false;
    }

    public Item setName(String name){
        this.name = name;
        return this;
    }

    public Item setLore(List<String> lore){
        List<String> newLore = new ArrayList<String>();
        for(String line : lore) {
            newLore.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        this.lore = newLore;
        return this;
    }

    public Item addLore(String lore) {
        this.lore.add(ChatColor.translateAlternateColorCodes('&', lore));
        return this;
    }

    public Item setData(int data) {
        this.data = data;
        return this;
    }

    public Item setDefaultLore(String... lore) {
        this.addLore("&8&m-------------------");
        this.addLore("&r");
        for(String query : lore) {
            this.addLore(query);
        }
        this.addLore("&r");
        this.addLore("&8&m-------------------");
        return this;
    }

    public Item setAmount(int amount){
        this.amount = amount;
        return this;
    }

    public Item setDurability(int durability) {
        this.durability = durability;
        return this;
    }

    public Item setGlow(boolean glow) {
        this.glow = glow;
        return this;
    }

    public boolean hasGlow() {
        return this.get().containsEnchantment(new Glow(255));
    }

    public Item addEnchantment(Enchantment enchantment, int level){
        if(this.enchantements.containsKey(enchantment))this.enchantements.remove(enchantment);
        this.enchantements.put(enchantment, level);
        return this;
    }

    public String getName() {
        return name != null ? Text.color(name) : Text.formatName(material.name());
    }

    @SuppressWarnings("deprecation")
    public ItemStack get(){
        ItemStack item = null;
        if(this.item != null) {
            item = this.item.clone();
        } else {
            item = new ItemStack(material, amount, (byte) durability);
            item.setData(new MaterialData(material, (byte)data));
        }
        ItemMeta meta = item.getItemMeta();
        if(name != null) meta.setDisplayName(Text.color(name));
        if(lore != null && !lore.isEmpty()) meta.setLore(lore);
        if(glow) {
            Glow glow = new Glow(255);
            meta.addEnchant(glow, 1, true);
        }
        if(enchantements != null && !enchantements.isEmpty()){
            for(Enchantment enchant : enchantements.keySet()){
                item.addEnchantment(enchant, enchantements.get(enchant));
            }
        }
        item.setItemMeta(meta);
        return item;
    }

}