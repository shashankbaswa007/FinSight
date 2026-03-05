package com.finsight.util;

import java.time.LocalDate;

/**
 * Date utility class for common date calculations used across services.
 */
public final class DateUtil {

    private DateUtil() {
        // Utility class – no instantiation
    }

    /** Get the first day of a given month/year. */
    public static LocalDate firstDayOfMonth(int month, int year) {
        return LocalDate.of(year, month, 1);
    }

    /** Get the last day of a given month/year. */
    public static LocalDate lastDayOfMonth(int month, int year) {
        LocalDate first = LocalDate.of(year, month, 1);
        return first.withDayOfMonth(first.lengthOfMonth());
    }

    /** Get the first day of the current month. */
    public static LocalDate firstDayOfCurrentMonth() {
        return LocalDate.now().withDayOfMonth(1);
    }

    /** Get the last day of the current month. */
    public static LocalDate lastDayOfCurrentMonth() {
        LocalDate now = LocalDate.now();
        return now.withDayOfMonth(now.lengthOfMonth());
    }
}
