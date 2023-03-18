package com.example.weatherapp.service;

import static com.example.weatherapp.DefaultConfig.NUMBER_OF_DAYS;
import static com.example.weatherapp.DefaultConfig.NUMBER_OF_HOURS;

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
import com.example.weatherapp.DefaultConfig;
import com.example.weatherapp.dao.AppDatabase;
import com.example.weatherapp.model.DailyWeather;
import com.example.weatherapp.model.HourlyWeather;
import com.example.weatherapp.model.AppLocation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ApiService {
    private Context context;
    private final String apiKey = "35957f6517fce7e6dca5158fc93ba98b";
    private final String hourlyWeatherUrl = "https://pro.openweathermap.org/data/2.5/forecast/hourly";
    private final String dailyWeatherUrl = "https://api.openweathermap.org/data/2.5/forecast/daily";
    private final String geocodingUrl = "http://api.openweathermap.org/geo/1.0/direct";

    public ApiService(Context context) {
        this.context = context;
    }

    public void getHourlyWeather(double latitude, double longitude,
        Response.Listener<String> responseListener) {
        String requestUrl =
            hourlyWeatherUrl + "?lat=" + latitude + "&lon=" + longitude + "&cnt=" + NUMBER_OF_HOURS
                + "&appid=" + apiKey;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, requestUrl,
            responseListener,
            error -> Toast.makeText(context, error.toString(), Toast.LENGTH_SHORT).show());
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);
    }

    public void getDailyWeather(double latitude, double longitude,
        Response.Listener<String> dailyListener) {
        String requestUrl =
            dailyWeatherUrl + "?lat=" + latitude + "&lon=" + longitude + "&cnt=" + NUMBER_OF_DAYS
                + "&appid=" + apiKey;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, requestUrl,
            dailyListener,
            error -> Toast.makeText(context, error.toString(), Toast.LENGTH_SHORT).show());
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);
    }

    public void getGeocodingTest(String locationName,
                              Response.Listener<String> locationListener) {
        if (!hasInternetConnection()) {
            Toast.makeText(context, "Please check Internet connection!", Toast.LENGTH_SHORT).show();
            return;
        }
        String requestUrl = geocodingUrl + "?q=" + locationName + "&appid=" + apiKey;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, requestUrl,
                locationListener,
                error -> Toast.makeText(context, error.toString(), Toast.LENGTH_SHORT).show());
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);
    }



    public void getGeocoding(String locationName) {
        if (!hasInternetConnection()) {
            Toast.makeText(context, "Please check Internet connection!", Toast.LENGTH_SHORT).show();
            return;
        }
        String requestUrl = geocodingUrl + "?q=" + locationName + "&appid=" + apiKey;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, requestUrl,
            response -> {
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    List<AppLocation> appLocationList = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject location = jsonArray.getJSONObject(i);
                        String name = location.getString("name");
                        double latitude = location.getDouble("lat");
                        double longitude = location.getDouble("lon");
                        String state = location.getString("state");
                        String country = location.getString("country");

                        appLocationList.add(
                            new AppLocation(name, latitude, longitude, state, country));
                    }
                    // TODO: add data to db

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context, error.toString(), Toast.LENGTH_SHORT).show();
            }
        });
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);
    }


    private boolean hasInternetConnection() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(
            Context.CONNECTIVITY_SERVICE);
        return cm.getNetworkCapabilities(cm.getActiveNetwork()) != null;
    }
}