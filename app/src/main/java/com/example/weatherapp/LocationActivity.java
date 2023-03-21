package com.example.weatherapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.example.weatherapp.adapter.LocationListAdapter;
import com.example.weatherapp.adapter.RecycleViewLocation;
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
    private ArrayList<AppLocation> appLocations = new ArrayList();
    LocationListAdapter adapter;
    private TextView searchView;
    private SearchView editSearch;
    private ListView listView;
    private Dialog dialog;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.location_activity);

        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "WeatherApp").allowMainThreadQueries().build();
        context = getApplicationContext();
        locationDao = db.locationDao();
        apiService = new ApiService(getApplicationContext());
        searchView = findViewById(R.id.searchTxt);
        apiService.getGeocodingTest("hanoi", locationListener);
        searchView.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        // dialog
                        dialog = new Dialog(LocationActivity.this);
                        dialog.setContentView(R.layout.dialog_searchable_spinner);
                        dialog.getWindow().setLayout(650, 800);
                        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        dialog.show();
                        //
                        editSearch = dialog.findViewById(R.id.search);
                        listView = dialog.findViewById(R.id.list_location);
                        listView.setOnItemClickListener((parent, view, position, id) -> {
                            System.out.println(((AppLocation) parent.getAdapter().getItem(position)).getName());
                            AppLocation appLocation = (AppLocation) parent.getAdapter().getItem(position);
                            List<AppLocation> savedLocations = locationDao.getAllLocations();
                            savedLocations.stream().filter(o -> o.getName().equals(appLocation.getName())).forEach(o -> locationDao.delete(o));
                            locationDao.insert(appLocation);
                            AppLocation savedLocation = locationDao.getLocationByName(appLocation.getName());
                            Intent intent = new Intent(LocationActivity.this, MainActivity.class);
                            intent.putExtra("current_location", savedLocation);
                            setResult(RESULT_OK, intent);
                            finish();
                        });

                        editSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                            @Override
                            public boolean onQueryTextSubmit(String query) {
                                return false;
                            }

                            @Override
                            public boolean onQueryTextChange(String newText) {
                                String text = newText;
                                Response.Listener<String> locationListener2 = response -> {
                                    try {
                                        JSONArray jsonArray = new JSONArray(response);
                                        appLocations = (ArrayList<AppLocation>) AppLocation.fromJsonArray(jsonArray);
                                        // Pass results to ListViewAdapter Class
                                        adapter = new LocationListAdapter(context, appLocations);
                                        adapter.notifyDataSetChanged();
                                        // Binds the Adapter to the ListView
                                        listView.setAdapter(adapter);
                                        // TODO: add data to db
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                };
                                apiService.getGeocodingTest(text, locationListener2);
                                return false;
                            }
                        });
                    }
                });
        RecyclerView recycleViewLocation = findViewById(R.id.rcv_saved_locations);
        RecycleViewLocation recycleViewLocationAdapter = new RecycleViewLocation(locationDao.getAllLocations(), this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recycleViewLocation.setLayoutManager(linearLayoutManager);
        recycleViewLocation.setAdapter(recycleViewLocationAdapter);
    }

    Response.Listener<String> locationListener = response -> {
        try {
            JSONArray jsonArray = new JSONArray(response);
            appLocations = (ArrayList<AppLocation>) AppLocation.fromJsonArray(jsonArray);
            System.out.println(appLocations);
            // TODO: add data to db
        } catch (JSONException e) {
            e.printStackTrace();
        }
    };

}