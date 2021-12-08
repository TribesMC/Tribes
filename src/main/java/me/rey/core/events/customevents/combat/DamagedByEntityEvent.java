package me.rey.core.events.customevents.combat;

import me.rey.core.Warriors;
import me.rey.core.players.combat.PlayerHit;
import me.rey.core.players.combat.PlayerHitCache;
import me.rey.core.pvp.ToolType.HitType;
import me.rey.core.utils.Text;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

public class DamagedByEntityEvent extends CustomDamageEvent {
	
	private PlayerHitCache cache = Warriors.getInstance().getHitCache();

	public DamagedByEntityEvent(HitType hitType, LivingEntity damager, Player damagee, double damage, ItemStack item, EntityDamageByEntityEvent originalEvent) {
		super(hitType, damager, damagee, damage, item, originalEvent);
	}

	@Override
	public EntityDamageByEntityEvent getOriginalEvent() {
		return (EntityDamageByEntityEvent) this.originalEvent;
	}
	
	@Override
	public Player getDamagee() {
		return (Player) this.damagee;
	}
	
	public void storeCache() {
		String name = Text.format(((LivingEntity) damager).getName());
		cache.addToPlayerCache((Player) damagee, new PlayerHit((Player) damagee, name, damage, null));
	}

}

