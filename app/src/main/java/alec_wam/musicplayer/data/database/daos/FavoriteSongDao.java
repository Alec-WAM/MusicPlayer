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
    void deleteById(long id);

    @Query("SELECT * FROM favorite_songs")
    List<FavoriteSong> getAll();

    @Query("SELECT id FROM favorite_songs")
    LiveData<List<Long>> getAllIds();

    @Query("SELECT id FROM favorite_songs")
    List<Long> getAllIdsSync();

}
