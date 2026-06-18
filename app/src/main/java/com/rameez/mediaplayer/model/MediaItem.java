package com.rameez.mediaplayer.model;

import android.net.Uri;

public class MediaItem {
    private final Uri uri;
    private final String displayName;
    private final long sizeBytes;
    private final long durationMs;

    public MediaItem(Uri uri, String displayName, long sizeBytes, long durationMs) {
        this.uri = uri;
        this.displayName = displayName;
        this.sizeBytes = sizeBytes;
        this.durationMs = durationMs;
    }

    public Uri getUri() {
        return uri;
    }

    public String getDisplayName() {
        return displayName;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    public long getDurationMs() {
        return durationMs;
    }
}
