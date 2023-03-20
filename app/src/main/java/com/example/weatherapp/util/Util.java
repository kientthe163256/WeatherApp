package com.example.weatherapp.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Util {

    private Util() {
    }

    public static String formatDate(String format, Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format, Locale.ENGLISH);
        simpleDateFormat.setTimeZone(java.util.TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        simpleDateFormat.setLenient(false);
        return simpleDateFormat.format(date);
    }

}
