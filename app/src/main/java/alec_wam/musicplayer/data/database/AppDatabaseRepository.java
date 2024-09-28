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

    public LiveData<List<Long>> getAllFavoriteSongs(){
        return mAllFavoriteSongs;
    }

    public List<Long> getAllFavoriteSongsSync(){
        return mFavoriteSongDao.getAllIdsSync();
    }

    public void insertFavoriteSong(long songId) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            FavoriteSong favoriteSong = new FavoriteSong();
            favoriteSong.id = songId;
            mFavoriteSongDao.insert(favoriteSong);
        });
    }

    public void deleteFavoriteSong(long songId){
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mFavoriteSongDao.deleteById(songId);
        });
    }

    public LiveData<List<String>> getAllFavoriteAlbums(){
        return mAllFavoriteAlbums;
    }

    public void insertFavoriteAlbum(String albumId) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            FavoriteAlbum favoriteAlbum = new FavoriteAlbum();
            favoriteAlbum.id = albumId;
            mFavoriteAlbumDao.insert(favoriteAlbum);
        });
    }

    public void deleteFavoriteAlbum(String albumId){
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mFavoriteAlbumDao.deleteById(albumId);
        });
    }

}
