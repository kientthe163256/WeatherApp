package com.example.weatherapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.TextView;

import com.android.volley.Response;
import com.example.weatherapp.dao.AppDatabase;
import com.example.weatherapp.dao.LocationDao;
import com.example.weatherapp.model.AppLocation;
import com.example.weatherapp.service.ApiService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LocationActivity extends AppCompatActivity {
    AppDatabase db;
    LocationDao locationDao;
    ApiService apiService;

    private SearchView searchView;
    private ArrayList<AppLocation> appLocations = new ArrayList();
    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.location_activity);

        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "WeatherApp").allowMainThreadQueries().build();
        locationDao = db.locationDao();
        apiService = new ApiService(getApplicationContext());
//        List<AppLocation> appLocationList = locationDao.getAllLocations();
//        TextView textView = findViewById(R.id.tv_location);
//        String text = "";
//        for (int i = 0; i < appLocationList.size(); i++) {
//            text += appLocationList.get(i).toString();
//        }
//        textView.setText(text);
//
//        findViewById(R.id.btnDelete).setOnClickListener(
//            v -> locationDao.delete(appLocationList.get(0)));
        editText = findViewById(R.id.searchTxt);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String searchContent = editText.getText().toString();
                apiService.getGeocodingTest(searchContent, locationListener);
            }
        });
    }

    Response.Listener<String> locationListener = response -> {
        try {
            JSONArray jsonArray = new JSONArray(response);
            appLocations = (ArrayList<AppLocation>) AppLocation.fromJsonArray(jsonArray);
            // TODO: add data to db

        } catch (JSONException e) {
            e.printStackTrace();
        }
    };

}