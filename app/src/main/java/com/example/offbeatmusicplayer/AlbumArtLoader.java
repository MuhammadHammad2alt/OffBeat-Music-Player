package com.example.offbeatmusicplayer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.widget.ImageView;

import java.io.InputStream;

public class AlbumArtLoader {

    // Colored fallback backgrounds when no album art exists
    private static final int[] fallbackColors = {
            R.drawable.album_art_bg_purple,
            R.drawable.album_art_bg_pink,
            R.drawable.album_art_bg_orange,
            R.drawable.album_art_bg_teal,
            R.drawable.album_art_bg_green
    };

    /**
     * Loads album art into an ImageView.
     * If no art found, shows a colored background with music icon.
     */
    public static void load(Context context, long albumId, int position,
                            ImageView artImageView, ImageView iconImageView) {
        Bitmap bitmap = getAlbumArt(context, albumId);

        if (bitmap != null) {
            // Real album art found — show it, hide the music note icon
            artImageView.setBackground(null);
            artImageView.setImageBitmap(bitmap);
            artImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            if (iconImageView != null) iconImageView.setVisibility(android.view.View.GONE);
        } else {
            // No art — show colored background + music icon
            artImageView.setImageBitmap(null);
            artImageView.setBackgroundResource(fallbackColors[position % fallbackColors.length]);
            artImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            if (iconImageView != null) iconImageView.setVisibility(android.view.View.VISIBLE);
        }
    }

    public static Bitmap getAlbumArt(Context context, long albumId) {
        if (albumId < 0) return null;
        try {
            Uri albumArtUri = Uri.parse("content://media/external/audio/albumart");
            Uri artUri = Uri.withAppendedPath(albumArtUri, String.valueOf(albumId));
            InputStream inputStream = context.getContentResolver().openInputStream(artUri);
            if (inputStream != null) {
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                inputStream.close();
                return bitmap;
            }
        } catch (Exception e) {
            // No album art available
        }
        return null;
    }
}