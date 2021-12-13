package me.rey.core.utils;

import org.bukkit.Material;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Skeleton;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class UtilEntity {

    public static List<EntityInfo> entities;
    public static Random random;

    static {
        random = new Random();
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
        entities.add(new EntityInfo("Creeper", EntityType.CREEPER, "a",
                (entity, max) -> {
                    List<ItemStack> stack = new ArrayList<>();
                    int amount = max ? 2 : random.nextInt(3);

                    stack.add(new ItemStack(Material.SULPHUR, amount));

                    return stack;
                }));
        entities.add(new EntityInfo("Skeleton", EntityType.SKELETON, "a",
                (entity, max) -> {
                    List<ItemStack> stack = new ArrayList<>();
                    int amount = max ? 2 : random.nextInt(3);

                    stack.add(new ItemStack(Material.BONE, amount));

                    if (entity instanceof Skeleton && ((Skeleton) entity).getSkeletonType() == Skeleton.SkeletonType.NORMAL) {
                        stack.add(new ItemStack(Material.ARROW));
                    }

                    return stack;
                }));
        entities.add(new EntityInfo("Spider", EntityType.SPIDER, "a",
                (entity, max) -> {
                    List<ItemStack> stack = new ArrayList<>();
                    int amount = max ? 2 : random.nextInt(3);

                    stack.add(new ItemStack(Material.STRING, amount));

//                    amount = random.nextInt(2);
//
//                    if (amount == 0) {
//                        stack.add(new ItemStack(Material.SPIDER_EYE));
//                    }

                    return stack;
                }));
        entities.add(new EntityInfo("Giant", EntityType.GIANT, "a"));
        entities.add(new EntityInfo("Zombie", EntityType.ZOMBIE, "a",
                (entity, max) -> {
                    List<ItemStack> stack = new ArrayList<>();
                    int amount = (max ? 1 : random.nextInt(2)) + 1;

                    stack.add(new ItemStack(Material.ROTTEN_FLESH, amount));

                    amount = random.nextInt(5);

                    if (amount == 0) {
                        switch (random.nextInt(3)) {
                            case 0:
                                stack.add(new ItemStack(Material.IRON_INGOT));
                                break;
                            case 1:
                                stack.add(new ItemStack(Material.CARROT));
                                break;
                            case 2:
                                stack.add(new ItemStack(Material.POTATO));
                                break;
                        }
                    }

                    return stack;
                }));
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
        entities.add(new EntityInfo("Pig", EntityType.PIG, "a",
                (entity, max) -> {
                    List<ItemStack> stack = new ArrayList<>();
                    int amount = (max ? 2 : random.nextInt(3)) + 1;

                    if (entity != null && entity.getFireTicks() > 0) {
                        stack.add(new ItemStack(Material.GRILLED_PORK, amount));
                    } else {
                        stack.add(new ItemStack(Material.PORK, amount));
                    }

//                    if (entity instanceof Pig && ((Pig) entity).hasSaddle()) {
//                        stack.add(new ItemStack(Material.SADDLE, 1));
//                    }
                    return stack;
                }));
        entities.add(new EntityInfo("Sheep", EntityType.SHEEP, "a",
                (entity, max) -> {
                    List<ItemStack> stack = new ArrayList<>();
//                    if (entity instanceof Sheep && !((Sheep) entity).isSheared()) {
//                        stack.add(new ItemStack(Material.WOOL));
//                    }

                    int amount = (max ? 1 : random.nextInt(2)) + 1;

                    if (entity != null && entity.getFireTicks() > 0) {
                        stack.add(new ItemStack(Material.COOKED_MUTTON, amount));
                    } else {
                        stack.add(new ItemStack(Material.MUTTON, amount));
                    }
                    return stack;
                }));
        entities.add(new EntityInfo("Cow", EntityType.COW, "a",
                (entity, max) -> {
                    List<ItemStack> stack = new ArrayList<>();
                    int amount = max ? 2 : random.nextInt(3);

                    stack.add(new ItemStack(Material.LEATHER, amount));

                    amount = (max ? 2 : random.nextInt(3)) + 1;

                    if (entity != null && entity.getFireTicks() > 0) {
                        stack.add(new ItemStack(Material.COOKED_BEEF, amount));
                    } else {
                        stack.add(new ItemStack(Material.RAW_BEEF, amount));
                    }
                    return stack;
                }));
        entities.add(new EntityInfo("Chicken", EntityType.CHICKEN, "a",
                (entity, max) -> {
                    List<ItemStack> stack = new ArrayList<>();
                    int amount = max ? 2 : random.nextInt(3);

                    stack.add(new ItemStack(Material.FEATHER, amount));

                    if (entity != null && entity.getFireTicks() > 0) {
                        stack.add(new ItemStack(Material.COOKED_CHICKEN, 1));
                    } else {
                        stack.add(new ItemStack(Material.RAW_CHICKEN, 1));
                    }
                    return stack;
                }));
        entities.add(new EntityInfo("Squid", EntityType.SQUID, "a"));
        entities.add(new EntityInfo("Wolf", EntityType.WOLF, "a"));
        entities.add(new EntityInfo("Mushroom Cow", EntityType.MUSHROOM_COW, "a"));
        entities.add(new EntityInfo("Snowman", EntityType.SNOWMAN, "a"));
        entities.add(new EntityInfo("Ocelot", EntityType.OCELOT, "an"));
        entities.add(new EntityInfo("Iron Golem", EntityType.IRON_GOLEM, "an"));
        entities.add(new EntityInfo("Horse", EntityType.HORSE, "a"));
        entities.add(new EntityInfo("Rabbit", EntityType.RABBIT, "a",
                (entity, max) -> {
                    List<ItemStack> stack = new ArrayList<>();
                    int amount = max ? 1 : random.nextInt(2);

                    stack.add(new ItemStack(Material.RABBIT_HIDE, amount));

                    amount = max ? 1 : random.nextInt(2);

                    if (entity != null && entity.getFireTicks() > 0) {
                        stack.add(new ItemStack(Material.COOKED_RABBIT, amount));
                    } else {
                        stack.add(new ItemStack(Material.RABBIT, amount));
                    }
                    return stack;
                }));
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
        public final DropsCalculator calculator;

        public EntityInfo(String name, EntityType type, String reference, DropsCalculator calculator) {
            this.type = type;
            this.name = name;
            this.reference = reference;
            this.calculator = calculator;
        }

        public EntityInfo(String name, EntityType type, String reference) {
            this.type = type;
            this.name = name;
            this.reference = reference;
            this.calculator = null;
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

        public List<ItemStack> calculateDrops(Entity entity, boolean giveMax) {
            if (calculator == null) return new ArrayList<>();
            return calculator.calc(entity, giveMax);
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

    public static int getNewEntityId(boolean modify) {
        try {
            Field field = UtilPacket.getClassNMS("Entity").getDeclaredField("entityCount");
            field.setAccessible(true);
            int id = field.getInt(null);
            if (modify) {
                field.set(null, id + 1);
            }
            return id;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static void anchor(Entity entity) {
        try {
            Object handle = entity.getClass().getMethod("getHandle").invoke(entity);
            Constructor<?> nbtCompound = Objects.requireNonNull(UtilPacket.getClassNMS("NBTTagCompound")).getConstructor();
            Object nbtTagCompound = nbtCompound.newInstance();
            handle.getClass().getMethod("c", nbtTagCompound.getClass()).invoke(handle, nbtTagCompound);

            nbtTagCompound.getClass().getMethod("setByte", String.class, byte.class).invoke(nbtTagCompound, "NoAI", (byte) 1);
            handle.getClass().getMethod("f", nbtTagCompound.getClass()).invoke(handle, nbtTagCompound);
        } catch (final IllegalAccessException | InvocationTargetException | NoSuchMethodException | InstantiationException e) {
            e.printStackTrace();
        }
    }

    public static void stupidify(Entity entity, boolean mute) {
        Field goalSelector;
        Field targetSelector;
        Field bsRestrictionGoal;
        Field pathfinderBList;
        Field pathfinderCList;
        try {
            Object handle = entity.getClass().getMethod("getHandle").invoke(entity);

            goalSelector = UtilPacket.getClassNMS("EntityInsentient").getDeclaredField("goalSelector");
            goalSelector.setAccessible(true);

            targetSelector = UtilPacket.getClassNMS("EntityInsentient").getDeclaredField("targetSelector");
            targetSelector.setAccessible(true);


            pathfinderBList = UtilPacket.getClassNMS("PathfinderGoalSelector").getDeclaredField("b");
            pathfinderBList.setAccessible(true);

            pathfinderCList = UtilPacket.getClassNMS("PathfinderGoalSelector").getDeclaredField("c");
            pathfinderCList.setAccessible(true);

            if (entity instanceof Creature) {
                bsRestrictionGoal = UtilPacket.getClassNMS("EntityCreature").getDeclaredField("c");
                bsRestrictionGoal.setAccessible(true);
                bsRestrictionGoal.set(handle, UtilPacket.getClassNMS("PathfinderGoalMoveTowardsRestriction").getConstructor(UtilPacket.getClassNMS("EntityCreature"), double.class).newInstance(handle, 0D));
            }

            if (UtilPacket.getClassNMS("Entity").isAssignableFrom(UtilPacket.getClassNMS("EntityInsentient"))) {
                Object creature = UtilPacket.getClassNMS("EntityInsentient").cast(handle);

                if (mute) {
                    handle.getClass().getMethod("b", boolean.class).invoke(handle, true);
                }

                Object gsb = goalSelector.get(creature);
                Object gsc = goalSelector.get(creature);
                Object tsb = targetSelector.get(creature);
                Object tsc = targetSelector.get(creature);

                ((List<?>) pathfinderBList.get(gsb)).clear();
                ((List<?>) pathfinderCList.get(gsc)).clear();
                ((List<?>) pathfinderBList.get(tsb)).clear();
                ((List<?>) pathfinderCList.get(tsc)).clear();
            }

            if (handle.getClass().isAssignableFrom(UtilPacket.getClassNMS("EntityBat"))) {
                UtilPacket.getClassNMS("EntityBat").cast(handle).getClass().getMethod("setVegetated", boolean.class).invoke(UtilPacket.getClassNMS("EntityBat").cast(handle), true);
            }

            if (handle.getClass().isAssignableFrom(UtilPacket.getClassNMS("EntityEnderDragon"))) {
                UtilPacket.getClassNMS("EntityEnderDragon").cast(handle).getClass().getMethod("setVegetated", boolean.class).invoke(UtilPacket.getClassNMS("EntityEnderDragon").cast(handle), true);
            }
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException | NoSuchMethodException | InvocationTargetException | InstantiationException e) {
            e.printStackTrace();
        }
    }

    private interface DropsCalculator {
        List<ItemStack> calc(Entity entity, boolean giveMax);
    }
}
