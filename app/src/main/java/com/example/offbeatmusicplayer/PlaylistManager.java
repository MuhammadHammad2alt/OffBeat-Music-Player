package com.example.offbeatmusicplayer;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class PlaylistManager {

    private static final String PREFS_NAME     = "OffBeatPrefs";
    private static final String KEY_PLAYLIST   = "playlist";
    private static final String KEY_FAVOURITES = "favourites";

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // ─── Playlist ─────────────────────────────────────────────────────────────

    public static void addToPlaylist(Context context, MusicModel song) {
        ArrayList<MusicModel> playlist = getPlaylist(context);
        // Avoid duplicates
        for (MusicModel s : playlist) {
            if (s.getSongPath().equals(song.getSongPath())) return;
        }
        playlist.add(song);
        saveList(context, KEY_PLAYLIST, playlist);
    }

    public static void removeFromPlaylist(Context context, MusicModel song) {
        ArrayList<MusicModel> playlist = getPlaylist(context);
        playlist.removeIf(s -> s.getSongPath().equals(song.getSongPath()));
        saveList(context, KEY_PLAYLIST, playlist);
    }

    public static ArrayList<MusicModel> getPlaylist(Context context) {
        return loadList(context, KEY_PLAYLIST);
    }

    public static boolean isInPlaylist(Context context, MusicModel song) {
        for (MusicModel s : getPlaylist(context)) {
            if (s.getSongPath().equals(song.getSongPath())) return true;
        }
        return false;
    }

    // ─── Favourites ───────────────────────────────────────────────────────────

    public static void addToFavourites(Context context, MusicModel song) {
        ArrayList<MusicModel> favs = getFavourites(context);
        for (MusicModel s : favs) {
            if (s.getSongPath().equals(song.getSongPath())) return;
        }
        favs.add(song);
        saveList(context, KEY_FAVOURITES, favs);
    }

    public static void removeFromFavourites(Context context, MusicModel song) {
        ArrayList<MusicModel> favs = getFavourites(context);
        favs.removeIf(s -> s.getSongPath().equals(song.getSongPath()));
        saveList(context, KEY_FAVOURITES, favs);
    }

    public static ArrayList<MusicModel> getFavourites(Context context) {
        return loadList(context, KEY_FAVOURITES);
    }

    public static boolean isInFavourites(Context context, MusicModel song) {
        for (MusicModel s : getFavourites(context)) {
            if (s.getSongPath().equals(song.getSongPath())) return true;
        }
        return false;
    }

    // ─── Save / Load ──────────────────────────────────────────────────────────

    private static void saveList(Context context, String key, ArrayList<MusicModel> list) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putString(key, new Gson().toJson(list));
        editor.apply();
    }

    private static ArrayList<MusicModel> loadList(Context context, String key) {
        String json = getPrefs(context).getString(key, null);
        if (json == null) return new ArrayList<>();
        Type type = new TypeToken<ArrayList<MusicModel>>(){}.getType();
        ArrayList<MusicModel> list = new Gson().fromJson(json, type);
        return list != null ? list : new ArrayList<>();
    }
}