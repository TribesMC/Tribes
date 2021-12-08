package me.rey.core.events.customevents.combat;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import me.rey.core.Warriors;
import me.rey.core.players.combat.PlayerHitCache;
import me.rey.core.pvp.ToolType.HitType;

public class DamageEvent extends CustomDamageEvent {

	private PlayerHitCache cache = Warriors.getInstance().getHitCache();
	
	public DamageEvent(HitType hitType, Player damager, LivingEntity damagee, double damage, ItemStack item, Event originalEvent) {
		super(hitType, damager, damagee, damage, item, originalEvent);
	}

	@Override
	public EntityDamageByEntityEvent getOriginalEvent() {
		return (EntityDamageByEntityEvent) this.originalEvent;
	}
	
	@Override
	public Player getDamager() {
		return (Player) this.damager;
	}
	
	public void storeCache() {
		// ADDING TO THEIR HIT CACHE IF THEY'RE A PLAYER
		if(damagee instanceof Player && !this.isCancelled())
			cache.addToPlayerCache((Player) damagee, this.getHit());
	}

}
