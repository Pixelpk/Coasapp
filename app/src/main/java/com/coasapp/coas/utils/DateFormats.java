package com.coasapp.coas.utils;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class DateFormats {
    SimpleDateFormat formatDateTimeDb = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    SimpleDateFormat formatDateTimeNative = new SimpleDateFormat("MM-dd-yyyy h:mm a");
    SimpleDateFormat formatDateDb = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat formatDateNative = new SimpleDateFormat("MM-dd-yyyy");
    SimpleDateFormat formatTimeDb = new SimpleDateFormat("HH:mm:ss");
    SimpleDateFormat formatTimeNative = new SimpleDateFormat("h:mm a");
    String timezone = TimeZone.getDefault().getID();

    public SimpleDateFormat getFormatDateDb() {
        return formatDateDb;
    }

    public SimpleDateFormat getFormatDateNative() {
        return formatDateNative;
    }

    public SimpleDateFormat getFormatTimeDb() {
        return formatTimeDb;
    }

    public SimpleDateFormat getFormatTimeNative() {
        return formatTimeNative;
    }


    public SimpleDateFormat getFormatDateTimeDb() {
        return formatDateTimeDb;
    }


    public SimpleDateFormat getFormatDateTimeNative() {
        return formatDateTimeNative;
    }


    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
        formatDateTimeDb.setTimeZone(TimeZone.getTimeZone(timezone));
        formatDateTimeNative.setTimeZone(TimeZone.getTimeZone(timezone));
        formatDateDb.setTimeZone(TimeZone.getTimeZone(timezone));
        formatTimeDb.setTimeZone(TimeZone.getTimeZone(timezone));
        formatDateNative.setTimeZone(TimeZone.getTimeZone(timezone));
        formatTimeNative.setTimeZone(TimeZone.getTimeZone(timezone));
    }
}
