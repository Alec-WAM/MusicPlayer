package alec_wam.musicplayer.data.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "favorite_songs")
public class FavoriteSong {
    @PrimaryKey
    private long id;

    @ColumnInfo(name = "date_added")
    private long dateAdded;

    public FavoriteSong(long id) {
        this.id = id;
        this.dateAdded = System.currentTimeMillis();
    }

    public void setId(long value){
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setDateAdded(long value){
        this.dateAdded = id;
    }

    public long getDateAdded() {
        return dateAdded;
    }
}