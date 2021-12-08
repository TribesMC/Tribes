package me.rey.core.classes.abilities.bandit.passive_a;

import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.classes.abilities.IConstant.IDroppable;
import me.rey.core.effects.ParticleEffect;
import me.rey.core.effects.SoundEffect;
import me.rey.core.effects.repo.Shock;
import me.rey.core.players.User;
import me.rey.core.utils.UtilLoc;
import me.rey.core.utils.UtilVelocity;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class Scream extends Ability implements IDroppable {
	
	public Scream() {
		super(133, "Scream", ClassType.BLACK, AbilityType.PASSIVE_A, 1, 3, 25, Arrays.asList(
				"Players within <variable>4.5+(0.5*l)</variable> (+0.5) blocks nearby will",
				"be pushed back <variable>2.5+(0.5*l)</variable> (+0.5) blocks and will",
				"be shocked and slowed for 2 Seconds.",
				"",
				"Recharge: <variable>45-(5*l)</variable> (-5) Seconds"
				));
		
		this.setWhileSlowed(false);
	}

	@Override
	protected boolean execute(User u, Player p, int level, Object... conditions) {
		double radius = 4.5 + (0.5 * level);
		double secondsForEffecs = 2;
		double knockback = 2.5 + (0.5 * level);

		p.getWorld().playSound(p.getLocation(), Sound.ZOMBIE_REMEDY, 0.5F, 3.0F);
		new SoundEffect(Sound.GHAST_SCREAM, 1.5F).setVolume(1F).play(p.getLocation());

		Iterator<Entity> nearby = p.getNearbyEntities(radius, 4, radius).iterator();
		Shock shock = new Shock();

		for (int i = 0; i < 3; i++) {
			ArrayList<Location> loc = UtilLoc.circleLocations(p.getLocation(), Math.max(0, radius-i));

			for (Location l : loc) {
				new ParticleEffect(Effect.CLOUD).play(l);
				new ParticleEffect.ColoredParticle(90, 193, 199).play(l);
				new ParticleEffect.ColoredParticle(255, 0, 0).play(l);
				new ParticleEffect.ColoredParticle(255, 255, 255).play(l);
			}
		}
		
		while(nearby.hasNext()) {
			Entity next = nearby.next();
			if(!(next instanceof LivingEntity)) continue;
			
			LivingEntity le = (LivingEntity) next;
			
			boolean kb = true;
			if(le instanceof Player) {
				if(u.getTeam().contains((Player) le))
					kb = false;
			}
			
            if(kb) { pushAway(p, le, knockback);
	            shock.apply(le, 2);
	            le.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) (20 * secondsForEffecs), 1, false, false));
            }
		}
		

		this.setCooldown(45 - (level * 5));
		return true;
	}

    public void pushAway(Player user, Entity pToPush, double blocks) {
        double pX = user.getLocation().getX();
        double pY = user.getLocation().getY();
        double pZ = user.getLocation().getZ();

        double tX = pToPush.getLocation().getX();
        double tY = pToPush.getLocation().getY();
        double tZ = pToPush.getLocation().getZ();

        double deltaX = tX - pX < 0 ? -0.25 : 0.25;
        double deltaY = tY - pY < 0 ? -0.3 : 0.3;
        double deltaZ = tZ - pZ < 0 ? -0.25 : 0.25;

		UtilVelocity.velocity(pToPush, user, new Vector(deltaX * blocks, deltaY * blocks, deltaZ * blocks).normalize().multiply(1.25).setY(0.3D));
    }
}
