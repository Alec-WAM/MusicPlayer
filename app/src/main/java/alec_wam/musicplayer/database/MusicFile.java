package alec_wam.musicplayer.database;

import android.net.Uri;

public class MusicFile {
    private final Uri uri;
    private final String name;
    private final int duration;
    private final String albumId;
    private final String album;
    private final String artist;
    private final int track;
    private Uri albumArtUri;

    public MusicFile(Uri uri, String name, int duration, String albumId, String album, String artist, int track){
        this.uri = uri;
        this.name = name;
        this.duration = duration;
        this.albumId = albumId;
        this.album = album;
        this.artist = artist;
        this.track = track;
    }

    public void setAlbumArtUri(Uri albumArtUri) {
        this.albumArtUri = albumArtUri;
    }

    public Uri getAlbumArtUri() {
        return this.albumArtUri;
    }

    public Uri getUri() {
        return uri;
    }

    public String getName() {
        return name;
    }

    public int getDuration() {
        return duration;
    }

    public String getAlbumId() {
        return albumId;
    }

    public String getAlbum() {
        return album;
    }

    public String getArtist() {
        return artist;
    }

    public int getTrack() {
        return track;
    }
}
