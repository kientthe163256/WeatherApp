package com.example.weatherapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.weatherapp.dao.AppDatabase;
import com.example.weatherapp.dao.DailyWeatherDao;
import com.example.weatherapp.dao.HourlyWeatherDao;
import com.example.weatherapp.dao.LocationDao;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private final int REQUEST_CODE = 100;
    private final int REQUEST_CHECK_SETTING = 1001;
    private final String APIKEY = "35957f6517fce7e6dca5158fc93ba98b";
    private final String HOURLY_WEATHER_URL = "https://pro.openweathermap.org/data/2.5/forecast/hourly";
    private final String DAILY_WEATHER_URL = "https://api.openweathermap.org/data/2.5/forecast/daily";
    private final String GEOCODING_URL = "http://api.openweathermap.org/geo/1.0/direct";

    public static com.example.weatherapp.model.Location location = new com.example.weatherapp.model.Location();

    AppDatabase db;
    LocationDao locationDao;
    HourlyWeatherDao hourlyWeatherDao;
    DailyWeatherDao dailyWeatherDao;
    Context context;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    public TextView tvLocationName;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "WeatherApp").allowMainThreadQueries().build();
        locationDao = db.locationDao();
        hourlyWeatherDao = db.hourlyWeatherDao();
        dailyWeatherDao = db.dailyWeatherDao();

        setContentView(R.layout.activity_main);

        context = getApplicationContext();
        //Recycler view weather by hour
        recyclerView = findViewById(R.id.weatherByHour);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);

        tvLocationName = findViewById(R.id.location);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
        /* Start app -> check permission -> check GPS:
            if on, get current location, call API/load from db (No internet)
            if off, request turn on GPS:
                user turn on -> get current location, call API/load db
                user not allow -> get default location
        * */
        checkLocationPermission();
    }

    public boolean hasInternetConnection() {
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getNetworkCapabilities(cm.getActiveNetwork()) != null;
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //permission is already granted
            checkGPS();
        } else {
            //request for permission
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
        }
    }

    private void checkGPS() {
        long timeInterval = 1000;
        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, timeInterval)
                .setWaitForAccurateLocation(true)
                .build();

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(getApplicationContext())
                .checkLocationSettings(builder.build());

        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    //when GPS is already on
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    getCurrentLocation();
                } catch (ApiException e) {
                    //when GPS is off
                    switch (e.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                //try to ask user turn on GPS
                                ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                                resolvableApiException.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTING);
                            } catch (IntentSender.SendIntentException ex) {
                                ex.printStackTrace();
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            //Device does not have location
                            break;
                    }
                }
            }
        });
    }

    //check after request location permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode==REQUEST_CODE){
            if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            }
            else {
                Toast.makeText(this, "LOCATION PERMISSION MUST BE GRANTED", Toast.LENGTH_LONG).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    //check after request turn on GPS
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode==REQUEST_CHECK_SETTING){
            if (resultCode == Activity.RESULT_OK){
                getCurrentLocation();
            }
            else {
                //user don't turn on GPS, set location to default location
                location = DefaultConfig.defaultLocation;
                getHourlyWeather(location.getLatitude(), location.getLongitude(), 24);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Please grant location permissions", Toast.LENGTH_SHORT).show();
            return;
        }
        fusedLocationProviderClient.getCurrentLocation(Priority.PRIORITY_LOW_POWER, null).addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                    List<Address> addresses = null;
                    try {
                        addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        Address address = addresses.get(0);
                        MainActivity.location.setLatitude(address.getLatitude());
                        MainActivity.location.setLongitude(location.getLongitude());
                        MainActivity.location.setName(address.getSubAdminArea());
                        MainActivity.location.setState(address.getAdminArea());
                        MainActivity.location.setCountry(address.getCountryName());
                        tvLocationName.setText(address.getSubAdminArea());

                        if (hasInternetConnection()){
                            getHourlyWeather(location.getLatitude(), location.getLongitude(), 24);
                        }

//                                insertLocation(MainActivity.location);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        })
        ;
    }

    private void insertLocation(@NonNull com.example.weatherapp.model.Location location){
        locationDao.insert(location);
    }

    public void getHourlyWeather(double lat, double lon, int cnt){
        String requestUrl = HOURLY_WEATHER_URL + "?lat=" + lat
                + "&lon=" + lon
                + "&cnt=" + cnt
                + "&appid=" + APIKEY;

        long idLocation = locationDao.getIdLocationBylatitudeAndlongitude(MainActivity.location.getLatitude(), MainActivity.location.getLongitude());
        List<HourlyWeather> hourlyWeathers = new ArrayList<>();

        StringRequest stringRequest = new StringRequest(Request.Method.GET, requestUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
//                                List<HourlyWeather> hourlyWeathers = new ArrayList<>();
                            JSONObject jsonResponse = new JSONObject(response);
                            JSONArray hourlyDataArray = jsonResponse.getJSONArray("list");
                            for (int i = 0; i < hourlyDataArray.length(); i++) {
                                JSONObject hourlyData = hourlyDataArray.getJSONObject(i);
                                //time in milliseconds to java Date
                                long time = hourlyData.getLong("dt") * 1000;

                                //get main data
                                JSONObject main = hourlyData.getJSONObject("main");
                                double temperature = main.getDouble("temp");
                                double feelsLike = main.getDouble("feels_like");
                                int pressure = main.getInt("pressure");
                                int humidity = main.getInt("humidity");

                                JSONObject weather = hourlyData.getJSONArray("weather").getJSONObject(0);
                                String mainWeather = weather.getString("main");
                                String description = weather.getString("description");

                                hourlyWeathers.add(new HourlyWeather(time, temperature, feelsLike, pressure, humidity, mainWeather, description,idLocation));
                            }
                            Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                            List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
                            tvLocationName.setText(addresses.get(0).getSubAdminArea());
                            //TODO: Bind data to view
                            RecyclerViewAdapter rvAdapter = new RecyclerViewAdapter(hourlyWeathers);
                            recyclerView.setAdapter(rvAdapter);

                            //TODO: add data to db
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("volley", error.toString());
            }
        }
        );
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);
    }

    public void btnTest(View view){
        List<HourlyWeather> hourlyWeathers = hourlyWeatherDao.getAllLocations();
        System.out.println(hourlyWeathers.size());
        TextView tvTest = findViewById(R.id.tvTest);
        String s = "";
        for (int i = 0; i < hourlyWeathers.size(); i++) {
            s += hourlyWeathers.get(i).toString();
        }
        tvTest.setText(s);
    }
}