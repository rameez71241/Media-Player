package com.rameez.mediaplayer.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import com.rameez.mediaplayer.model.MediaItem;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AlbumManager {

    private static final String PREFS = "media_albums";
    private static final String KEY_ALBUMS = "albums_json";

    private final SharedPreferences prefs;

    public AlbumManager(Context context) {
        this.prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public boolean createAlbum(String name) {
        String albumName = safeName(name);
        if (albumName.isEmpty()) {
            return false;
        }
        JSONObject root = getRoot();
        if (root.has(albumName)) {
            return false;
        }
        try {
            root.put(albumName, new JSONArray());
            saveRoot(root);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    public boolean addVideoToAlbum(String name, MediaItem item) {
        String albumName = safeName(name);
        if (albumName.isEmpty() || item == null || item.getUri() == null) {
            return false;
        }
        JSONObject root = getRoot();
        JSONArray array = root.optJSONArray(albumName);
        if (array == null) {
            array = new JSONArray();
        }

        String targetUri = item.getUri().toString();
        // Keep album entries unique by media URI so users don't get duplicate videos in custom albums.
        for (int i = 0; i < array.length(); i++) {
            JSONObject existing = array.optJSONObject(i);
            if (existing != null && targetUri.equals(existing.optString("uri"))) {
                return false;
            }
        }

        try {
            JSONObject entry = new JSONObject();
            entry.put("uri", targetUri);
            entry.put("name", item.getDisplayName());
            entry.put("size", item.getSizeBytes());
            entry.put("duration", item.getDurationMs());
            array.put(entry);
            root.put(albumName, array);
            saveRoot(root);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    public List<String> getAlbumNames() {
        List<String> names = new ArrayList<>();
        JSONObject root = getRoot();
        Iterator<String> keys = root.keys();
        while (keys.hasNext()) {
            names.add(keys.next());
        }
        return names;
    }

    public Map<String, Integer> getAlbumCounts() {
        Map<String, Integer> data = new LinkedHashMap<>();
        JSONObject root = getRoot();
        Iterator<String> keys = root.keys();
        while (keys.hasNext()) {
            String name = keys.next();
            JSONArray array = root.optJSONArray(name);
            data.put(name, array != null ? array.length() : 0);
        }
        return data;
    }

    public List<MediaItem> getAlbumVideos(String name) {
        List<MediaItem> items = new ArrayList<>();
        JSONArray array = getRoot().optJSONArray(safeName(name));
        if (array == null) {
            return items;
        }
        for (int i = 0; i < array.length(); i++) {
            JSONObject json = array.optJSONObject(i);
            if (json == null) {
                continue;
            }
            String uri = json.optString("uri", "");
            if (uri.isEmpty()) {
                continue;
            }
            items.add(new MediaItem(
                    Uri.parse(uri),
                    json.optString("name", "Video"),
                    json.optLong("size", 0),
                    json.optLong("duration", 0)
            ));
        }
        return items;
    }

    private JSONObject getRoot() {
        try {
            return new JSONObject(prefs.getString(KEY_ALBUMS, "{}"));
        } catch (Exception ignored) {
            return new JSONObject();
        }
    }

    private void saveRoot(JSONObject root) {
        prefs.edit().putString(KEY_ALBUMS, root.toString()).apply();
    }

    private String safeName(String name) {
        return name == null ? "" : name.trim();
    }
}
