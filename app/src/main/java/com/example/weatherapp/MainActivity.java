package com.example.weatherapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.example.weatherapp.dao.AppDatabase;
import com.example.weatherapp.dao.DailyWeatherDao;
import com.example.weatherapp.model.DailyWeather;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        List<Weather> weatherList = mockWeatherList();
        //Recycler view weather by hour
        RecyclerView recyclerView = findViewById(R.id.weatherByHour);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);

        RecyclerViewAdapter rvAdapter = new RecyclerViewAdapter(weatherList);
        recyclerView.setAdapter(rvAdapter);


//        db = Room.databaseBuilder(getApplicationContext(), AppDatabase .class, "DemoRoom").allowMainThreadQueries().build();
        Button button = findViewById(R.id.testBtn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ApiService apiService = new ApiService(getApplicationContext());
//                apiService.getHourlyWeather(20.2545421, 105.9764854, 24);
                apiService.getDailyWeather(20.2545421, 105.9764854, 7);
//                apiService.getGeocoding("Thanh Hoa");
            }
        });
    }

    private List<Weather> mockWeatherList() {
        List<Weather> weatherList = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            Weather weather = new Weather();
            weather.setTime(new Date());
            weather.setTemperature(getRandomNumber(5, 32));
            weather.setHumidity(getRandomNumber(1, 80));
            weatherList.add(weather);
        }
        return weatherList;
    }

    private int getRandomNumber(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }






}