package alec_wam.musicplayer.data.database.daos;

import java.util.List;

import alec_wam.musicplayer.data.database.entities.FavoriteSong;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface FavoriteSongDao {

    @Insert
    void insert(FavoriteSong favoriteSong);

    @Delete
    void delete(FavoriteSong favoriteAlbum);

    @Query("DELETE FROM favorite_songs WHERE id = :id")
    void deleteById(String id);

    @Query("SELECT * FROM favorite_songs")
    LiveData<List<FavoriteSong>> getAll();

    @Query("SELECT * FROM favorite_songs")
    List<FavoriteSong> getAllSync();

    @Query("SELECT id FROM favorite_songs")
    LiveData<List<String>> getAllIds();

    @Query("SELECT id FROM favorite_songs ORDER BY date_added DESC")
    LiveData<List<String>> getAllIdsSorted();

    @Query("SELECT id FROM favorite_songs")
    List<String> getAllIdsSync();

    @Query("SELECT id FROM favorite_songs ORDER BY date_added DESC")
    List<String> getAllIdsSortedSync();

}
