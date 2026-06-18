package com.rameez.mediaplayer.util;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import com.rameez.mediaplayer.model.MediaItem;

import java.util.ArrayList;
import java.util.List;

public final class MediaStoreScanner {

    private MediaStoreScanner() {
    }

    public static List<MediaItem> getVideos(Context context) {
        Uri contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String[] projection = new String[]{
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.DURATION
        };
        return queryMedia(context, contentUri, projection, MediaStore.Video.Media.DATE_ADDED + " DESC");
    }

    public static List<MediaItem> getAudios(Context context) {
        Uri contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = new String[]{
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.DURATION
        };
        String selection = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                ? MediaStore.Audio.Media.IS_MUSIC + " != 0"
                : null;
        return queryMedia(context, contentUri, projection, MediaStore.Audio.Media.DATE_ADDED + " DESC", selection);
    }

    private static List<MediaItem> queryMedia(Context context, Uri collection, String[] projection, String sortOrder) {
        return queryMedia(context, collection, projection, sortOrder, null);
    }

    private static List<MediaItem> queryMedia(Context context, Uri collection, String[] projection, String sortOrder, String selection) {
        List<MediaItem> mediaItems = new ArrayList<>();
        try (Cursor cursor = context.getContentResolver().query(collection, projection, selection, null, sortOrder)) {
            if (cursor == null) {
                return mediaItems;
            }
            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID);
            int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME);
            int sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE);
            int durationColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DURATION);

            while (cursor.moveToNext()) {
                long id = cursor.getLong(idColumn);
                String name = cursor.getString(nameColumn);
                long size = cursor.getLong(sizeColumn);
                long duration = cursor.getLong(durationColumn);
                Uri itemUri = Uri.withAppendedPath(collection, String.valueOf(id));
                mediaItems.add(new MediaItem(itemUri, name, size, duration));
            }
        }
        return mediaItems;
    }
}
