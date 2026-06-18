package com.rameez.mediaplayer.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.rameez.mediaplayer.R;
import com.rameez.mediaplayer.adapter.AlbumAdapter;
import com.rameez.mediaplayer.adapter.AudioAdapter;
import com.rameez.mediaplayer.adapter.VideoAdapter;
import com.rameez.mediaplayer.model.MediaItem;
import com.rameez.mediaplayer.util.AlbumManager;
import com.rameez.mediaplayer.util.MediaStoreScanner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mediaRecyclerView;
    private ProgressBar progressBar;
    private TextView emptyText;
    private FloatingActionButton addAlbumButton;

    private VideoAdapter videoAdapter;
    private AudioAdapter audioAdapter;
    private AlbumAdapter albumAdapter;

    private final List<MediaItem> videos = new ArrayList<>();
    private final List<MediaItem> audios = new ArrayList<>();

    private final ExecutorService mediaExecutor = Executors.newSingleThreadExecutor();
    private AlbumManager albumManager;

    private final ActivityResultLauncher<String[]> permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            result -> {
                boolean granted = true;
                for (String permission : getReadPermissions()) {
                    Boolean isGranted = result.get(permission);
                    if (isGranted == null || !isGranted) {
                        granted = false;
                        break;
                    }
                }
                if (granted) {
                    scanMedia();
                } else {
                    showPermissionDenied();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        albumManager = new AlbumManager(this);

        mediaRecyclerView = findViewById(R.id.recycler_media);
        progressBar = findViewById(R.id.progress_loading);
        emptyText = findViewById(R.id.text_empty_state);
        addAlbumButton = findViewById(R.id.fab_add_album);

        mediaRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        videoAdapter = new VideoAdapter(getContentResolver(), this::openVideoFeed);
        audioAdapter = new AudioAdapter(this::openAudio);
        albumAdapter = new AlbumAdapter(this::openAlbumFeed);

        mediaRecyclerView.setAdapter(videoAdapter);

        TabLayout tabLayout = findViewById(R.id.tab_layout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switchAdapter(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        addAlbumButton.setOnClickListener(v -> showCreateAlbumDialog());

        if (hasReadPermissions()) {
            scanMedia();
        } else {
            permissionLauncher.launch(getReadPermissions());
        }
    }

    private void switchAdapter(int tabPosition) {
        if (tabPosition == 0) {
            mediaRecyclerView.setAdapter(videoAdapter);
            addAlbumButton.setVisibility(View.GONE);
            updateEmptyState(videos.isEmpty(), R.string.no_media_found);
        } else if (tabPosition == 1) {
            mediaRecyclerView.setAdapter(audioAdapter);
            addAlbumButton.setVisibility(View.GONE);
            updateEmptyState(audios.isEmpty(), R.string.no_media_found);
        } else {
            mediaRecyclerView.setAdapter(albumAdapter);
            addAlbumButton.setVisibility(View.VISIBLE);
            boolean empty = albumManager.getAlbumCounts().isEmpty();
            updateEmptyState(empty, R.string.no_albums_found);
        }
    }

    private void scanMedia() {
        progressBar.setVisibility(View.VISIBLE);
        // MediaStore queries are done off the main thread to keep scrolling and tab switching responsive.
        mediaExecutor.execute(() -> {
            List<MediaItem> videoItems = MediaStoreScanner.getVideos(this);
            List<MediaItem> audioItems = MediaStoreScanner.getAudios(this);

            runOnUiThread(() -> {
                videos.clear();
                videos.addAll(videoItems);
                audios.clear();
                audios.addAll(audioItems);

                videoAdapter.submitList(videos);
                audioAdapter.submitList(audios);
                refreshAlbums();

                progressBar.setVisibility(View.GONE);
                int selectedTab = ((TabLayout) findViewById(R.id.tab_layout)).getSelectedTabPosition();
                switchAdapter(selectedTab);
            });
        });
    }

    private void refreshAlbums() {
        albumAdapter.submitMap(albumManager.getAlbumCounts());
    }

    private void openVideoFeed(@NonNull MediaItem selectedItem) {
        if (videos.isEmpty()) {
            return;
        }
        int index = videos.indexOf(selectedItem);
        if (index < 0) {
            index = 0;
        }
        VideoPlayerActivity.start(this, videos, index, null);
    }

    private void openAlbumFeed(@NonNull String albumName) {
        List<MediaItem> albumVideos = albumManager.getAlbumVideos(albumName);
        if (albumVideos.isEmpty()) {
            Toast.makeText(this, R.string.no_media_found, Toast.LENGTH_SHORT).show();
            return;
        }
        VideoPlayerActivity.start(this, albumVideos, 0, albumName);
    }

    private void openAudio(@NonNull MediaItem item) {
        Intent intent = new Intent(this, AudioPlayerActivity.class);
        intent.putExtra(AudioPlayerActivity.EXTRA_MEDIA_URI, item.getUri().toString());
        intent.putExtra(AudioPlayerActivity.EXTRA_TITLE, item.getDisplayName());
        startActivity(intent);
    }

    private void updateEmptyState(boolean empty, int textRes) {
        if (empty) {
            emptyText.setVisibility(View.VISIBLE);
            emptyText.setText(textRes);
        } else {
            emptyText.setVisibility(View.GONE);
        }
    }

    private boolean hasReadPermissions() {
        for (String permission : getReadPermissions()) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private String[] getReadPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return new String[]{Manifest.permission.READ_MEDIA_AUDIO, Manifest.permission.READ_MEDIA_VIDEO};
        }
        return new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
    }

    private void showCreateAlbumDialog() {
        TextInputLayout inputLayout = new TextInputLayout(this);
        TextInputEditText input = new TextInputEditText(this);
        input.setHint(R.string.album_name_hint);
        inputLayout.addView(input);

        new AlertDialog.Builder(this)
                .setTitle(R.string.create_album)
                .setView(inputLayout)
                .setPositiveButton(R.string.create, (dialog, which) -> {
                    String name = input.getText() != null ? input.getText().toString().trim() : "";
                    if (!albumManager.createAlbum(name)) {
                        Toast.makeText(this, R.string.album_create_failed, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    refreshAlbums();
                    switchAdapter(2);
                    Toast.makeText(this, R.string.album_created, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void showPermissionDenied() {
        progressBar.setVisibility(View.GONE);
        emptyText.setVisibility(View.VISIBLE);
        emptyText.setText(R.string.permission_required);
        Toast.makeText(this, R.string.permission_required, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaExecutor.shutdownNow();
    }
}
