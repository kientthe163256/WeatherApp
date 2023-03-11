package com.example.weatherapp.service;

import android.content.Context;
import android.net.ConnectivityManager;
import android.widget.Toast;

import androidx.room.Room;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.weatherapp.MainActivity;
import com.example.weatherapp.dao.AppDatabase;
import com.example.weatherapp.model.DailyWeather;
import com.example.weatherapp.model.HourlyWeather;
import com.example.weatherapp.model.Location;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ApiService {
    AppDatabase db;
    private Context context;
    private final String apiKey = "35957f6517fce7e6dca5158fc93ba98b";
    private final String hourlyWeatherUrl = "https://pro.openweathermap.org/data/2.5/forecast/hourly";
    private final String dailyWeatherUrl = "https://api.openweathermap.org/data/2.5/forecast/daily";
    private final String geocodingUrl = "http://api.openweathermap.org/geo/1.0/direct";

    public ApiService(Context context) {
        this.context = context;
        this.db = Room.databaseBuilder(context, AppDatabase.class, "DemoRoom").allowMainThreadQueries().build();
    }

    public void getHourlyWeather(double latitude, double longitude, int numberOfHours) {
        if (!hasInternetConnection()) {
            Toast.makeText(context, "Please check Internet connection!", Toast.LENGTH_SHORT).show();
            return;
        }
        String requestUrl = hourlyWeatherUrl + "?lat=" + latitude
                + "&lon=" + longitude
                + "&cnt=" + numberOfHours
                + "&appid=" + apiKey;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, requestUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            JSONArray hourlyDataArray = jsonResponse.getJSONArray("list");
                            List<HourlyWeather> weatherList = new ArrayList<>();
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

                                weatherList.add(new HourlyWeather(time, temperature, feelsLike, pressure, humidity, mainWeather, description));
                            }
                            //TODO: add data to db

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context, error.toString(), Toast.LENGTH_SHORT).show();
            }
        }
        );
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);
    }

    public void getDailyWeather(double latitude, double longitude, int numberOfDays) {
        if (!hasInternetConnection()) {
            Toast.makeText(context, "Please check Internet connection!", Toast.LENGTH_SHORT).show();
            return;
        }
        String requestUrl = dailyWeatherUrl + "?lat=" + latitude
                + "&lon=" + longitude
                + "&cnt=" + numberOfDays
                + "&appid=" + apiKey;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, requestUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            JSONArray dailyDataArray = jsonResponse.getJSONArray("list");
                            List<DailyWeather> weatherList = new ArrayList<>();
                            for (int i = 0; i < dailyDataArray.length(); i++) {
                                JSONObject dailyData = dailyDataArray.getJSONObject(i);
                                //time in milliseconds to java Date
                                long time = dailyData.getLong("dt") * 1000;
                                long sunrise = dailyData.getLong("sunrise") * 1000;
                                long sunset = dailyData.getLong("sunset") * 1000;

                                //get main data
                                JSONObject temp = dailyData.getJSONObject("temp");
                                double minTemp = temp.getDouble("min");
                                double maxTemp = temp.getDouble("max");
                                //in API response: weather is an array
                                JSONObject weather = dailyData.getJSONArray("weather").getJSONObject(0);
                                String mainWeather = weather.getString("main");
                                String description = weather.getString("description");

                                weatherList.add(new DailyWeather(time, sunrise, sunset, minTemp, maxTemp, mainWeather, description));
                            }
                            //TODO: add data to db
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context, error.toString(), Toast.LENGTH_SHORT).show();
            }
        }
        );
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);
    }

    public void getGeocoding(String locationName) {
        if (!hasInternetConnection()) {
            Toast.makeText(context, "Please check Internet connection!", Toast.LENGTH_SHORT).show();
            return;
        }
        String requestUrl = geocodingUrl + "?q=" + locationName
                + "&limit=5"
                + "&appid=" + apiKey;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, requestUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            List<Location> locationList = new ArrayList<>();
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject location = jsonArray.getJSONObject(i);
                                String name = location.getString("name");
                                double latitude = location.getDouble("lat");
                                double longitude = location.getDouble("lon");
                                String state = location.getString("state");
                                String country = location.getString("country");

                                locationList.add(new Location(name, latitude, longitude, state, country));
                            }
                            //TODO: add data to db
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context, error.toString(), Toast.LENGTH_SHORT).show();
            }
        }
        );
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);
    }


    public boolean hasInternetConnection() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getNetworkCapabilities(cm.getActiveNetwork()) != null;
    }
}