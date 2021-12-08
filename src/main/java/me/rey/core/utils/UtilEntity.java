package me.rey.core.utils;

import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UtilEntity {

    public static List<EntityInfo> entities;

    static {
        entities = new ArrayList<>();
        entities.add(new EntityInfo("Dropped Item", EntityType.DROPPED_ITEM, "a"));
        entities.add(new EntityInfo("Experience Orb", EntityType.EXPERIENCE_ORB, "an"));
        entities.add(new EntityInfo("Leash Hitch", EntityType.LEASH_HITCH, "a"));
        entities.add(new EntityInfo("Painting", EntityType.PAINTING, "a"));
        entities.add(new EntityInfo("Arrow", EntityType.ARROW, "an"));
        entities.add(new EntityInfo("Snowball", EntityType.SNOWBALL, "a"));
        entities.add(new EntityInfo("Fireball", EntityType.FIREBALL, "a"));
        entities.add(new EntityInfo("Small Fireball", EntityType.SMALL_FIREBALL, "a"));
        entities.add(new EntityInfo("Ender Pearl", EntityType.ENDER_PEARL, "an"));
        entities.add(new EntityInfo("Ender Signal", EntityType.ENDER_SIGNAL, "an"));
        entities.add(new EntityInfo("Thrown EXP Bottle", EntityType.THROWN_EXP_BOTTLE, "a"));
        entities.add(new EntityInfo("Item Frame", EntityType.ITEM_FRAME, "an"));
        entities.add(new EntityInfo("Wither Skull", EntityType.WITHER_SKULL, "a"));
        entities.add(new EntityInfo("Primed TNT", EntityType.PRIMED_TNT, "a"));
        entities.add(new EntityInfo("Falling Block", EntityType.FALLING_BLOCK, "a"));
        entities.add(new EntityInfo("Firework", EntityType.FIREWORK, "a"));
        entities.add(new EntityInfo("Armor Stand", EntityType.ARMOR_STAND, "an"));
        entities.add(new EntityInfo("Minecart with Command Block", EntityType.MINECART_COMMAND, "a"));
        entities.add(new EntityInfo("Boat", EntityType.BOAT, "a"));
        entities.add(new EntityInfo("Minecart", EntityType.MINECART, "a"));
        entities.add(new EntityInfo("Minecart with Chest", EntityType.MINECART_CHEST, "a"));
        entities.add(new EntityInfo("Minecart with Furnace", EntityType.MINECART_FURNACE, "a"));
        entities.add(new EntityInfo("Minecart with TNT", EntityType.MINECART_TNT, "a"));
        entities.add(new EntityInfo("Minecart with Hopper", EntityType.MINECART_HOPPER, "a"));
        entities.add(new EntityInfo("Minecart with Mob Spawner", EntityType.MINECART_MOB_SPAWNER, "a"));
        entities.add(new EntityInfo("Creeper", EntityType.CREEPER, "a"));
        entities.add(new EntityInfo("Skeleton", EntityType.SKELETON, "a"));
        entities.add(new EntityInfo("Spider", EntityType.SPIDER, "a"));
        entities.add(new EntityInfo("Giant", EntityType.GIANT, "a"));
        entities.add(new EntityInfo("Zombie", EntityType.ZOMBIE, "a"));
        entities.add(new EntityInfo("Slime", EntityType.SLIME, "a"));
        entities.add(new EntityInfo("Ghast", EntityType.GHAST, "a"));
        entities.add(new EntityInfo("Zombie Pigman", EntityType.PIG_ZOMBIE, "a"));
        entities.add(new EntityInfo("Enderman", EntityType.ENDERMAN, "an"));
        entities.add(new EntityInfo("Cave Spider", EntityType.CAVE_SPIDER, "a"));
        entities.add(new EntityInfo("Silverfish", EntityType.SILVERFISH, "a"));
        entities.add(new EntityInfo("Blaze", EntityType.BLAZE, "a"));
        entities.add(new EntityInfo("Magma Cube", EntityType.MAGMA_CUBE, "a"));
        entities.add(new EntityInfo("Ender Dragon", EntityType.ENDER_DRAGON, "an"));
        entities.add(new EntityInfo("Wither", EntityType.WITHER, "a"));
        entities.add(new EntityInfo("Bat", EntityType.BAT, "a"));
        entities.add(new EntityInfo("Witch", EntityType.WITCH, "a"));
        entities.add(new EntityInfo("Endermite", EntityType.ENDERMITE, "an"));
        entities.add(new EntityInfo("Guardian", EntityType.GUARDIAN, "a"));
        entities.add(new EntityInfo("Pig", EntityType.PIG, "a"));
        entities.add(new EntityInfo("Sheep", EntityType.SHEEP, "a"));
        entities.add(new EntityInfo("Cow", EntityType.COW, "a"));
        entities.add(new EntityInfo("Chicken", EntityType.CHICKEN, "a"));
        entities.add(new EntityInfo("Squid", EntityType.SQUID, "a"));
        entities.add(new EntityInfo("Wolf", EntityType.WOLF, "a"));
        entities.add(new EntityInfo("Mushroom Cow", EntityType.MUSHROOM_COW, "a"));
        entities.add(new EntityInfo("Snowman", EntityType.SNOWMAN, "a"));
        entities.add(new EntityInfo("Ocelot", EntityType.OCELOT, "an"));
        entities.add(new EntityInfo("Iron Golem", EntityType.IRON_GOLEM, "an"));
        entities.add(new EntityInfo("Horse", EntityType.HORSE, "a"));
        entities.add(new EntityInfo("Rabbit", EntityType.RABBIT, "a"));
        entities.add(new EntityInfo("Villager", EntityType.VILLAGER, "a"));
        entities.add(new EntityInfo("Ender Crystal", EntityType.ENDER_CRYSTAL, "an"));
        entities.add(new EntityInfo("Splash Potion", EntityType.SPLASH_POTION, "a"));
        entities.add(new EntityInfo("Egg", EntityType.EGG, "an"));
        entities.add(new EntityInfo("Fishing Hook", EntityType.FISHING_HOOK, "a"));
        entities.add(new EntityInfo("Lightning", EntityType.LIGHTNING, "a"));
        entities.add(new EntityInfo("Weather", EntityType.WEATHER, "a"));
        entities.add(new EntityInfo("Player", EntityType.PLAYER, "a"));
        entities.add(new EntityInfo("Complex Part", EntityType.COMPLEX_PART, "a"));
        entities.add(new EntityInfo("Unknown", EntityType.UNKNOWN, "an"));
    }

    public static EntityInfo getEntityByType(EntityType type) {
        for (EntityInfo entityInfo : entities) {
            if (entityInfo.type == type) {
                return entityInfo;
            }
        }
        return null;
    }

    public static class EntityInfo {
        public final EntityType type;
        public final String name, reference;

        public EntityInfo(String name, EntityType type, String reference) {
            this.type = type;
            this.name = name;
            this.reference = reference;
        }

        public EntityType getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        public String getReference() {
            return reference;
        }

        @Override
        public String toString() {
            Map<String, Object> map = new HashMap<>();
            map.put("type", "EntityInfo." + type.name());
            map.put("name", name);
            map.put("reference", reference);
            return map.toString();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            } else if (this == obj) {
                return true;
            } else if (!(obj instanceof EntityInfo)) {
                return false;
            } else {
                return ((EntityInfo) obj).type == type &&
                        ((EntityInfo) obj).name.equals(name) &&
                        ((EntityInfo) obj).reference.equals(reference);
            }
        }
    }
}
