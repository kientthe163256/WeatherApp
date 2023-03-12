package com.example.weatherapp.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class AppLocation {
    @PrimaryKey(autoGenerate = true)
    private long id;

    private String name;
    private double latitude;
    private double longitude;
    private String state;
    private String country;

    public AppLocation() {
    }

    public AppLocation(String name, double latitude, double longitude, String state, String country) {
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
        return "AppLocation{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", state='" + state + '\'' +
                ", country='" + country + '\'' +
                '}';
    }
}
