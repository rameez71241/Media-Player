package com.rameez.mediaplayer.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
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

import java.util.ArrayList;
import java.util.List;

public class AudioAdapter extends RecyclerView.Adapter<AudioAdapter.AudioViewHolder> {

    public interface OnAudioClickListener {
        void onPlayClicked(MediaItem item);
    }

    private final List<MediaItem> items = new ArrayList<>();
    private final OnAudioClickListener listener;

    public AudioAdapter(OnAudioClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<MediaItem> mediaItems) {
        items.clear();
        items.addAll(mediaItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AudioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_audio, parent, false);
        return new AudioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AudioViewHolder holder, int position) {
        MediaItem item = items.get(position);
        holder.nameText.setText(item.getDisplayName());
        holder.metaText.setText(parentMeta(item));
        holder.playButton.setOnClickListener(v -> listener.onPlayClicked(item));

        holder.thumbnailView.setImageResource(R.drawable.ic_audio_placeholder);
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(holder.itemView.getContext(), item.getUri());
            byte[] artwork = retriever.getEmbeddedPicture();
            if (artwork != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(artwork, 0, artwork.length);
                holder.thumbnailView.setImageBitmap(bitmap);
            }
            retriever.release();
        } catch (Exception ignored) {
            holder.thumbnailView.setImageResource(R.drawable.ic_audio_placeholder);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private String parentMeta(MediaItem item) {
        return FileUtils.formatDuration(item.getDurationMs()) + " • " + FileUtils.formatFileSize(item.getSizeBytes());
    }

    static class AudioViewHolder extends RecyclerView.ViewHolder {
        private final ImageView thumbnailView;
        private final TextView nameText;
        private final TextView metaText;
        private final Button playButton;

        AudioViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnailView = itemView.findViewById(R.id.image_audio_thumbnail);
            nameText = itemView.findViewById(R.id.text_audio_name);
            metaText = itemView.findViewById(R.id.text_audio_meta);
            playButton = itemView.findViewById(R.id.button_play_audio);
        }
    }
}
