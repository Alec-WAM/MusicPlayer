package alec_wam.musicplayer.data.database;

import android.app.Application;

import java.util.List;

import alec_wam.musicplayer.data.database.entities.Playlist;
import alec_wam.musicplayer.data.database.entities.PlaylistSong;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class AppDatabaseViewModel extends AndroidViewModel {

    private AppDatabaseRepository mRepository;

    private final LiveData<List<String>> mAllFavoriteSongs;
    private final LiveData<List<String>> mAllFavoriteSongsSorted;
    private final LiveData<List<String>> mAllFavoriteAlbums;

    private final LiveData<List<Playlist>> mAllPlaylists;

    public AppDatabaseViewModel(@NonNull Application application) {
        super(application);

        mRepository = new AppDatabaseRepository(application);
        mAllFavoriteSongs = mRepository.getAllFavoriteSongIds();
        mAllFavoriteSongsSorted = mRepository.getAllFavoriteSongIdsSorted();
        mAllFavoriteAlbums = mRepository.getAllFavoriteAlbums();
        mAllPlaylists = mRepository.getAllPlaylists();
    }

    //FAV SONG
    public LiveData<List<String>> getAllFavoriteSongs() { return mAllFavoriteSongs; }

    public LiveData<List<String>> getAllFavoriteSongsSorted() { return mAllFavoriteSongsSorted; }

    public void insertFavoriteSong(String songId) { mRepository.insertFavoriteSong(songId); }

    public void deleteFavoriteSong(String songId) { mRepository.deleteFavoriteSong(songId); }

    //FAV ALBUM
    public LiveData<List<String>> getAllFavoriteAlbums() { return mAllFavoriteAlbums; }

    public void insertFavoriteAlbum(String albumId) { mRepository.insertFavoriteAlbum(albumId); }

    public void deleteFavoriteAlbum(String albumId) { mRepository.deleteFavoriteAlbum(albumId); }

    //PLAYLISTS
    public LiveData<List<Playlist>> getAllPlaylists() { return mAllPlaylists; }

    public List<Playlist> getAllPlaylistsSync() { return mRepository.getAllPlaylistsSync(); }

    public LiveData<Playlist> getPlaylist(int playlistId) { return mRepository.getPlaylistById(playlistId); }

    public void insertPlaylist(String name) { mRepository.insertPlaylist(name); }

    public void deletePlaylist(int id) { mRepository.deletePlaylist(id); }

    public void updatePlaylistCoverImage(int playlistId, String filePath){ mRepository.updatePlaylistCoverImage(playlistId, filePath); }

    public LiveData<List<PlaylistSong>> getPlaylistSongs(int playlistId) { return mRepository.getPlaylistSongs(playlistId); }

    public void insertPlaylistSong(int playlistId, String songId) { mRepository.addSongToPlaylist(playlistId, songId); }

    public void deletePlaylistSong(int playlistSongId) { mRepository.deletePlaylistSong(playlistSongId); }

    public int getPlaylistSize(int playlistId) { return mRepository.getPlaylistSize(playlistId); }

    public void updatePlaylistSongPosition(int playlistSongId, int position) { mRepository.updatePlaylistSongPosition(playlistSongId, position); }

    public LiveData<List<String>> getFavoriteSongIdsInPlaylist(int playlistId) { return mRepository.getFavoriteSongIdsInPlaylist(playlistId); }
}
