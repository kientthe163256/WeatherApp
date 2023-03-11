package com.example.weatherapp.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.weatherapp.model.DailyWeather;
import com.example.weatherapp.model.HourlyWeather;

import java.util.List;

@Dao
public interface HourlyWeatherDao {
    @Query("SELECT * from HourlyWeather where location_id = :locationId and time between :fromTime and :toTime")
    List<HourlyWeather> getByLocationId(int locationId, long fromTime, long toTime);

    @Insert
    void insert(List<HourlyWeather> weatherList);
}
