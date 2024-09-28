package alec_wam.musicplayer.data.database.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "favorite_songs")
public class FavoriteSong {
    @PrimaryKey
    @NonNull
    private String id;

    @ColumnInfo(name = "date_added")
    private long dateAdded;

    public FavoriteSong(String id) {
        this.id = id;
        this.dateAdded = System.currentTimeMillis();
    }

    public void setId(String value){
        this.id = value;
    }

    public String getId() {
        return id;
    }

    public void setDateAdded(long value){
        this.dateAdded = value;
    }

    public long getDateAdded() {
        return dateAdded;
    }
}