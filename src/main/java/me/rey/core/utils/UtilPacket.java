package me.rey.core.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class UtilPacket {

    private static Map<String, Object> enumCache;

    public enum NMSType {
        CRAFTBUKKIT,
        MINECRAFT
    }

    public static void sendTitle(Player player, String jsonTitleString, String jsonSubtitleString, int fadeInTime, int showTime, int fadeOutTime) {
        try {
            if(jsonTitleString != null) {
                Object titleEnum = Objects.requireNonNull(getClassNMS("PacketPlayOutTitle")).getDeclaredClasses()[0].getField("TITLE").get(null);
                Object chatSerializer = Objects.requireNonNull(getClassNMS("IChatBaseComponent")).getDeclaredClasses()[0].getMethod("a", String.class).invoke(null, jsonTitleString);
                Constructor<?> titleConstructor = Objects.requireNonNull(getClassNMS("PacketPlayOutTitle")).getConstructor(Objects.requireNonNull(getClassNMS("PacketPlayOutTitle")).getDeclaredClasses()[0], getClassNMS("IChatBaseComponent"), int.class, int.class, int.class);
                Object packet = titleConstructor.newInstance(titleEnum, chatSerializer, fadeInTime, showTime, fadeOutTime);
                sendPacket(player, packet);
            }

            if(jsonSubtitleString != null) {
                Object subtitleEnum = Objects.requireNonNull(getClassNMS("PacketPlayOutTitle")).getDeclaredClasses()[0].getField("SUBTITLE").get(null);
                Object ChatSerializer = Objects.requireNonNull(getClassNMS("IChatBaseComponent")).getDeclaredClasses()[0].getMethod("a", String.class).invoke(null, jsonSubtitleString);
                Constructor<?> subtitleConstructor = Objects.requireNonNull(getClassNMS("PacketPlayOutTitle")).getConstructor(Objects.requireNonNull(getClassNMS("PacketPlayOutTitle")).getDeclaredClasses()[0], getClassNMS("IChatBaseComponent"), int.class, int.class, int.class);
                Object packet = subtitleConstructor.newInstance(subtitleEnum, ChatSerializer, fadeInTime, showTime, fadeOutTime);
                sendPacket(player, packet);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendActionBarMessage(Player player, String text) {
        try {
            Object chatSerializer = Objects.requireNonNull(getClassNMS("IChatBaseComponent")).getDeclaredClasses()[0].getMethod("a", String.class).invoke(null, "{\"text\":\" " + text + " " + "\"}");
            Constructor<?> packetConstructor = Objects.requireNonNull(getClassNMS("PacketPlayOutChat")).getConstructor(Objects.requireNonNull(getClassNMS("IChatBaseComponent")), Byte.TYPE);
            Object packet = packetConstructor.newInstance(chatSerializer, (byte) 2);
            sendPacket(player, packet);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
    }

    public static void displayProgress(String prefix, double amount, String suffix, boolean progressDirectionSwap, Player... players) {
        if (progressDirectionSwap) {
            amount = 1.0 - amount;
        }
        StringBuilder progressBar = new StringBuilder(String.valueOf(ChatColor.GREEN));
        boolean colorChange = false;
        for (int i = 0; i < 24; ++i) {
            if (!colorChange && i / 24.0f >= amount) {
                progressBar.append(ChatColor.RED);
                colorChange = true;
            }
            progressBar.append("\u258c");
        }
        for (Player player : players) {
            sendActionBarMessage(player, ((prefix == null) ? "" : prefix + ChatColor.RESET + " ") + progressBar + ((suffix == null) ? "" : (suffix + ChatColor.RESET + " ")));
        }
    }

    public static void sendPacket(Player player, Object packet) {
        try {
            Object handle = player.getClass().getMethod("getHandle").invoke(player);
            Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
            playerConnection.getClass().getMethod("sendPacket", getClassNMS("Packet")).invoke(playerConnection, packet);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public static Object getEnumConstant(Class<?> clss, String constant) {
        if (enumCache.containsKey(clss.getName() + "." + constant)) {
            return enumCache.get(clss.getName() + "." + constant);
        }

        List<Object> enumConstants;
        for (int length = (enumConstants = Collections.singletonList(clss.getEnumConstants())).size(), i = 0; i < length; ++i) {
            Object constantChild = enumConstants.get(i);
            if (constantChild != null) {
                if (constantChild.toString().equalsIgnoreCase(constant)) {
                    enumCache.put(clss.getName() + "." + constant, constantChild);
                    return constantChild;
                }
            }
        }
        return null;
    }

    public static Class<?> getClassNMS(String methodName) {
        return getClassNMS(methodName, NMSType.MINECRAFT);
    }

    public static Class<?> getClassNMS(String methodName, NMSType type, String... extraArgs) {
        StringBuilder extra = new StringBuilder();
        for (String arg : extraArgs) {
            extra.append(arg).append(".");
        }
        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        switch (type) {
            case CRAFTBUKKIT:
                try {
                    return Class.forName("org.bukkit.craftbukkit." + version + "." + extra + methodName);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            case MINECRAFT:
                try {
                    return Class.forName("net.minecraft.server." + version + "." + extra + methodName);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
        }
        return null;
    }

    public static Object getField(String name, Object instance) {
        try {
            Field field = instance.getClass().getDeclaredField(name);
            field.setAccessible(true);
            return field.get(instance);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void setField(Field field, Object instance, Object value) {
        try {
            field.setAccessible(true);
            field.set(instance, value);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static void getAndSetField(String fieldName, Object instance, Object value) {
        try {
            Field field = instance.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(instance, value);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    static {
        enumCache = new HashMap<>();
    }
}
