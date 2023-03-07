package com.example.weatherapp.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.weatherapp.model.DailyWeather;
import com.example.weatherapp.model.HourlyWeather;

import java.util.List;

@Dao
public interface HourlyWeatherDao {
    @Query("SELECT * from HourlyWeather")
    List<HourlyWeather> getAll();

    @Insert
    void insertAll(List<HourlyWeather> weatherList);
}
