package alec_wam.musicplayer.data.database.daos;

import java.util.List;

import alec_wam.musicplayer.data.database.entities.PlaylistSong;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface PlaylistSongDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertPlaylistSong(PlaylistSong playlistSong);

    @Update
    void updatePlaylistSong(PlaylistSong playlistSong);

    @Query("UPDATE playlist_song SET position = :position WHERE id = :id")
    void updateSongPosition(int id, int position);

    @Delete
    void deletePlaylistSong(PlaylistSong playlistSong);

    @Query("DELETE FROM playlist_song WHERE id = :id")
    void deleteById(int id);

    @Query("SELECT * FROM playlist_song WHERE playlistId = :playlistId ORDER BY position")
    LiveData<List<PlaylistSong>> getSongsInPlaylist(int playlistId);

    @Query("SELECT * FROM playlist_song WHERE playlistId = :playlistId ORDER BY position")
    List<PlaylistSong> getSongsInPlaylistSync(int playlistId);

    @Query("SELECT COUNT(*) FROM playlist_song WHERE playlistId = :playlistId")
    int getPlaylistSize(int playlistId);

}
