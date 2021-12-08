package me.rey.core.utils;

import me.rey.core.events.customevents.block.CustomBlockPlaceEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class UtilBlock {

    private static HashSet<Byte> blockAirFoliageSet = new HashSet<>();
    private static HashSet<Byte> blockPassSet = new HashSet<>();

    @SuppressWarnings("deprecation")
    public static boolean replaceBlock(CustomBlockPlaceEvent.PlaceCause cause, Block old, Material replace, byte data) {
        CustomBlockPlaceEvent event = new CustomBlockPlaceEvent(cause, old, replace, data);
        Bukkit.getServer().getPluginManager().callEvent(event);

        if(!event.isCancelled()) {
            old.setType(replace);
            old.setData(data);
            return true;
        }

        return false;
    }

    public static Block getHighestClosestAir(Block start) {
        for (int i = start.getY(); i < 250; i++)
            if (UtilBlock.airFoliage(new Location(start.getWorld(), start.getX(), i, start.getZ()).getBlock()))
                return new Location(start.getWorld(), start.getX(), i, start.getZ()).getBlock();

        return new Location(start.getWorld(), start.getX(), 250, start.getZ()).getBlock();
    }

    public static Block getLowestBlockFrom(Block start) {
        for (int i = start.getY(); i > 0; i--)
            if (UtilBlock.solid(new Location(start.getWorld(), start.getX(), i, start.getZ()).getBlock()))
                return new Location(start.getWorld(), start.getX(), i, start.getZ()).getBlock();

        return new Location(start.getWorld(), start.getX(), 0, start.getZ()).getBlock();
    }

    private static List<Material> usable = new ArrayList<>();

    public static List<Material> usableBlocks(){
        if(usable.isEmpty()) {
            usable = Arrays.asList(
                    Material.ACACIA_DOOR,
                    Material.ACACIA_FENCE_GATE,
                    Material.ACACIA_FENCE,
                    Material.ANVIL,
                    Material.STANDING_BANNER,
                    Material.BANNER,
                    Material.WALL_BANNER,
                    Material.BEACON,
                    Material.BED,
                    Material.BIRCH_DOOR,
                    Material.BIRCH_FENCE_GATE,
                    Material.BIRCH_FENCE,
                    Material.BOAT,
                    Material.BREWING_STAND,
                    Material.COMMAND,
                    Material.CHEST,
                    Material.DARK_OAK_DOOR,
                    Material.DARK_OAK_FENCE,
                    Material.DARK_OAK_FENCE_GATE,
                    Material.DAYLIGHT_DETECTOR,
                    Material.DAYLIGHT_DETECTOR_INVERTED,
                    Material.DISPENSER,
                    Material.DROPPER,
                    Material.ENCHANTMENT_TABLE,
                    Material.ENDER_CHEST,
                    Material.FENCE_GATE,
                    Material.FENCE,
                    Material.FURNACE,
                    Material.HOPPER,
                    Material.HOPPER_MINECART,
                    Material.ITEM_FRAME,
                    Material.IRON_DOOR,
                    Material.IRON_DOOR_BLOCK,
                    Material.IRON_TRAPDOOR,
                    Material.JUNGLE_DOOR,
                    Material.JUNGLE_FENCE,
                    Material.JUNGLE_FENCE_GATE,
                    Material.LEVER,
                    Material.MINECART,
                    Material.NOTE_BLOCK,
                    Material.POWERED_MINECART,
                    Material.REDSTONE_COMPARATOR,
                    Material.REDSTONE_COMPARATOR_OFF,
                    Material.REDSTONE_COMPARATOR_ON,
                    Material.SIGN,
                    Material.SIGN_POST,
                    Material.SPRUCE_DOOR,
                    Material.SPRUCE_FENCE,
                    Material.SPRUCE_FENCE_GATE,
                    Material.STORAGE_MINECART,
                    Material.TRAP_DOOR,
                    Material.TRAPPED_CHEST,
                    Material.WALL_SIGN,
                    Material.WOOD_BUTTON,
                    Material.WOODEN_DOOR,
                    Material.WOOD_DOOR
            );
        }

        return usable;
    }

    @SuppressWarnings("deprecation")
    public static boolean solid(Block block) {
        if (block == null) return false;
        return solid(block.getTypeId());
    }

    public static boolean solid(int block) {
        return solid((byte)block);
    }

    public static boolean solid(byte block) {
        if (blockPassSet.isEmpty()) {
            blockPassSet.add(Byte.valueOf((byte)0));
            blockPassSet.add(Byte.valueOf((byte)6));
            blockPassSet.add(Byte.valueOf((byte)8));
            blockPassSet.add(Byte.valueOf((byte)9));
            blockPassSet.add(Byte.valueOf((byte)10));
            blockPassSet.add(Byte.valueOf((byte)11));
            blockPassSet.add(Byte.valueOf((byte)26));
            blockPassSet.add(Byte.valueOf((byte)27));
            blockPassSet.add(Byte.valueOf((byte)28));
            blockPassSet.add(Byte.valueOf((byte)30));
            blockPassSet.add(Byte.valueOf((byte)31));
            blockPassSet.add(Byte.valueOf((byte)32));
            blockPassSet.add(Byte.valueOf((byte)37));
            blockPassSet.add(Byte.valueOf((byte)38));
            blockPassSet.add(Byte.valueOf((byte)39));
            blockPassSet.add(Byte.valueOf((byte)40));
            blockPassSet.add(Byte.valueOf((byte)50));
            blockPassSet.add(Byte.valueOf((byte)51));
            blockPassSet.add(Byte.valueOf((byte)55));
            blockPassSet.add(Byte.valueOf((byte)59));
            blockPassSet.add(Byte.valueOf((byte)63));
            blockPassSet.add(Byte.valueOf((byte)64));
            blockPassSet.add(Byte.valueOf((byte)65));
            blockPassSet.add(Byte.valueOf((byte)66));
            blockPassSet.add(Byte.valueOf((byte)68));
            blockPassSet.add(Byte.valueOf((byte)69));
            blockPassSet.add(Byte.valueOf((byte)70));
            blockPassSet.add(Byte.valueOf((byte)71));
            blockPassSet.add(Byte.valueOf((byte)72));
            blockPassSet.add(Byte.valueOf((byte)75));
            blockPassSet.add(Byte.valueOf((byte)76));
            blockPassSet.add(Byte.valueOf((byte)77));
            blockPassSet.add(Byte.valueOf((byte)78));
            blockPassSet.add(Byte.valueOf((byte)83));
            blockPassSet.add(Byte.valueOf((byte)90));
            blockPassSet.add(Byte.valueOf((byte)92));
            blockPassSet.add(Byte.valueOf((byte)93));
            blockPassSet.add(Byte.valueOf((byte)94));
            blockPassSet.add(Byte.valueOf((byte)96));
            blockPassSet.add(Byte.valueOf((byte)101));
            blockPassSet.add(Byte.valueOf((byte)102));
            blockPassSet.add(Byte.valueOf((byte)104));
            blockPassSet.add(Byte.valueOf((byte)105));
            blockPassSet.add(Byte.valueOf((byte)106));
            blockPassSet.add(Byte.valueOf((byte)107));
            blockPassSet.add(Byte.valueOf((byte)111));
            blockPassSet.add(Byte.valueOf((byte)115));
            blockPassSet.add(Byte.valueOf((byte)116));
            blockPassSet.add(Byte.valueOf((byte)117));
            blockPassSet.add(Byte.valueOf((byte)118));
            blockPassSet.add(Byte.valueOf((byte)119));
            blockPassSet.add(Byte.valueOf((byte)120));
            blockPassSet.add(Byte.valueOf((byte)-85));
        }

        return !blockPassSet.contains(Byte.valueOf(block));
    }

    public static Block getTargetBlock(Player player, double range) {
        org.bukkit.Location loc = player.getEyeLocation();
        Vector dir = loc.getDirection().normalize();

        Block b = null;

        for (int i = 0; i <= range; i++) {

            b = loc.add(dir).getBlock();
        }
        return b;
    }

    public static Block getBlockUnderneath(Block b) {
        org.bukkit.Location loc = new org.bukkit.Location(b.getWorld(), b.getLocation().getX(),
                b.getLocation().getY() - 1.0, b.getLocation().getZ());
        Block bu = Bukkit.getWorld(b.getWorld().getName()).getBlockAt(loc);
        return bu;
    }

    public static Block getBlockAbove(Block b) {
        org.bukkit.Location loc = new org.bukkit.Location(b.getWorld(), b.getLocation().getX(),
                b.getLocation().getY() + 1.0, b.getLocation().getZ());
        Block ba = Bukkit.getWorld(b.getWorld().getName()).getBlockAt(loc);
        return ba;
    }

    public static BlockFace getClosestFace(float direction) {

        direction = direction % 360;

        if (direction < 0)
            direction += 360;

        direction = Math.round(direction / 90);

        switch ((int) direction) {

            case 0:
                return BlockFace.WEST;
            case 1:
                return BlockFace.NORTH;
            case 2:
                return BlockFace.EAST;
            case 3:
                return BlockFace.SOUTH;
            default:
                return BlockFace.WEST;

        }
    }

    @SuppressWarnings("deprecation")
    public static boolean airFoliage(Block block) {
        if (block == null) return false;
        return airFoliage(block.getTypeId());
    }

    public static boolean airFoliage(int block) {
        return airFoliage((byte)block);
    }

    public static boolean airFoliage(byte block) {
        if (blockAirFoliageSet.isEmpty())
        {
            blockAirFoliageSet.add(Byte.valueOf((byte)0));
            blockAirFoliageSet.add(Byte.valueOf((byte)6));
            blockAirFoliageSet.add(Byte.valueOf((byte)31));
            blockAirFoliageSet.add(Byte.valueOf((byte)32));
            blockAirFoliageSet.add(Byte.valueOf((byte)37));
            blockAirFoliageSet.add(Byte.valueOf((byte)38));
            blockAirFoliageSet.add(Byte.valueOf((byte)39));
            blockAirFoliageSet.add(Byte.valueOf((byte)40));
            blockAirFoliageSet.add(Byte.valueOf((byte)51));
            blockAirFoliageSet.add(Byte.valueOf((byte)59));
            blockAirFoliageSet.add(Byte.valueOf((byte)104));
            blockAirFoliageSet.add(Byte.valueOf((byte)105));
            blockAirFoliageSet.add(Byte.valueOf((byte)115));
            blockAirFoliageSet.add(Byte.valueOf((byte)-115));
            blockAirFoliageSet.add(Byte.valueOf((byte)-114));
            blockAirFoliageSet.add(Byte.valueOf((byte)425));
        }

        return blockAirFoliageSet.contains(Byte.valueOf(block));
    }

}
