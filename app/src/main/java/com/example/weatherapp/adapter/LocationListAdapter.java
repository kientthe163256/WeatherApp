package com.example.weatherapp.adapter;

import android.content.Context;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.BaseAdapter;
        import android.widget.TextView;

        import com.android.volley.Response;
        import com.example.weatherapp.R;
        import com.example.weatherapp.model.AppLocation;
        import com.example.weatherapp.service.ApiService;

        import org.json.JSONArray;
        import org.json.JSONException;

        import java.util.ArrayList;
        import java.util.List;
        import java.util.Locale;

public class LocationListAdapter extends BaseAdapter {


    Context mContext;
    LayoutInflater inflater;
    private List<AppLocation> locationList = null;

    public LocationListAdapter(Context context, List<AppLocation> animalNamesList) {
        mContext = context;
        this.locationList = animalNamesList;
        inflater = LayoutInflater.from(mContext);
    }

    public class ViewHolder {
        TextView name;
    }

    @Override
    public int getCount() {
        return locationList.size();
    }

    @Override
    public AppLocation getItem(int position) {
        return locationList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View view, ViewGroup parent) {
        final ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = inflater.inflate(R.layout.activity_test_list_item, null);
            // Locate the TextViews in listview_item.xml
            holder.name = (TextView) view.findViewById(R.id.name);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        // Set the results into TextViews
        holder.name.setText(locationList.get(position).getName());

        return view;
    }


    // Filter Class
//    public void filter(String charText) {
//        charText = charText.toLowerCase(Locale.getDefault());
//
//        animalNamesList.clear();
//        if (charText.length() == 0) {
//            animalNamesList.addAll(arraylistBlank);
//        } else {
//            for (AppLocation wp : appLocations) {
//                animalNamesList.add(wp);
//            }
//        }
//        notifyDataSetChanged();
//    }


}
