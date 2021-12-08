package me.rey.core.classes.conditions;

import me.rey.core.classes.ClassCondition;
import me.rey.core.classes.ClassType;
import me.rey.core.events.customevents.combat.CombatKnockbackEvent;
import me.rey.core.players.User;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.Arrays;

public class Vigour extends ClassCondition {

	public Vigour() {
		super(ClassType.BLACK, "Vigour", Arrays.asList(
				"User receives Speed 1, takes no knockback",
				"and fall damage is reduced by 2 points."
		));
	}

	@Override
	protected void execute(User user, Player player) {
		// IGNORE
	}
	
	@EventHandler (priority = EventPriority.LOWEST)
	public void onCustomKB(CombatKnockbackEvent e) {
		if(!(e.getDamager() instanceof Player)) return;
		if(new User((Player) e.getDamager()).getWearingClass() != this.getClassType()) return;

		e.setCancelled(true);
	}

}
