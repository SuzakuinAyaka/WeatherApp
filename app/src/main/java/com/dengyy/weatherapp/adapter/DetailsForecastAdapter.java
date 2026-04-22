package com.dengyy.weatherapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dengyy.weatherapp.R;
import com.dengyy.weatherapp.model.ForecastWeather;

import java.util.ArrayList;
import java.util.List;

public class DetailsForecastAdapter extends RecyclerView.Adapter<DetailsForecastAdapter.ViewHolder> {

    private final List<ForecastWeather> items = new ArrayList<>();

    public void submitList(List<ForecastWeather> forecasts) {
        items.clear();
        if (forecasts != null) {
            items.addAll(forecasts);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_details_forecast, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ForecastWeather item = items.get(position);
        holder.dateView.setText(item.getForecastDate());
        holder.weatherView.setText(item.getDayWeather() + " / " + item.getNightWeather());
        holder.tempView.setText(
                holder.itemView.getContext().getString(
                        R.string.main_high_low,
                        item.getDayTemp(),
                        item.getNightTemp()
                )
        );
        holder.dayWindView.setText("Day  " + item.getDayWind() + "  " + item.getDayPower());
        holder.nightWindView.setText("Night  " + item.getNightWind() + "  " + item.getNightPower());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView dateView;
        private final TextView weatherView;
        private final TextView tempView;
        private final TextView dayWindView;
        private final TextView nightWindView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            dateView = itemView.findViewById(R.id.text_details_forecast_date);
            weatherView = itemView.findViewById(R.id.text_details_forecast_weather);
            tempView = itemView.findViewById(R.id.text_details_forecast_temp);
            dayWindView = itemView.findViewById(R.id.text_details_forecast_day_wind);
            nightWindView = itemView.findViewById(R.id.text_details_forecast_night_wind);
        }
    }
}
