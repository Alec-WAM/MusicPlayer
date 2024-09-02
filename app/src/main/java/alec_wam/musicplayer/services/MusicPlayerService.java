package alec_wam.musicplayer.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.IBinder;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Logger;

import alec_wam.musicplayer.database.MusicDatabase;
import alec_wam.musicplayer.database.MusicFile;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.session.MediaSession;
import androidx.media3.session.MediaSessionService;

public class MusicPlayerService extends MediaSessionService {

    private final Logger LOGGER = Logger.getLogger("MusicPlayerService");

    public static final String INTENT_PLAY_SONG = "MP_PLAY_SONG";
    public static final String BUNDLE_PLAY_SONG_SONG = "song";
    public static final String BUNDLE_PLAY_SONG_ALBUM = "album";
    public static final String INTENT_PAUSE_SONG = "MP_PAUSE_SONG";
    public static final String BUNDLE_PAUSE_SONG = "paused";
    public static final String INTENT_UPDATE_QUEUE = "MP_UPDATE_QUEUE";
    public static final String BUNDLE_UPDATE_QUEUE_SONGS = "songs";
    public static final String INTENT_CLEAR_QUEUE = "MP_CLEAR_QUEUE";

    private MusicFile currentSong;
    private ExoPlayer player;
    private MediaSession mediaSession;
    private Queue<Long> queue = new LinkedList<>();
    private boolean isPaused = false;

    public MusicPlayerService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LOGGER.info("Created MusicService");
        player = new ExoPlayer.Builder(this).build();
        mediaSession = new MediaSession.Builder(this, player).build();

        IntentFilter filter = new IntentFilter();
        filter.addAction(INTENT_PLAY_SONG);
        filter.addAction(INTENT_PAUSE_SONG);
        filter.addAction(INTENT_UPDATE_QUEUE);
        filter.addAction(INTENT_CLEAR_QUEUE);
        LocalBroadcastManager.getInstance(this).registerReceiver(
                queueUpdateReceiver, filter
        );
        LOGGER.info("Registered LocalBroadcastManager");
    }

    private final BroadcastReceiver queueUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                if(INTENT_PLAY_SONG.equalsIgnoreCase(intent.getAction())){
                    LOGGER.info("BroadcastReceiver: Play Song Message Received");
                    long songId = intent.getLongExtra(BUNDLE_PLAY_SONG_SONG, -1);
                    if(songId > -1L) {
                        MusicPlayerService.this.clearQueue();
                        MusicPlayerService.this.playSong(songId);
                        //TODO Handle Album
                    }
                }
                if(INTENT_PAUSE_SONG.equalsIgnoreCase(intent.getAction())) {
                    LOGGER.info("BroadcastReceiver: Pause Song Message Received");
                    boolean paused = intent.getBooleanExtra(BUNDLE_PAUSE_SONG, false);
                    if(paused){
                        MusicPlayerService.this.pauseSong();
                    }
                    else {
                        MusicPlayerService.this.resumeSong();
                    }
                }
                if(INTENT_UPDATE_QUEUE.equalsIgnoreCase(intent.getAction())) {
                    LOGGER.info("BroadcastReceiver: Update Queue Message Received");
                    long[] songs = intent.getLongArrayExtra(BUNDLE_UPDATE_QUEUE_SONGS);
                    final boolean wasEmpty = MusicPlayerService.this.isQueueEmpty();
                    MusicPlayerService.this.addSongs(songs);
                    if(wasEmpty) {
                        MusicPlayerService.this.resumeSong();
                    }
                }
                if(INTENT_CLEAR_QUEUE.equalsIgnoreCase(intent.getAction())) {
                    LOGGER.info("BroadcastReceiver: Clear Queue Message Received");
                    MusicPlayerService.this.clearQueue();
                }
            }
        }
    };

    public boolean isQueueEmpty(){
        return this.queue.isEmpty();
    }

    public MediaItem buildMediaItem(MusicFile musicFile){
        MediaItem mediaItem =
                new MediaItem.Builder()
                        .setMediaId(""+musicFile.getId())
                        .setUri(musicFile.getUri())
                        .setMediaMetadata(
                                new MediaMetadata.Builder()
                                        .setArtist(musicFile.getArtist())
                                        .setTitle(musicFile.getName())
                                        .setArtworkUri(musicFile.getAlbumArtUri())
                                        .build())
                        .build();
        return mediaItem;
    }

    public void addSong(long song){
        MusicFile musicFile = MusicDatabase.SONGS.get(song);
        if(musicFile !=null){
            player.addMediaItem(buildMediaItem(musicFile));
        }
    }

    public void addSongs(long[] songs) {
        for(long song : songs) {
            this.addSong(song);
        }
    }

    public void clearQueue(){
        player.clearMediaItems();
    }

    private void playSong(long songId) {
        LOGGER.info("Playing Song...");
        MusicFile musicFile = MusicDatabase.SONGS.get(songId);
        if(musicFile !=null) {
            MediaItem mediaItem = buildMediaItem(musicFile);
            player.setMediaItem(mediaItem);
            player.prepare();
            player.play();
        }
//        if (mediaPlayer.isPlaying()) {
//            mediaPlayer.stop();
//            mediaPlayer.reset();
//            LOGGER.info("Stopped Current Song");
//        }
//
//        try {
//            mediaPlayer.setDataSource(this, song.getFilePath());
//            mediaPlayer.prepareAsync();
//            LOGGER.info("Preparing Song...");
//            mediaPlayer.setOnPreparedListener(mp -> {
//                LOGGER.info("Song Prepared. Playing...");
//                mp.start();
//                isPreparing = false;
//                isPaused = false;
//            });
//            isPreparing = true;
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    private void pauseSong() {
//        if (mediaPlayer.isPlaying()) {
//            mediaPlayer.pause();
//            isPaused = true;
//        }
        this.player.pause();
    }

    private void resumeSong() {
//        if (isPaused && mediaPlayer != null) {
//            mediaPlayer.start();
//            isPaused = false;
//        }
        this.player.play();
    }

    @Override
    public void onTaskRemoved(@Nullable Intent rootIntent) {
        if (player.getPlayWhenReady()) {
            // Make sure the service is not in foreground.
            player.pause();
        }
        stopSelf();
    }

    @Override
    public void onDestroy() {
        mediaSession.getPlayer().release();
        mediaSession.release();
        mediaSession = null;
        LocalBroadcastManager.getInstance(this).unregisterReceiver(queueUpdateReceiver);
        super.onDestroy();
    }

    @Override
    public MediaSession onGetSession(MediaSession.ControllerInfo controllerInfo) {
        return mediaSession;
    }
}