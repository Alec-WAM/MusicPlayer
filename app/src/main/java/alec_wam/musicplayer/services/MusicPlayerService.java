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

import alec_wam.musicplayer.database.MusicDatabase;
import alec_wam.musicplayer.database.MusicFile;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class MusicPlayerService extends Service {

    public static final String INTENT_PAUSE_SONG = "MP_PAUSE_SONG";
    public static final String BUNDLE_PAUSE_SONG = "paused";
    public static final String INTENT_UPDATE_QUEUE = "MP_UPDATE_QUEUE";
    public static final String BUNDLE_UPDATE_QUEUE_SONGS = "songs";
    public static final String INTENT_CLEAR_QUEUE = "MP_CLEAR_QUEUE";

    private MediaPlayer mediaPlayer;
    private MusicFile currentSong;
    private Queue<Long> queue = new LinkedList<>();
    private boolean isPreparing = false;
    private boolean isPaused = false;

    public MusicPlayerService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(mp -> playNextSong());

        IntentFilter filter = new IntentFilter();
        filter.addAction(INTENT_UPDATE_QUEUE);
        filter.addAction(INTENT_CLEAR_QUEUE);
        LocalBroadcastManager.getInstance(this).registerReceiver(
                queueUpdateReceiver, filter
        );
    }

    private final BroadcastReceiver queueUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                if(INTENT_PAUSE_SONG.equalsIgnoreCase(intent.getAction())) {
                    boolean paused = intent.getBooleanExtra(BUNDLE_PAUSE_SONG, false);
                    if(paused){
                        MusicPlayerService.this.pauseSong();
                    }
                    else {
                        MusicPlayerService.this.resumeSong();
                    }
                }
                if(INTENT_UPDATE_QUEUE.equalsIgnoreCase(intent.getAction())) {
                    long[] songs = intent.getLongArrayExtra(BUNDLE_UPDATE_QUEUE_SONGS);
                    final boolean wasEmpty = MusicPlayerService.this.isQueueEmpty();
                    MusicPlayerService.this.addSongs(songs);
                    if(wasEmpty) {
                        MusicPlayerService.this.playNextSong();
                    }
                }
                if(INTENT_CLEAR_QUEUE.equalsIgnoreCase(intent.getAction())) {
                    MusicPlayerService.this.clearQueue();
                }
            }
        }
    };

    public boolean isQueueEmpty(){
        return this.queue.isEmpty();
    }

    public void addSongs(long[] songs) {
        for(long song : songs) {
            this.queue.add(song);
        }
    }

    public void clearQueue(){
        this.queue.clear();
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.reset();
        }
    }

    private void playSong(MusicFile song) {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.reset();
        }

        try {
            mediaPlayer.setDataSource(this, song.getFilePath());
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(mp -> {
                mp.start();
                isPreparing = false;
                isPaused = false;
            });
            isPreparing = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void playNextSong() {
        if (queue != null && !queue.isEmpty()) {
            long songId = queue.poll();
            MusicFile foundSong = MusicDatabase.SONGS.get(songId);
            this.currentSong = foundSong;
            if(foundSong == null){
                this.playNextSong();
                return;
            }
            playSong(currentSong);
        }
    }

    private void pauseSong() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPaused = true;
        }
    }

    private void resumeSong() {
        if (isPaused && mediaPlayer != null) {
            mediaPlayer.start();
            isPaused = false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(queueUpdateReceiver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}