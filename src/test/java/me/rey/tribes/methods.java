package me.rey.tribes;

import me.rey.clans.utils.UtilTime;
import me.rey.core.utils.UtilPacket;
import org.apache.commons.lang.Validate;
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

    public static String numberInWords(int num) {
        // String builder > string += string, since concatenation through += creates a
        // new string in memory because strings are immutable, where StringBuilder stays
        // constant, and is mutable
        StringBuilder builder = new StringBuilder();
        char[] numsAsChars = ("" + num).toCharArray();
        for (char cha : numsAsChars) {
            /*
             * A switch case is like a collection of 'if' statements which execute a different
             * piece of code depending on what condition is met, in this case, if 'cha' is equal
             * to any of the below cases, it executes the code inside it. If it does not equal
             * any of them, it executes 'default' instead. If a case/default is not broken using
             * 'break', it execute the one below it too.
             */
            switch (cha) {
                case '0':
                    builder.append("zero").append(" ");
                    break;
                case '1':
                    builder.append("one").append(" ");
                    break;
                case '2':
                    builder.append("two").append(" ");
                    break;
                case '3':
                    builder.append("three").append(" ");
                    break;
                case '4':
                    builder.append("four").append(" ");
                    break;
                case '5':
                    builder.append("five").append(" ");
                    break;
                case '6':
                    builder.append("six").append(" ");
                    break;
                case '7':
                    builder.append("seven").append(" ");
                    break;
                case '8':
                    builder.append("eight").append(" ");
                    break;
                case '9':
                    builder.append("nine").append(" ");
                    break;
                default:
                    builder.append(cha).append(" ");
                    break;
            }
        }
        builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
    }

    public static int highestNumberBelowNDivisibleByTwoNumbers(int divisor1, int divisor2, int number) {
        for (int i = number; i > 0; i--) {
            if ((i % divisor1 == 0) && (i % divisor2 == 0)) {
                return i;
            }
        }
        return -1;
    }

    public static boolean stringsAtIndexMatch(String[] array, int pos1, int pos2) {
        return array[pos1].equals(array[pos2]);
    }

    public static String apaFormatCitation(String[] authors, int year) {
        StringBuilder authorsAsString = new StringBuilder("(");
        for (int i = 0; i < authors.length; i++) {
            String author = authors[i];
            if (i == author.length() - 1) {
                authorsAsString.append("& ");
            }
            authorsAsString.append(author).append(", ");
        }
        authorsAsString.append(year).append(")");
        return authorsAsString.toString();
    }

    public static String receiptLine(String productName, String priceForm, int maxLine) {
        /* THE £ SIGN APPEARS AS AN ASCII ERROR ON SOME COMPILERS (looks like this on IntelliJ)
         * '�'
         *
         * REMOVE THE FOLLOWING COMMENT LINES:
         * The instructions state that if the maxLine is exceeded by the length of
         * productName and priceForm alone, you should not attempt to truncate it...
         * This makes no sense, because then the line is exceeded which in an actual
         * software environment, would lead to serious issues, but who tf am I to judge.
         */
        if (!priceForm.contains("£")) {
            priceForm = "£" + priceForm;
        }
        if (productName.length() + priceForm.length() > maxLine) {
            return productName + priceForm;
        } else {
            int dots = maxLine - (productName.length() + priceForm.length());
            StringBuilder builder = new StringBuilder(productName);
            for (int i = 0; i < dots; i++) {
                builder.append(".");
            }
            builder.append(priceForm);
            return builder.toString();
        }
    }

    public static String[] pagedData(String[] array, int pos1, int maxSize) {
        // Math#min(int, int) returns the lowest integer of 2 integers
        int max = Math.min(array.length - pos1, maxSize);
        String[] words = new String[max];
        System.arraycopy(array, pos1, words, 0, max);
        return words;
    }

    @Test
    public void test() {
        System.out.println(Arrays.toString(pagedData(new String[]{
                "penis",
                "man",
                "men",
                "people"
        }, 2, 20)));
    }
}
