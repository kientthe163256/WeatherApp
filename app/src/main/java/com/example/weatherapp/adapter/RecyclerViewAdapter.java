package com.example.weatherapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherapp.R;
import com.example.weatherapp.model.HourlyWeather;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    private List<HourlyWeather> weatherList;

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
                .inflate(R.layout.hourly_weather_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        long timeMillis = weatherList.get(position).getTime();
        Date weatherTime = new Date(timeMillis);
        String hourAndMinute = new SimpleDateFormat("HH").format(weatherTime);
        holder.getTime().setText(hourAndMinute);
        holder.getIcon().setImageResource(R.drawable.moon);
        double tempInKelvin = weatherList.get(position).getTemperature();
        int tempInCelsius =  (int) (tempInKelvin - 273.15);
        holder.getTemperature().setText(String.valueOf(tempInCelsius));
        holder.getHumidity().setText(Integer.toString(weatherList.get(position).getHumidity()));
    }

    @Override
    public int getItemCount() {
        return weatherList.size();
    }


}
