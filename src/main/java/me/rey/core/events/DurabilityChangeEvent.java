package me.rey.core.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;

public class DurabilityChangeEvent implements Listener {
	
	@EventHandler
	public void onItemDamage(PlayerItemDamageEvent e) {
		if(e.getItem() == null) return;
		e.setCancelled(true);
		
		me.rey.core.utils.Utils.updateItem(e.getItem());
		
	}
	
	@EventHandler
	public void onInventoryEvent(InventoryClickEvent e) {
		if(e.getCurrentItem() == null) return;
		
		me.rey.core.utils.Utils.updateItem(e.getCurrentItem());
	}
	
}
