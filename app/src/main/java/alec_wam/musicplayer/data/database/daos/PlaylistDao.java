package alec_wam.musicplayer.data.database.daos;

import java.util.List;

import alec_wam.musicplayer.data.database.entities.Playlist;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

@Dao
public interface PlaylistDao {

    @Insert
    void insertPlaylist(Playlist playlist);

    @Update
    void updatePlaylist(Playlist playlist);

    @Delete
    void deletePlaylist(Playlist playlist);

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    void deleteById(int playlistId);

    @Query("SELECT * FROM playlists WHERE id = :playlistId")
    LiveData<Playlist> getPlaylistById(int playlistId);

    @Query("SELECT * FROM playlists")
    LiveData<List<Playlist>> getAllPlaylists();

}
