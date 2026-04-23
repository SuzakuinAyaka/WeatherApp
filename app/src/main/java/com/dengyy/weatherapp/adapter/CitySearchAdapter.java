package com.dengyy.weatherapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dengyy.weatherapp.R;
import com.dengyy.weatherapp.model.City;

import java.util.ArrayList;
import java.util.List;

public class CitySearchAdapter extends RecyclerView.Adapter<CitySearchAdapter.ViewHolder> {

    public interface OnCityClickListener {
        void onCityClick(City city);
    }

    private final List<City> items = new ArrayList<>();
    private final OnCityClickListener onCityClickListener;

    public CitySearchAdapter(OnCityClickListener onCityClickListener) {
        this.onCityClickListener = onCityClickListener;
    }

    public void submitList(List<City> cities) {
        items.clear();
        if (cities != null) {
            items.addAll(cities);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_city_search, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        City city = items.get(position);
        holder.titleView.setText(city.getCityName());
        holder.subtitleView.setText(city.getProvince());
        holder.itemView.setOnClickListener(v -> {
            if (onCityClickListener != null) {
                onCityClickListener.onCityClick(city);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView titleView;
        private final TextView subtitleView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleView = itemView.findViewById(R.id.text_title);
            subtitleView = itemView.findViewById(R.id.text_subtitle);
        }
    }
}
