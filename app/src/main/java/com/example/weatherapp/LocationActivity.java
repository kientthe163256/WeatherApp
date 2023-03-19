package com.example.weatherapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
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

    private TextView searchView;
    private ArrayList<AppLocation> appLocations = new ArrayList();
    private EditText editText;
    private ListView listView;
    
    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.location_activity);

        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "WeatherApp").allowMainThreadQueries().build();
        locationDao = db.locationDao();
        apiService = new ApiService(getApplicationContext());
        searchView = findViewById(R.id.searchTxt);
        searchView.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog = new Dialog(LocationActivity.this);
                        dialog.setContentView(R.layout.dialog_searchable_spinner);
                        dialog.getWindow().setLayout(650, 800);
                        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        dialog.show();
                        editText = dialog.findViewById(R.id.search_edit_text);
                        listView = dialog.findViewById(R.id.list_location);
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
                                apiService.getGeocodingTest(searchContent,locationListener);
                            }
                        });
                        // viet cai list view o day
                    }
                });



    }

    Response.Listener<String> locationListener = response -> {
        try {
            JSONArray jsonArray = new JSONArray(response);
            appLocations = (ArrayList<AppLocation>) AppLocation.fromJsonArray(jsonArray);
            // TODO: add data to db
            System.out.println(appLocations);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    };

}