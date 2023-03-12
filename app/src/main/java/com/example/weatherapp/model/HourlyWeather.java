package com.example.weatherapp.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(foreignKeys = {@ForeignKey(entity = Location.class,
        parentColumns = "id",
        childColumns = "location_id",
        onDelete = ForeignKey.CASCADE)})
public class HourlyWeather {
    @PrimaryKey(autoGenerate = true)
    private long id;

    private long time;

    private double temperature;

    @ColumnInfo(name = "feels_like")
    private double feelsLike;

    private int pressure;

    private int humidity;

    private String weather;

    private String description;

    @ColumnInfo(name = "last_update")
    private long lastUpdate;

    @ColumnInfo(name = "location_id")
    private long locationId;

    public HourlyWeather() {
        Date now = new Date();
        this.lastUpdate = now.getTime();
    }

    public HourlyWeather(long time, double temperature, double feelsLike, int pressure, int humidity, String weather, String description) {
        this.time = time;
        this.temperature = temperature;
        this.feelsLike = feelsLike;
        this.pressure = pressure;
        this.humidity = humidity;
        this.weather = weather;
        this.description = description;
        Date now = new Date();
        this.lastUpdate = now.getTime();
    }

    public HourlyWeather(long time, double temperature, double feelsLike, int pressure, int humidity, String weather, String description,long locationId) {
        this.time = time;
        this.temperature = temperature;
        this.feelsLike = feelsLike;
        this.pressure = pressure;
        this.humidity = humidity;
        this.weather = weather;
        this.description = description;
        this.locationId = locationId;
        Date now = new Date();
        this.lastUpdate = now.getTime();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getFeelsLike() {
        return feelsLike;
    }

    public void setFeelsLike(double feelsLike) {
        this.feelsLike = feelsLike;
    }

    public int getPressure() {
        return pressure;
    }

    public void setPressure(int pressure) {
        this.pressure = pressure;
    }

    public int getHumidity() {
        return humidity;
    }

    public void setHumidity(int humidity) {
        this.humidity = humidity;
    }

    public String getWeather() {
        return weather;
    }

    public void setWeather(String weather) {
        this.weather = weather;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public long getLocationId() {
        return locationId;
    }

    public void setLocationId(long locationId) {
        this.locationId = locationId;
    }

    @Override
    public String toString() {
        return "HourlyWeather{" +
                "id=" + id +
                ", time=" + time +
                ", temperature=" + temperature +
                ", feelsLike=" + feelsLike +
                ", pressure=" + pressure +
                ", humidity=" + humidity +
                ", weather='" + weather + '\'' +
                ", description='" + description + '\'' +
                ", lastUpdate=" + lastUpdate +
                ", locationId=" + locationId +
                '}';
    }
}
