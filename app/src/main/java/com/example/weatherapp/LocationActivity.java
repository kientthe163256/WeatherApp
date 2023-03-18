package com.example.weatherapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Response;
import com.example.weatherapp.dao.AppDatabase;
import com.example.weatherapp.dao.LocationDao;
import com.example.weatherapp.model.AppLocation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LocationActivity extends AppCompatActivity {
    AppDatabase db;
    LocationDao locationDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.location_activity);

        db = Room.databaseBuilder(getApplicationContext(), AppDatabase .class, "WeatherApp").allowMainThreadQueries().build();
        locationDao = db.locationDao();

        List<AppLocation> appLocationList = locationDao.getAllLocations();
        TextView textView = findViewById(R.id.tv_location);
        String text = "";
        for (int i = 0; i < appLocationList.size(); i++) {
            text += appLocationList.get(i).toString();
        }
        textView.setText(text);

        findViewById(R.id.btnDelete).setOnClickListener(
            v -> locationDao.delete(appLocationList.get(0)));
    }

    Response.Listener<String> locationListener = response -> {
        try {
            JSONArray jsonArray = new JSONArray(response);
            List<AppLocation> appLocationList = AppLocation.fromJsonArray(jsonArray);
            // TODO: add data to db

        } catch (JSONException e) {
            e.printStackTrace();
        }
    };

}