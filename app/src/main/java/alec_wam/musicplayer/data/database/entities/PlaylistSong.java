package alec_wam.musicplayer.data.database.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "playlist_song",
        foreignKeys = @ForeignKey(entity = Playlist.class, parentColumns = "id", childColumns = "playlistId", onDelete = ForeignKey.CASCADE),
        indices = {@Index(value = "playlistId")}
)
public class PlaylistSong {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public int playlistId;  // Foreign key linking to Playlist
    public String songId;  // ID of the song
    public int position;  // Position of the song in the playlist

    public PlaylistSong(int playlistId, String songId, int position) {
        this.playlistId = playlistId;
        this.songId = songId;
        this.position = position;
    }
}
