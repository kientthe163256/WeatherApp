package com.example.weatherapp.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.weatherapp.model.AppLocation;

import java.util.List;

@Dao
public abstract class LocationDao {
    @Query("SELECT * from AppLocation")
    public abstract List<AppLocation> getAllLocations();

    @Query("SELECT id from AppLocation where latitude = :latitude and longitude = :longitude")
    public abstract long getIdLocationBylatitudeAndlongitude(double latitude,double longitude);

    @Insert
    public abstract void insert(AppLocation appLocation);

    @Update
    public abstract void update(AppLocation... appLocations);

    @Delete
    public abstract void delete(AppLocation appLocation);

}
