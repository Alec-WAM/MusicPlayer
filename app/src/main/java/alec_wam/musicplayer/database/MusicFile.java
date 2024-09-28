package alec_wam.musicplayer.database;

import android.net.Uri;

public class MusicFile {
    private final String id;
    private final long mediaId;
    private final Uri uri;
    private final String name;
    private final Uri filePath;
    private final int duration;
    private final String albumId;
    private final String album;
    private final String artistId;
    private final String artist;
    private final int track;
    private Uri albumArtUri;

    public MusicFile(String id, long mediaId, Uri uri, String name, Uri filePath, int duration, String albumId, String album, String artistId, String artist, int track){
        this.id = id;
        this.mediaId = mediaId;
        this.uri = uri;
        this.name = name;
        this.filePath = filePath;
        this.duration = duration;
        this.albumId = albumId;
        this.album = album;
        this.artistId = artistId;
        this.artist = artist;
        this.track = track;
    }

    public String getId(){
        return id;
    }

    public long getMediaId() { return mediaId; }

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

    public Uri getFilePath() {
        return this.filePath;
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

    public String getArtistId() {
        return artistId;
    }

    public String getArtist() {
        return artist;
    }

    public int getTrack() {
        return track;
    }
}
