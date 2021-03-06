package me.rey.clans.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class UtilTime {

    public enum TimeUnit {
        MONTHS(2592000000L),
        DAYS(86400000L),
        HOURS(3600000L),
        MINUTES(60000),
        SECONDS(1000),
        MILLISECONDS(1);

        public final long divider;

        TimeUnit(long divider) {
            this.divider = divider;
        }
    }

    public static double convert(long time, int trim, TimeUnit type) {
        switch (type) {
            case MONTHS:
                return trim(trim, (double) time / 2592000000L);
            case DAYS:
                return trim(trim, (double) time / 86400000L);
            case HOURS:
                return trim(trim, (double) time / 3600000L);
            case MINUTES:
                return trim(trim, (double) time / 60000L);
            case SECONDS:
                return trim(trim, (double) time / 1000L);
            default:
                return trim(trim, time);
        }
    }

    public static double trim(int degree, double d) {
        StringBuilder format = new StringBuilder("#.#");
        for (int i = 1; i < degree; i++) {
            format.append("#");
        }

        DecimalFormatSymbols symb = new DecimalFormatSymbols(Locale.US);
        DecimalFormat twoDForm = new DecimalFormat(format.toString(), symb);
        return Double.parseDouble(twoDForm.format(d));
    }

    public static TimeUnit getBestUnit(long time) {
        if (time >= 2592000000L) {
            return TimeUnit.MONTHS;
        }
        if (time >= 86400000L) {
            return TimeUnit.DAYS;
        }
        if (time >= 3600000L) {
            return TimeUnit.HOURS;
        }
        if (time >= 60000L) {
            return TimeUnit.MINUTES;
        }
        return TimeUnit.SECONDS;
    }

    public static Breakdown getBreakdown(final long milliseconds) {
        // Don't ask why I have to treat the input like its in microseconds
        // I don't know
        long timeToUse = milliseconds;
        Breakdown breakdown = new Breakdown();
        if (milliseconds == 0) {
            return breakdown;
        }

        if (timeToUse >= 2592000000L) {
            long uses = timeToUse / 2592000000L;
            timeToUse = timeToUse - (uses * 2592000000L);
            breakdown.months = uses;
        }
        if (timeToUse >= 86400000L) {
            long uses = timeToUse / 86400000L;
            timeToUse = timeToUse - (uses * 86400000L);
            breakdown.days = uses;
        }
        if (timeToUse >= 3600000L) {
            long uses = timeToUse / 3600000L;
            timeToUse = timeToUse - (uses * 3600000L);
            breakdown.hours = uses;
        }
        if (timeToUse >= 60000L) {
            long uses = timeToUse / 60000L;
            timeToUse = timeToUse - (uses * 60000L);
            breakdown.minutes = uses;
        }
        if (timeToUse >= 1000L) {
            long uses = timeToUse / 1000L;
            timeToUse = timeToUse - (uses * 1000L);
            breakdown.seconds = uses;
        }
        breakdown.milliseconds = timeToUse;
        return breakdown;
    }

    public static class Breakdown {
        public long months, days, hours, minutes, seconds, milliseconds;

        public Breakdown() {
            months = 0;
            days = 0;
            hours = 0;
            minutes = 0;
            seconds = 0;
            milliseconds = 0;
        }

        @Override
        public String toString() {
            Map<String, Object> map = new HashMap<>();
            map.put("months", months);
            map.put("days", days);
            map.put("hours", hours);
            map.put("minutes", minutes);
            map.put("seconds", seconds);
            map.put("milliseconds", milliseconds);
            return map.toString();
        }
    }
}
