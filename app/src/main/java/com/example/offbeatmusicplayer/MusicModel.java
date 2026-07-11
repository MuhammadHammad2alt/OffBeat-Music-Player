package com.example.offbeatmusicplayer;

public class MusicModel {

    private String songName;
    private String songPath;
    private String artist;
    private long   duration;
    private long   albumId;  // NEW — used to load album art

    public MusicModel(String songName, String songPath, String artist, long duration, long albumId) {
        this.songName = songName;
        this.songPath = songPath;
        this.artist   = artist;
        this.duration = duration;
        this.albumId  = albumId;
    }

    // Old constructor so nothing breaks
    public MusicModel(String songName, String songPath) {
        this.songName = songName;
        this.songPath = songPath;
        this.artist   = "Unknown Artist";
        this.duration = 0;
        this.albumId  = -1;
    }

    public MusicModel(String songName, String songPath, String artist, long duration) {
        this.songName = songName;
        this.songPath = songPath;
        this.artist   = artist;
        this.duration = duration;
        this.albumId  = -1;
    }

    public String getSongName() { return songName; }
    public String getSongPath() { return songPath; }
    public String getArtist()   { return artist;   }
    public long   getDuration() { return duration; }
    public long   getAlbumId()  { return albumId;  }

    public void setArtist(String artist)   { this.artist   = artist;   }
    public void setDuration(long duration) { this.duration = duration; }
}