package com.example.weatherapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherapp.R;
import com.example.weatherapp.model.AppLocation;
import com.example.weatherapp.model.HourlyWeather;
import com.example.weatherapp.util.DefaultConfig;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RecycleViewLocation extends RecyclerView.Adapter<RecycleViewLocation.ViewHolder>{

    private final List<AppLocation> appLocations;

    public RecycleViewLocation(List<AppLocation> appLocations) {
        this.appLocations = appLocations;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView name;
        TextView country;
        Button btnDelete;

        public ViewHolder(@NonNull View view) {
            super(view);

            name = view.findViewById(R.id.row_name);
            country = view.findViewById(R.id.row_country);
            btnDelete = view.findViewById(R.id.btnDelete);
        }

        public TextView getName() {
            return name;
        }

        public TextView getCountry() {
            return country;
        }

        public Button getBtnDelete() {
            return btnDelete;
        }
    }

    @NonNull
    @Override
    public RecycleViewLocation.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_location, parent, false);
        return new RecycleViewLocation.ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull RecycleViewLocation.ViewHolder holder, int position) {
        String name = appLocations.get(position).getName();
        String country = appLocations.get(position).getCountry();

        holder.getName().setText(name);
        holder.getCountry().setText(country);
        holder.getBtnDelete().setOnClickListener(v -> {

        });
    }

    @Override
    public int getItemCount() {
        return appLocations.size();
    }
}
