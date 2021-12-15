package me.rey.tribes;

import me.rey.clans.utils.UtilTime;
import me.rey.core.utils.UtilPacket;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

public class methods {

    public static boolean sameOrientation(int xStart1, int yStart1, int xEnd1, int yEnd1, int xStart2, int yStart2, int xEnd2, int yEnd2) {
        /*
         * Get slope using the equation
         * (y2-y1)/(x2-x1) and compare
         */

        // Cast to double because you don't want to cast to int
        // in a floating point context
        double slope1 = (double) (yStart1-yEnd1) / (xStart1-xEnd1);
        double slope2 = (double) (yStart2-yEnd2) / (xStart2-xEnd2);
        return slope1 == slope2;
    }

    public static String formattedRegistration(String plate) {
        if (plate.length() < 2) throw new IllegalArgumentException("plate must be 2 or more characters");

        // First
        if (plate.matches("[A-Z]{2}[0-9]{2}[A-Z]")) {
            // AA00A -> AA00 A
            return plate.substring(0, 4) + " " + plate.charAt(4);
        }
        if (plate.matches("[A-Z]{2}[0-9]{2} [A-Z]")) {
            // is already in the form AA00 A
            return plate;
        }

        // Second
        if (plate.matches("[A-Z][0-9]{1,3}[A-Z]{3}")) {
            // A0{1,3}AAA -> A0{1,3} AAA
            String[] split = plate.split("(?<=[0-9]{1,3})");
            StringBuilder string = new StringBuilder();
            for (int i = 0; i < split.length - 1; i++) {
                string.append(split[i]);
            }
            string.append(" ").append(split[split.length - 1]);
            return string.toString();
        }
        if (plate.matches("[A-Z][0-9]{1,3} [A-Z]{3}")) {
            // is already in the form A0{1,3} AAA
            return plate;
        }

        // Third
        if (plate.matches("[A-Z]{3}[0-9]{1,3}[A-Z]")) {
            // AAA0{1-3}A -> AAA 0{1-3}A
            return plate.substring(0, 3) + " " + plate.substring(3);
        }
        if (plate.matches("[A-Z]{3} [0-9]{1,3}[A-Z]")) {
            // is already in the form AAA 0{1,3}A
            return plate;
        }

        // Fourth
        if (plate.matches("[A-Z]{1,3}[0-9]{1,3}")) {
            // A{1-3}0{1-3} -> A{1-3} 0{1-3}
            int changesAt = -1;
            for (int i = 0; i < plate.toCharArray().length; i++) {
                if (Character.isDigit(plate.toCharArray()[i])) {
                    changesAt = i;
                    break;
                }
            }
            return plate.substring(0, changesAt) + " " + plate.substring(changesAt);
        }
        if (plate.matches("[A-Z]{1,3} [0-9]{1,3}")) {
            // is already in the form A{1-3} 0{1-3}
            return plate;
        }
        if (plate.matches("[0-9]{1,3}[A-Z]{1,3}")) {
            // 0{1-3}A{1-3} -> 0{1-3} A{1-3}
            int changesAt = -1;
            for (int i = 0; i < plate.toCharArray().length; i++) {
                if (Character.isAlphabetic(plate.toCharArray()[i])) {
                    changesAt = i;
                    break;
                }
            }
            return plate.substring(0, changesAt) + " " + plate.substring(changesAt);
        }
        // is already in the form 0{1-3} A{1-3}
        return plate;
    }

//    AA00 A
//    A0{1-3} AAA
//    AAA 0{1-3}A
//    A{1-3}0{1-3} OR 0{1-3}A{1-3}

    @Test
    public void test() {
        System.out.println(formattedRegistration("AAA000"));
//        System.out.println(Arrays.toString("A049 AAA".split("(?<=[0-9]{1,3})")));
    }
}
