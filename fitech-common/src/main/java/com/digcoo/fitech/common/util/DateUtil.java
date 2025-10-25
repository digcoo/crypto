package com.digcoo.fitech.common.util;

import com.digcoo.fitech.common.enums.CandlestickPeriod;
import org.apache.commons.lang3.tuple.Pair;

import java.time.*;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAdjuster;

public final class DateUtil {

    /**
     * 如果时间超过15:00，则返回当天15:00的时间戳
     * @param timestamp
     * @return
     */
    public static long resetTo1500(long timestamp) {
        Instant instant = Instant.ofEpochMilli(timestamp);
        LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());

        LocalDateTime cutoffTime = dateTime.toLocalDate().atTime(15, 0);

//        if (dateTime.isAfter(cutoffTime)) {
            return cutoffTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
//        }
//        return timestamp;

    }

    /**
     * 如果时间超过15:00，则返回当天15:00的时间戳
     * @param timestamp
     * @return
     */
    public static long resetTo0930(long timestamp) {
        Instant instant = Instant.ofEpochMilli(timestamp);
        LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());

        LocalDateTime cutoffTime = dateTime.toLocalDate().atTime(9, 30);

//        if (dateTime.isBefore(cutoffTime)) {
            return cutoffTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
//        }
//        return timestamp;

    }

    private static LocalDate toLocalDate(long timestamp) {
        Instant instant = Instant.ofEpochMilli(timestamp);
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).toLocalDate();
    }

    public static Pair<Long, Long> resetToFirstAndLastDayOfPeriod(long timestamp, CandlestickPeriod candlestickPeriod) {
        LocalDate localDate = toLocalDate(timestamp);
        LocalDate firstDay = null, lastDay = null;

        switch (candlestickPeriod) {
            case DAILY:
                firstDay = localDate;
                lastDay = localDate;
                break;

            case WEEKLY:
                firstDay = localDate.with(DayOfWeek.MONDAY);
                lastDay = localDate.with(DayOfWeek.SUNDAY);
                break;

            case MONTHLY:
                firstDay = localDate.withDayOfMonth(1);
                lastDay = localDate.withDayOfMonth(localDate.lengthOfMonth());
            break;
            case YEARLY:
                firstDay = localDate.withDayOfYear(1);
                lastDay = localDate.withDayOfYear(localDate.lengthOfYear());
                break;
            default:
                break;
        }

        return Pair.of(firstDay.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                lastDay.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli());
    }
}
