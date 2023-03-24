package com.example.weatherapp.model;

import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
public class AppLocation implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String name;
    private double latitude;
    private double longitude;
    private String state;
    private String country;

    public AppLocation() {
    }

    public AppLocation(String name, double latitude, double longitude, String state,
        String country) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.state = state;
        this.country = country;
    }

    public AppLocation(long id, String name, double latitude, double longitude, String state,
        String country) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.state = state;
        this.country = country;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    @Override
    public String toString() {
        return "AppLocation{" + "id=" + id + ", name='" + name + '\'' + ", latitude=" + latitude
            + ", longitude=" + longitude + ", state='" + state + '\'' + ", country='" + country
            + '\'' + '}';
    }

    public static AppLocation fromJson(JSONObject locationData) throws JSONException {
        String name = locationData.getString("name");
        double latitude = locationData.getDouble("lat");
        double longitude = locationData.getDouble("lon");
        String state = "";
        if (locationData.has("state")) {
            state = locationData.getString("state");
        }
        String country = locationData.getString("country");

        return new AppLocation(name, latitude, longitude, state, country);
    }

    public static List<AppLocation> fromJsonArray(JSONArray locationDataArray)
        throws JSONException {
        List<AppLocation> locationWeathers = new ArrayList<>();
        for (int i = 0; i < locationDataArray.length(); i++) {
            JSONObject locationData = locationDataArray.getJSONObject(i);
            AppLocation locationWeather = AppLocation.fromJson(locationData);
            locationWeathers.add(locationWeather);
        }
        return locationWeathers;
    }
}
