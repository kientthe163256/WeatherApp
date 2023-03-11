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
public class DailyWeather {
    @PrimaryKey(autoGenerate = true)
    private long id;

    private long time;

    private long sunrise;

    private long sunset;

    @ColumnInfo(name = "min_temp")
    private double minTemp;

    @ColumnInfo(name = "max_temp")
    private double maxTemp;

    private String weather;

    private String description;

    @ColumnInfo(name = "location_id")
    private long locationId;

    @ColumnInfo(name = "last_update")
    private long lastUpdate;

    public DailyWeather() {
        Date now = new Date();
        lastUpdate = now.getTime();
    }

    public DailyWeather(long time, long sunrise, long sunset, double minTemp, double maxTemp, String weather, String description) {
        this.time = time;
        this.sunrise = sunrise;
        this.sunset = sunset;
        this.minTemp = minTemp;
        this.maxTemp = maxTemp;
        this.weather = weather;
        this.description = description;
        Date now = new Date();
        lastUpdate = now.getTime();
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

    public long getSunrise() {
        return sunrise;
    }

    public void setSunrise(long sunrise) {
        this.sunrise = sunrise;
    }

    public long getSunset() {
        return sunset;
    }

    public void setSunset(long sunset) {
        this.sunset = sunset;
    }

    public double getMinTemp() {
        return minTemp;
    }

    public void setMinTemp(double minTemp) {
        this.minTemp = minTemp;
    }

    public double getMaxTemp() {
        return maxTemp;
    }

    public void setMaxTemp(double maxTemp) {
        this.maxTemp = maxTemp;
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

    public long getLocationId() {
        return locationId;
    }

    public void setLocationId(long locationId) {
        this.locationId = locationId;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}
