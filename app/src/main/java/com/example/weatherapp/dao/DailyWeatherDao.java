package com.example.weatherapp.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.weatherapp.model.DailyWeather;

import java.util.List;

@Dao
public interface DailyWeatherDao {
    @Query("SELECT * from DailyWeather")
    List<DailyWeather> getAll();

    @Insert
    void insertAll(List<DailyWeather> weatherList);
}
