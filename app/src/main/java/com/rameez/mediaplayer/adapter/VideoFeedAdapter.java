package com.rameez.mediaplayer.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rameez.mediaplayer.R;
import com.rameez.mediaplayer.model.MediaItem;
import com.rameez.mediaplayer.util.AlbumManager;

import java.util.ArrayList;
import java.util.List;

public class VideoFeedAdapter extends RecyclerView.Adapter<VideoFeedAdapter.VideoFeedViewHolder> {

    private final List<MediaItem> videos = new ArrayList<>();
    private final AlbumManager albumManager;
    private int activePosition = RecyclerView.NO_POSITION;

    public VideoFeedAdapter(Context context, List<MediaItem> items) {
        this.albumManager = new AlbumManager(context);
        this.videos.addAll(items);
    }

    public void setActivePosition(int position) {
        int old = activePosition;
        activePosition = position;
        if (old != RecyclerView.NO_POSITION) {
            notifyItemChanged(old);
        }
        if (activePosition != RecyclerView.NO_POSITION) {
            notifyItemChanged(activePosition);
        }
    }

    @NonNull
    @Override
    public VideoFeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video_feed, parent, false);
        return new VideoFeedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoFeedViewHolder holder, int position) {
        MediaItem item = videos.get(position);
        holder.titleText.setText(item.getDisplayName());
        holder.bindVideo(item.getUri(), position == activePosition);
        holder.playPauseButton.setOnClickListener(v -> holder.togglePlayPause());
        holder.addAlbumButton.setOnClickListener(v -> showAlbumDialog(holder.itemView.getContext(), item));
    }

    @Override
    public void onViewRecycled(@NonNull VideoFeedViewHolder holder) {
        super.onViewRecycled(holder);
        holder.release();
    }

    @Override
    public int getItemCount() {
        return videos.size();
    }

    private void showAlbumDialog(Context context, MediaItem item) {
        List<String> names = albumManager.getAlbumNames();
        List<String> choices = new ArrayList<>(names);
        choices.add(context.getString(R.string.create_new_album));

        new AlertDialog.Builder(context)
                .setTitle(R.string.add_to_album)
                .setItems(choices.toArray(new String[0]), (dialog, which) -> {
                    if (which == choices.size() - 1) {
                        showCreateAlbumDialog(context, item);
                        return;
                    }
                    addToAlbum(context, choices.get(which), item);
                })
                .show();
    }

    private void showCreateAlbumDialog(Context context, MediaItem item) {
        EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint(R.string.album_name_hint);

        new AlertDialog.Builder(context)
                .setTitle(R.string.create_album)
                .setView(input)
                .setPositiveButton(R.string.create, (dialog, which) -> {
                    String name = input.getText() != null ? input.getText().toString().trim() : "";
                    if (albumManager.createAlbum(name)) {
                        addToAlbum(context, name, item);
                    } else {
                        Toast.makeText(context, R.string.album_create_failed, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void addToAlbum(Context context, String albumName, MediaItem item) {
        boolean added = albumManager.addVideoToAlbum(albumName, item);
        Toast.makeText(context, added ? R.string.added_to_album : R.string.already_in_album, Toast.LENGTH_SHORT).show();
    }

    static class VideoFeedViewHolder extends RecyclerView.ViewHolder {

        private final VideoView videoView;
        private final TextView titleText;
        private final Button playPauseButton;
        private final Button addAlbumButton;

        VideoFeedViewHolder(@NonNull View itemView) {
            super(itemView);
            videoView = itemView.findViewById(R.id.video_view_feed);
            titleText = itemView.findViewById(R.id.text_video_title_feed);
            playPauseButton = itemView.findViewById(R.id.button_play_pause_feed);
            addAlbumButton = itemView.findViewById(R.id.button_add_album_feed);
        }

        void bindVideo(Uri uri, boolean shouldAutoPlay) {
            videoView.setVideoURI(uri);
            videoView.setOnPreparedListener(mp -> {
                mp.setLooping(true);
                if (shouldAutoPlay) {
                    videoView.start();
                    playPauseButton.setText(R.string.pause);
                } else {
                    playPauseButton.setText(R.string.play);
                }
            });
            videoView.setOnErrorListener((MediaPlayer mp, int what, int extra) -> {
                Toast.makeText(itemView.getContext(), R.string.playback_error, Toast.LENGTH_SHORT).show();
                return true;
            });
        }

        void togglePlayPause() {
            if (videoView.isPlaying()) {
                videoView.pause();
                playPauseButton.setText(R.string.play);
            } else {
                videoView.start();
                playPauseButton.setText(R.string.pause);
            }
        }

        void release() {
            videoView.stopPlayback();
        }
    }
}
