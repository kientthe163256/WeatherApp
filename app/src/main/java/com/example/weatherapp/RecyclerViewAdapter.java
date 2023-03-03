package com.example.weatherapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    private List<Weather> weatherList;

    public RecyclerViewAdapter(List<Weather> weatherList) {
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
        Date weatherTime = weatherList.get(position).getTime();
        String hourAndMinute = new SimpleDateFormat("HH:mm").format(weatherTime);
        holder.getTime().setText(hourAndMinute);
        holder.getIcon().setImageResource(R.drawable.moon);
        holder.getTemperature().setText(Integer.toString((int) weatherList.get(position).getTemperature()));
        holder.getHumidity().setText(Integer.toString((int) weatherList.get(position).getHumidity()));
    }

    @Override
    public int getItemCount() {
        return weatherList.size();
    }


}
