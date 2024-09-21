package alec_wam.musicplayer.database;

import android.graphics.Bitmap;
import android.net.Uri;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import alec_wam.musicplayer.utils.Utils;

public class MusicAlbum {
    private final String albumId;
    private final String name;
    private final String artist;
    private Uri albumArtUri;
    private Bitmap cover;
    private Map<Integer, List<MusicFile>> albumMusic;


    public MusicAlbum(String albumId, String name, String artist) {
        this.albumId = albumId;
        this.name = name;
        this.artist = artist;
        this.albumMusic = new HashMap<>();
    }

    public String getAlbumId() {
        return albumId;
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

    public void addMusic(MusicFile music){
        int diskNumber = Utils.getDiskNumber(music.getTrack());
        List<MusicFile> tracks = this.albumMusic.getOrDefault(diskNumber, new ArrayList<MusicFile>());
        tracks.add(music);
        this.albumMusic.put(diskNumber, tracks);
    }

    public Map<Integer, List<MusicFile>> getAlbumMusic(){
        return this.albumMusic;
    }

    public List<MusicFile> getAllMusicFiles() {
        return getAlbumMusic().values().stream()
                .flatMap(List::stream).sorted(Comparator.comparingInt(MusicFile::getTrack)).collect(Collectors.toList());
    }
}
