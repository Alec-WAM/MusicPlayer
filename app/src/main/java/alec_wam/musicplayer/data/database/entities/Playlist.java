package alec_wam.musicplayer.data.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "playlists")
public class Playlist {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;

    @ColumnInfo(name = "cover_image_path")
    public String coverImagePath; // Path to the image file for the playlist cover
}
