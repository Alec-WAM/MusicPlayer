package alec_wam.musicplayer.data.database;

import android.app.Application;

import java.util.List;

import alec_wam.musicplayer.data.database.daos.FavoriteAlbumDao;
import alec_wam.musicplayer.data.database.daos.FavoriteSongDao;
import alec_wam.musicplayer.data.database.entities.FavoriteAlbum;
import alec_wam.musicplayer.data.database.entities.FavoriteSong;
import androidx.lifecycle.LiveData;

public class AppDatabaseRepository {

    private FavoriteSongDao mFavoriteSongDao;
    private LiveData<List<Long>> mAllFavoriteSongs;

    private FavoriteAlbumDao mFavoriteAlbumDao;
    private LiveData<List<String>> mAllFavoriteAlbums;


    public AppDatabaseRepository(Application application){
        AppDatabase db = AppDatabase.getDatabase(application);
        mFavoriteSongDao = db.favoriteSongDao();
        mAllFavoriteSongs = mFavoriteSongDao.getAllIds();
        mFavoriteAlbumDao = db.favoriteAlbumDao();
        mAllFavoriteAlbums = mFavoriteAlbumDao.getAllIds();
    }

    LiveData<List<Long>> getAllFavoriteSongs(){
        return mAllFavoriteSongs;
    }

    void insertFavoriteSong(long songId) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            FavoriteSong favoriteSong = new FavoriteSong();
            favoriteSong.id = songId;
            mFavoriteSongDao.insert(favoriteSong);
        });
    }

    void deleteFavoriteSong(long songId){
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mFavoriteSongDao.deleteById(songId);
        });
    }

    LiveData<List<String>> getAllFavoriteAlbums(){
        return mAllFavoriteAlbums;
    }

    void insertFavoriteAlbum(String albumId) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            FavoriteAlbum favoriteAlbum = new FavoriteAlbum();
            favoriteAlbum.id = albumId;
            mFavoriteAlbumDao.insert(favoriteAlbum);
        });
    }

    void deleteFavoriteAlbum(String albumId){
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mFavoriteAlbumDao.deleteById(albumId);
        });
    }

}
