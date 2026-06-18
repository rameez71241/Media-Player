package com.rameez.mediaplayer.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.rameez.mediaplayer.R;
import com.rameez.mediaplayer.adapter.VideoFeedAdapter;
import com.rameez.mediaplayer.model.MediaItem;

import java.util.ArrayList;
import java.util.List;

public class VideoPlayerActivity extends AppCompatActivity {

    public static final String EXTRA_VIDEO_URIS = "extra_video_uris";
    public static final String EXTRA_VIDEO_NAMES = "extra_video_names";
    public static final String EXTRA_VIDEO_SIZES = "extra_video_sizes";
    public static final String EXTRA_VIDEO_DURATIONS = "extra_video_durations";
    public static final String EXTRA_START_INDEX = "extra_start_index";
    public static final String EXTRA_ALBUM_NAME = "extra_album_name";

    public static void start(Context context, List<MediaItem> videos, int startIndex, @Nullable String albumName) {
        Intent intent = new Intent(context, VideoPlayerActivity.class);
        ArrayList<String> uris = new ArrayList<>();
        ArrayList<String> names = new ArrayList<>();
        long[] sizes = new long[videos.size()];
        long[] durations = new long[videos.size()];

        for (int i = 0; i < videos.size(); i++) {
            MediaItem item = videos.get(i);
            uris.add(item.getUri().toString());
            names.add(item.getDisplayName());
            sizes[i] = item.getSizeBytes();
            durations[i] = item.getDurationMs();
        }

        intent.putStringArrayListExtra(EXTRA_VIDEO_URIS, uris);
        intent.putStringArrayListExtra(EXTRA_VIDEO_NAMES, names);
        intent.putExtra(EXTRA_VIDEO_SIZES, sizes);
        intent.putExtra(EXTRA_VIDEO_DURATIONS, durations);
        intent.putExtra(EXTRA_START_INDEX, Math.max(startIndex, 0));
        if (albumName != null) {
            intent.putExtra(EXTRA_ALBUM_NAME, albumName);
        }
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        ArrayList<String> uris = getIntent().getStringArrayListExtra(EXTRA_VIDEO_URIS);
        ArrayList<String> names = getIntent().getStringArrayListExtra(EXTRA_VIDEO_NAMES);
        long[] sizes = getIntent().getLongArrayExtra(EXTRA_VIDEO_SIZES);
        long[] durations = getIntent().getLongArrayExtra(EXTRA_VIDEO_DURATIONS);
        int startIndex = getIntent().getIntExtra(EXTRA_START_INDEX, 0);

        if (uris == null || uris.isEmpty()) {
            finish();
            return;
        }

        if (names == null) {
            names = new ArrayList<>();
        }
        if (sizes == null || sizes.length != uris.size()) {
            sizes = new long[uris.size()];
        }
        if (durations == null || durations.length != uris.size()) {
            durations = new long[uris.size()];
        }

        List<MediaItem> videos = new ArrayList<>();
        for (int i = 0; i < uris.size(); i++) {
            String name = i < names.size() ? names.get(i) : getString(R.string.video_default_name);
            videos.add(new MediaItem(android.net.Uri.parse(uris.get(i)), name, sizes[i], durations[i]));
        }

        String albumName = getIntent().getStringExtra(EXTRA_ALBUM_NAME);
        setTitle(albumName == null ? getString(R.string.video_feed) : albumName);

        ViewPager2 pager = findViewById(R.id.video_pager);
        // Vertical pager provides a TikTok-like swipe experience between videos.
        pager.setOrientation(ViewPager2.ORIENTATION_VERTICAL);
        pager.setOffscreenPageLimit(1);

        VideoFeedAdapter adapter = new VideoFeedAdapter(this, videos);
        pager.setAdapter(adapter);

        int boundedIndex = Math.min(Math.max(0, startIndex), videos.size() - 1);
        pager.setCurrentItem(boundedIndex, false);
        adapter.setActivePosition(boundedIndex);

        pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                adapter.setActivePosition(position);
            }
        });
    }
}
