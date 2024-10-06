package alec_wam.musicplayer.data.database.daos;

import java.util.List;

import alec_wam.musicplayer.data.database.entities.FavoriteAlbum;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface FavoriteAlbumDao {

    @Insert
    void insert(FavoriteAlbum favoriteAlbum);

    @Delete
    void delete(FavoriteAlbum favoriteAlbum);

    @Query("DELETE FROM favorite_albums WHERE id = :id")
    void deleteById(String id);

    @Query("SELECT * FROM favorite_albums")
    List<FavoriteAlbum> getAll();

    @Query("SELECT id FROM favorite_albums")
    LiveData<List<String>> getAllIds();

    @Query("SELECT id FROM favorite_albums")
    List<String> getAllIdsSync();

}
