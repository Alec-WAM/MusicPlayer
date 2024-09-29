package alec_wam.musicplayer.data.database;

import android.app.Application;

import java.util.List;

import alec_wam.musicplayer.data.database.daos.FavoriteAlbumDao;
import alec_wam.musicplayer.data.database.daos.FavoriteSongDao;
import alec_wam.musicplayer.data.database.daos.PlaylistDao;
import alec_wam.musicplayer.data.database.daos.PlaylistSongDao;
import alec_wam.musicplayer.data.database.entities.FavoriteAlbum;
import alec_wam.musicplayer.data.database.entities.FavoriteSong;
import alec_wam.musicplayer.data.database.entities.Playlist;
import alec_wam.musicplayer.data.database.entities.PlaylistSong;
import androidx.lifecycle.LiveData;

public class AppDatabaseRepository {

    private FavoriteSongDao mFavoriteSongDao;
    private LiveData<List<String>> mAllFavoriteSongIds;
    private LiveData<List<String>> mAllFavoriteSongIdsSorted;

    private FavoriteAlbumDao mFavoriteAlbumDao;
    private LiveData<List<String>> mAllFavoriteAlbums;

    private PlaylistDao mPlaylistDao;
    private LiveData<List<Playlist>> mAllPlaylists;

    private PlaylistSongDao mPlaylistSongDao;


    public AppDatabaseRepository(Application application){
        AppDatabase db = AppDatabase.getDatabase(application);
        mFavoriteSongDao = db.favoriteSongDao();
        mAllFavoriteSongIds = mFavoriteSongDao.getAllIds();
        mAllFavoriteSongIdsSorted = mFavoriteSongDao.getAllIdsSorted();
        mFavoriteAlbumDao = db.favoriteAlbumDao();
        mAllFavoriteAlbums = mFavoriteAlbumDao.getAllIds();

        mPlaylistDao = db.playlistDao();
        mAllPlaylists = mPlaylistDao.getAllPlaylists();
        mPlaylistSongDao = db.playlistSongDao();
    }

    //FAV SONGS
    public LiveData<List<String>> getAllFavoriteSongIds(){
        return mAllFavoriteSongIds;
    }
    public LiveData<List<String>> getAllFavoriteSongIdsSorted(){
        return mAllFavoriteSongIdsSorted;
    }
    public List<String> getAllFavoriteSongIdsSync(){
        return mFavoriteSongDao.getAllIdsSync();
    }
    public List<String> getAllFavoriteSongIdsSortedSync(){
        return mFavoriteSongDao.getAllIdsSortedSync();
    }

    public void insertFavoriteSong(String songId) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            FavoriteSong favoriteSong = new FavoriteSong(songId);
            mFavoriteSongDao.insert(favoriteSong);
        });
    }

    public void deleteFavoriteSong(String songId){
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mFavoriteSongDao.deleteById(songId);
        });
    }

    //FAV ALBUMS
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

    //PLAYLISTS
    public LiveData<List<Playlist>> getAllPlaylists(){ return mAllPlaylists; }

    public LiveData<Playlist> getPlaylistById(int playlistId){
        return mPlaylistDao.getPlaylistById(playlistId);
    }

    public void insertPlaylist(String name){
        AppDatabase.databaseWriteExecutor.execute(() -> {
            Playlist playlist = new Playlist();
            playlist.name = name;
            mPlaylistDao.insertPlaylist(playlist);
        });
    }

    public void deletePlaylist(int id){
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mPlaylistDao.deleteById(id);
        });
    }

    public LiveData<List<PlaylistSong>> getPlaylistSongs(int playlistId){
        return mPlaylistSongDao.getSongsInPlaylist(playlistId);
    }

    public List<PlaylistSong> getPlaylistSongsSync(int playlistId){
        return mPlaylistSongDao.getSongsInPlaylistSync(playlistId);
    }

    public void addSongToPlaylist(int playlistId, String songId) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            // Get the current playlist size
            int playlistSize = mPlaylistSongDao.getPlaylistSize(playlistId);

            // Create a new PlaylistSong entry
            PlaylistSong newPlaylistSong = new PlaylistSong(playlistId, songId, playlistSize);

            // Insert the new song into the playlist
            mPlaylistSongDao.insertPlaylistSong(newPlaylistSong);
        });
    }

    public void deletePlaylistSong(int id){
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mPlaylistSongDao.deleteById(id);
        });
    }

    public void updatePlaylistSongPosition(int playlistSongId, int position) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mPlaylistSongDao.updateSongPosition(playlistSongId, position);
        });
    }


}
