package com.example.weatherapp.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.weatherapp.model.DailyWeather;
import com.example.weatherapp.model.Location;

import java.util.List;

@Dao
public interface LocationDao {
    @Query("SELECT * from DailyWeather")
    List<Location> getAll();

    @Insert
    void insertAll(List<Location> locationList);
}
