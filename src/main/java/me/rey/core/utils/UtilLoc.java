package me.rey.core.utils;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class UtilLoc {


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

        double[] xzCords = new double[2];

        xzCords[0] = xMultiplier;
        xzCords[1] = zMultiplier;

        return xzCords;
    }

    public static double[] getXZCordsFromDegree(Location loc, boolean rotated, double addAngle, double radius,
                                                double degree) {
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

    public static boolean atBlockGap(Player p, Block block) {

        /*
         * Sketch https://imgur.com/a/H1BUrBQ
         */

        double yaw = p.getLocation().getYaw();
        double angle = Math.toRadians(yaw);

        /* South - West */
        if (angle >= 0 + 0.3 && angle <= Math.PI / 2 - 0.3
                || angle >= -2 * Math.PI + 0.3 && angle <= -3 * (Math.PI / 2) - 0.3) {

            Location locAtX = block.getLocation();
            locAtX.setX(locAtX.getX() - 1);

            Location locAtZ = block.getLocation();
            locAtZ.setZ(locAtZ.getZ() + 1);

            if (locAtX.getBlock().getType().isSolid() && locAtZ.getBlock().getType().isSolid()) {
                return true;
            }

        }

        /* North - West */
        if (angle >= Math.PI / 2 + 0.3 && angle <= Math.PI - 0.3
                || angle >= -3 * (Math.PI / 2) + 0.3 && angle <= -Math.PI - 0.3) {

            Location locAtX = block.getLocation();
            locAtX.setX(locAtX.getX() - 1);

            Location locAtZ = block.getLocation();
            locAtZ.setZ(locAtZ.getZ() - 1);

            if (locAtX.getBlock().getType().isSolid() && locAtZ.getBlock().getType().isSolid()) {
                return true;
            }

        }

        /* North - East */
        if (angle >= Math.PI + 0.3 && angle <= 3 * (Math.PI / 2) - 0.3
                || angle >= -Math.PI + 0.3 && angle <= -1 * (Math.PI / 2) - 0.3) {

            Location locAtX = block.getLocation();
            locAtX.setX(locAtX.getX() + 1);

            Location locAtZ = block.getLocation();
            locAtZ.setZ(locAtZ.getZ() - 1);

            if (locAtX.getBlock().getType().isSolid() && locAtZ.getBlock().getType().isSolid()) {
                return true;
            }

        }

        /* South - East */
        if (angle >= 3 * (Math.PI / 2) + 0.3 && angle <= 2 * Math.PI - 0.3
                || angle >= -1 * (Math.PI / 2) + 0.3 && angle <= 0 - 0.3) {

            Location locAtX = block.getLocation();
            locAtX.setX(locAtX.getX() + 1);

            Location locAtZ = block.getLocation();
            locAtZ.setZ(locAtZ.getZ() + 1);

            if (locAtX.getBlock().getType().isSolid() && locAtZ.getBlock().getType().isSolid()) {
                return true;
            }

        }

        return false;

    }

    public static Location getMidPoint(Location loc1, Location loc2) {
        Location midpoint = loc1.clone();

        double midx = (loc1.getX() + loc2.getX()) / 2;
        double midy = (loc1.getY() + loc2.getY()) / 2;
        double midz = (loc1.getZ() + loc2.getZ()) / 2;

        midpoint.setX(midx);
        midpoint.setY(midy);
        midpoint.setZ(midz);

        return midpoint;
    }

    public static Location highestLocation(Location locat) {
        double blockY = locat.getBlockY();

        Location tplocation = null;
        for (int i = (int) blockY; i > 1; i--) {
            Location loc = new Location(locat.getWorld(), locat.getX(), i, locat.getZ(), locat.getYaw(),
                    locat.getPitch());

            if (loc.getBlock().getType().isSolid()) {
                tplocation = new Location(locat.getWorld(), locat.getX(), i + 1.5, locat.getZ(), locat.getYaw(),
                        locat.getPitch());
                break;
            }
        }

        return tplocation;
    }

    public static HashMap<Block, Double> getBlocksInRadius(Location loc, Double dR, double heightLimit) {
        HashMap<Block, Double> blockList = new HashMap<>();
        int iR = dR.intValue() + 1;

        for (int x = -iR; x <= iR; x++) {
            for (int z = -iR; z <= iR; z++)
                for (int y = -iR; y <= iR; y++) {
                    if (Math.abs(y) <= heightLimit) {

                        Block curBlock = loc.getWorld().getBlockAt((int) (loc.getX() + x), (int) (loc.getY() + y),
                                (int) (loc.getZ() + z));

                        double offset = UtilMath.offset(loc, curBlock.getLocation().add(0.5D, 0.5D, 0.5D));

                        if (offset <= dR.doubleValue())
                            blockList.put(curBlock, Double.valueOf(1.0D - offset / dR.doubleValue()));
                    }
                }
        }
        return blockList;
    }

    public static HashMap<Block, Double> getBlocksInRadius(Location loc, Double dR) {
        return getBlocksInRadius(loc, dR, 999.0D);
    }

    public static HashMap<Entity, Double> getEntityMapInCircle(Location loc, double radius) {

        HashMap<Entity, Double> en = new HashMap<>();

        for(Entity e : loc.getWorld().getNearbyEntities(loc, radius, radius, radius)) {
            HashMap<Double, double[]> maxmincords = new HashMap<Double, double[]>();

            for (double degree = 0; degree <= 360; degree++) {
                maxmincords.put(degree, getXZCordsFromDegree(loc, radius, degree));
            }

            for (double degree = 0; degree <= 90; degree++) {

                double[] maxcords = maxmincords.get(degree);
                double[] mincords = maxmincords.get(180 + degree);

                double maxX = maxcords[0];
                double maxZ = maxcords[1];

                double minX = mincords[0];
                double minZ = mincords[1];

                if (e.getLocation().getX() <= maxX && e.getLocation().getZ() <= maxZ && e.getLocation().getX() >= minX && e.getLocation().getZ() >= minZ) {
                    en.put(e, e.getLocation().distance(loc));
                } else {
                    continue;
                }
            }
        }

        return en;
    }

    public static Set<Entity> getEntitiesInCircle(Location loc, double radius) {
        return getEntityMapInCircle(loc, radius).keySet();
    }

    public static ArrayList<Location> circleLocations(Location loc, double radius) {
        ArrayList<Location> cl = new ArrayList<Location>();

        for(int degree=0; degree<=360; degree++) {

            double x = getXZCordsFromDegree(loc, radius, degree)[0];
            double z = getXZCordsFromDegree(loc, radius, degree)[1];

            cl.add(new Location(loc.getWorld(), x, loc.getY(), z));
        }

        return cl;
    }

    public static ArrayList<Location> circleLocations(Location loc, double radius, int iteratecount) {
        ArrayList<Location> cl = new ArrayList<Location>();

        for(int degree=0; degree<=360; degree += iteratecount) {

            double x = getXZCordsFromDegree(loc, radius, degree)[0];
            double z = getXZCordsFromDegree(loc, radius, degree)[1];

            cl.add(new Location(loc.getWorld(), x, loc.getY(), z));
        }

        return cl;
    }

    public static ArrayList<Location> circleLocations(Location loc, double radius, int iteratecount, double y) {
        ArrayList<Location> cl = new ArrayList<Location>();

        for(int degree=0; degree<=360; degree += iteratecount) {

            double x = getXZCordsFromDegree(loc, radius, degree)[0];
            double z = getXZCordsFromDegree(loc, radius, degree)[1];

            cl.add(new Location(loc.getWorld(), x, y, z));
        }

        return cl;
    }

    public static ArrayList<Location> circleLocations(Location loc, double radius, double y) {
        ArrayList<Location> cl = new ArrayList<Location>();

        for(int degree=0; degree<=360; degree++) {

            double x = getXZCordsFromDegree(loc, radius, degree)[0];
            double z = getXZCordsFromDegree(loc, radius, degree)[1];

            cl.add(new Location(loc.getWorld(), x, y, z));
        }

        return cl;
    }

}
