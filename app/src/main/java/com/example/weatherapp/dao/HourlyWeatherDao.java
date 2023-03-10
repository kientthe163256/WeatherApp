package com.example.weatherapp.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.weatherapp.model.HourlyWeather;

import java.util.List;

@Dao
public interface HourlyWeatherDao {
    @Query("SELECT * from HourlyWeather where location_id = :locationId and time between :fromTime and :toTime")
    List<HourlyWeather> getByLocationId(int locationId, long fromTime, long toTime);

    @Insert
    void insert(List<HourlyWeather> weatherList);

    @Query("DELETE from HourlyWeather where location_id = :locationId")
     void deleteByLocationId(int locationId);

    //    Test HourlyWeather DB
    @Query("SELECT * from HourlyWeather where location_id = :locationId")
    List<HourlyWeather> getByLocationIdTest(int locationId);

    @Query("Select * from HourlyWeather")
    List<HourlyWeather> getAllLocations();
}
