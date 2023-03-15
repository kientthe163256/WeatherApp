package com.example.weatherapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.android.volley.Response;
import com.example.weatherapp.adapter.RecyclerViewAdapter;
import com.example.weatherapp.dao.AppDatabase;
import com.example.weatherapp.dao.DailyWeatherDao;
import com.example.weatherapp.dao.HourlyWeatherDao;
import com.example.weatherapp.dao.LocationDao;
import com.example.weatherapp.model.AppLocation;
import com.example.weatherapp.model.DailyWeather;
import com.example.weatherapp.model.HourlyWeather;
import com.example.weatherapp.service.ApiService;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.Task;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private final int REQUEST_CODE = 100;
    private final int REQUEST_CHECK_SETTING = 1001;

    public static AppLocation appLocation = DefaultConfig.DEFAULT_APP_LOCATION;

    AppDatabase db;
    LocationDao locationDao;
    HourlyWeatherDao hourlyWeatherDao;
    DailyWeatherDao dailyWeatherDao;
    Context context;
    private FusedLocationProviderClient fusedLocationProviderClient;
    public TextView tvLocationName;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "WeatherApp")
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries().build();
        locationDao = db.locationDao();
        hourlyWeatherDao = db.hourlyWeatherDao();
        dailyWeatherDao = db.dailyWeatherDao();
        initializeCurrentLocationInDb();
        context = getApplicationContext();
        apiService = new ApiService(context);

        setContentView(R.layout.activity_main);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(
            MainActivity.this);

        tvLocationName = findViewById(R.id.location);

        if (!hasInternetConnection()){
            getDefaultLocationWeather();
        } else {
            checkLocationPermission();
        }

        setUpTimeInfo();
        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            getWeatherData();
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    Response.Listener<String> hourlyListener = response -> {
        try {
            JSONObject jsonResponse = new JSONObject(response);
            JSONArray hourlyDataArray = jsonResponse.getJSONArray("list");
            List<HourlyWeather> hourlyWeathers = new ArrayList<>();
            for (int i = 0; i < hourlyDataArray.length(); i++) {
                JSONObject hourlyData = hourlyDataArray.getJSONObject(i);
                // time in milliseconds to java Date
                long time = hourlyData.getLong("dt") * 1000;

                // get main data
                JSONObject main = hourlyData.getJSONObject("main");
                double temperature = main.getDouble("temp");
                double feelsLike = main.getDouble("feels_like");
                int pressure = main.getInt("pressure");
                int humidity = main.getInt("humidity");

                JSONObject weather = hourlyData.getJSONArray("weather").getJSONObject(0);
                String mainWeather = weather.getString("main");
                String description = weather.getString("description");

                hourlyWeathers.add(
                    new HourlyWeather(time, temperature, feelsLike, pressure, humidity, mainWeather,
                        description, appLocation.getId()));
            }
            hourlyWeatherDao.deleteByLocationId(appLocation.getId());
            hourlyWeatherDao.insert(hourlyWeathers);

            setUpHourlyWeather(hourlyWeathers);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    };

    Response.Listener<String> dailyListener = response -> {
        try {
            JSONObject jsonResponse = new JSONObject(response);
            JSONArray dailyDataArray = jsonResponse.getJSONArray("list");
            List<DailyWeather> dailyWeathers = new ArrayList<>();
            for (int i = 0; i < dailyDataArray.length(); i++) {
                JSONObject dailyData = dailyDataArray.getJSONObject(i);
                // time in milliseconds to java Date
                long time = dailyData.getLong("dt") * 1000;
                long sunrise = dailyData.getLong("sunrise") * 1000;
                long sunset = dailyData.getLong("sunset") * 1000;

                // get main data
                JSONObject temp = dailyData.getJSONObject("temp");
                double minTemp = temp.getDouble("min");
                double maxTemp = temp.getDouble("max");
                // in API response: weather is an array
                JSONObject weather = dailyData.getJSONArray("weather").getJSONObject(0);
                String mainWeather = weather.getString("main");
                String description = weather.getString("description");

                dailyWeathers.add(
                    new DailyWeather(time, sunrise, sunset, minTemp, maxTemp, mainWeather,
                        description, appLocation.getId()));
            }
            dailyWeatherDao.deleteByLocationId(appLocation.getId());
            dailyWeatherDao.insert(dailyWeathers);

            setUpDailyWeather(dailyWeathers);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    };

    private void setUpTimeInfo() {
        TextView tvTime = findViewById(R.id.date_time);
        SimpleDateFormat formatter = new SimpleDateFormat("EE, HH:mm");
        Date date = new Date();
        tvTime.setText(formatter.format(date));
    }

    private void setUpCurrentTempInfo(String temp) {
        TextView tvTemp = findViewById(R.id.temperature);
        tvTemp.setText(temp);
    }

    public boolean hasInternetConnection() {
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(
            Context.CONNECTIVITY_SERVICE);
        return cm.getNetworkCapabilities(cm.getActiveNetwork()) != null;
    }

    private void setUpHourlyWeather(List<HourlyWeather> hourlyWeathers) {
        RecyclerView recyclerView = findViewById(R.id.weatherByHour);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this,
            LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);

        RecyclerViewAdapter rvAdapter = new RecyclerViewAdapter(hourlyWeathers);
        recyclerView.setAdapter(rvAdapter);
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            // permission is granted
            checkGPS();
        } else {
            // request for permission
            requestPermission();
        }
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
    }

    private void checkGPS() {
        long timeInterval = 1000;
        LocationRequest locationRequest = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, timeInterval).setWaitForAccurateLocation(true).build();

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(
                locationRequest).setAlwaysShow(true);

        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(
                getApplicationContext()).checkLocationSettings(builder.build());


        result.addOnCompleteListener(task -> {
            try {
                // when GPS is already on
                task.getResult(ApiException.class);
                getCurrentLocation();
            } catch (ApiException e) {
                if (e.getStatusCode() == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                    try {
                        // try to ask user turn on GPS
                        ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                        resolvableApiException.startResolutionForResult(MainActivity.this,
                                REQUEST_CHECK_SETTING);
                    } catch (SendIntentException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
    }

    // check after request appLocation permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                Toast.makeText(this, "LOCATION PERMISSION MUST BE GRANTED", Toast.LENGTH_LONG)
                        .show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    // check after request turn on GPS
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CHECK_SETTING) {
            if (resultCode == Activity.RESULT_OK) {
                getCurrentLocation();
            } else {
                // user don't turn on GPS, set appLocation to default appLocation
                MainActivity.appLocation = DefaultConfig.DEFAULT_APP_LOCATION;
                getWeatherData();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void getCurrentLocation() {
        // the entire comparison is here because the editor will throw a warning otherwise
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Please grant location permissions", Toast.LENGTH_SHORT).show();
            return;
        }
        fusedLocationProviderClient.getCurrentLocation(Priority.PRIORITY_LOW_POWER, null)
            .addOnSuccessListener(this, this::processReceivedLocation);
    }


    private void processReceivedLocation(Location location) {
        if (location != null) {
            Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(location.getLatitude(),
                    location.getLongitude(), 1);
                Address address = addresses.get(0);
                MainActivity.appLocation.setId(DefaultConfig.CURRENT_LOCATION_ID);
                MainActivity.appLocation.setLatitude(address.getLatitude());
                MainActivity.appLocation.setLongitude(location.getLongitude());
                MainActivity.appLocation.setName(address.getSubAdminArea());
                MainActivity.appLocation.setState(address.getAdminArea());
                MainActivity.appLocation.setCountry(address.getCountryName());

                AppLocation currentLocation = locationDao.getLocationById(DefaultConfig.CURRENT_LOCATION_ID);
                currentLocation.setLatitude(address.getLatitude());
                currentLocation.setLongitude(location.getLongitude());
                currentLocation.setName(address.getSubAdminArea());
                currentLocation.setState(address.getAdminArea());
                currentLocation.setCountry(address.getCountryName());
                locationDao.update(currentLocation);

            } catch (IOException e) {
                e.printStackTrace();
            }
            getWeatherData();
        }
    }

    private void getWeatherData(){
        if (hasInternetConnection()) {
            //has Internet, call API
            apiService.getHourlyWeather(appLocation.getLatitude(), appLocation.getLongitude(), hourlyListener);
            apiService.getDailyWeather(appLocation.getLatitude(), appLocation.getLongitude(), dailyListener);
        } else {
            //no Internet, get data from db
            Toast.makeText(MainActivity.this, "No Internet", Toast.LENGTH_SHORT).show();
            getDefaultLocationWeather();
        }
        tvLocationName.setText(appLocation.getName());
    }

    private void getDefaultLocationWeather(){
        List<HourlyWeather> hourlyWeathers = hourlyWeatherDao.getByLocationId(appLocation.getId());
        List<DailyWeather> dailyWeathers = dailyWeatherDao.getByLocationId(appLocation.getId());

        tvLocationName.setText(appLocation.getName());
        setUpHourlyWeather(hourlyWeathers);
        setUpDailyWeather(dailyWeathers);
    }

    private void initializeCurrentLocationInDb(){
        if (locationDao.getLocationById(DefaultConfig.CURRENT_LOCATION_ID) == null){
            AppLocation appLocation = DefaultConfig.DEFAULT_APP_LOCATION;
            appLocation.setId(DefaultConfig.CURRENT_LOCATION_ID);
            locationDao.insert(appLocation);
        }
    }

    private void setUpDailyWeather(List<DailyWeather> dailyWeathers) {
        double KELVIN_DELTA = 273.15;

        dailyWeathers.forEach(dailyWeather -> {
            dailyWeather.setMinTemp(dailyWeather.getMinTemp() - KELVIN_DELTA);
            dailyWeather.setMaxTemp(dailyWeather.getMaxTemp() - KELVIN_DELTA);
        });

        TableLayout tableLayout = findViewById(R.id.daily_weather);
        LayoutInflater inflater = getLayoutInflater();
        int maxTemp = getMaxTemp(dailyWeathers);
        int minTemp = getMinTemp(dailyWeathers);
        int maxTempDiff = maxTemp - minTemp;
        for (DailyWeather dailyWeather : dailyWeathers) {
            TableRow row = inflater.inflate(R.layout.daily_weather_layout, null, false)
                .findViewById(R.id.row_day);
            TextView date = row.findViewById(R.id.row_day_day);
            String dateStr = getDayOfWeek("EEEE", new Date(dailyWeather.getTime()));
            date.setText(dateStr);
            ImageView icon = row.findViewById(R.id.row_day_icon);
            HashMap<String, Integer> iconMap = getIconMap();
            iconMap.put("Cloudy", R.drawable.cloudy);
            iconMap.put("Sunny", R.drawable.sun);
            icon.setImageResource(
                iconMap.getOrDefault(dailyWeather.getWeather(), R.drawable.cloudy));

            View tempDiff = row.findViewById(R.id.row_day_range_fg);
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) tempDiff.getLayoutParams();
            params.leftMargin = dpToPx(
                (int) ((dailyWeather.getMinTemp() - minTemp) / maxTempDiff * 100));
            int width = dpToPx(
                (int) ((dailyWeather.getMaxTemp() - dailyWeather.getMinTemp()) / maxTempDiff
                    * 100));
            params.width = dpToPx(width);
            tempDiff.setLayoutParams(params);

            TextView minTempView = row.findViewById(R.id.row_day_min_temp);
            minTempView.setText(String.valueOf((int) dailyWeather.getMinTemp()));

            TextView maxTempView = row.findViewById(R.id.row_day_max_temp);
            maxTempView.setText(String.valueOf((int) dailyWeather.getMaxTemp()));
            tableLayout.addView(row);
        }
    }

    private int dpToPx(int dp) {
        return dp * (getResources().getDisplayMetrics().densityDpi
            / DisplayMetrics.DENSITY_DEFAULT);
    }

    private int getMaxTemp(List<DailyWeather> dailyWeathers) {
        return dailyWeathers.stream().map(dailyWeather -> (int) dailyWeather.getMaxTemp())
            .max(Integer::compareTo).orElse(0);
    }

    private int getMinTemp(List<DailyWeather> dailyWeathers) {
        return dailyWeathers.stream().map(dailyWeather -> (int) dailyWeather.getMinTemp())
            .min(Integer::compareTo).orElse(0);
    }

    private HashMap<String, Integer> getIconMap() {
        HashMap<String, Integer> iconMap = new HashMap<>();
        iconMap.put("Sunny", R.drawable.sun);
        iconMap.put("Cloudy", R.drawable.cloudy);
        return iconMap;
    }

    private String getDayOfWeek(String format, Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format, Locale.ENGLISH);
        simpleDateFormat.setLenient(false);
        return simpleDateFormat.format(date);
    }

    private String currentHourAndMinute() {
        return getDayOfWeek("HH:mm", new Date());
    }
}