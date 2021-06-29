package com.coasapp.coas.utils;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class DataFormats {
    public static NumberFormat formatInr = NumberFormat.getCurrencyInstance(Locale.getDefault());
    public static SimpleDateFormat dateFormatDb = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    public static SimpleDateFormat dateFormatFile = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.getDefault());
    public static SimpleDateFormat dateFormatDbDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    public static SimpleDateFormat dateFormatDbTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
    public static SimpleDateFormat dateFormatNative = new SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.getDefault());
    public static SimpleDateFormat dateFormatNative2 = new SimpleDateFormat("dd-MMM-yyyy\nhh:mm a", Locale.getDefault());
    public static SimpleDateFormat dateFormatNativeDate = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
    public static SimpleDateFormat dateFormatNativeTime = new SimpleDateFormat("hh:mm a", Locale.getDefault());

    public static String getFormattedDateTime(String dbDate) {

        if (dbDate.equalsIgnoreCase("0000-00-00 00:00:00")) {
            return "";
        }
        try {
            return DataFormats.dateFormatNative.format(DataFormats.dateFormatDb.parse(dbDate));
        } catch (ParseException e) {
            e.printStackTrace();
            return dbDate;
        }
    }

    public static String getFormattedDate(String dbDate) {
        if (dbDate.equalsIgnoreCase("0000-00-00 00:00:00")) {
            return "";
        }
        try {
            return DataFormats.dateFormatNativeDate.format(DataFormats.dateFormatDb.parse(dbDate));
        } catch (ParseException e) {
            e.printStackTrace();
            return dbDate;
        }
    }

    public static String getFormattedTime(String dbDate) {
        if (dbDate.equalsIgnoreCase("0000-00-00 00:00:00")) {
            return "";
        }
        try {
            return DataFormats.dateFormatNativeTime.format(DataFormats.dateFormatDb.parse(dbDate));
        } catch (ParseException e) {
            e.printStackTrace();
            return dbDate;
        }
    }

    public static String getFormattedDateFromDate(String dbDate) {
        if (dbDate.equalsIgnoreCase("0000-00-00")) {
            return "";
        }
        try {
            return DataFormats.dateFormatNativeDate.format(DataFormats.dateFormatDbDate.parse(dbDate));
        } catch (ParseException e) {
            e.printStackTrace();
            return dbDate;
        }
    }

    public static String getFormattedTimeFromTime(String dbDate) {
        if (dbDate.equalsIgnoreCase("00:00:00")) {
            return "";
        }
        try {
            return DataFormats.dateFormatNativeTime.format(DataFormats.dateFormatDbTime.parse(dbDate));
        } catch (ParseException e) {
            e.printStackTrace();
            return dbDate;
        }
    }

}