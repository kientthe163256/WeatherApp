package com.example.weatherapp;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.weatherapp.adapter.RecyclerViewAdapter;
import com.example.weatherapp.dao.AppDatabase;
import com.example.weatherapp.model.DailyWeather;
import com.example.weatherapp.model.HourlyWeather;
import com.example.weatherapp.service.ApiService;
import com.example.weatherapp.util.UIUpdater;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

  AppDatabase db;
  UIUpdater timeUIUpdater;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    List<HourlyWeather> hourlyWeathers = mockWeatherList();
    List<DailyWeather> dailyWeathers = mockDailyWeatherList();

    int currentTemp = (int) hourlyWeathers.get(0).getTemperature();
    setUpMainInfo(currentTemp);
    setUpHourlyWeather(hourlyWeathers);
    setUpDailyWeather(dailyWeathers);

    Button button = findViewById(R.id.testBtn);
    button.setOnClickListener(v -> {
      ApiService apiService = new ApiService(getApplicationContext());
      apiService.getDailyWeather(20.2545421, 105.9764854, 7);
    });
  }

  private void setUpHourlyWeather(List<HourlyWeather> hourlyWeathers) {
    RecyclerView recyclerView = findViewById(R.id.weatherByHour);
    LinearLayoutManager layoutManager = new LinearLayoutManager(this,
        LinearLayoutManager.HORIZONTAL, false);
    recyclerView.setLayoutManager(layoutManager);

    RecyclerViewAdapter rvAdapter = new RecyclerViewAdapter(hourlyWeathers);
    recyclerView.setAdapter(rvAdapter);
  }

  private void setUpMainInfo(int currentTemp) {
    TextView currentTempView = findViewById(R.id.temperature);
    currentTempView.setText(currentTemp + "°");

    TextView dateTime = findViewById(R.id.date_time);
    String dayOfWeek = getDayOfWeek("EE", new Date());
    timeUIUpdater = new UIUpdater(() -> {
      dateTime.setText(dayOfWeek + ", " + currentHourAndMinute());
    }, 1000);
    timeUIUpdater.startUpdates();
  }

  private String getDayOfWeek(String format, Date date) {
    return new SimpleDateFormat(format, Locale.ENGLISH).format(
        date);
  }

  private String currentHourAndMinute() {
    return getDayOfWeek("HH:mm", new Date());
  }

  private void setUpDailyWeather(List<DailyWeather> dailyWeathers) {
    TableLayout tableLayout = findViewById(R.id.daily_weather);
    LayoutInflater inflater = getLayoutInflater();
    int maxTemp = getMaxTemp(dailyWeathers);
    int minTemp = getMinTemp(dailyWeathers);
    int maxTempDiff = maxTemp - minTemp;
    for (DailyWeather dailyWeather : dailyWeathers) {
      TableRow row = inflater.inflate(R.layout.daily_weather_layout, null,
              false)
          .findViewById(R.id.row_day);
      TextView date = row.findViewById(R.id.row_day_day);
      String dateStr = getDayOfWeek("EEEE", new Date(dailyWeather.getTime()));
      date.setText(dateStr);
      ImageView icon = row.findViewById(R.id.row_day_icon);
      HashMap<String, Integer> iconMap = getIconMap();
      iconMap.put("Cloudy", R.drawable.cloudy);
      iconMap.put("Sunny", R.drawable.sun);
      icon.setImageResource(
          iconMap.getOrDefault(dailyWeather.getWeather(), R.drawable.cloudy));

      View tempDiff = row.findViewById(R.id.row_day_range_fg);
      ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) tempDiff.getLayoutParams();
      params.leftMargin = dpToPx(
          (int) ((dailyWeather.getMinTemp() - minTemp) / maxTempDiff * 100));
      int width = dpToPx(
          (int) ((dailyWeather.getMaxTemp() - dailyWeather.getMinTemp())
              / maxTempDiff * 100));
      params.width = dpToPx(width);
      tempDiff.setLayoutParams(params);

      TextView minTempView = row.findViewById(R.id.row_day_min_temp);
      minTempView.setText(String.valueOf((int) dailyWeather.getMinTemp()));

      TextView maxTempView = row.findViewById(R.id.row_day_max_temp);
      maxTempView.setText(String.valueOf((int) dailyWeather.getMaxTemp()));
      tableLayout.addView(row);
    }
  }

  private int dpToPx(int dp) {
    return dp * (getResources().getDisplayMetrics().densityDpi
        / DisplayMetrics.DENSITY_DEFAULT);
  }

  private int getMaxTemp(List<DailyWeather> dailyWeathers) {
    return dailyWeathers.stream().map(
            dailyWeather -> (int) dailyWeather.getMaxTemp()).max(Integer::compareTo)
        .orElse(0);
  }

  private int getMinTemp(List<DailyWeather> dailyWeathers) {
    return dailyWeathers.stream().map(
            dailyWeather -> (int) dailyWeather.getMinTemp()).min(Integer::compareTo)
        .orElse(0);
  }

  private HashMap<String, Integer> getIconMap() {
    HashMap<String, Integer> iconMap = new HashMap<>();
    iconMap.put("Sunny", R.drawable.sun);
    iconMap.put("Cloudy", R.drawable.cloudy);
    return iconMap;
  }

  private List<HourlyWeather> mockWeatherList() {
    List<HourlyWeather> weatherList = new ArrayList<>();
    for (int i = 0; i < 20; i++) {
      HourlyWeather weather = new HourlyWeather();
      weather.setTemperature(getRandomNumber(20, 30));
      weather.setHumidity(getRandomNumber(20, 30));
      weather.setTime(Instant.now().plus(i, ChronoUnit.HOURS).toEpochMilli());
      weather.setFeelsLike(getRandomNumber(20, 30));
      weather.setPressure(getRandomNumber(20, 30));
      weather.setHumidity(getRandomNumber(40, 80));
      if (getRandomNumber(0, 2) == 0) {
        weather.setWeather("Sunny");
      } else {
        weather.setWeather("Cloudy");
      }
      weatherList.add(weather);
    }
    return weatherList;
  }

  private List<DailyWeather> mockDailyWeatherList() {
    List<DailyWeather> weatherList = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      DailyWeather weather = new DailyWeather();
      weather.setTime(Instant.now().plus(i, ChronoUnit.DAYS).toEpochMilli());
      weather.setLastUpdate(Instant.now().toEpochMilli());
      weather.setMinTemp(getRandomNumber(15, 20));
      weather.setMaxTemp(weather.getMinTemp() + getRandomNumber(3, 8));
      if (getRandomNumber(0, 3) != 0) {
        weather.setWeather("Sunny");
      } else {
        weather.setWeather("Cloudy");
      }
      weatherList.add(weather);
    }
    return weatherList;
  }

  private int getRandomNumber(int min, int max) {
    return (int) ((Math.random() * (max - min)) + min);
  }
}