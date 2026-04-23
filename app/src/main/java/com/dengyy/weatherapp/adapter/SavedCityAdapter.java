package com.dengyy.weatherapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dengyy.weatherapp.R;
import com.dengyy.weatherapp.model.City;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class SavedCityAdapter extends RecyclerView.Adapter<SavedCityAdapter.ViewHolder> {

    public interface OnCityClickListener {
        void onCityClick(City city);
    }

    public interface OnCityLongClickListener {
        void onCityLongClick(City city);
    }

    private final List<City> items = new ArrayList<>();
    private final OnCityClickListener onCityClickListener;
    private final OnCityLongClickListener onCityLongClickListener;

    public SavedCityAdapter(
            OnCityClickListener onCityClickListener,
            OnCityLongClickListener onCityLongClickListener
    ) {
        this.onCityClickListener = onCityClickListener;
        this.onCityLongClickListener = onCityLongClickListener;
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
                .inflate(R.layout.item_saved_city, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        City city = items.get(position);
        holder.titleView.setText(city.getCityName());
        holder.subtitleView.setText(city.isCurrent()
                ? holder.itemView.getContext().getString(R.string.main_city_tag_current)
                : city.getProvince());

        if (city.isCurrent()) {
            holder.cardView.setCardBackgroundColor(
                    holder.itemView.getContext().getColor(R.color.main_drawer_city_selected_bg)
            );
            holder.cardView.setStrokeWidth(dpToPx(holder.itemView, 1));
            holder.cardView.setStrokeColor(
                    holder.itemView.getContext().getColor(R.color.main_drawer_city_selected_stroke)
            );
        } else {
            holder.cardView.setCardBackgroundColor(
                    holder.itemView.getContext().getColor(R.color.main_drawer_city_bg)
            );
            holder.cardView.setStrokeWidth(0);
        }

        holder.itemView.setOnClickListener(v -> {
            if (onCityClickListener != null) {
                onCityClickListener.onCityClick(city);
            }
        });
        holder.itemView.setOnLongClickListener(v -> {
            if (onCityLongClickListener != null) {
                onCityLongClickListener.onCityLongClick(city);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private static int dpToPx(View view, int dp) {
        float density = view.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final MaterialCardView cardView;
        private final TextView titleView;
        private final TextView subtitleView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_city_item);
            titleView = itemView.findViewById(R.id.text_city_name);
            subtitleView = itemView.findViewById(R.id.text_city_meta);
        }
    }
}
