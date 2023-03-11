package com.example.weatherapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.weatherapp.dao.AppDatabase;
import com.example.weatherapp.dao.LocationDao;
import com.example.weatherapp.model.Location;

import java.util.List;
import java.util.stream.Collectors;

public class LocationActivity extends AppCompatActivity {
    AppDatabase db;
    LocationDao locationDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.location_activity);

        db = Room.databaseBuilder(getApplicationContext(), AppDatabase .class, "WeatherApp").allowMainThreadQueries().build();
        locationDao = db.locationDao();

        List<Location> locationList = locationDao.getAllLocations();
        TextView textView = findViewById(R.id.tv_location);
        String text = "";
        for (int i = 0; i < locationList.size(); i++) {
            text += locationList.get(i).toString();
        }
        textView.setText(text);

        findViewById(R.id.btnDelete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationDao.delete(locationList.get(0));
            }
        });
    }

}