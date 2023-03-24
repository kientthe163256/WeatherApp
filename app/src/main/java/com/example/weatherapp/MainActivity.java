package com.example.weatherapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
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
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
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
import com.example.weatherapp.util.DefaultConfig;
import com.example.weatherapp.util.LocationHelper;
import com.example.weatherapp.util.Util;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private AppLocation appLocation = DefaultConfig.DEFAULT_APP_LOCATION;
    private LocationDao locationDao;
    private HourlyWeatherDao hourlyWeatherDao;
    private DailyWeatherDao dailyWeatherDao;
    private ApiService apiService;
    private LocationHelper locationHelper;
    private Integer timeZoneOffset = 7 * 60 * 60;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppDatabase db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class,
            "WeatherApp").fallbackToDestructiveMigration().allowMainThreadQueries().build();
        locationDao = db.locationDao();
        hourlyWeatherDao = db.hourlyWeatherDao();
        dailyWeatherDao = db.dailyWeatherDao();
        apiService = new ApiService(getApplicationContext());
        locationHelper = new LocationHelper(this);

        setContentView(R.layout.activity_main);
        System.out.println(locationDao.getAllLocations());

        initializeCurrentLocationInDb();

        if (!hasInternetConnection()) {
            Toast.makeText(this, "No Internet", Toast.LENGTH_SHORT).show();
            getDefaultLocationWeather();
        } else {
            locationHelper.checkLocationPermission(this::processReceivedLocation,
                this::processByDefaultLocation);
        }

        setUpTimeInfo();

        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            getWeatherData();
            setUpTimeInfo();
            swipeRefreshLayout.setRefreshing(false);
        });

        TextView menu = findViewById(R.id.menu_btn);
        menu.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LocationActivity.class);
            activityResultLauncher.launch(intent);
        });
    }

    // activity launcher
    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Intent data = result.getData();
                if (data != null) {
                    appLocation = (AppLocation) data.getSerializableExtra("current_location");
                    getWeatherData();
                }
            }
        });

    Response.Listener<String> hourlyListener = response -> {
        try {
            JSONObject jsonResponse = new JSONObject(response);
            JSONArray hourlyData = jsonResponse.getJSONArray("list");
            List<HourlyWeather> hourlyWeathers = HourlyWeather.fromJsonArray(hourlyData,
                appLocation.getId());

            timeZoneOffset = jsonResponse.getJSONObject("city").getInt("timezone");

            hourlyWeatherDao.deleteByLocationId(appLocation.getId());
            hourlyWeatherDao.insert(hourlyWeathers);
            setUpFeelLike(hourlyWeathers.get(0).getFeelsLike());
            setUpPressure(hourlyWeathers.get(0).getPressure());
            setUpHourlyWeather(hourlyWeathers);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    };

    Response.Listener<String> dailyListener = response -> {
        try {
            JSONObject jsonResponse = new JSONObject(response);
            JSONArray dailyDataArray = jsonResponse.getJSONArray("list");
            List<DailyWeather> dailyWeathers = DailyWeather.fromJsonArray(dailyDataArray,
                appLocation.getId());
            dailyWeatherDao.deleteByLocationId(appLocation.getId());
            dailyWeatherDao.insert(dailyWeathers);
            setUpDailyWeather(dailyWeathers);
            Date sunrise = new Date(dailyWeathers.get(0).getSunrise());
            Date sunset = new Date(dailyWeathers.get(0).getSunset());
            setUpSunInfo(sunrise, sunset);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    };

    public boolean hasInternetConnection() {
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(
            Context.CONNECTIVITY_SERVICE);
        return cm.getNetworkCapabilities(cm.getActiveNetwork()) != null;
    }

    // check after request location permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
        @NonNull int[] grantResults) {
        if (requestCode == DefaultConfig.REQUEST_CODE && (grantResults.length <= 0
            || grantResults[0] != PackageManager.PERMISSION_GRANTED)) {
            Toast.makeText(this, "LOCATION PERMISSION MUST BE GRANTED", Toast.LENGTH_LONG).show();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    // check after request turn on GPS
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == DefaultConfig.REQUEST_CHECK_SETTING) {
            if (resultCode == Activity.RESULT_OK) {
                locationHelper.getCurrentLocation(this::processReceivedLocation,
                    this::processByDefaultLocation);
            } else {
                // user don't turn on GPS, set appLocation to default appLocation
                appLocation = DefaultConfig.DEFAULT_APP_LOCATION;
                getWeatherData();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void processByDefaultLocation(Exception e) {
        Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
        appLocation = DefaultConfig.DEFAULT_APP_LOCATION;
        getWeatherData();
    }

    private void processReceivedLocation(Location location) {
        if (location == null) {
            return;
        }
        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(),
                location.getLongitude(), 1);
            Address address = addresses.get(0);

            // get timezone
            appLocation.setId(DefaultConfig.CURRENT_LOCATION_ID);
            appLocation.setLatitude(address.getLatitude());
            appLocation.setLongitude(location.getLongitude());
            appLocation.setName(address.getSubAdminArea());
            appLocation.setState(address.getAdminArea());
            appLocation.setCountry(address.getCountryName());

            AppLocation currentLocation = locationDao.getLocationById(
                DefaultConfig.CURRENT_LOCATION_ID);

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

    private void getWeatherData() {
        if (hasInternetConnection()) {
            // has Internet, call API
            apiService.getHourlyWeather(appLocation.getLatitude(), appLocation.getLongitude(),
                hourlyListener);
            apiService.getDailyWeather(appLocation.getLatitude(), appLocation.getLongitude(),
                dailyListener);
        } else {
            // no Internet, get data from db
            Toast.makeText(MainActivity.this, "No Internet", Toast.LENGTH_SHORT).show();
            getDefaultLocationWeather();
        }
        TextView tvLocationName = findViewById(R.id.location);
        tvLocationName.setText(appLocation.getName());
    }

    private void getDefaultLocationWeather() {
        List<HourlyWeather> hourlyWeathers = hourlyWeatherDao.getByLocationId(appLocation.getId());
        List<DailyWeather> dailyWeathers = dailyWeatherDao.getByLocationId(appLocation.getId());

        TextView tvLocationName = findViewById(R.id.location);
        tvLocationName.setText(appLocation.getName());
        setUpHourlyWeather(hourlyWeathers);
        setUpDailyWeather(dailyWeathers);
    }

    private void initializeCurrentLocationInDb() {
        if (locationDao.getLocationById(DefaultConfig.CURRENT_LOCATION_ID) == null) {
            AppLocation appLocation = DefaultConfig.DEFAULT_APP_LOCATION;
            appLocation.setId(DefaultConfig.CURRENT_LOCATION_ID);
            locationDao.insert(appLocation);
        }
    }

    /**
     * UI display section
     */

    private void displayTempRange(int minTemp, int maxTempDiff, DailyWeather dailyWeather,
        TableRow row) {
        View tempDiff = row.findViewById(R.id.row_day_range_fg);
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) tempDiff.getLayoutParams();
        params.leftMargin = dpToPx(
            (int) ((dailyWeather.getMinTemp() - minTemp) / maxTempDiff * 100));
        int width = dpToPx(
            (int) ((dailyWeather.getMaxTemp() - dailyWeather.getMinTemp()) / maxTempDiff * 100));
        params.width = dpToPx(width);
        tempDiff.setLayoutParams(params);
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

    private void setUpTimeInfo() {
        TextView tvTime = findViewById(R.id.date_time);
        String time = Util.formatDate("EE, HH:mm", new Date(), timeZoneOffset);
        tvTime.setText(time);
    }

    private void setUpSunInfo(Date sunrise, Date sunset) {
        TextView tvSunrise = findViewById(R.id.sun_rise_time);
        TextView tvSunset = findViewById(R.id.sun_set_time);
        String sunriseTime = Util.formatDate("HH:mm", sunrise, timeZoneOffset);
        String sunsetTime = Util.formatDate("HH:mm", sunset, timeZoneOffset);
        tvSunrise.setText(sunriseTime);
        tvSunset.setText(sunsetTime);
    }
    private void setUpFeelLike(double feelLike){
        TextView tvFeelLike = findViewById(R.id.feelLikeContent);
        tvFeelLike.setText(String.valueOf(feelLike));
    }

    private void setUpPressure(int pressure){
        TextView tvPressure = findViewById(R.id.pressureContent);
        tvPressure.setText(String.valueOf(pressure));
    }

    private void setUpCurrentTempInfo(String temp) {
        TextView tvTemp = findViewById(R.id.temperature);
        tvTemp.setText(temp);
    }

    private void setUpCurrentDescriptionInfo(String description) {
        TextView tvDescription = findViewById(R.id.description);
        tvDescription.setText(description);
    }

    private void setUpHourlyWeather(List<HourlyWeather> hourlyWeathers) {
        RecyclerView recyclerView = findViewById(R.id.weatherByHour);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this,
            LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);

        RecyclerViewAdapter rvAdapter = new RecyclerViewAdapter(hourlyWeathers);
        recyclerView.setAdapter(rvAdapter);
        // display temperature
        int currentTemp = (int) Math.round(
            hourlyWeathers.get(0).getTemperature() - DefaultConfig.KELVIN_DELTA);
        setUpCurrentTempInfo(currentTemp + "°");
        // display description
        String currentDescription = hourlyWeathers.get(0).getDescription();
        setUpCurrentDescriptionInfo(currentDescription);
        setUpTimeInfo();
    }

    private void setUpDailyWeather(List<DailyWeather> dailyWeathers) {
        LayoutInflater inflater = getLayoutInflater();
        TableLayout tableLayout = findViewById(R.id.daily_weather);
        tableLayout.removeAllViews();

        dailyWeathers.forEach(dailyWeather -> {
            dailyWeather.setMinTemp(dailyWeather.getMinTemp() - DefaultConfig.KELVIN_DELTA);
            dailyWeather.setMaxTemp(dailyWeather.getMaxTemp() - DefaultConfig.KELVIN_DELTA);
        });

        int maxTemp = getMaxTemp(dailyWeathers);
        int minTemp = getMinTemp(dailyWeathers);
        int maxTempDiff = maxTemp - minTemp;
        for (DailyWeather dailyWeather : dailyWeathers) {
            TableRow row = inflater.inflate(R.layout.daily_weather_layout, tableLayout, false)
                .findViewById(R.id.row_day);
            TextView date = row.findViewById(R.id.row_day_day);
            String dateStr = Util.formatDate("EEEE", new Date(dailyWeather.getTime()),
                timeZoneOffset);
            date.setText(dateStr);

            ImageView icon = row.findViewById(R.id.row_day_icon);
            icon.setImageResource(DefaultConfig.getIconResource(dailyWeather.getWeather()));

            displayTempRange(minTemp, maxTempDiff, dailyWeather, row);

            TextView minTempView = row.findViewById(R.id.row_day_min_temp);
            minTempView.setText(String.valueOf((int) dailyWeather.getMinTemp()));

            TextView maxTempView = row.findViewById(R.id.row_day_max_temp);
            maxTempView.setText(String.valueOf((int) dailyWeather.getMaxTemp()));
            tableLayout.addView(row);
        }
    }
}