package me.rey.core.classes.abilities.bandit.passive_b;

import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.classes.abilities.IDamageTrigger.IPlayerDamagedEntity;
import me.rey.core.events.customevents.combat.DamageEvent;
import me.rey.core.players.User;
import me.rey.core.utils.UtilEnt;
import org.bukkit.Effect;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class Backstab extends Ability implements IPlayerDamagedEntity {

	public Backstab() {
		super(141, "Backstab", ClassType.BLACK, AbilityType.PASSIVE_B, 1, 3, 0.00, Arrays.asList(
				"Attacks from behind opponents",
				"deal <variable>0.75*l+1.5</variable> (+0.75) additional damage."
				));
		this.setIgnoresCooldown(true);
		this.setInLiquid(true);
	}

	@Override
	protected boolean execute(User u, Player p, int level, Object... conditions) {
		DamageEvent e = null;

		try {
			e = (DamageEvent) conditions[0];
		} catch (ArrayIndexOutOfBoundsException s) {
			System.out.println("Already known exception occurred [Backstab]");
			return false;
		}
		
		Player damager = e.getDamager();
		LivingEntity damagee = e.getDamagee();

		if(UtilEnt.isBehind(damager, damagee)) {
			
			// DAMAGE
			e.addMod(0.75*level+1.5);
			
			// PLAY EFFECTS
			damagee.getWorld().playSound(damagee.getLocation(), Sound.HURT_FLESH, 1f, 2f);
			damagee.getWorld().playEffect(damagee.getLocation(), Effect.STEP_SOUND, 55);
			return true;
		}
		return false;
	}

}
