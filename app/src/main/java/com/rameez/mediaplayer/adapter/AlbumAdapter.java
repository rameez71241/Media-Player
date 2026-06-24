package com.rameez.mediaplayer.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rameez.mediaplayer.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder> {

    public interface OnAlbumClickListener {
        void onOpenClicked(String albumName);
    }

    private final List<Map.Entry<String, Integer>> entries = new ArrayList<>();
    private final OnAlbumClickListener listener;

    public AlbumAdapter(OnAlbumClickListener listener) {
        this.listener = listener;
    }

    public void submitMap(Map<String, Integer> data) {
        entries.clear();
        entries.addAll(data.entrySet());
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_album, parent, false);
        return new AlbumViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumViewHolder holder, int position) {
        Map.Entry<String, Integer> entry = entries.get(position);
        holder.title.setText(entry.getKey());
        holder.count.setText(holder.itemView.getContext().getString(R.string.album_video_count, entry.getValue()));
        holder.openButton.setOnClickListener(v -> listener.onOpenClicked(entry.getKey()));
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    static class AlbumViewHolder extends RecyclerView.ViewHolder {
        private final TextView title;
        private final TextView count;
        private final Button openButton;

        AlbumViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.text_album_name);
            count = itemView.findViewById(R.id.text_album_count);
            openButton = itemView.findViewById(R.id.button_open_album);
        }
    }
}
