package com.childsafe.auth.Utils;

import android.util.Log;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimeUtil {

    static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-DDD-HH-mm");
    static DateTimeFormatter formatter_read = DateTimeFormatter.ofPattern("yyyy/M/d HH:mm");

    public static String getCurrentTime()
    {
        LocalDateTime myDateObj=LocalDateTime.now();
        return myDateObj.format(formatter);
    }

    public static LocalDateTime toLocalDateTime(String dateValue)
    {
        return (dateValue == null ? null : LocalDateTime.parse(dateValue, formatter));
    }

    public static String toReadableTime(LocalDateTime time) {
        return time.format(formatter_read);
    }

    public static Long getTimeDiff(String currentTime, String oldTime)
    {
        Duration duration = Duration.between(toLocalDateTime(oldTime), toLocalDateTime(currentTime));
        // total seconds of difference (using Math.abs to avoid negative values)
        Long seconds = Math.abs(duration.getSeconds());
        return seconds;
    }

}
