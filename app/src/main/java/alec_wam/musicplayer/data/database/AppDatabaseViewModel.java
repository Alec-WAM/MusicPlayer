package alec_wam.musicplayer.data.database;

import android.app.Application;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class AppDatabaseViewModel extends AndroidViewModel {

    private AppDatabaseRepository mRepository;

    private final LiveData<List<String>> mAllFavoriteSongs;
    private final LiveData<List<String>> mAllFavoriteSongsSorted;
    private final LiveData<List<String>> mAllFavoriteAlbums;

    public AppDatabaseViewModel(@NonNull Application application) {
        super(application);

        mRepository = new AppDatabaseRepository(application);
        mAllFavoriteSongs = mRepository.getAllFavoriteSongIds();
        mAllFavoriteSongsSorted = mRepository.getAllFavoriteSongIdsSorted();
        mAllFavoriteAlbums = mRepository.getAllFavoriteAlbums();
    }

    public LiveData<List<String>> getAllFavoriteSongs() { return mAllFavoriteSongs; }

    public LiveData<List<String>> getAllFavoriteSongsSorted() { return mAllFavoriteSongsSorted; }

    public void insertFavoriteSong(String songId) { mRepository.insertFavoriteSong(songId); }

    public void deleteFavoriteSong(String songId) { mRepository.deleteFavoriteSong(songId); }

    public LiveData<List<String>> getAllFavoriteAlbums() { return mAllFavoriteAlbums; }

    public void insertFavoriteAlbum(String albumId) { mRepository.insertFavoriteAlbum(albumId); }

    public void deleteFavoriteAlbum(String albumId) { mRepository.deleteFavoriteAlbum(albumId); }
}
