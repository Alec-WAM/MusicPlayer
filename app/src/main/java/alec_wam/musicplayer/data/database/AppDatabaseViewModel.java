package alec_wam.musicplayer.data.database;

import android.app.Application;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class AppDatabaseViewModel extends AndroidViewModel {

    private AppDatabaseRepository mRepository;

    private final LiveData<List<Long>> mAllFavoriteSongs;
    private final LiveData<List<String>> mAllFavoriteAlbums;

    public AppDatabaseViewModel(@NonNull Application application) {
        super(application);

        mRepository = new AppDatabaseRepository(application);
        mAllFavoriteSongs = mRepository.getAllFavoriteSongs();
        mAllFavoriteAlbums = mRepository.getAllFavoriteAlbums();
    }

    public LiveData<List<Long>> getAllFavoriteSongs() { return mAllFavoriteSongs; }

    public void insertFavoriteSong(long songId) { mRepository.insertFavoriteSong(songId); }

    public void deleteFavoriteSong(long songId) { mRepository.deleteFavoriteSong(songId); }

    public LiveData<List<String>> getAllFavoriteAlbums() { return mAllFavoriteAlbums; }

    public void insertFavoriteAlbum(String albumId) { mRepository.insertFavoriteAlbum(albumId); }

    public void deleteFavoriteAlbum(String albumId) { mRepository.deleteFavoriteAlbum(albumId); }
}
