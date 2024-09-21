package alec_wam.musicplayer.services;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.util.logging.Logger;

import alec_wam.musicplayer.data.MusicSessionData;
import androidx.annotation.OptIn;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.session.MediaSession;

public class MusicSessionManager {

    private static final Logger LOGGER = Logger.getLogger("MusicSessionManager");

    private static final String PREFS_NAME = "music_session_prefs";
    private static final String PREF_MUSIC_SESSION = "music_session";

    private SharedPreferences sharedPreferences;
    private Gson gson;

    public MusicSessionManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
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
}
