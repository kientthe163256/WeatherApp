package com.example.weatherapp;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherapp.model.HourlyWeather;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

  private final List<HourlyWeather> weatherList;

  public RecyclerViewAdapter(List<HourlyWeather> weatherList) {
    this.weatherList = weatherList;
  }

  public class ViewHolder extends RecyclerView.ViewHolder {

    TextView time;
    ImageView icon;
    TextView temperature;
    TextView humidity;


    public ViewHolder(@NonNull View view) {
      super(view);

      time = view.findViewById(R.id.row_hour_time);
      icon = view.findViewById(R.id.row_hour_star_icon);
      temperature = view.findViewById(R.id.row_hour_temperature);
      humidity = view.findViewById(R.id.row_hour_humidity);
    }

    public TextView getTime() {
      return time;
    }

    public ImageView getIcon() {
      return icon;
    }

    public TextView getTemperature() {
      return temperature;
    }

    public TextView getHumidity() {
      return humidity;
    }

  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.rv_row, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    Instant time = Instant.ofEpochMilli(weatherList.get(position).getTime());
    String hourAndMinute = new SimpleDateFormat("HH").format(Date.from(time));
    holder.getTime().setText(hourAndMinute);

    HashMap<String, Integer> weatherIconMap = new HashMap<>();
    weatherIconMap.put("Sunny", R.drawable.sun);
    weatherIconMap.put("Cloudy", R.drawable.cloudy);
    holder.getIcon().setImageResource(weatherIconMap.getOrDefault(weatherList.get(position).getWeather(), R.drawable.cloudy));

    holder.getHumidity().setText(weatherList.get(position).getHumidity() + "%");
    holder.getTemperature()
        .setText(Integer.toString((int) weatherList.get(position).getTemperature()));
    // holder.getHumidity().setText(Integer.toString((int) weatherList.get(position).getHumidity()));
  }

  @Override
  public int getItemCount() {
    return weatherList.size();
  }


}
