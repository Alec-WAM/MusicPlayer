package alec_wam.musicplayer.database;

import java.util.HashSet;
import java.util.Set;

public class MusicArtist {

    private String name;
    private Set<String> albumIds;

    public MusicArtist(String name) {
        this.name = name;
        this.albumIds = new HashSet<>();
    }

    public String getName() {
        return name;
    }

    public void addAlbum(String albumId){
        this.albumIds.add(albumId);
    }

    public Set<String> getAlbumIds() {
        return this.albumIds;
    }
}
