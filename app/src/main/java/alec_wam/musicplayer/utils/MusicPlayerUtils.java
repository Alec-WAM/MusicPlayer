package alec_wam.musicplayer.utils;

import android.content.Context;
import android.content.Intent;

import java.util.Optional;
import java.util.logging.Logger;

import alec_wam.musicplayer.database.MusicAlbum;
import alec_wam.musicplayer.database.MusicFile;
import alec_wam.musicplayer.services.MusicPlayerService;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class MusicPlayerUtils {
    private final static Logger LOGGER = Logger.getLogger("MusicPlayerUtils");

    public static final String INTENT_SONG_CHANGE = "MP_SONG_CHANGE";
    public static final String BUNDLE_SONG_CHANGE_SONG = "song";
    public static final String BUNDLE_SONG_CHANGE_ALBUM = "album";

    public static void playSong(Context context, long songId, Optional<String> albumId){
        Intent intent = new Intent(MusicPlayerService.INTENT_PLAY_SONG);
        intent.putExtra(MusicPlayerService.BUNDLE_PLAY_SONG_SONG, songId);
        if(albumId.isPresent()) {
            intent.putExtra(MusicPlayerService.BUNDLE_PLAY_SONG_ALBUM, albumId.get());
        }
        LOGGER.info("Sending Play Song Message");
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public static void broadcastSongChange(Context context, long songId){
        Intent intent = new Intent(INTENT_SONG_CHANGE);
        intent.putExtra(BUNDLE_SONG_CHANGE_SONG, songId);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
