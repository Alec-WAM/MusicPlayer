package alec_wam.musicplayer.services;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import alec_wam.musicplayer.data.MusicSessionData;
import androidx.annotation.OptIn;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.session.MediaSession;

public class MusicPlayerSavedDataManager {

    private static final Logger LOGGER = Logger.getLogger("MusicPlayerSavedDataManager");

    private static final String MUSIC_SESSION_PREFS = "music_player_prefs";
    private static final String PREF_MUSIC_SESSION = "music_session";
    private static final String PREF_RECENT_ALBUMS = "recent_albums";

    private SharedPreferences sharedPreferences;
    private Gson gson;

    private List<String> recentAlbums;

    public MusicPlayerSavedDataManager(Context context) {
        sharedPreferences = context.getSharedPreferences(MUSIC_SESSION_PREFS, Context.MODE_PRIVATE);
        gson = new Gson();
        recentAlbums = new ArrayList<>();
        loadRecentAlbums();
    }

    public void saveMusicSession(ExoPlayer player){
        MusicSessionData sessionData = MusicSessionData.saveFromPlayer(player);

        String jsonMediaSession = gson.toJson(sessionData);

        // Save JSON string to SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PREF_MUSIC_SESSION, jsonMediaSession);
        editor.apply();
    }

    public long loadMusicSession(ExoPlayer player) {
        String jsonMediaSession = sharedPreferences.getString(PREF_MUSIC_SESSION, null);
        LOGGER.info("Loading Music Session");
        if (jsonMediaSession != null) {
            LOGGER.info("Found Saved Session");
            LOGGER.info(jsonMediaSession);
            MusicSessionData sessionData = MusicSessionData.loadFromJSON(gson, jsonMediaSession);
            if(sessionData != null) {
                return sessionData.loadToPlayer(player);
            }
        }
        return -1;
    }

    @OptIn(markerClass = UnstableApi.class)
    public MediaSession.MediaItemsWithStartPosition getResumption() {
        String jsonMediaSession = sharedPreferences.getString(PREF_MUSIC_SESSION, null);
        if (jsonMediaSession != null) {
            MusicSessionData sessionData = MusicSessionData.loadFromJSON(gson, jsonMediaSession);
            return new MediaSession.MediaItemsWithStartPosition(sessionData.getMediaItems(), sessionData.playingSongIndex, sessionData.playingSongProgress);
        }
        return null;
    }

    //RECENT ALBUMS
    public void addRecentAlbum(String albumId){
        this.recentAlbums.remove(albumId);
        this.recentAlbums.add(albumId);
        if(this.recentAlbums.size() > 5){
            this.recentAlbums.remove(0); //Remove last item
        }
        this.saveRecentAlbums();
    }


    public List<String> getRecentAlbums(){
        return this.recentAlbums;
    }

    public void loadRecentAlbums(){
        String jsonRecentAlbums = sharedPreferences.getString(PREF_RECENT_ALBUMS, null);
        LOGGER.info("Loading Recent Albums");
        if (jsonRecentAlbums != null) {
            Type type = new TypeToken<List<String>>() {}.getType();
            List<String> list = gson.fromJson(jsonRecentAlbums, type);
            this.recentAlbums.clear();
            for(String album : list){
                this.recentAlbums.add(album);
            }
        }
    }

    public void saveRecentAlbums(){
        LOGGER.info("Saving Recent Albums: " + this.recentAlbums.toString());
        String jsonRecentAlbums = gson.toJson(this.recentAlbums);

        // Save JSON string to SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PREF_RECENT_ALBUMS, jsonRecentAlbums);
        editor.apply();
    }
}
