package com.example.weatherapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

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

        //List view weather by day
//        ListViewAdapter lvAdapter = new ListViewAdapter(this, weatherList);

//        ListView listView = findViewById(R.id.weatherByDay);
//        listView.setAdapter(lvAdapter);

        initTable(weatherList);
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

    public void initTable(List<Weather> weatherList) {
        for (int i = 0; i < weatherList.size(); i++) {
            TableLayout tableLayout = findViewById(R.id.tableLayout);
            TableRow row = new TableRow(MainActivity.this);
            TextView day = new TextView(MainActivity.this);
            day.setText("Friday");
            row.addView(day);
            TextView humidity = new TextView(MainActivity.this);
            humidity.setText(Integer.toString((int) weatherList.get(i).getHumidity()));
            row.addView(humidity);
            ImageView dayIcon = new ImageView(MainActivity.this);
            dayIcon.setImageResource(R.drawable.sun);
            TableLayout.LayoutParams params = new TableLayout.LayoutParams(25,25);
            dayIcon.setLayoutParams(params);
            row.addView(dayIcon);
            ImageView nightIcon = new ImageView(MainActivity.this);
            params = new TableLayout.LayoutParams(20, 20);
            nightIcon.setImageResource(R.drawable.moon);
            nightIcon.setLayoutParams(params);
            row.addView(nightIcon);
            tableLayout.addView(row);
        }
    }


}