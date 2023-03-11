package com.example.weatherapp.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.example.weatherapp.model.DailyWeather;
import com.example.weatherapp.model.Location;

import java.util.List;

@Dao
public abstract class LocationDao {
    @Query("SELECT * from Location")
    public abstract List<Location> getAllLocations();

    @Insert
    public abstract void insert(Location location);

    @Update
    public abstract void update(Location... locations);

    @Delete
    public abstract void delete(Location location);

}
