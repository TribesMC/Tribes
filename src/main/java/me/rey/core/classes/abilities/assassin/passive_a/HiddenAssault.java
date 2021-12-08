package me.rey.core.classes.abilities.assassin.passive_a;

import me.rey.core.Warriors;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.classes.abilities.IConstant;
import me.rey.core.events.customevents.CustomPlayerInteractEvent;
import me.rey.core.events.customevents.update.UpdateEvent;
import me.rey.core.players.User;
import me.rey.core.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class HiddenAssault extends Ability implements IConstant {

	private static final double energyPerTick = 2D;
	
	private Set<UUID> shifting = new HashSet<>(), loading = new HashSet<>();

	public HiddenAssault() {
		super(31, "Hidden Assault", ClassType.LEATHER, AbilityType.PASSIVE_A, 1, 3, 0.00, Arrays.asList(
				"Shifting for <variable>3.5-(0.5*l)</variable> seconds allows",
				"you to become completely invisible.", "",
				"You become visible when you unshift."
				));
		
		this.setIgnoresCooldown(true);
	}

	@Override
	protected boolean execute(User u, Player p, int level, Object... conditions) {
		boolean isShifting = p.isSneaking(), isInLiquid = isInLiquid(p);
		double requiredShiftTime = 3.5 - (0.5 * level);
	
		if(!isShifting || isInLiquid) {
			this.remove(p);
			return false;
		}

		// Player is always shifting at this point
		if(!this.shifting.contains(p.getUniqueId()) && !loading.contains(p.getUniqueId())) {
			loading.add(p.getUniqueId());
			
			BukkitTask task = new BukkitRunnable() {
				@Override
				public void run() {
					if(loading.contains(p.getUniqueId()) && !shifting.contains(p.getUniqueId())) {
						shifting.add(p.getUniqueId());
						loading.remove(p.getUniqueId());
						p.removePotionEffect(PotionEffectType.INVISIBILITY);
						sendAbilityMessage(p, "You are now invisible.");
						p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 20 * 100000, 1, false, false));
						Utils.hidePlayer(p);
						
						Location loc = p.getEyeLocation();
						loc.setY(loc.getY() + 1);
						p.getWorld().spigot().playEffect(loc, Effect.LARGE_SMOKE, 0, 0, 0.1F, 0.3F, 0.1F, 0F, 50, 50);
					}
				}
			}.runTaskLater(Warriors.getInstance().getPlugin(), (int) (requiredShiftTime * 20));
			
			new BukkitRunnable() {
				@Override
				public void run() {
					if(!p.isSneaking() || isInLiquid(p)) {
						loading.remove(p.getUniqueId());
						shifting.remove(p.getUniqueId());
						task.cancel();
						this.cancel();
					} else if (!loading.contains(p.getUniqueId())) {
						task.cancel();
						this.cancel();
					}
					
				}
			}.runTaskTimer(Warriors.getInstance().getPlugin(), 0, 1);
			
		}
		
		if(this.shifting.contains(p.getUniqueId())) {
			Utils.hidePlayer(p);
			this.setEnergyCost(energyPerTick, 0);
		}
		
		return true;
	}
	
	@EventHandler
	public void hidingPlayers(UpdateEvent e) {
		for(UUID uuid : shifting) {
			Player p = Bukkit.getServer().getPlayer(uuid);
			if(p == null || !p.isOnline()) {
				this.shifting.remove(uuid);
				continue;
			}
			
			if(!p.isSneaking() || isInLiquid(p)) {
				this.sendNoLongerInvisible(p);
			}
		}
	}
	
	@EventHandler
	public void onPlayerHit(CustomPlayerInteractEvent e) {
		this.remove(e.getPlayer());
	}

	/**
	 * Reveals the player again
	 * and removes them from the maps shifting and loading
	 *
	 * This is done without a Map.contains() check as this
	 * call uses unnecessary processing and resource time.
	 * @param p the player to reveal
	 */
	private void remove(Player p) {
		if(this.isUsing(p)) {
			if (p != null && p.isOnline()) this.sendNoLongerInvisible(p);
			this.shifting.remove(p.getUniqueId());
			this.loading.remove(p.getUniqueId());
		}
	}
	
	private boolean isInLiquid(Player p) {
		return p.getLocation().getBlock() != null && p.getLocation().getBlock().isLiquid();
	}
	
	private void sendNoLongerInvisible(Player p) {
		Utils.showPlayer(p);
		this.shifting.remove(p.getUniqueId());
		this.loading.remove(p.getUniqueId());
		p.removePotionEffect(PotionEffectType.INVISIBILITY);
		sendAbilityMessage(p, "You are no longer invisible.");
	}

	private boolean isUsing(Player p) {
		return this.shifting.contains(p.getUniqueId()) || this.loading.contains(p.getUniqueId());
	}

}