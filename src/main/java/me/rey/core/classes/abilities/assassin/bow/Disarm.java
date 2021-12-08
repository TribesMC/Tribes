package me.rey.core.classes.abilities.assassin.bow;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import me.rey.core.events.customevents.ability.BowAbilityHitEvent;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.classes.abilities.IBowPreparable;
import me.rey.core.effects.repo.Silence;
import me.rey.core.events.customevents.combat.CustomDamageEvent;
import me.rey.core.events.customevents.combat.DamageEvent;
import me.rey.core.players.User;

public class Disarm extends Ability implements IBowPreparable {
	
	private Set<UUID> prepared = new HashSet<>(), shot = new HashSet<>();
	
	public Disarm() {
		super(22, "Disarm", ClassType.LEATHER, AbilityType.BOW, 1, 3, 7.00, Arrays.asList(
				"Prepare yourself to deal a Silencing",
				"effect on your next target with a",
				"duration of <variable>3.0+(0.5*l)</variable> (+0.5) Seconds.", "",
				"Recharge: 7 Seconds"
				));
	}

	@Override
	protected boolean execute(User u, Player p, int level, Object... conditions) {
		
		if(conditions.length == 1 && conditions[0] instanceof PlayerInteractEvent) {
			this.prepare(p);
			return true;
		}
		
		if(conditions.length == 1 && conditions[0] instanceof BowAbilityHitEvent) {

			BowAbilityHitEvent event = ((BowAbilityHitEvent) conditions[0]);
			if (event.getDamagee() == null) return false;

			double seconds = 3.0 + (0.5 * level);
			LivingEntity hit = event.getDamagee();
			if(!(hit instanceof Player)) return false;
		
			new Silence().apply(hit, seconds);
		}

		this.setCooldownCanceled(true);
		return true;
	}
	
	@Override
	public boolean prepare(Player player) {
		return prepared.add(player.getUniqueId());
	}
	
	@Override
	public boolean isPrepared(Player player) {
		return prepared.contains(player.getUniqueId());
	}
	
	@Override
	public boolean unprepare(Player player) {
		return prepared.remove(player.getUniqueId());
	}

	@Override
	public boolean shoot(Player player) {
		return shot.add(player.getUniqueId());
	}

	@Override
	public boolean hasShot(Player player) {
		return shot.contains(player.getUniqueId());
	}

	@Override
	public boolean unshoot(Player player) {
		return shot.remove(player.getUniqueId());
	}

}
