package me.rey.core.utils;

import java.text.SimpleDateFormat;

public class UtilsTime {

    public static String getSimpleDurationStringFromSeconds(long seconds) {
        if (seconds > 0) {
            if (seconds >= 60) {
                double mins = round((double) seconds / 60, 1);
                if (mins >= 60) {
                    double hours = round(mins / 60, 1);
                    if (hours >= 24) {
                        return trimTime(round(hours / 24, 1) == 1 ? round(hours / 24, 1) + " day" : round(hours / 24, 1) + " days");
                    } else {
                        return trimTime(hours == 1 ? hours + " hour" : hours + " hours");
                    }
                } else {
                    return trimTime(mins == 1 ? mins + " minute" : mins + " minutes");
                }
            } else {
                return trimTime(seconds == 1 ? seconds + " second" : seconds + " seconds");
            }
        } else {
            return "0 seconds";
        }
    }

    public static String trimTime(String time) {
        int lowest = Integer.MAX_VALUE;
        String[] numbersToLookFor = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9"};
        for (String num : numbersToLookFor) {
            if (time.contains(num) && time.indexOf(num) < lowest) {
                lowest = time.indexOf(num);
            }
        }
        if (lowest != Integer.MAX_VALUE) {
            time = time.substring(lowest);
            return time.replace(".0", "");
        } else {
            return "0";
        }
    }

    public static String getTimeAndDateFromEpoch(long seconds) {
        return new SimpleDateFormat("MM'/'dd'/'yyyy '('h:mm:ss a') [EST]'").format(seconds * 1000 - 7200000);
    }

    public static double round(double value, int places) {
        if (places >= 0) {
            long factor = (long) Math.pow(10, places);
            value = value * factor;
            long tmp = Math.round(value);
            return (double) tmp / factor;
        } else {
            throw new IllegalArgumentException("decimal places must be 0 or more");
        }
    }
}
