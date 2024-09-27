package alec_wam.musicplayer.data.database.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "favorite_albums")
public class FavoriteAlbum {
    @PrimaryKey
    @NonNull
    public String id;
}