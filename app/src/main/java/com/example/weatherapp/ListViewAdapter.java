// package com.example.weatherapp;
//
// import android.content.Context;
// import android.view.LayoutInflater;
// import android.view.View;
// import android.view.ViewGroup;
// import android.widget.BaseAdapter;
// import android.widget.ImageView;
// import android.widget.TextView;
//
// import java.text.SimpleDateFormat;
// import java.util.List;
//
// public class ListViewAdapter extends BaseAdapter {
//     private Context context;
//     private List<Weather> weatherList;
//     private LayoutInflater layoutInflater;
//
//     public ListViewAdapter(Context context, List<Weather> weatherList) {
//         this.weatherList = weatherList;
//         this.context = context;
//         layoutInflater = LayoutInflater.from(context);
//     }
//
//     static class ViewHolder {
//         TextView day;
//         TextView humidity;
//         ImageView dayIcon;
//         ImageView nightIcon;
//         TextView dayTemperature;
//         TextView nightTemperature;
//     }
//
//     // override other abstract methods here
//
//     @Override
//     public int getCount() {
//         return weatherList.size();
//     }
//
//     @Override
//     public Object getItem(int position) {
//         return weatherList.get(position);
//     }
//
//     @Override
//     public long getItemId(int position) {
//         return position;
//     }
//
//     @Override
//     public View getView(int position, View convertView, ViewGroup container) {
//         ViewHolder holder;
//         if (convertView == null) {
//             convertView = layoutInflater.inflate(R.layout.lv_item, null);
//             holder = new ViewHolder();
//             holder.day = convertView.findViewById(R.id.row_day_day);
//             holder.humidity = convertView.findViewById(R.id.row_day_humidity);
//             holder.dayIcon = convertView.findViewById(R.id.row_day_icon_day);
//             holder.nightIcon = convertView.findViewById(R.id.row_day_icon_night);
//             holder.dayTemperature = convertView.findViewById(R.id.row_day_temp_day);
//             holder.nightTemperature = convertView.findViewById(R.id.row_day_temp_night);
//             convertView.setTag(holder);
//         } else {
//             holder = (ViewHolder) convertView.getTag();
//         }
//
//         Weather weather = weatherList.get(position);
//         String weekDay = new SimpleDateFormat("EEEE").format(weather.getTime());
//         holder.day.setText(weekDay);
//         holder.humidity.setText(Integer.toString((int) weather.getHumidity()));
//         holder.dayIcon.setImageResource(R.drawable.sun);
//         holder.nightIcon.setImageResource(R.drawable.moon);
//         holder.dayTemperature.setText(Integer.toString((int) weather.getTemperature()));
//         holder.nightTemperature.setText(Integer.toString((int) (weather.getTemperature() - 5)));
//         return convertView;
//     }
// }
