package com.example.weatherapp.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Util {

    private Util() {
    }

    public static String formatDate(String format, Date date, Integer offset) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format, Locale.ENGLISH);
        simpleDateFormat.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
        simpleDateFormat.setLenient(false);
        if (offset != null) {
            date.setTime(date.getTime() + offset * 1000);
        }
        return simpleDateFormat.format(date);
    }

}
