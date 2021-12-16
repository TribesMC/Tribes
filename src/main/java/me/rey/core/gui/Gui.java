package me.rey.core.gui;

import me.rey.core.effects.SoundEffect;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

public abstract class Gui implements Listener {

	protected final SoundEffect highPitch = new SoundEffect(Sound.NOTE_PLING, 2F);
	protected final SoundEffect lowPitch = new SoundEffect(Sound.NOTE_PLING, 0.25F);

	Inventory inventory;
	
	private String name;
	private int rows, size;
	private Plugin plugin;
    private Map<Integer, GuiItem> events = new HashMap<Integer, GuiItem>();
    private InventoryType type;
	
	public Gui(String name, int rows, Plugin plugin) {
		this.name = name;
		this.rows = rows;
		this.size = rows*9;
		this.plugin = plugin;
		this.create();
	}
	
	public Gui(InventoryType type, String name, int rows, Plugin plugin) {
		this.type = type;
		this.name = name;
		this.rows = rows;
		this.size = rows*9-1;
		this.plugin = plugin;
		this.create();
	}
	
	private void create() {
		inventory = Bukkit.createInventory(null, 9*rows, ChatColor.translateAlternateColorCodes('&', name));
		if(type != null) inventory = Bukkit.createInventory(null, type);
		
		Bukkit.getPluginManager().registerEvents(this, plugin);
		
		init();
	}
	
	
	public abstract void init();
	
	protected void addItem(GuiItem guiItem) {
		if(this.getInventory().firstEmpty() == -1) return;
		setItem(guiItem, this.getInventory().firstEmpty());
	}
	
	protected void removeItem(int slot, int... slots) {
		for(int query : slots) {
			if(!events.containsKey(query)) continue;
			events.remove(query);
			this.inventory.setItem(query, new ItemStack(Material.AIR));
		}
		
		if(events.containsKey(slot)) {
			events.remove(slot);
			this.inventory.setItem(slot, new ItemStack(Material.AIR));
		}
	}

	protected void setItem(GuiItem item, int slot, int... slots) {
		setItem(item, slot, false, slots);
	}

	protected void setItem(GuiItem item, int slot, boolean override, int... slots) {
		for(int query : slots) {
			if (!override) {
				if (events.containsKey(query)) continue;
			}
			this.events.put(query, item);
			this.inventory.setItem(query, item.get());
		}

		if (!override) {
			if (events.containsKey(slot)) return;
		}
		this.events.put(slot, item);
		this.inventory.setItem(slot, item.get());
	}
	
	protected void fillEmptySlots(GuiItem guiItem) {
		for(int i = 0; i < this.getInventory().getSize() - 1; i++) {
			this.addItem(guiItem);
		}
	}
	
	public void open(Player player) {
		player.openInventory(this.inventory);
	}

	public Inventory getInventory() {
		return inventory;
	}

	public String getName() {
		return name;
	}

	public int getRows() {
		return rows;
	}

	public int getSize() {
		return size;
	}
	
	public GuiItem getItem(int slot) {
		if(events.containsKey(slot)) {
			return events.get(slot);
		}
		return null;
	}
	
	@EventHandler
	public void onClick(InventoryClickEvent e) {
		Player player = (Player) e.getWhoClicked();

	
		if(e.getInventory().equals(this.inventory)) {
			e.setCancelled(true);
			if(!this.events.containsKey(e.getSlot())) return;
			
			if(e.getClickedInventory() == null) return;
	        if(e.getClickedInventory().getItem(e.getSlot()) == null) return;
	        if(e.getView().getBottomInventory() == null) return;
	        if((e.getClickedInventory().getHolder() instanceof Player)) return;
			
			GuiItem item = this.events.get(e.getSlot());
			item.onUse(player, e.getClick(), e.getSlot());	
		}
	}
	
	
}
