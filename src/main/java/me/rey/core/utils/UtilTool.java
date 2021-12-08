package me.rey.core.utils;

import org.apache.commons.lang3.Validate;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;

public class UtilTool {

    public static ItemStack setDamage(ItemStack stack, int damage) {
        try {
            Object craftStack = Objects.requireNonNull(UtilPacket.getClassNMS("ItemStack")).cast(Objects.requireNonNull(UtilPacket.getClassNMS("inventory.CraftItemStack", UtilPacket.NMSType.CRAFTBUKKIT)).getMethod("asNMSCopy", ItemStack.class).invoke(null, stack));
            Object nbtTagCompound = craftStack.getClass().getMethod("getTag").invoke(craftStack);
            if (nbtTagCompound == null) {
                nbtTagCompound = Objects.requireNonNull(UtilPacket.getClassNMS("NBTTagCompound")).getConstructor().newInstance();
                craftStack.getClass().getMethod("setTag", Objects.requireNonNull(UtilPacket.getClassNMS("NBTTagCompound"))).invoke(craftStack, nbtTagCompound);
                nbtTagCompound = craftStack.getClass().getMethod("getTag").invoke(craftStack);
            }

            Object modifiers = Objects.requireNonNull(UtilPacket.getClassNMS("NBTTagList")).getConstructor().newInstance();
            Object damageCompound = Objects.requireNonNull(UtilPacket.getClassNMS("NBTTagCompound")).getConstructor().newInstance();

            Object damageAttributeName = Objects.requireNonNull(UtilPacket.getClassNMS("NBTTagString")).getConstructor(String.class).newInstance("generic.attackDamage");
            damageCompound.getClass().getMethod("set", String.class, Objects.requireNonNull(UtilPacket.getClassNMS("NBTBase"))).invoke(damageCompound, "AttributeName", damageAttributeName);
            damageCompound.getClass().getMethod("set", String.class, Objects.requireNonNull(UtilPacket.getClassNMS("NBTBase"))).invoke(damageCompound, "Name", damageAttributeName);

            Object damageAmount = Objects.requireNonNull(UtilPacket.getClassNMS("NBTTagInt")).getConstructor(int.class).newInstance(damage);
            damageCompound.getClass().getMethod("set", String.class, Objects.requireNonNull(UtilPacket.getClassNMS("NBTBase"))).invoke(damageCompound, "Amount", damageAmount);

            Object damageOperation = Objects.requireNonNull(UtilPacket.getClassNMS("NBTTagInt")).getConstructor(int.class).newInstance(0);
            damageCompound.getClass().getMethod("set", String.class, Objects.requireNonNull(UtilPacket.getClassNMS("NBTBase"))).invoke(damageCompound, "Operation", damageOperation);

            Object damageUUIDLeast = Objects.requireNonNull(UtilPacket.getClassNMS("NBTTagInt")).getConstructor(int.class).newInstance(894654);
            damageCompound.getClass().getMethod("set", String.class, Objects.requireNonNull(UtilPacket.getClassNMS("NBTBase"))).invoke(damageCompound, "UUIDLeast", damageUUIDLeast);

            Object damageUUIDMost = Objects.requireNonNull(UtilPacket.getClassNMS("NBTTagInt")).getConstructor(int.class).newInstance(2872);
            damageCompound.getClass().getMethod("set", String.class, Objects.requireNonNull(UtilPacket.getClassNMS("NBTBase"))).invoke(damageCompound, "UUIDMost", damageUUIDMost);

            modifiers.getClass().getMethod("add", Objects.requireNonNull(UtilPacket.getClassNMS("NBTBase"))).invoke(modifiers, damageCompound);
            nbtTagCompound.getClass().getMethod("set", String.class, Objects.requireNonNull(UtilPacket.getClassNMS("NBTBase"))).invoke(nbtTagCompound, "AttributeModifiers", modifiers);

            craftStack.getClass().getMethod("setTag", Objects.requireNonNull(UtilPacket.getClassNMS("NBTTagCompound"))).invoke(craftStack, nbtTagCompound);
            return (ItemStack) Objects.requireNonNull(UtilPacket.getClassNMS("inventory.CraftItemStack", UtilPacket.NMSType.CRAFTBUKKIT)).getMethod("asBukkitCopy", Objects.requireNonNull(UtilPacket.getClassNMS("ItemStack"))).invoke(null, craftStack);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | InstantiationException e) {
            e.printStackTrace();
        }

        return stack;
    }

    public static ItemStack setNBTTag(ItemStack stack, String key, Object value, NBTTagType tagType) {
        Validate.notNull(stack, "stack cannot be null");
        Validate.notNull(key, "key cannot be null");
        Validate.isTrue(key.length() > 0, "key length must be over 0 characters");
        Validate.notNull(value, "value must not be null");
        Validate.notNull(tagType, "tagType must not be null");

        Class<?> craftItemStack = UtilPacket.getClassNMS("CraftItemStack", UtilPacket.NMSType.CRAFTBUKKIT, "inventory");
        if (craftItemStack == null) return null;
        try {
            Object nmsItemStack = craftItemStack.getMethod("asNMSCopy", ItemStack.class).invoke(null, stack);
            Object nbtCompound;
            boolean hasTag = (boolean) nmsItemStack.getClass().getMethod("hasTag").invoke(nmsItemStack);
            if (hasTag) {
                nbtCompound = nmsItemStack.getClass().getMethod("getTag").invoke(nmsItemStack);
            } else {
                nbtCompound = UtilPacket.getClassNMS("NBTTagCompound").getConstructor().newInstance();
            }

            switch (tagType) {
                case STRING:
                    nbtCompound.getClass()
                            .getMethod("set", String.class, UtilPacket.getClassNMS("NBTBase"))
                            .invoke(nbtCompound, key, UtilPacket.getClassNMS("NBTTagString")
                                    .getConstructor(String.class)
                                    .newInstance((String) value)
                            );
                    break;
                case INT:
                    nbtCompound.getClass()
                            .getMethod("set", String.class, UtilPacket.getClassNMS("NBTBase"))
                            .invoke(nbtCompound, key, UtilPacket.getClassNMS("NBTTagInt")
                                    .getConstructor(int.class)
                                    .newInstance((int) value)
                            );
                    break;
                case DOUBLE:
                    nbtCompound.getClass()
                            .getMethod("set", String.class, UtilPacket.getClassNMS("NBTBase"))
                            .invoke(nbtCompound, key, UtilPacket.getClassNMS("NBTTagDouble")
                                    .getConstructor(double.class)
                                    .newInstance((double) value)
                            );
                    break;
                case FLOAT:
                    nbtCompound.getClass()
                            .getMethod("set", String.class, UtilPacket.getClassNMS("NBTBase"))
                            .invoke(nbtCompound, key, UtilPacket.getClassNMS("NBTTagFloat")
                                    .getConstructor(float.class)
                                    .newInstance((float) value)
                            );
                    break;
                case LONG:
                    nbtCompound.getClass()
                            .getMethod("set", String.class, UtilPacket.getClassNMS("NBTBase"))
                            .invoke(nbtCompound, key, UtilPacket.getClassNMS("NBTTagLong")
                                    .getConstructor(long.class)
                                    .newInstance((long) value)
                            );
                    break;
                case SHORT:
                    nbtCompound.getClass()
                            .getMethod("set", String.class, UtilPacket.getClassNMS("NBTBase"))
                            .invoke(nbtCompound, key, UtilPacket.getClassNMS("NBTTagShort")
                                    .getConstructor(short.class)
                                    .newInstance((short) value)
                            );
                    break;
            }

            nmsItemStack.getClass().getMethod("setTag", UtilPacket.getClassNMS("NBTTagCompound")).invoke(nmsItemStack, nbtCompound);

            return (ItemStack) Objects.requireNonNull(UtilPacket.getClassNMS("CraftItemStack", UtilPacket.NMSType.CRAFTBUKKIT, "inventory")).getMethod("asBukkitCopy", UtilPacket.getClassNMS("ItemStack")).invoke(null, nmsItemStack);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | InstantiationException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object getNBTTag(ItemStack stack, String key, NBTTagType tagType) {
        Validate.notNull(stack, "stack cannot be null");
        Validate.notNull(key, "key cannot be null");
        Validate.isTrue(key.length() > 0, "key length must be over 0 characters");
        Validate.notNull(tagType, "tagType must not be null");

        Class<?> craftItemStack = UtilPacket.getClassNMS("CraftItemStack", UtilPacket.NMSType.CRAFTBUKKIT, "inventory");
        if (craftItemStack == null) return null;
        try {
            Object nmsItemStack = craftItemStack.getMethod("asNMSCopy", ItemStack.class).invoke(null, stack);
            Object nbtCompound;
            boolean hasTag = (boolean) nmsItemStack.getClass().getMethod("hasTag").invoke(nmsItemStack);
            if (hasTag) {
                nbtCompound = nmsItemStack.getClass().getMethod("getTag").invoke(nmsItemStack);
            } else {
                return null;
            }

            switch (tagType) {
                case STRING:
                    return nbtCompound.getClass()
                            .getMethod("getString", String.class)
                            .invoke(nbtCompound, key);
                case INT:
                    return nbtCompound.getClass()
                            .getMethod("getInt", String.class)
                            .invoke(nbtCompound, key);
                case DOUBLE:
                    return nbtCompound.getClass()
                            .getMethod("getDouble", String.class)
                            .invoke(nbtCompound, key);
                case FLOAT:
                    return nbtCompound.getClass()
                            .getMethod("getFloat", String.class)
                            .invoke(nbtCompound, key);
                case LONG:
                    return nbtCompound.getClass()
                            .getMethod("getLong", String.class)
                            .invoke(nbtCompound, key);
                case SHORT:
                    return nbtCompound.getClass()
                            .getMethod("getShort", String.class)
                            .invoke(nbtCompound, key);
            }
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    public enum NBTTagType {
        STRING,
        INT,
        INTARRAY,
        DOUBLE,
        FLOAT,
        BYTE,
        BYTEARRAY,
        LIST,
        LONG,
        SHORT
    }

    public static ItemStack getWaterBottle() {
        ItemStack stack = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) stack.getItemMeta();
        meta.clearCustomEffects();
        stack.setItemMeta(meta);
        return stack;
    }
}
