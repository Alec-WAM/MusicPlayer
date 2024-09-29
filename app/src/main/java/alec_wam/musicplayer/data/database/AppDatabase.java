package alec_wam.musicplayer.data.database;

import android.content.Context;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import alec_wam.musicplayer.data.database.daos.FavoriteAlbumDao;
import alec_wam.musicplayer.data.database.daos.FavoriteSongDao;
import alec_wam.musicplayer.data.database.daos.PlaylistDao;
import alec_wam.musicplayer.data.database.daos.PlaylistSongDao;
import alec_wam.musicplayer.data.database.entities.FavoriteAlbum;
import alec_wam.musicplayer.data.database.entities.FavoriteSong;
import alec_wam.musicplayer.data.database.entities.Playlist;
import alec_wam.musicplayer.data.database.entities.PlaylistSong;
import androidx.room.AutoMigration;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

//VERSION 1 = Favorite Songs and Favorite Albums
//VERSION 2 = Playlists

@Database(
        entities = {
            FavoriteSong.class, FavoriteAlbum.class,
            Playlist.class, PlaylistSong.class
        },
        version = 2,
        autoMigrations = {
                @AutoMigration (from = 1, to = 2)
        },
        exportSchema = true
)
public abstract class AppDatabase extends RoomDatabase {
    public abstract FavoriteSongDao favoriteSongDao();
    public abstract FavoriteAlbumDao favoriteAlbumDao();

    public abstract PlaylistDao playlistDao();
    public abstract PlaylistSongDao playlistSongDao();

    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "app_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
