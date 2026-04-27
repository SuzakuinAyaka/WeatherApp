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

public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ViewHolder> {

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
                .inflate(R.layout.item_forecast_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ForecastWeather item = items.get(position);
        holder.dayView.setText(item.getForecastDate());
        holder.weatherView.setText(item.getDayWeather());
        holder.tempView.setText(item.getDayTemp() + "/" + item.getNightTemp());
        holder.windView.setText(item.getDayWind() + " · " + item.getDayPower());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView dayView;
        private final TextView weatherView;
        private final TextView tempView;
        private final TextView windView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            dayView = itemView.findViewById(R.id.text_forecast_day);
            weatherView = itemView.findViewById(R.id.text_forecast_weather);
            tempView = itemView.findViewById(R.id.text_forecast_temp);
            windView = itemView.findViewById(R.id.text_forecast_wind);
        }
    }
}
