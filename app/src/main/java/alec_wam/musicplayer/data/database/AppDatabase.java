package alec_wam.musicplayer.data.database;

import android.content.Context;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import alec_wam.musicplayer.data.database.daos.FavoriteAlbumDao;
import alec_wam.musicplayer.data.database.daos.FavoriteSongDao;
import alec_wam.musicplayer.data.database.entities.FavoriteAlbum;
import alec_wam.musicplayer.data.database.entities.FavoriteSong;
import androidx.room.AutoMigration;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

//VERSION 1 = Favorite Songs
//VERSION 2 = Added Favorite Albums

@Database(
        entities = {FavoriteSong.class, FavoriteAlbum.class},
        version = 1,
        exportSchema = true
)
public abstract class AppDatabase extends RoomDatabase {
    public abstract FavoriteSongDao favoriteSongDao();
    public abstract FavoriteAlbumDao favoriteAlbumDao();

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
