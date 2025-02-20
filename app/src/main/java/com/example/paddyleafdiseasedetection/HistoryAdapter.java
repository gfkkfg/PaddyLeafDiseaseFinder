package com.example.paddyleafdiseasedetection;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private final List<HistoryItem> historyList = new ArrayList<>();

    public void addHistoryItem(HistoryItem item) {
        historyList.add(item);
        notifyItemInserted(historyList.size() - 1); // Notify RecyclerView of new item
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_item, parent, false);
        return new HistoryViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        HistoryItem item = historyList.get(position);

        // Use holder.itemView.getContext().getString() to fetch string resources
        holder.diseaseTextView.setText(holder.itemView.getContext().getString(R.string.disease) + ": " + item.getDisease());
        holder.dateTextView.setText(holder.itemView.getContext().getString(R.string.date) + ": " + new SimpleDateFormat("yyyy-MM-dd HH:mm").format(item.getTimestamp()));
        holder.confidenceTextView.setText(holder.itemView.getContext().getString(R.string.confidence) + ": " + String.format("%.2f%%", item.getConfidence() * 100));

        if (item.getLocation() != null && !item.getLocation().isEmpty()) {
            holder.locationTextView.setText(holder.itemView.getContext().getString(R.string.location) + ": " + item.getLocation());
        } else {
            holder.locationTextView.setVisibility(View.GONE); // Hide if no location
        }

        // Optional: Load image using Glide or Picasso
        if (item.getImageUrl() != null) {
            Glide.with(holder.itemView.getContext()).load(item.getImageUrl()).into(holder.imageView);
        }
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView diseaseTextView;
        TextView dateTextView;
        TextView confidenceTextView; // New field
        TextView locationTextView;   // New field
        ImageView imageView;         // For image preview

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            diseaseTextView = itemView.findViewById(R.id.disease_name);
            dateTextView = itemView.findViewById(R.id.detection_date);
            confidenceTextView = itemView.findViewById(R.id.confidence_score); // Add this in XML
            locationTextView = itemView.findViewById(R.id.detection_location); // Add this in XML
            imageView = itemView.findViewById(R.id.detection_image);           // Add this in XML
        }
    }
}
