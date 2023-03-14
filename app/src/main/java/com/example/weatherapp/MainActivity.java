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
import android.net.ConnectivityManager;
import android.os.AsyncTask;
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
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.weatherapp.dao.AppDatabase;
import com.example.weatherapp.dao.DailyWeatherDao;
import com.example.weatherapp.dao.HourlyWeatherDao;
import com.example.weatherapp.dao.LocationDao;
import com.example.weatherapp.model.AppLocation;
import com.example.weatherapp.model.DailyWeather;
import com.example.weatherapp.model.HourlyWeather;
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
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
    private final String API_KEY = "35957f6517fce7e6dca5158fc93ba98b";
    private final String HOURLY_WEATHER_URL = "https://pro.openweathermap.org/data/2.5/forecast/hourly";
    private final String DAILY_WEATHER_URL = "https://pro.openweathermap.org/data/2.5/forecast/daily";
    private final String GEOCODING_URL = "http://api.openweathermap.org/geo/1.0/direct";

    public static AppLocation appLocation = new AppLocation();

    AppDatabase db;
    LocationDao locationDao;
    HourlyWeatherDao hourlyWeatherDao;
    DailyWeatherDao dailyWeatherDao;
    Context context;
    private FusedLocationProviderClient fusedLocationProviderClient;
    public TextView tvLocationName;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class,
            "WeatherApp").allowMainThreadQueries().build();
        locationDao = db.locationDao();
        hourlyWeatherDao = db.hourlyWeatherDao();
        dailyWeatherDao = db.dailyWeatherDao();

        setContentView(R.layout.activity_main);

        context = getApplicationContext();
        // Recycler view weather by hour
        recyclerView = findViewById(R.id.weatherByHour);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this,
            LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);

        tvLocationName = findViewById(R.id.location);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(
            MainActivity.this);
        /* Start app -> check permission -> check GPS:
            if on, get current appLocation, call API/load from db (No internet)
            if off, request turn on GPS:
                user turn on -> get current appLocation, call API/load db
                user not allow -> get default appLocation
        * */
        checkLocationPermission();
        List<DailyWeather> dailyWeathers = mockDailyWeatherList();
        setUpDailyWeather(dailyWeathers);
        //    Location location = checkLocation();
        //    List<HourlyWeather> hourlyWeathers = api.getHourlyWeather(location);
        //    setUpHourlyWeather(hourlyWeathers);
        //    List<DailyWeather> dailyWeathers = api.getDailyWeather(location);
        //    setUpDailyWeather(dailyWeathers);
    }

    public boolean hasInternetConnection() {
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(
            Context.CONNECTIVITY_SERVICE);
        return cm.getNetworkCapabilities(cm.getActiveNetwork()) != null;
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_FINE_LOCATION)
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
            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
            REQUEST_CODE);
    }

    private void checkGPS() {
        long timeInterval = 1000;
        LocationRequest locationRequest = new LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            timeInterval).setWaitForAccurateLocation(true).build();

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(
            locationRequest).setAlwaysShow(true);

        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(
            getApplicationContext()).checkLocationSettings(builder.build());

        result.addOnCompleteListener(
            task -> {
                try {
                    // when GPS is already on
                    task.getResult(ApiException.class);
                    getCurrentLocation();
                    new HourlyTask().execute(appLocation.getLatitude(),
                        appLocation.getLongitude(), 24.0);
                } catch (ApiException e) {
                    if (e.getStatusCode()
                        == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                        try {
                            // try to ask user turn on GPS
                            ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                            resolvableApiException.startResolutionForResult(
                                MainActivity.this,
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
    public void onRequestPermissionsResult(int requestCode,
        @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                Toast.makeText(this, "LOCATION PERMISSION MUST BE GRANTED",
                    Toast.LENGTH_LONG).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions,
            grantResults);
    }

    // check after request turn on GPS
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
        @Nullable Intent data) {
        if (requestCode == REQUEST_CHECK_SETTING) {
            if (resultCode == Activity.RESULT_OK) {
                getCurrentLocation();
            } else {
                // user don't turn on GPS, set appLocation to default appLocation
                MainActivity.appLocation = DefaultConfig.DEFAULT_APP_LOCATION;
                if (hasInternetConnection()) {
                    new HourlyTask().execute(appLocation.getLatitude(),
                        appLocation.getLongitude(), 24.0);
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void getCurrentLocation() {
        // the entire comparison is here because the editor will throw a warning otherwise
        if (ActivityCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Please grant location permissions",
                Toast.LENGTH_SHORT).show();
            return;
        }
        fusedLocationProviderClient.getLastLocation()
            .addOnSuccessListener(this, this::processReceivedLocation)
            .addOnCompleteListener(task -> new HourlyTask().execute(
                MainActivity.appLocation.getLatitude(),
                MainActivity.appLocation.getLongitude(), 24.0));
    }


    private void processReceivedLocation(Location location) {
        if (location != null) {
            Geocoder geocoder = new Geocoder(MainActivity.this,
                Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(
                    location.getLatitude(), location.getLongitude(), 1);
                Address address = addresses.get(0);
                MainActivity.appLocation.setLatitude(
                    address.getLatitude());
                MainActivity.appLocation.setLongitude(
                    location.getLongitude());
                MainActivity.appLocation.setName(
                    address.getSubAdminArea());
                MainActivity.appLocation.setState(
                    address.getAdminArea());
                MainActivity.appLocation.setCountry(
                    address.getCountryName());
                tvLocationName.setText(address.getSubAdminArea());

                insertLocation(MainActivity.appLocation);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void insertLocation(
        @NonNull AppLocation appLocation) {
        locationDao.insert(appLocation);
    }

    private class HourlyTask extends AsyncTask<Double, Integer, Integer> {

        @Override
        protected Integer doInBackground(Double... doubles) {
            int cnt = doubles[2].intValue();
            String requestUrl =
                HOURLY_WEATHER_URL + "?lat=" + doubles[0] + "&lon=" + doubles[1]
                    + "&cnt=" + cnt + "&appid=" + API_KEY;

            long idLocation = locationDao.getIdLocationBylatitudeAndlongitude(
                appLocation.getLatitude(),
                appLocation.getLongitude());
            List<HourlyWeather> hourlyWeathers = new ArrayList<>();

            StringRequest stringRequest = new StringRequest(Request.Method.GET,
                requestUrl, response -> processHourlyWeatherResponse(idLocation,
                hourlyWeathers, response),
                error -> Toast.makeText(context, error.toString(),
                    Toast.LENGTH_SHORT).show());
            RequestQueue requestQueue = Volley.newRequestQueue(context);
            requestQueue.add(stringRequest);

            deleteHourlyWeatherByLocationId(
                Integer.parseInt(String.valueOf(idLocation)));
            insertHourlyWeather(hourlyWeathers);
            return 1;
        }

        private void processHourlyWeatherResponse(long idLocation,
            List<HourlyWeather> hourlyWeathers, String response) {
            try {
                JSONObject jsonResponse = new JSONObject(response);
                JSONArray hourlyDataArray = jsonResponse.getJSONArray(
                    "list");
                for (int i = 0; i < hourlyDataArray.length(); i++) {
                    JSONObject hourlyData = hourlyDataArray.getJSONObject(
                        i);
                    // time in milliseconds to java Date
                    long time = hourlyData.getLong("dt") * 1000;

                    // get main data
                    JSONObject main = hourlyData.getJSONObject("main");
                    double temperature = main.getDouble("temp");
                    double feelsLike = main.getDouble("feels_like");
                    int pressure = main.getInt("pressure");
                    int humidity = main.getInt("humidity");

                    JSONObject weather = hourlyData.getJSONArray("weather")
                        .getJSONObject(0);
                    String mainWeather = weather.getString("main");
                    String description = weather.getString("description");

                    hourlyWeathers.add(
                        new HourlyWeather(time, temperature, feelsLike,
                            pressure, humidity, mainWeather, description,
                            idLocation));
                }
                JSONObject city = jsonResponse.getJSONObject("city");
                JSONObject coord = city.getJSONObject("coord");
                double lat = coord.getDouble("lat");
                double lon = coord.getDouble("lon");
                Geocoder geocoder = new Geocoder(MainActivity.this,
                    Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(lat, lon,
                    1);
                tvLocationName.setText(addresses.get(0).getSubAdminArea());
                // TODO: Bind data to view
                RecyclerViewAdapter rvAdapter = new RecyclerViewAdapter(
                    hourlyWeathers);
                recyclerView.setAdapter(rvAdapter);

                // TODO: add data to db
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
        }

        private void insertHourlyWeather(List<HourlyWeather> weatherList) {
            hourlyWeatherDao.insert(weatherList);
        }

        private void deleteHourlyWeatherByLocationId(int locationId) {
            hourlyWeatherDao.deleteByLocationId(locationId);
        }
    }

    private List<DailyWeather> mockDailyWeatherList() {
        List<DailyWeather> weatherList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            DailyWeather weather = new DailyWeather();
            weather.setTime(Instant.now().plus(i, ChronoUnit.DAYS).toEpochMilli());
            weather.setLastUpdate(Instant.now().toEpochMilli());
            weather.setMinTemp(getRandomNumber(15, 20));
            weather.setMaxTemp(weather.getMinTemp() + getRandomNumber(3, 8));
            if (getRandomNumber(0, 3) != 0) {
                weather.setWeather("Sunny");
            } else {
                weather.setWeather("Cloudy");
            }
            weatherList.add(weather);
        }
        return weatherList;
    }

    private int getRandomNumber(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }

    private void setUpDailyWeather(List<DailyWeather> dailyWeathers) {
        TableLayout tableLayout = findViewById(R.id.daily_weather);
        LayoutInflater inflater = getLayoutInflater();
        int maxTemp = getMaxTemp(dailyWeathers);
        int minTemp = getMinTemp(dailyWeathers);
        int maxTempDiff = maxTemp - minTemp;
        for (DailyWeather dailyWeather : dailyWeathers) {
            TableRow row = inflater.inflate(R.layout.daily_weather_layout, null,
                    false)
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
                (int) ((dailyWeather.getMaxTemp() - dailyWeather.getMinTemp())
                    / maxTempDiff * 100));
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
        return dailyWeathers.stream().map(
                dailyWeather -> (int) dailyWeather.getMaxTemp()).max(Integer::compareTo)
            .orElse(0);
    }

    private int getMinTemp(List<DailyWeather> dailyWeathers) {
        return dailyWeathers.stream().map(
                dailyWeather -> (int) dailyWeather.getMinTemp()).min(Integer::compareTo)
            .orElse(0);
    }

    private HashMap<String, Integer> getIconMap() {
        HashMap<String, Integer> iconMap = new HashMap<>();
        iconMap.put("Sunny", R.drawable.sun);
        iconMap.put("Cloudy", R.drawable.cloudy);
        return iconMap;
    }

    private String getDayOfWeek(String format, Date date) {
        return new SimpleDateFormat(format, Locale.ENGLISH).format(
            date);
    }

    private String currentHourAndMinute() {
        return getDayOfWeek("HH:mm", new Date());
    }
}