package com.rameez.mediaplayer.adapter;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rameez.mediaplayer.R;
import com.rameez.mediaplayer.model.MediaItem;
import com.rameez.mediaplayer.util.FileUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {

    public interface OnVideoClickListener {
        void onPlayClicked(MediaItem item);
    }

    private final List<MediaItem> items = new ArrayList<>();
    private final OnVideoClickListener listener;
    private final ContentResolver contentResolver;

    public VideoAdapter(ContentResolver contentResolver, OnVideoClickListener listener) {
        this.contentResolver = contentResolver;
        this.listener = listener;
    }

    public void submitList(List<MediaItem> mediaItems) {
        items.clear();
        items.addAll(mediaItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        MediaItem item = items.get(position);
        holder.nameText.setText(item.getDisplayName());
        holder.metaText.setText(parentMeta(item));
        holder.playButton.setOnClickListener(v -> listener.onPlayClicked(item));

        holder.thumbnailView.setImageResource(R.drawable.ic_video_placeholder);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                Bitmap bitmap = contentResolver.loadThumbnail(item.getUri(), new Size(256, 256), null);
                holder.thumbnailView.setImageDrawable(new BitmapDrawable(holder.itemView.getResources(), bitmap));
            } catch (IOException ignored) {
                holder.thumbnailView.setImageResource(R.drawable.ic_video_placeholder);
            }
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private String parentMeta(MediaItem item) {
        return FileUtils.formatDuration(item.getDurationMs()) + " • " + FileUtils.formatFileSize(item.getSizeBytes());
    }

    static class VideoViewHolder extends RecyclerView.ViewHolder {
        private final ImageView thumbnailView;
        private final TextView nameText;
        private final TextView metaText;
        private final Button playButton;

        VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnailView = itemView.findViewById(R.id.image_video_thumbnail);
            nameText = itemView.findViewById(R.id.text_video_name);
            metaText = itemView.findViewById(R.id.text_video_meta);
            playButton = itemView.findViewById(R.id.button_play_video);
        }
    }
}
