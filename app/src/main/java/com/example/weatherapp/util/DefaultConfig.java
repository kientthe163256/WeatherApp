package com.example.weatherapp.util;

import com.example.weatherapp.R;
import com.example.weatherapp.model.AppLocation;
import java.util.HashMap;

public class DefaultConfig {

    public static final int REQUEST_CODE = 100;
    public static final int REQUEST_CHECK_SETTING = 1001;
    public static final long CURRENT_LOCATION_ID = 1;
    private static final long DEFAULT_LOCATION_ID = 2;
    public static double KELVIN_DELTA = 273.15;
    public static final AppLocation DEFAULT_APP_LOCATION = new AppLocation(DEFAULT_LOCATION_ID,
        "Hoàn Kiếm", 21.03, 105.85, "Ha Noi", "VN");
    public static final int NUMBER_OF_HOURS = 24;
    public static final int NUMBER_OF_DAYS = 7;

    public static final HashMap<String, Integer> iconMap = new HashMap<String, Integer>() {{
        put("clouds", R.drawable.cloudy);
        put("sunny", R.drawable.sun);
        put("rain", R.drawable.rainy);
    }};

    public static int getIconResource(String weather) {
        Integer res = iconMap.get(weather.toLowerCase());
        if (res == null) {
            return R.drawable.sun;
        }
        return res;
    }
}
