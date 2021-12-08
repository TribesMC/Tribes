package me.rey.core.combat;

import com.google.common.collect.ImmutableMap;
import me.rey.core.Warriors;
import me.rey.core.classes.ClassType;
import me.rey.core.events.customevents.ability.AbilityUseEvent;
import me.rey.core.events.customevents.combat.*;
import me.rey.core.players.User;
import me.rey.core.players.combat.PlayerHit;
import me.rey.core.players.combat.PlayerHitCache;
import me.rey.core.pvp.ToolType;
import me.rey.core.pvp.ToolType.HitType;
import me.rey.core.utils.Text;
import me.rey.core.utils.UtilVelocity;
import net.minecraft.server.v1_8_R3.AttributeModifier;
import org.bukkit.Bukkit;
import org.bukkit.EntityEffect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.*;

public class DamageHandler implements Listener {

	private final long HIT_DELAY = 400;
	PlayerHitCache cache = Warriors.getInstance().getHitCache();

	@EventHandler (priority = EventPriority.HIGHEST)
	public void onDamage(EntityDamageByEntityEvent e) {

		if (!(e.getEntity() instanceof LivingEntity)) return;
		if (e.isCancelled()) return;

		/*
		 * HIT DELAY
		 */
		double noDamageTicks = ((LivingEntity) e.getEntity()).getMaximumNoDamageTicks() * HIT_DELAY / 20D;
		if (CombatManager.timeAgo((LivingEntity) e.getEntity()) <= noDamageTicks) {
			((LivingEntity) e.getEntity()).setMaximumNoDamageTicks(20);
			e.setCancelled(true);
			return;
		}
	
		CustomDamageEvent damageEvent = null;
	
		/*
		 * SETTING ENTITY TO SHOOTER
		 */
		HitType hitType = HitType.MELEE;
		Entity damager = e.getDamager(), damagee = e.getEntity();;
		if (damager instanceof Projectile && ((Projectile) damager).getShooter() != null && ((Projectile) damager).getShooter() instanceof LivingEntity) {
			damager = (LivingEntity) ((Projectile) damager).getShooter();
			hitType = e.getDamager() instanceof Arrow ? HitType.ARCHERY : HitType.OTHER;
		}

		/* Cancelling if it's own hit */
		if (damager instanceof Player && damagee instanceof Player && damager.getUniqueId().equals(damagee.getUniqueId())) {
			e.setCancelled(true);
			return;
		}
		
		/*
		 * ADDING TO HIT CACHE
		 */
		if (!(damager instanceof Player) && (e.getEntity() instanceof Player) && (damager instanceof LivingEntity)) {
			String name = Text.format(((LivingEntity) damager).getName());
			cache.addToPlayerCache((Player) e.getEntity(), new PlayerHit((Player) e.getEntity(), name, e.getDamage(), null));
		}
		
		// RAW DAMAGE
		ItemStack hold = damager instanceof LivingEntity ? ((LivingEntity) damager).getEquipment().getItemInHand() : null;
		double rawDamage = this.getDamage(hold, hitType, e.getDamage());
		if (damager instanceof Player) e.setDamage(rawDamage);


		/*
		 * CALL DAMAGE EVENT
		 */
		if(damager instanceof Player) {
				
			Player playerDamager = (Player) damager;
			
			damageEvent = new DamageEvent(hitType, playerDamager, (LivingEntity) damagee, e.getDamage(), hold, e);
			Bukkit.getServer().getPluginManager().callEvent(damageEvent);
			
			e.setDamage(damageEvent.getDamage());
			
			if(damageEvent.isCancelled())
				e.setCancelled(true);

			playerDamager.setLevel((int) Math.round(e.getDamage()));

			// CALCULATE EFECTS
			e.setDamage(calcEffects(e.getDamage(), (LivingEntity) damagee, (LivingEntity) damager));

			((DamageEvent) damageEvent).storeCache();
		}
		
		/*
		 * ENTITY DAMAGED PLAYER
		 */
		if(damagee instanceof Player && damager instanceof LivingEntity) {
			double multiplier = -11111;
			if (damageEvent != null) multiplier = damageEvent.getKnockbackMult();

			damageEvent = new DamagedByEntityEvent(hitType, (LivingEntity) damager, (Player) damagee, e.getDamage(), ((LivingEntity) damagee).getEquipment().getItemInHand(), e);
			if (multiplier != -11111) damageEvent.setKnockbackMult(multiplier);
			Bukkit.getServer().getPluginManager().callEvent(damageEvent);

			e.setDamage(damageEvent.getDamage());

			if(damageEvent.isCancelled()) {
				e.setCancelled(true);
			}

		}

		if (!(damager instanceof LivingEntity)) return;

		// CALCULATING FINAL DAMAGE ON ARMOR
		e.setDamage(calcArmor(e.getDamage(), (LivingEntity) damagee, e));
		
		FinalEntityDamageEvent end = new FinalEntityDamageEvent(hitType, (LivingEntity) damager, (LivingEntity) damagee, e.getDamage(), hold, e);
		Bukkit.getServer().getPluginManager().callEvent(end);
		e.setDamage(end.getDamage());
		
		if(end.isCancelled())
			e.setCancelled(true);
		
		if(!e.isCancelled()) {
			
			/*
			 * CUSTOM DAMAGE
			 */
			if(hitType == HitType.MELEE) {
				e.setCancelled(true);

				((LivingEntity) e.getEntity()).setHealth(Math.max(0, Math.min(((LivingEntity) e.getEntity()).getHealth() - e.getDamage(),
						((LivingEntity) e.getEntity()).getMaxHealth())));
				
				CombatManager.resetTime((LivingEntity) e.getEntity());
			}


			/*
			 * DISPLAY SOUNDS
			 */
			playEntitySound((LivingEntity) e.getEntity());

			/*
			 * KNOCKBACK
			 */
			double multiplier = 1;
			if(damageEvent != null) {
				multiplier *= damageEvent.getKnockbackMult();
			}

			CombatKnockbackEvent kbEvent = new CombatKnockbackEvent(damagee, damager, e.getDamage(), multiplier);
			Bukkit.getServer().getPluginManager().callEvent(kbEvent);

			/* Cancelling KB if dead */
			double health = ((LivingEntity) e.getEntity()).getHealth();
			if (e.getEntity().isDead() || health <= 0) kbEvent.setCancelled(true);
			
			if(!kbEvent.isCancelled())
				kb(kbEvent.getDamagee(), kbEvent.getDamager(), kbEvent.getDamage(), kbEvent.getMult());
		}
	}
	
	@EventHandler
	public void onAnyDamage(EntityDamageEvent e) {
		/*
		 * CACHE
		 */
		if(e instanceof EntityDamageByEntityEvent) return;
		if(!(e.getEntity() instanceof Player)) return;
		if(e.isCancelled()) return;
		
		Player target = (Player) e.getEntity();
		String damager = Text.format(e.getCause().name());
		
		cache.addToPlayerCache(target, new PlayerHit(target, damager, e.getDamage(), null));
		
		if (e.getCause() == DamageCause.FALL) return;
		// armor values
		e.setDamage(calcEffects(e.getDamage(), target, null));
		e.setDamage(calcArmor(e.getDamage(), target, e));
	}
	
	@EventHandler (priority = EventPriority.HIGHEST)
	public void onAbilityUse(AbilityUseEvent e) {
		if(!e.isCancelled())
			cache.startCombatTimer(e.getPlayer());
	}
	
	@EventHandler (priority = EventPriority.HIGHEST)
	public void onDamage(DamageEvent e) {
		if(e.isCancelled()) return;
		
		if(!(e.getDamagee() instanceof Player)) return;
		cache.startCombatTimer(e.getDamager());
		cache.startCombatTimer((Player) e.getDamagee());
	}
	
	public static double calcArmor(double damage, LivingEntity entity, EntityDamageEvent e) {

		HashSet<DamageCause> trueDamageCauses = new HashSet<>(Arrays.asList(
			DamageCause.DROWNING,
			DamageCause.FIRE,
			DamageCause.FIRE_TICK,
			DamageCause.WITHER,
			DamageCause.POISON,
			DamageCause.LIGHTNING,
			DamageCause.STARVATION,
			DamageCause.VOID
		));

		if (e != null && e.getCause() != null && trueDamageCauses.contains(e.getCause())) return damage;

		/*
		 * ARMOR VALUES
		 */
		if(entity instanceof Player) {
			ClassType wearing = new User((Player) entity).getWearingClass();
			
			if(e != null && e.isApplicable(DamageModifier.ARMOR))
				e.setDamage(DamageModifier.ARMOR, 0);
			
			if(wearing != null) {
				damage = Math.max(0, Math.min(damage, entity.getMaxHealth()));
				return (entity.getMaxHealth() / wearing.getHealth()) * damage;
			}
		
		}
		return damage;
	}
	
	public static double calcEffects(double damage, LivingEntity hit, LivingEntity hitter) {
		
		Map<PotionEffectType, Double> damager = ImmutableMap.of(
				PotionEffectType.INCREASE_DAMAGE, 1.00, // STRENGTH
				PotionEffectType.WEAKNESS, -1.00 // WEAKNESS
				);
		
		Map<PotionEffectType, Double> damagee = ImmutableMap.of(
				PotionEffectType.DAMAGE_RESISTANCE, -1.00 // RESISTANCE
				);
		
		/*
		 * DAMAGER POTION EFFECTS
		 */
		if(hitter != null) {
			LivingEntity ent = hitter;
			
			if(!ent.getActivePotionEffects().isEmpty()) {
				Map<PotionEffectType, Integer> types = new HashMap<>();
				ent.getActivePotionEffects().forEach((effect) -> types.put(effect.getType(), effect.getAmplifier()));
				
				
				for(PotionEffectType effect : damager.keySet()) {
					if(types.containsKey(effect))
						damage = damage + (damager.get(effect) * (types.get(effect) + 1));
				}
			}
		}
		
		/*
		 * DAMAGEE POTION EFFECTS
		 */
		LivingEntity ent = hit;
		
		if(!ent.getActivePotionEffects().isEmpty()) {
			Map<PotionEffectType, Integer> types = new HashMap<>();
			ent.getActivePotionEffects().forEach((effect) -> types.put(effect.getType(), effect.getAmplifier()));
			
			for(PotionEffectType effect : damagee.keySet()) {
				if(types.containsKey(effect))
					damage = damage + (damagee.get(effect) * (types.get(effect) + 1));
			}
		}
		damage = Math.max(0, Math.min(damage, ent.getMaxHealth()));
		return damage;
	}
	
	private void kb(Entity entity, Entity hitter, double damage, double multiplier) {
		
		damage += 3; 
		if (damage < 2.0D) damage = 2.0D;
		damage = Math.log10(damage);
		
		Vector trajectory = entity.getLocation().toVector().subtract(hitter.getLocation().toVector());
		trajectory.multiply(0.05D * damage * 2D);

		if(entity.isOnGround()) {
			trajectory.setY(0.02D);
		} else {
			trajectory.setY(0D);
		}
		
		UtilVelocity.velocity(entity, hitter, trajectory,
				multiplier, 0.3D + trajectory.length() * 0.8D, false, 0.0D, Math.abs(0.2D * damage), 0.4D + 0.04D * damage, true);
	}
	
	public static void playEntitySound(LivingEntity damagee) {
	    Sound sound;
	    float pitch = 0.8F + (float)(0.4000000059604645D * Math.random());
	    float volume = 1.5F + (float)(0.5D * Math.random());
	    
	    switch(damagee.getType()) {
	    case BAT:sound = Sound.BAT_HURT; break;
	    case BLAZE: sound = Sound.BLAZE_HIT; break;
	    case CAVE_SPIDER: sound = Sound.SPIDER_IDLE; break;
	    case CHICKEN: sound = Sound.CHICKEN_HURT; break;
	    case COW: sound = Sound.COW_HURT; break;
	    case CREEPER: sound = Sound.CREEPER_HISS; break;
	    case ENDER_DRAGON: sound = Sound.ENDERDRAGON_GROWL; break;
	    case ENDERMAN: sound = Sound.ENDERMAN_HIT; break;
	    case GHAST: sound = Sound.GHAST_SCREAM; break;
	    case GIANT: sound = Sound.ZOMBIE_HURT; break;
	    case IRON_GOLEM: sound = Sound.IRONGOLEM_HIT; break;
	    case MAGMA_CUBE: sound = Sound.MAGMACUBE_JUMP; break;
	    case MUSHROOM_COW: sound = Sound.COW_HURT; break;
	    case OCELOT: sound = Sound.CAT_MEOW; break;
	    case PIG: sound = Sound.PIG_IDLE; break;
	    case PIG_ZOMBIE: sound = Sound.ZOMBIE_PIG_HURT; break;
	    case SHEEP: sound = Sound.SHEEP_IDLE; break;
	    case SILVERFISH: sound = Sound.SILVERFISH_HIT; break;
	    case SKELETON: sound = Sound.SKELETON_HURT; break;
	    case SLIME: sound = Sound.SLIME_ATTACK; break;
	    case SNOWMAN: sound = Sound.STEP_SNOW; break;
	    case SPIDER: sound = Sound.SPIDER_IDLE; break;
	    case VILLAGER: sound = Sound.VILLAGER_HIT; break;
	    case WITHER: sound = Sound.WITHER_HURT; break;
	    case WOLF: sound = Sound.WOLF_HURT; break;
	    case ZOMBIE: sound = Sound.ZOMBIE_HURT; break;
	    case ARMOR_STAND: sound = null; break;
	    default:
	    	sound = Sound.HURT_FLESH;
	    	if(damagee instanceof Player && new User((Player) damagee).getWearingClass() != null){
				ClassType type = new User((Player) damagee).getWearingClass();
				sound = type.getSound().getSound();
				pitch = type.getSound().getPitch();
				volume = 1.0f;
			}
	    	break;
	    }

	    if (sound != null) {
			damagee.getWorld().playSound(damagee.getLocation(), sound, volume, pitch);
		}
	    damagee.playEffect(EntityEffect.HURT);
	}
	
	private double getDamage(ItemStack hold, HitType hitType, double initialDamage) {
		final double baseDmg = 1D;
		
		if (hold != null && !hold.getType().equals(Material.AIR)) {

			if (hitType.equals(HitType.ARCHERY)) {
				return Math.min(5, initialDamage * 6 / 7);
			}

			for(ToolType toolType : ToolType.values()) {
				if(hitType.equals(HitType.MELEE) && hold.getType().equals(toolType.getType()))
					return toolType.getDamage();
			}
			
			net.minecraft.server.v1_8_R3.ItemStack item = CraftItemStack.asNMSCopy(hold);
			Iterator<AttributeModifier> attackDmg = item.B().get("generic.attackDamage").iterator();;
			double defDamage = attackDmg.hasNext() ? attackDmg.next().d() : baseDmg;

			return defDamage != initialDamage ? initialDamage : defDamage;
		}
		
		return baseDmg;
	}
	
//    @EventHandler
//    public void onKB(CustomKnockbackEvent e) {
//        double knockback = e.getDamage();
//        if (e.getDamager() instanceof Player) {
//            Player player = (Player) e.getDamager();
//            if (player.isSprinting()) {
//                if (e.d.getCause() == DamageCause.ENTITY_ATTACK) {
//                    knockback += 3;
//                }
//            }
//        }
//        if (knockback < 2.0D) knockback = 2.0D;
//        knockback = Math.log10(knockback);
//
//        e.setDamage(knockback);
//    }
//
//    @EventHandler(priority = EventPriority.MONITOR)
//    public void onFinalKB(CustomKnockbackEvent e) {
//        Vector trajectory = UtilVelocity.getTrajectory2d(e.getDamager(), e.getDamagee());
//        trajectory.multiply(0.8D * e.getDamage());
//        trajectory.setY(Math.abs(trajectory.getY()));
//
//        UtilVelocity.velocity(e.getDamagee(),
//                trajectory, 0.3D + trajectory.length() * 0.8D, false, 0.0D, Math.abs(0.2D * e.getDamage()), 0.4D + 0.04D * e.getDamage(), true);
//    }

}
