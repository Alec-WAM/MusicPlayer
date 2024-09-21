package alec_wam.musicplayer.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import alec_wam.musicplayer.database.MusicAlbum;
import alec_wam.musicplayer.database.MusicArtist;
import alec_wam.musicplayer.database.MusicDatabase;
import alec_wam.musicplayer.database.MusicFile;
import alec_wam.musicplayer.utils.MusicPlayerUtils;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.common.util.Util;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.session.LibraryResult;
import androidx.media3.session.MediaLibraryService;
import androidx.media3.session.MediaSession;
import androidx.media3.session.SessionError;

public class MusicPlayerService extends MediaLibraryService {

    private final static Logger LOGGER = Logger.getLogger("MusicPlayerService");

    public static final String INTENT_PLAY_SONG = "MP_PLAY_SONG";
    public static final String BUNDLE_PLAY_SONG_SONG = "song";
    public static final String BUNDLE_PLAY_SONG_ALBUM = "album";
    public static final String INTENT_PAUSE_SONG = "MP_PAUSE_SONG";
    public static final String BUNDLE_PAUSE_SONG = "paused";
    public static final String INTENT_UPDATE_QUEUE = "MP_UPDATE_QUEUE";
    public static final String BUNDLE_UPDATE_QUEUE_SONGS = "songs";
    public static final String INTENT_CLEAR_QUEUE = "MP_CLEAR_QUEUE";

    public static MediaItem currentSong;
    private ExoPlayer player;
    private MediaLibrarySession mediaLibrarySession;
    private Queue<Long> queue = new LinkedList<>();
    private boolean isPaused = false;

    private MusicSessionManager sessionManager;

    public MusicPlayerService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LOGGER.info("Created MusicService");
        player = new ExoPlayer.Builder(this).build();
        sessionManager = new MusicSessionManager(this.getApplicationContext());
        mediaLibrarySession = new MediaLibrarySession.Builder(this, player, new LibrarySessionCallback(sessionManager)).build();


        player.addListener(new Player.Listener() {
            @Override
            public void onMediaItemTransition(
                    @Nullable MediaItem mediaItem,
                    @Player.MediaItemTransitionReason int reason
            ){
                MusicPlayerService.this.currentSong = mediaItem;
                long mediaId = -1;
                if(mediaItem !=null) {
                    mediaId = Long.parseLong(mediaItem.mediaId);
                }
                MusicPlayerUtils.broadcastSongChange(MusicPlayerService.this, mediaId);
            }
        });

        IntentFilter filter = new IntentFilter();
        filter.addAction(INTENT_PLAY_SONG);
        filter.addAction(INTENT_PAUSE_SONG);
        filter.addAction(INTENT_UPDATE_QUEUE);
        filter.addAction(INTENT_CLEAR_QUEUE);
        LocalBroadcastManager.getInstance(this).registerReceiver(
                queueUpdateReceiver, filter
        );
        LOGGER.info("Registered LocalBroadcastManager");

        sessionManager.loadMusicSession(this.player);
    }

    public class LibrarySessionCallback implements MediaLibraryService.MediaLibrarySession.Callback {

        private final MusicSessionManager sessionManager;

        public LibrarySessionCallback(MusicSessionManager sessionManager) {
            this.sessionManager = sessionManager;
        }

        @Override
        @OptIn(markerClass = UnstableApi.class)
        public ListenableFuture<MediaSession.MediaItemsWithStartPosition> onPlaybackResumption(
                MediaSession mediaSession,
                MediaSession.ControllerInfo controller
        ) {
            SettableFuture<MediaSession.MediaItemsWithStartPosition> settableFuture = SettableFuture.create();
            settableFuture.addListener(() -> {
                // Your app is responsible for storing the playlist and the start position
                // to use here
                MediaSession.MediaItemsWithStartPosition resumptionPlaylist = sessionManager.getResumption();
                settableFuture.set(resumptionPlaylist);
            }, MoreExecutors.directExecutor());
            return settableFuture;
        }

        @NonNull
        @Override
        @UnstableApi
        public ListenableFuture<MediaSession.MediaItemsWithStartPosition> onSetMediaItems(
                @NonNull MediaSession mediaSession,
                @NonNull MediaSession.ControllerInfo controller,
                @NonNull List<MediaItem> mediaItems,
                int startIndex,
                long startPositionMs) {
            LOGGER.info("Set Media Items: " + mediaItems + " Index: " + startIndex);
            if(mediaItems.size() == 1){
                return buildAlbumMediaItemList(mediaItems.get(0), startIndex, startPositionMs);
            }

            return Util.transformFutureAsync(
                    onAddMediaItems(mediaSession, controller, mediaItems),
                    (mediaItemList) -> {
                        LOGGER.info("Added Items: " + mediaItemList);
                        return Futures.immediateFuture(
                                    new MediaSession.MediaItemsWithStartPosition(mediaItemList, startIndex, startPositionMs));
                    });
        }

        @OptIn(markerClass = UnstableApi.class)
        private ListenableFuture<MediaSession.MediaItemsWithStartPosition> buildAlbumMediaItemList(MediaItem mediaItem, int startIndex, long startPositionMs){
            List<MediaItem> mediaItemList = new ArrayList<>();
            int indexInAlbum = startIndex;

            long mediaId = Long.parseLong(mediaItem.mediaId);
            MusicFile musicFile = MusicDatabase.SONGS.get(mediaId);
            boolean addedAlbum = false;
            if(musicFile !=null){
                MusicAlbum album = MusicDatabase.getAlbumById(musicFile.getAlbumId());
                if(album !=null){
                    List<MusicFile> albumMusicFiles = album.getAllMusicFiles();
                    for(int i = 0; i < albumMusicFiles.size(); i++) {
                        MusicFile albumMusicFile = albumMusicFiles.get(i);
                        if(albumMusicFile.getId() == mediaId){
                            indexInAlbum = i;
                        }
                        mediaItemList.add(MusicPlayerService.buildMediaItem(albumMusicFile));
                    }
                    addedAlbum = true;
                }
            }

            if(!addedAlbum){
                if (musicFile != null) {
                    mediaItemList.add(MusicPlayerService.buildMediaItem(musicFile));
                    indexInAlbum = 0;
                }
            }

            return Futures.immediateFuture(
                    new MediaSession.MediaItemsWithStartPosition(mediaItemList, indexInAlbum, startPositionMs));
        }

        @NonNull
        @Override
        public ListenableFuture<List<MediaItem>> onAddMediaItems(
                @NonNull MediaSession mediaSession, MediaSession.ControllerInfo controller, List<MediaItem> mediaItems) {
            List<MediaItem> fixedMediaItems = new ArrayList<>();
            for (MediaItem mediaItem : mediaItems) {
                if (mediaItem.localConfiguration == null) {
                    MusicFile musicFile = MusicDatabase.SONGS.get(Long.parseLong(mediaItem.mediaId));
                    if (musicFile != null) {
                        fixedMediaItems.add(MusicPlayerService.buildMediaItem(musicFile));
                    }
                }
            }
            return Futures.immediateFuture(fixedMediaItems);
        }

        @NonNull
        @Override
        public ListenableFuture<LibraryResult<MediaItem>> onGetLibraryRoot(
                @NonNull MediaLibrarySession session,
                @NonNull MediaSession.ControllerInfo controller,
                @NonNull LibraryParams params) {

            MediaItem rootMediaItem = new MediaItem.Builder()
                    .setMediaId("root_library")
                    .setMediaMetadata(new MediaMetadata.Builder()
                            .setIsBrowsable(true)
                            .setIsPlayable(false)
                            .build()
                    )
                    .build();

            // Return the root items as a LibraryResult
            LibraryResult libraryRootResult = LibraryResult.ofItem(rootMediaItem, params);

            return Futures.immediateFuture(libraryRootResult);
        }

        @NonNull
        @Override
        public ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> onGetChildren(
                MediaLibrarySession session,
                MediaSession.ControllerInfo browser,
                String parentId,
                @IntRange(from = 0) int page,
                @IntRange(from = 1) int pageSize,
                @Nullable LibraryParams params) {
            List<MediaItem> mediaItems = new ArrayList<>();

            if(parentId.equalsIgnoreCase("root_library")){
                // MediaItem for "Artists"
                MediaItem artistsCategory = new MediaItem.Builder()
                        .setMediaId("root_artists")
                        .setMediaMetadata(new MediaMetadata.Builder()
                                .setTitle("Artists")
                                .setIsBrowsable(true)
                                .setIsPlayable(false)
                                .build())
                        .build();

                // MediaItem for "Albums"
                MediaItem albumsCategory = new MediaItem.Builder()
                        .setMediaId("root_albums")
                        .setMediaMetadata(new MediaMetadata.Builder()
                                .setTitle("Albums")
                                .setIsBrowsable(true)
                                .setIsPlayable(false)
                                .build())
                        .build();

                // Add these root items to the list
                mediaItems.add(artistsCategory);
                mediaItems.add(albumsCategory);
            }
            else if(parentId.equalsIgnoreCase("root_artists")){
                for(MusicArtist artist : MusicDatabase.ARTISTS.values()){
                    MediaItem artistMediaItem = new MediaItem.Builder()
                            .setMediaId("artist_" + artist.getId())
                            .setMediaMetadata(new MediaMetadata.Builder()
                                    .setTitle(artist.getName())
                                    .setIsBrowsable(true)
                                    .setIsPlayable(false)
                                    .build())
                            .build();
                    mediaItems.add(artistMediaItem);
                }
            }
            else if(parentId.startsWith("artist_")){
                String artistId = parentId.substring("artist_".length());

                MusicArtist artist = MusicDatabase.ARTISTS.get(artistId);

                if(artist !=null){
                    for(String albumId : artist.getAlbumIds()){
                        MusicAlbum album = MusicDatabase.ALBUMS.get(albumId);
                        if(album !=null){
                            MediaItem albumMediaItem = new MediaItem.Builder()
                                    .setMediaId("album_" + album.getAlbumId())
                                    .setMediaMetadata(new MediaMetadata.Builder()
                                            .setTitle(album.getName())
                                            .setArtworkUri(album.getAlbumArtUri())
                                            .setArtist(album.getArtist())
                                            .setIsBrowsable(true)
                                            .setIsPlayable(false)
                                            .build())
                                    .build();
                            mediaItems.add(albumMediaItem);
                        }
                    }
                }
            }
            else if(parentId.equalsIgnoreCase("root_albums")){
                for(MusicAlbum album : MusicDatabase.ALBUMS.values()){
                    MediaItem albumMediaItem = new MediaItem.Builder()
                            .setMediaId("album_" + album.getAlbumId())
                            .setMediaMetadata(new MediaMetadata.Builder()
                                    .setTitle(album.getName())
                                    .setArtworkUri(album.getAlbumArtUri())
                                    .setArtist(album.getArtist())
                                    .setIsBrowsable(true)
                                    .setIsPlayable(false)
                                    .build())
                            .build();
                    mediaItems.add(albumMediaItem);
                }
            }
            else if(parentId.startsWith("album_")){
                String albumId = parentId.substring("album_".length());

                MusicAlbum album = MusicDatabase.ALBUMS.get(albumId);

                if(album !=null){
                    for(MusicFile musicFile : album.getAllMusicFiles()){
                        if(musicFile !=null){
                            LOGGER.info("URI: " + musicFile.getUri());
                            MediaItem mediaItem = MusicPlayerService.buildBrowserMediaItem(musicFile, true);
                            LOGGER.info("Has RequestMeta: " + (mediaItem.requestMetadata !=null));
                            mediaItems.add(mediaItem);
                        }
                    }
                }
            }

            LibraryResult libraryRootResult = LibraryResult.ofItemList(mediaItems, params);

            return Futures.immediateFuture(libraryRootResult);
        }

        @OptIn(markerClass = UnstableApi.class)
        @NonNull
        @Override
        public ListenableFuture<LibraryResult<MediaItem>> onGetItem(
                MediaLibrarySession session, MediaSession.ControllerInfo browser, String mediaId) {
            MusicFile musicFile = MusicDatabase.SONGS.get(mediaId);
            if(musicFile != null){
                MediaItem mediaItem = MusicPlayerService.buildBrowserMediaItem(musicFile, true);
                return Futures.immediateFuture(LibraryResult.ofItem(mediaItem, null));
            }
            return Futures.immediateFuture(LibraryResult.ofError(SessionError.ERROR_BAD_VALUE));
        }
    }


    private final BroadcastReceiver queueUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                if(INTENT_PLAY_SONG.equalsIgnoreCase(intent.getAction())){
                    LOGGER.info("BroadcastReceiver: Play Song Message Received");
                    long songId = intent.getLongExtra(BUNDLE_PLAY_SONG_SONG, -1);
                    String albumId = intent.getStringExtra(BUNDLE_PLAY_SONG_ALBUM);
                    if(songId > -1L) {
                        MusicPlayerService.this.clearQueue();
                        MusicPlayerService.this.playSong(songId, albumId);
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

    public static MediaItem buildMediaItem(MusicFile musicFile){
        if(musicFile == null){
            return null;
        }
        MediaItem mediaItem =
                new MediaItem.Builder()
                        .setMediaId(""+musicFile.getId())
                        .setUri(musicFile.getUri())
                        .setMediaMetadata(
                                new MediaMetadata.Builder()
                                        .setArtworkUri(musicFile.getAlbumArtUri())
                                        .setTitle(musicFile.getName())
                                        .setArtist(musicFile.getArtist())
                                        .build())
                        .build();
        return mediaItem;
    }

    public static MediaItem buildBrowserMediaItem(MusicFile musicFile, boolean isAlbum){
        if(musicFile == null){
            return null;
        }
        Bundle extraBundle = new Bundle();
        if(isAlbum){
            extraBundle.putString("album_id", musicFile.getAlbumId());
        }
        MediaItem mediaItem =
                new MediaItem.Builder()
                        .setMediaId(""+musicFile.getId())
                        .setUri(musicFile.getUri())
                        .setMediaMetadata(
                                new MediaMetadata.Builder()
                                        .setArtworkUri(musicFile.getAlbumArtUri())
                                        .setTitle(musicFile.getName())
                                        .setArtist(musicFile.getArtist())
                                        .setIsBrowsable(false)
                                        .setIsPlayable(true)
                                        .build())
                        .setRequestMetadata(new MediaItem.RequestMetadata.Builder().setMediaUri(musicFile.getUri()).setExtras(extraBundle).build())
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

    private void playSong(long songId, String albumId) {
        LOGGER.info("Playing Song...");
        MusicAlbum album = MusicDatabase.getAlbumById(albumId);
        if(album !=null){
            final List<MusicFile> allSongs = album.getAllMusicFiles();
            final int songIndex = IntStream.range(0, allSongs.size())
                    .filter(i -> allSongs.get(i).getId() == songId)
                    .findFirst()
                    .orElse(-1);
            List<MediaItem> mediaItems = allSongs.stream().map(MusicPlayerService::buildMediaItem).toList();
            player.setMediaItems(mediaItems);
            if(songIndex > -1) {
                player.seekTo(songIndex, 0);
            }
            player.prepare();
            player.play();
        }
        else {
            MusicFile musicFile = MusicDatabase.SONGS.get(songId);
            if (musicFile != null) {
                MediaItem mediaItem = buildMediaItem(musicFile);
                player.setMediaItem(mediaItem);
                player.prepare();
                player.play();
            }
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
        if(sessionManager !=null && player !=null){
            this.sessionManager.saveMusicSession(this.player);
        }
        stopSelf();
    }

    @Override
    public void onDestroy() {
        if(sessionManager !=null && player !=null){
            this.sessionManager.saveMusicSession(this.player);
        }
        mediaLibrarySession.getPlayer().release();
        mediaLibrarySession.release();
        mediaLibrarySession = null;
        LocalBroadcastManager.getInstance(this).unregisterReceiver(queueUpdateReceiver);
        super.onDestroy();
    }

    @Override
    public MediaLibrarySession onGetSession(MediaSession.ControllerInfo controllerInfo) {
        return mediaLibrarySession;
    }
}