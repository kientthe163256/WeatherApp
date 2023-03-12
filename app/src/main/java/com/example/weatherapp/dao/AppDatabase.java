package com.example.weatherapp.dao;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.weatherapp.model.DailyWeather;
import com.example.weatherapp.model.HourlyWeather;
import com.example.weatherapp.model.Location;

@Database(entities = {DailyWeather.class, HourlyWeather.class, Location.class}, version = 2, exportSchema = true)
public abstract class AppDatabase extends RoomDatabase {
    public abstract DailyWeatherDao dailyWeatherDao();
}