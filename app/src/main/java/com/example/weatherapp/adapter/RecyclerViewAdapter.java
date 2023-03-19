package com.example.weatherapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherapp.util.DefaultConfig;
import com.example.weatherapp.R;
import com.example.weatherapp.model.HourlyWeather;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private final List<HourlyWeather> hourlyWeathers;

    public RecyclerViewAdapter(List<HourlyWeather> hourlyWeathers) {
        this.hourlyWeathers = hourlyWeathers;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

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
        long timeMillis = hourlyWeathers.get(position).getTime();
        Date weatherTime = new Date(timeMillis);
        String hour = new SimpleDateFormat("HH", Locale.ENGLISH).format(weatherTime);
        String weather = hourlyWeathers.get(position).getWeather();
        double tempInKelvin = hourlyWeathers.get(position).getTemperature();
        int tempInCelsius = (int) (tempInKelvin - 273.15);

        holder.getTime().setText(hour);
        holder.getIcon().setImageResource(DefaultConfig.getIconResource(weather));
        holder.getTemperature().setText(String.valueOf(tempInCelsius));
        holder.getHumidity().setText(hourlyWeathers.get(position).getHumidity() + "");
    }

    @Override
    public int getItemCount() {
        return hourlyWeathers.size();
    }
}
