package me.rey.core.classes.abilities.brute.axe;

import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.effects.Effect;
import me.rey.core.effects.ParticleEffect;
import me.rey.core.effects.SoundEffect;
import me.rey.core.effects.repo.Silence;
import me.rey.core.players.User;
import me.rey.core.utils.UtilLoc;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Arrays;

public class Adrenaline extends Ability {

	public Adrenaline() {
		super(611, "Adrenaline", ClassType.DIAMOND, AbilityType.AXE, 1, 5, 15.0D, Arrays.asList(
				"Clear all your status effects and",
				"gain Resistance 1 and Speed 1 for",
				"<variable>5+(0.5*l)</variable> (+0.5) Seconds.",
				"",
				"Recharge: 15 Seconds"
				));
		
		this.setWhileSilenced(true);
	}

	@Override
	protected boolean execute(User u, Player p, int level, Object... conditions) {
		PotionEffect RES = new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, (int) Math.round((5 + (0.5*level)) * 20), 0, false, false),
				 	 SPEED = new PotionEffect(PotionEffectType.SPEED, (int) Math.round((5 + (0.5*level)) * 20), 0, false, false);

		RES.apply(p	);
		SPEED.apply(p);
		Effect.clearAllEffects(p, Arrays.asList(Silence.class));

		new SoundEffect(Sound.DRINK, 0.5F).play(p.getLocation());
		new SoundEffect(Sound.SILVERFISH_IDLE, 0.5F).play(p.getLocation());

		for (int i = 0; i < 5; i++) {
			ArrayList<Location> locs = UtilLoc.circleLocations(p.getLocation(), 2+i, 36);

			for (Location loc : locs) new ParticleEffect.ColoredParticle(52, 113, 199).setEffect(org.bukkit.Effect.POTION_SWIRL).play(loc);
		}

		this.sendUsedMessageToPlayer(p, this.getName());
		return true;
	}

}
