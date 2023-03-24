package com.example.weatherapp.adapter;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;
import com.example.weatherapp.R;
import com.example.weatherapp.dao.AppDatabase;
import com.example.weatherapp.dao.LocationDao;
import com.example.weatherapp.model.AppLocation;
import java.util.List;

public class RecycleViewLocation extends RecyclerView.Adapter<RecycleViewLocation.ViewHolder>{
    private final LocationDao locationDao;
    private List<AppLocation> appLocations;
    private final Activity activity;
    public RecycleViewLocation(List<AppLocation> appLocations, Activity activity) {
        appLocations.sort(((o1, o2) -> (int) (o2.getId() - o1.getId())));
        this.appLocations = appLocations;
        this.activity = activity;
        this.locationDao = Room.databaseBuilder(activity.getApplicationContext(), AppDatabase.class,
                "WeatherApp").fallbackToDestructiveMigration().allowMainThreadQueries().build().locationDao();
    }
    @Override
    public int getItemCount() {
        return appLocations.size();
    }
    @NonNull
    @Override
    public RecycleViewLocation.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_location, parent, false);
        return new RecycleViewLocation.ViewHolder(view);
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

    @Override
    public void onBindViewHolder(@NonNull RecycleViewLocation.ViewHolder holder, int position) {
        String name = appLocations.get(position).getName();
        String country = appLocations.get(position).getCountry();

        holder.getName().setText(name);
        holder.getCountry().setText(country);
        holder.getBtnDelete().setOnClickListener(v -> {
            System.out.println(appLocations.get(position));
            System.out.println("Delete location with id: " + appLocations.get(position).getId());
            locationDao.deleteById(appLocations.get(position).getId());
            appLocations = locationDao.getAllLocations();
            notifyDataSetChanged();
        });
        holder.itemView.setOnClickListener(v -> {
            AppLocation appLocation = appLocations.get(position);
        //    back to main activity
            Intent intent = new Intent();
            intent.putExtra("current_location", appLocation);
            activity.setResult(Activity.RESULT_OK, intent);
            activity.finish();
        });
    }



}
