package com.example.weatherapp.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.weatherapp.model.DailyWeather;
import com.example.weatherapp.model.HourlyWeather;

import java.util.List;

@Dao
public interface DailyWeatherDao {
    @Query("SELECT * from DailyWeather where location_id = :locationId and time between :fromTime and :toTime")
    List<DailyWeather> getByLocationId(int locationId, long fromTime, long toTime);

    @Insert
    void insert(List<DailyWeather> weatherList);
}
