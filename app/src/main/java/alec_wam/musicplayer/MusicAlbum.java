package alec_wam.musicplayer;

import android.graphics.Bitmap;
import android.net.Uri;

public class MusicAlbum {
    private final String name;
    private final String artist;
    private Uri albumArtUri;
    private Bitmap cover;

    public MusicAlbum(String name, String artist) {
        this.name = name;
        this.artist = artist;
    }

    public String getName() {
        return name;
    }

    public String getArtist() {
        return artist;
    }

    public Uri getAlbumArtUri() {
        return albumArtUri;
    }

    public void setAlbumArtUri(Uri albumArtUri) {
        this.albumArtUri = albumArtUri;
    }

    public void setCover(Bitmap bitmap) {
        this.cover = bitmap;
    }

    public Bitmap getCover() {
        return cover;
    }
}
