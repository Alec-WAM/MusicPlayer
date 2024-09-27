package alec_wam.musicplayer.data.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "favorite_songs")
public class FavoriteSong {
    @PrimaryKey
    public long id;
}