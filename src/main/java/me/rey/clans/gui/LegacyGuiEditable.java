package me.rey.clans.gui;

import org.bukkit.event.inventory.InventoryType;
import org.bukkit.plugin.Plugin;

@Deprecated
public abstract class LegacyGuiEditable extends Gui {

	public LegacyGuiEditable(String name, int rows, Plugin plugin) {
		super(name, rows, plugin);
	}

	public LegacyGuiEditable(InventoryType type, String name, int rows, Plugin plugin) {
		super(type, name, rows, plugin);
	}
	
	public abstract void setup();
	
	public abstract void init();
	
	public void updateInventory() {
		
		for(int i = 0; i < this.getSize(); i++) {
			this.removeItem(i);
		}
		
		this.setup();
		this.init();
		
	}

}
