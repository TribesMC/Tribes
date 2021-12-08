package me.rey.core.utils;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class UtilMath {

	public static double trim(int degree, double d) {
		String format = "#.#";

		for (int i = 1; i < degree; i++) {
			format = format + "#";
		}
		DecimalFormat twoDForm = new DecimalFormat(format);
		return Double.valueOf(twoDForm.format(d)).doubleValue();
	}

	public static Random random = new Random();

	public static int r(int i) {
		return random.nextInt(i);
	}

	public static double randBetween(double min, double max) {
		return (int) ((Math.random() * (max - min)) + min);
	}

	public static int randBetween(int min, int max) {
		return (int) ((Math.random() * (max - min)) + min);
	}

	public static double offset2d(Entity a, Entity b) {
		return offset2d(a.getLocation().toVector(), b.getLocation().toVector());
	}

	public static double offset2d(Location a, Location b) {
		return offset2d(a.toVector(), b.toVector());
	}

	public static double offset2d(Vector a, Vector b) {
		a.setY(0);
		b.setY(0);
		return a.subtract(b).length();
	}

	public static double offset(Entity a, Entity b) {
		return offset(a.getLocation().toVector(), b.getLocation().toVector());
	}

	public static double offset(Location a, Location b) {
		return offset(a.toVector(), b.toVector());
	}

	public static double offset(Vector a, Vector b) {
		return a.subtract(b).length();
	}

	public static double trimToDecimalPlace(double number, int decimalPlaces) {
		if (decimalPlaces <= 0) {
			return (int) number;
		}

		StringBuilder decimalFormat = new StringBuilder("#.#");

		for (int i = 0; i < (decimalPlaces - 1); i++) {
			decimalFormat.append("#");
		}

		return Double.parseDouble(new DecimalFormat(decimalFormat.toString()).format(number));
	}

	public static Integer parseInt(String string, boolean removeAlphabets) {
		string = string.replaceAll(",", "");
		double start;
		double multiplier;

		if (string.toLowerCase().endsWith("k")) {
			multiplier = 1000;
			string = string.substring(0, string.length() - 1);
		} else if (string.toLowerCase().endsWith("m")) {
			multiplier = 1000000;
			string = string.substring(0, string.length() - 1);
		} else if (string.toLowerCase().endsWith("b")) {
			multiplier = 1000000000;
			string = string.substring(0, string.length() - 1);
		} else {
			multiplier = 1;
		}

		String charless = removeAlphabets ? removeAlphabets(string, '.') : string;
		try {
			start = Double.parseDouble(charless);
		} catch (NumberFormatException e) {
			return null;
		}
		start = start * multiplier;
		return (int) start;
	}

	public static Long parseLong(String string, boolean removeAlphabets) {
		string = string.replaceAll(",", "");
		double start;
		double multiplier;

		if (string.toLowerCase().endsWith("k")) {
			multiplier = 1000;
			string = string.substring(0, string.length() - 1);
		} else if (string.toLowerCase().endsWith("m")) {
			multiplier = 1000000;
			string = string.substring(0, string.length() - 1);
		} else if (string.toLowerCase().endsWith("b")) {
			multiplier = 1000000000;
			string = string.substring(0, string.length() - 1);
		} else {
			multiplier = 1;
		}

		String charless = removeAlphabets ? removeAlphabets(string, '.') : string;
		try {
			start = Double.parseDouble(charless);
		} catch (NumberFormatException e) {
			return null;
		}
		start = start * multiplier;
		return (long) start;
	}

	public static String removeAlphabets(String string, Character... exceptions) {

		for (int i = 0; i < string.length(); i++) {

			if (string.charAt(i) >= 'A' && string.charAt(i) <= 'Z' ||
					string.charAt(i) >= 'a' && string.charAt(i) <= 'z') {
				if (!Arrays.asList(exceptions).contains(string.charAt(i))) {
					string = string.substring(0, i) + string.substring(i + 1);
					i--;
				}
			}
		}
		return string;
	}

	public static List<Location> getLocationsBetween(Location start, Location end) {
		List<Location> locations = new ArrayList<>();
		int topBlockX = Math.max(start.getBlockX(), end.getBlockX());
		int bottomBlockX = Math.min(start.getBlockX(), end.getBlockX());

		int topBlockY = Math.max(start.getBlockY(), end.getBlockY());
		int bottomBlockY = Math.min(start.getBlockY(), end.getBlockY());

		int topBlockZ = Math.max(start.getBlockZ(), end.getBlockZ());
		int bottomBlockZ = Math.min(start.getBlockZ(), end.getBlockZ());

		for (int x = bottomBlockX; x <= topBlockX; x++) {
			for (int z = bottomBlockZ; z <= topBlockZ; z++) {
				for (int y = bottomBlockY; y <= topBlockY; y++) {
					locations.add(start.getWorld().getBlockAt(x, y, z).getLocation()); // return location here
				}
			}
		}
		return locations;
	}

	public static Location getLocationInCircumference(Location center, double radius, double angleInRadians) {
		double x = center.getX() + radius * Math.cos(angleInRadians);
		double z = center.getZ() + radius * Math.sin(angleInRadians);
		double y = center.getY();
		Location location = new Location(center.getWorld(), x, y, z);
		Vector difference = center.toVector().clone().subtract(location.toVector());
		location.setDirection(difference);
		return location;
	}
}
