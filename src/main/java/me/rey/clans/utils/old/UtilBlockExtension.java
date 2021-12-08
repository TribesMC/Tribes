package me.rey.clans.utils.old;

import me.rey.core.events.customevents.block.CustomBlockPlaceEvent;
import me.rey.core.utils.UtilMath;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.*;

public class UtilBlockExtension {

    private static HashSet<Byte> blockAirFoliageSet = new HashSet();
    private static HashSet<Byte> blockPassSet = new HashSet();

    public static Block getHighestClosestAir(Block start) {
        for(int i = start.getY(); i < 250; ++i) {
            if (airFoliage((new Location(start.getWorld(), (double)start.getX(), (double)i, (double)start.getZ())).getBlock())) {
                return (new Location(start.getWorld(), (double)start.getX(), (double)i, (double)start.getZ())).getBlock();
            }
        }

        return (new Location(start.getWorld(), (double)start.getX(), 250.0D, (double)start.getZ())).getBlock();
    }

    public static Block getLowestBlockFrom(Block start) {
        for(int i = start.getY(); i > 0; --i) {
            if (solid((new Location(start.getWorld(), (double)start.getX(), (double)i, (double)start.getZ())).getBlock())) {
                return (new Location(start.getWorld(), (double)start.getX(), (double)i, (double)start.getZ())).getBlock();
            }
        }

        return (new Location(start.getWorld(), (double)start.getX(), 0.0D, (double)start.getZ())).getBlock();
    }

    public static boolean solid(Block block) {
        return block != null && solid(block.getTypeId());
    }

    public static boolean solid(int block) {
        return solid((byte)block);
    }

    public static boolean solid(byte block) {
        if (blockPassSet.isEmpty()) {
            blockPassSet.add((byte)0);
            blockPassSet.add((byte)6);
            blockPassSet.add((byte)8);
            blockPassSet.add((byte)9);
            blockPassSet.add((byte)10);
            blockPassSet.add((byte)11);
            blockPassSet.add((byte)26);
            blockPassSet.add((byte)27);
            blockPassSet.add((byte)28);
            blockPassSet.add((byte)30);
            blockPassSet.add((byte)31);
            blockPassSet.add((byte)32);
            blockPassSet.add((byte)37);
            blockPassSet.add((byte)38);
            blockPassSet.add((byte)39);
            blockPassSet.add((byte)40);
            blockPassSet.add((byte)50);
            blockPassSet.add((byte)51);
            blockPassSet.add((byte)55);
            blockPassSet.add((byte)59);
            blockPassSet.add((byte)63);
            blockPassSet.add((byte)64);
            blockPassSet.add((byte)65);
            blockPassSet.add((byte)66);
            blockPassSet.add((byte)68);
            blockPassSet.add((byte)69);
            blockPassSet.add((byte)70);
            blockPassSet.add((byte)71);
            blockPassSet.add((byte)72);
            blockPassSet.add((byte)75);
            blockPassSet.add((byte)76);
            blockPassSet.add((byte)77);
            blockPassSet.add((byte)78);
            blockPassSet.add((byte)83);
            blockPassSet.add((byte)90);
            blockPassSet.add((byte)92);
            blockPassSet.add((byte)93);
            blockPassSet.add((byte)94);
            blockPassSet.add((byte)96);
            blockPassSet.add((byte)101);
            blockPassSet.add((byte)102);
            blockPassSet.add((byte)104);
            blockPassSet.add((byte)105);
            blockPassSet.add((byte)106);
            blockPassSet.add((byte)107);
            blockPassSet.add((byte)111);
            blockPassSet.add((byte)115);
            blockPassSet.add((byte)116);
            blockPassSet.add((byte)117);
            blockPassSet.add((byte)118);
            blockPassSet.add((byte)119);
            blockPassSet.add((byte)120);
            blockPassSet.add((byte)-85);
        }

        return !blockPassSet.contains(block);
    }

    public static boolean replaceBlock(CustomBlockPlaceEvent.PlaceCause cause, Block old, Material replace, byte data) {
        CustomBlockPlaceEvent event = new CustomBlockPlaceEvent(cause, old, replace, data);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            old.setType(replace);
            old.setData(data);
            return true;
        } else {
            return false;
        }
    }

    public static Block getTargetBlock(Player player, int range) {
        Location loc = player.getEyeLocation();
        Vector dir = loc.getDirection().normalize();
        Block b = null;

        for(int i = 0; i <= range; ++i) {
            b = loc.add(dir).getBlock();
        }

        return b;
    }

    public static boolean atBlockGap(Player p, Block block) {
        double yaw = (double)p.getLocation().getYaw();
        double angle = Math.toRadians(yaw);
        Location locAtX;
        Location locAtZ;
        if (angle >= 0.3D && angle <= 1.2707963267948965D || angle >= -5.983185307179586D && angle <= -5.0123889803846895D) {
            locAtX = block.getLocation();
            locAtX.setX(locAtX.getX() - 1.0D);
            locAtZ = block.getLocation();
            locAtZ.setZ(locAtZ.getZ() + 1.0D);
            if (locAtX.getBlock().getType().isSolid() && locAtZ.getBlock().getType().isSolid()) {
                return true;
            }
        }

        if (angle >= 1.8707963267948966D && angle <= 2.8415926535897933D || angle >= -4.41238898038469D && angle <= -3.441592653589793D) {
            locAtX = block.getLocation();
            locAtX.setX(locAtX.getX() - 1.0D);
            locAtZ = block.getLocation();
            locAtZ.setZ(locAtZ.getZ() - 1.0D);
            if (locAtX.getBlock().getType().isSolid() && locAtZ.getBlock().getType().isSolid()) {
                return true;
            }
        }

        if (angle >= 3.441592653589793D && angle <= 4.41238898038469D || angle >= -2.8415926535897933D && angle <= -1.8707963267948966D) {
            locAtX = block.getLocation();
            locAtX.setX(locAtX.getX() + 1.0D);
            locAtZ = block.getLocation();
            locAtZ.setZ(locAtZ.getZ() - 1.0D);
            if (locAtX.getBlock().getType().isSolid() && locAtZ.getBlock().getType().isSolid()) {
                return true;
            }
        }

        if (angle >= 5.0123889803846895D && angle <= 5.983185307179586D || angle >= -1.2707963267948965D && angle <= -0.3D) {
            locAtX = block.getLocation();
            locAtX.setX(locAtX.getX() + 1.0D);
            locAtZ = block.getLocation();
            locAtZ.setZ(locAtZ.getZ() + 1.0D);
            if (locAtX.getBlock().getType().isSolid() && locAtZ.getBlock().getType().isSolid()) {
                return true;
            }
        }

        return false;
    }

    public static Block getBlockUnderneath(Block b) {
        Location loc = new Location(b.getWorld(), b.getLocation().getX(), b.getLocation().getY() - 1.0D, b.getLocation().getZ());
        Block bu = Bukkit.getWorld(b.getWorld().getName()).getBlockAt(loc);
        return bu;
    }

    public static Block getBlockAbove(Block b) {
        Location loc = new Location(b.getWorld(), b.getLocation().getX(), b.getLocation().getY() + 1.0D, b.getLocation().getZ());
        Block ba = Bukkit.getWorld(b.getWorld().getName()).getBlockAt(loc);
        return ba;
    }

    public static BlockFace getClosestFace(float direction) {
        direction %= 360.0F;
        if (direction < 0.0F) {
            direction += 360.0F;
        }

        direction = (float)Math.round(direction / 90.0F);
        switch((int)direction) {
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

    public static double[] getXZCordsFromDegree(Player p, double radius, double degree) {
        double radian = Math.toRadians(degree);
        double xMultiplier = Math.cos(radian);
        double zMultiplier = Math.sin(radian);
        double addX = xMultiplier * radius;
        double addZ = zMultiplier * radius;
        double[] xzCords = new double[2];
        double x = p.getLocation().getX() + addX;
        double z = p.getLocation().getZ() + addZ;
        xzCords[0] = x;
        xzCords[1] = z;
        return xzCords;
    }

    public static double[] getXZCordsFromDegree(Location loc, double radius, double degree) {
        double radian = Math.toRadians(degree);
        double xMultiplier = Math.cos(radian);
        double zMultiplier = Math.sin(radian);
        double addX = xMultiplier * radius;
        double addZ = zMultiplier * radius;
        double[] xzCords = new double[2];
        double x = loc.getX() + addX;
        double z = loc.getZ() + addZ;
        xzCords[0] = x;
        xzCords[1] = z;
        return xzCords;
    }

    public static double[] getXZCordsMultipliersFromDegree(double degree) {
        double radian = Math.toRadians(degree);
        double xMultiplier = Math.cos(radian);
        double zMultiplier = Math.sin(radian);
        double[] xzCords = new double[]{xMultiplier, zMultiplier};
        return xzCords;
    }

    public static double[] getXZCordsFromDegree(Location loc, boolean rotated, double addAngle, double radius, double degree) {
        double radian = Math.toRadians(degree);
        double xMultiplier = Math.cos(radian);
        double zMultiplier = Math.sin(radian + addAngle);
        double addX = xMultiplier * radius;
        double addZ = zMultiplier * radius;
        double[] xzCords = new double[2];
        double x = loc.getX();
        double z = loc.getZ();
        if (rotated) {
            x += addX;
            z -= addZ;
        } else {
            x += addX;
            z += addZ;
        }

        xzCords[0] = x;
        xzCords[1] = z;
        return xzCords;
    }

    public static double getYCordsMultiplierByPitch(double pitchdegree) {
        return Math.sin(Math.toRadians(-pitchdegree));
    }

    public static Location getMidPoint(Location loc1, Location loc2) {
        Location midpoint = loc1.clone();
        double midx = (loc1.getX() + loc2.getX()) / 2.0D;
        double midy = (loc1.getY() + loc2.getY()) / 2.0D;
        double midz = (loc1.getZ() + loc2.getZ()) / 2.0D;
        midpoint.setX(midx);
        midpoint.setY(midy);
        midpoint.setZ(midz);
        return midpoint;
    }

    public static Location highestLocation(Location locat) {
        double blockY = (double)locat.getBlockY();
        Location tplocation = null;

        for(int i = (int)blockY; i > 1; --i) {
            Location loc = new Location(locat.getWorld(), locat.getX(), (double)i, locat.getZ(), locat.getYaw(), locat.getPitch());
            if (loc.getBlock().getType().isSolid()) {
                tplocation = new Location(locat.getWorld(), locat.getX(), (double)i + 1.5D, locat.getZ(), locat.getYaw(), locat.getPitch());
                break;
            }
        }

        return tplocation;
    }

    public static HashMap<Block, Double> getBlocksInRadius(Location loc, Double dR, double heightLimit) {
        HashMap<Block, Double> blockList = new HashMap();
        int iR = dR.intValue() + 1;

        for(int x = -iR; x <= iR; ++x) {
            for(int z = -iR; z <= iR; ++z) {
                for(int y = -iR; y <= iR; ++y) {
                    if ((double)Math.abs(y) <= heightLimit) {
                        Block curBlock = loc.getWorld().getBlockAt((int)(loc.getX() + (double)x), (int)(loc.getY() + (double)y), (int)(loc.getZ() + (double)z));
                        double offset = UtilMath.offset(loc, curBlock.getLocation().add(0.5D, 0.5D, 0.5D));
                        if (offset <= dR) {
                            blockList.put(curBlock, 1.0D - offset / dR);
                        }
                    }
                }
            }
        }

        return blockList;
    }

    public static HashMap<Block, Double> getBlocksInRadius(Location loc, Double dR) {
        return getBlocksInRadius(loc, dR, 999.0D);
    }

    public static Set<Entity> getEntitiesInCircle(Location loc, double radius) {
        Set<Entity> en = new HashSet<>();

        for (Entity e : loc.getWorld().getNearbyEntities(loc, radius, radius, radius)) {
            HashMap<Double, double[]> maxmincords = new HashMap<>();

            double degree;
            for (degree = 0.0D; degree <= 360.0D; ++degree) {
                maxmincords.put(degree, getXZCordsFromDegree(loc, radius, degree));
            }

            for (degree = 0.0D; degree <= 90.0D; ++degree) {
                double[] maxcords = maxmincords.get(degree);
                double[] mincords = maxmincords.get(180.0D + degree);
                double maxX = maxcords[0];
                double maxZ = maxcords[1];
                double minX = mincords[0];
                double minZ = mincords[1];
                if (e.getLocation().getX() <= maxX && e.getLocation().getZ() <= maxZ && e.getLocation().getX() >= minX && e.getLocation().getZ() >= minZ) {
                    en.add(e);
                }
            }
        }

        return en;
    }

    public static List<Location> circleLocations(Location loc, double radius) {
        List<Location> cl = new ArrayList<>();

        for(int degree = 0; degree <= 360; ++degree) {
            double x = getXZCordsFromDegree(loc, radius, degree)[0];
            double z = getXZCordsFromDegree(loc, radius, degree)[1];
            cl.add(new Location(loc.getWorld(), x, loc.getY(), z));
        }

        return cl;
    }

    public static List<Location> circleLocations(Location loc, double radius, int iteratecount) {
        List<Location> cl = new ArrayList<>();

        for(int degree = 0; degree <= 360; degree += iteratecount) {
            double x = getXZCordsFromDegree(loc, radius, degree)[0];
            double z = getXZCordsFromDegree(loc, radius, degree)[1];
            cl.add(new Location(loc.getWorld(), x, loc.getY(), z));
        }

        return cl;
    }

    public static List<Location> circleLocations(Location loc, double radius, int iteratecount, double y) {
        ArrayList<Location> cl = new ArrayList();

        for(int degree = 0; degree <= 360; degree += iteratecount) {
            double x = getXZCordsFromDegree(loc, radius, (double)degree)[0];
            double z = getXZCordsFromDegree(loc, radius, (double)degree)[1];
            cl.add(new Location(loc.getWorld(), x, y, z));
        }

        return cl;
    }

    public static List<Location> circleLocations(Location loc, double radius, double y) {
        List<Location> cl = new ArrayList<>();

        for(int degree = 0; degree <= 360; ++degree) {
            double x = getXZCordsFromDegree(loc, radius, (double)degree)[0];
            double z = getXZCordsFromDegree(loc, radius, (double)degree)[1];
            cl.add(new Location(loc.getWorld(), x, y, z));
        }

        return cl;
    }

    public static boolean airFoliage(Block block) {
        return block != null && airFoliage(block.getTypeId());
    }

    public static boolean airFoliage(int block) {
        return airFoliage((byte)block);
    }

    public static boolean airFoliage(byte block) {
        if (blockAirFoliageSet.isEmpty()) {
            blockAirFoliageSet.add((byte)0);
            blockAirFoliageSet.add((byte)6);
            blockAirFoliageSet.add((byte)31);
            blockAirFoliageSet.add((byte)32);
            blockAirFoliageSet.add((byte)37);
            blockAirFoliageSet.add((byte)38);
            blockAirFoliageSet.add((byte)39);
            blockAirFoliageSet.add((byte)40);
            blockAirFoliageSet.add((byte)51);
            blockAirFoliageSet.add((byte)59);
            blockAirFoliageSet.add((byte)104);
            blockAirFoliageSet.add((byte)105);
            blockAirFoliageSet.add((byte)115);
            blockAirFoliageSet.add((byte)-115);
            blockAirFoliageSet.add((byte)-114);
        }

        return blockAirFoliageSet.contains(block);
    }
}
