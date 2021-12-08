package me.rey.core.classes;

import me.rey.core.Warriors;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.classes.abilities.bandit.axe.Leap;
import me.rey.core.effects.SoundEffect;
import me.rey.core.gui.Item;
import me.rey.core.pvp.Build;
import me.rey.core.pvp.Build.DefaultBuild;
import me.rey.core.utils.enums.ArmorPiece;
import me.rey.core.utils.items.ColoredArmor;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public enum ClassType {
	
	LEATHER(38, "Assassin", SoundEffect.SOFT,
			new DefaultBuild(),
			Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS,
			new PotionEffect(PotionEffectType.SPEED, 20 * 100000, 0, false, false)
	),
	
	CHAIN(46, "Marksman", SoundEffect.MILD_TANK_A,
			new DefaultBuild(),
			Material.CHAINMAIL_HELMET, Material.CHAINMAIL_CHESTPLATE, Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_BOOTS
	),
	
	GOLD(44, "Mage", SoundEffect.MILD_TANK_B,
			new DefaultBuild(),
			Material.GOLD_HELMET, Material.GOLD_CHESTPLATE, Material.GOLD_LEGGINGS, Material.GOLD_BOOTS
	),
	
	IRON(60, "Knight", SoundEffect.TANK_A,
			new DefaultBuild(),
			Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS, Material.IRON_BOOTS
	),
	
	DIAMOND(58, "Brute", SoundEffect.TANK_B,
			new DefaultBuild(),
			Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS
	),
	
	BLACK(38, "Bandit", SoundEffect.SOFT,
			new DefaultBuild(new Leap().setTempDefaultLevel(4)),

			new ColoredArmor(ArmorPiece.HELMET, 0, 0, 0), new ColoredArmor(ArmorPiece.CHESTPLATE, 0, 0, 0),
			new ColoredArmor(ArmorPiece.LEGGINGS, 0, 0, 0), new ColoredArmor(ArmorPiece.BOOTS, 0, 0, 0),
			new PotionEffect(PotionEffectType.SPEED, 20 * 100000, 0, false, false)
			),
	GREEN(44, "Shaman", SoundEffect.MILD_TANK_B,
			new DefaultBuild(),
			new ColoredArmor(ArmorPiece.HELMET, 39, 174, 96), new ColoredArmor(ArmorPiece.CHESTPLATE, 39, 174, 96),
			new ColoredArmor(ArmorPiece.LEGGINGS, 39, 174, 96), new ColoredArmor(ArmorPiece.BOOTS, 39, 174, 96)
			);
	
	private String name;
	private Item helmet, chestplate, leggings, boots;
	private PotionEffect[] effects;
	private DefaultBuild defaultBuild;
	private double health;
	private SoundEffect sound;
	protected boolean canUseBow = false;
	
	ClassType(double health, String name, SoundEffect hitSound, DefaultBuild defaultBuild, Material helmet, Material chestplate, Material leggings, Material boots, PotionEffect... effects){
		this.health = health;
		this.name = name;
		this.helmet = new Item(helmet);
		this.chestplate = new Item(chestplate);
		this.leggings = new Item(leggings);
		this.boots = new Item(boots);
		this.effects = effects;
		this.defaultBuild = defaultBuild;
		this.sound = hitSound;
	}
	
	ClassType(double health, String name, SoundEffect hitSound, DefaultBuild defaultBuild, ColoredArmor helmet, ColoredArmor chestplate, ColoredArmor leggings, ColoredArmor boots, PotionEffect... effects){
		this.health = health;
		this.name = name;
		this.helmet = new Item(helmet.get()).setName(null);
		this.chestplate = new Item(chestplate.get()).setName(null);
		this.leggings = new Item(leggings.get()).setName(null);
		this.boots = new Item(boots.get()).setName(null);
		this.effects = effects;
		this.defaultBuild = defaultBuild;
		this.sound = hitSound;
	}

	public static void scheduleChecks() {
		for (ClassType type : ClassType.values()) {
			for (Ability ability : Warriors.getInstance().getAbilitiesInCache()) {
				if (type.equals(ability.getClassType()) && ability.getAbilityType().equals(AbilityType.BOW)) {
					type.canUseBow = true;
					break;
				}
			}
		}
	}
	
	public double getHealth() {
		return health;
	}

	public String getName() {
		return name;
	}

	public Item getHelmet() {
		return helmet;
	}

	public Item getChestplate() {
		return chestplate;
	}

	public Item getLeggings() {
		return leggings;
	}

	public Item getBoots() {
		return boots;
	}
	
	public Item[] getArmor() {
		return new Item[] {this.getHelmet(), this.getChestplate(), this.getLeggings(), this.getBoots()};
	}
	
	public PotionEffect[] getEffects() {
		return effects;
	}

	public Build getDefaultBuild() {
		return this.defaultBuild;
	}
	
	public SoundEffect getSound() {
		return this.sound;
	}

    public boolean canUseBow() {
		return canUseBow;
    }

    public static ClassType getClassTypeFromString(String str) {
		switch(str.toUpperCase()) {
			case "LEATHER":
			case "ASSASSIN":
				return LEATHER;

			case "CHAIN":
			case "MARKSMAN":
				return CHAIN;

			case "GOLD":
			case "MAGE":
				return GOLD;

			case "IRON":
			case "KNIGHT":
				return IRON;

			case "DIAMOND":
			case "BRUTE":
				return DIAMOND;

			case "BLACK":
			case "BANDIT":
				return BLACK;

			case "GREEN":
			case "SHAMAN":
				return GREEN;

			default:
				return null;
		}
	}

	public List<ClassCondition> getClassConditions() {
		return ClassCondition.getClassConditions(this);
	}

}
