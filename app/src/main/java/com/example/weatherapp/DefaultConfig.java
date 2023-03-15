package com.example.weatherapp;

import com.example.weatherapp.model.AppLocation;

public class DefaultConfig {
    public static final long CURRENT_LOCATION_ID = 1;
    private static final long DEFAULT_LOCATION_ID = 2;
    public static final AppLocation DEFAULT_APP_LOCATION = new AppLocation(DEFAULT_LOCATION_ID,"Hoàn Kiếm", 21.03, 105.85, "Ha Noi", "VN");
    public static final int NUMBER_OF_HOURS = 24;
    public static final int NUMBER_OF_DAYS = 7;
}
