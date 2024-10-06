package alec_wam.musicplayer.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import alec_wam.musicplayer.R;
import alec_wam.musicplayer.data.database.AppDatabaseRepository;
import alec_wam.musicplayer.data.database.entities.PlaylistSong;
import alec_wam.musicplayer.database.MusicAlbum;
import alec_wam.musicplayer.database.MusicArtist;
import alec_wam.musicplayer.database.MusicDatabase;
import alec_wam.musicplayer.database.MusicFile;
import alec_wam.musicplayer.utils.MusicPlayerUtils;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.media3.common.AudioAttributes;
import androidx.media3.common.C;
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

import static alec_wam.musicplayer.utils.MusicPlayerUtils.BUNDLE_SONG_CHANGE_SONG_PLAYLIST_SONG_ID;

public class MusicPlayerService extends MediaLibraryService {

    private final static Logger LOGGER = Logger.getLogger("MusicPlayerService");

    public static final String INTENT_PLAY_SONG = "MP_PLAY_SONG";
    public static final String BUNDLE_PLAY_SONG_SONG = "song";
    public static final String BUNDLE_PLAY_SONG_ALBUM = "album";
    public static final String BUNDLE_PLAY_SONG_FAVORITE_LIST = "favorite_list";
    public static final String INTENT_PAUSE_SONG = "MP_PAUSE_SONG";
    public static final String BUNDLE_PAUSE_SONG = "paused";
    public static final String INTENT_PLAY_ALBUM = "MP_PLAY_ALBUM";
    public static final String BUNDLE_PLAY_ALBUM_ALBUM = "album";
    public static final String BUNDLE_PLAY_ALBUM_SHUFFLE = "shuffle";
    public static final String INTENT_PLAY_FAVORITES = "MP_PLAY_FAVORITES";
    public static final String INTENT_PLAY_PLAYLIST = "MP_PLAY_PLAYLIST";
    public static final String BUNDLE_PLAY_PLAYLIST_PLAYLIST = "playlist";
    public static final String BUNDLE_PLAY_PLAYLIST_PLAYLIST_SONG = "playlist_song";
    public static final String BUNDLE_PLAY_PLAYLIST_SHUFFLE = "shuffle";
    public static final String INTENT_UPDATE_QUEUE = "MP_UPDATE_QUEUE";
    public static final String BUNDLE_UPDATE_QUEUE_SONGS = "songs";
    public static final String INTENT_CLEAR_QUEUE = "MP_CLEAR_QUEUE";

    public static final String BUNDLE_PLAYLIST= "playlist";
    public static final String BUNDLE_PLAYLIST_SONG = "playlist_song";

    public static final String NOTIFICATION_CHANNEL_ID = "music_player_service";
    public static final int NOTIFICATION_ID = 1;

    private Handler backgroundHandler;
    private ExecutorService backgroundExecutor;

    public static MediaItem currentSong;
    private ExoPlayer player;
    private MediaLibrarySession mediaLibrarySession;
    private Queue<String> queue = new LinkedList<>();
    private boolean isPaused = false;

    private MusicPlayerSavedDataManager musicPlayerSavedDataManager;
    private AppDatabaseRepository appDatabaseRepository;
    private boolean isLibraryBuilt = false;

    public MusicPlayerService() {
    }

    public Future<Boolean> buildAlbumListAsync() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        return executor.submit(() -> {
            MusicDatabase.buildAlbumList(MusicPlayerService.this);
            this.isLibraryBuilt = true;
            LOGGER.info("Music Library Built In thread");
            return true;
        });
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        LOGGER.info("MusicPlayerService onStart");
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED){
            LOGGER.warning("Invalid Permissions for MusicPlayerService");
            stopSelf();
            return START_NOT_STICKY;
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private Notification createNotification() {
        NotificationChannel channel = new NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Music Service",
                NotificationManager.IMPORTANCE_LOW
        );

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.createNotificationChannel(channel);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setContentTitle("Music Player")
                .setContentText("Playing music")
                .setSmallIcon(R.drawable.ic_app_icon)
                .setPriority(NotificationCompat.PRIORITY_LOW);

        return notificationBuilder.build();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        backgroundHandler = new Handler(Looper.getMainLooper());
        backgroundExecutor = Executors.newSingleThreadExecutor();

        Notification notification = createNotification();
        startForeground(NOTIFICATION_ID, notification);

        appDatabaseRepository = new AppDatabaseRepository(getApplication());

        if(!isLibraryBuilt) {
            Future<Boolean> future = buildAlbumListAsync();
            try {
                future.get();
                backgroundHandler.post(() -> {
                    try {
                        if(this.musicPlayerSavedDataManager !=null && this.player !=null){
                            this.musicPlayerSavedDataManager.loadMusicSession(this.player);
                        }
                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        LOGGER.info("Created MusicService");
        AudioAttributes audioAttributes = new AudioAttributes.Builder().setContentType(C.AUDIO_CONTENT_TYPE_MUSIC).setUsage(C.USAGE_MEDIA).build();
        player = new ExoPlayer.Builder(this).setAudioAttributes(audioAttributes, true).build();
        musicPlayerSavedDataManager = new MusicPlayerSavedDataManager(this.getApplicationContext());
        mediaLibrarySession = new MediaLibrarySession.Builder(this, player, new LibrarySessionCallback(backgroundExecutor, appDatabaseRepository, musicPlayerSavedDataManager)).build();

        player.addListener(new Player.Listener() {
            @Override
            public void onMediaItemTransition(
                    @Nullable MediaItem mediaItem,
                    @Player.MediaItemTransitionReason int reason
            ){
                MusicPlayerService.this.currentSong = mediaItem;
                String mediaId = null;
                int playlistSongId = -1;
                if(mediaItem !=null) {
                    mediaId = mediaItem.mediaId;
                    MediaMetadata mediaMetadata = mediaItem.mediaMetadata;
                    if(mediaMetadata.extras !=null){
                        if(mediaMetadata.extras.containsKey(BUNDLE_PLAYLIST_SONG)){
                            playlistSongId = mediaMetadata.extras.getInt(BUNDLE_PLAYLIST_SONG, -1);
                        }
                    }
                }
                MusicPlayerUtils.broadcastSongChange(MusicPlayerService.this, mediaId, Optional.of(playlistSongId));
            }
        });

        IntentFilter filter = new IntentFilter();
        filter.addAction(INTENT_PLAY_SONG);
        filter.addAction(INTENT_PAUSE_SONG);
        filter.addAction(INTENT_PLAY_ALBUM);
        filter.addAction(INTENT_PLAY_PLAYLIST);
        filter.addAction(INTENT_PLAY_FAVORITES);
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
//                    LOGGER.info("BroadcastReceiver: Play Song Message Received");
                    String songId = intent.getStringExtra(BUNDLE_PLAY_SONG_SONG);
                    String albumId = intent.getStringExtra(BUNDLE_PLAY_SONG_ALBUM);
                    boolean isFromFavorites = intent.getBooleanExtra(BUNDLE_PLAY_SONG_FAVORITE_LIST, false);
                    if(songId !=null) {
                        if(isFromFavorites){
                            MusicPlayerService.this.playFavoriteSong(songId);
                        }
                        else {
                            MusicPlayerService.this.playSong(songId, albumId);
                        }
                    }
                }
                if(INTENT_PAUSE_SONG.equalsIgnoreCase(intent.getAction())) {
//                    LOGGER.info("BroadcastReceiver: Pause Song Message Received");
                    boolean paused = intent.getBooleanExtra(BUNDLE_PAUSE_SONG, false);
                    if(paused){
                        MusicPlayerService.this.pauseSong();
                    }
                    else {
                        MusicPlayerService.this.resumeSong();
                    }
                }
                if(INTENT_PLAY_ALBUM.equalsIgnoreCase(intent.getAction())){
                    String albumId = intent.getStringExtra(BUNDLE_PLAY_ALBUM_ALBUM);
                    boolean shuffle = intent.getBooleanExtra(BUNDLE_PLAY_ALBUM_SHUFFLE, false);
                    if(albumId !=null) {
                        MusicPlayerService.this.playAlbum(albumId, shuffle);
                    }
                }
                if(INTENT_PLAY_FAVORITES.equalsIgnoreCase(intent.getAction())){
                    boolean shuffle = intent.getBooleanExtra(BUNDLE_PLAY_ALBUM_SHUFFLE, false);
                    MusicPlayerService.this.playFavoriteSongs(shuffle);
                }
                if(INTENT_PLAY_PLAYLIST.equalsIgnoreCase(intent.getAction())){
                    int playlistId = intent.getIntExtra(BUNDLE_PLAY_PLAYLIST_PLAYLIST, -1);
                    int playlistSongId = intent.getIntExtra(BUNDLE_PLAY_PLAYLIST_PLAYLIST_SONG, -1);
                    boolean shuffleMode = intent.getBooleanExtra(BUNDLE_PLAY_PLAYLIST_SHUFFLE, false);

                    if(playlistId >= 0){
                        if(playlistSongId >= 0){
                            MusicPlayerService.this.playPlaylistSong(playlistId, playlistSongId);
                        }
                        else {
                            MusicPlayerService.this.playPlaylist(playlistId, shuffleMode);
                        }
                    }
                }
                if(INTENT_UPDATE_QUEUE.equalsIgnoreCase(intent.getAction())) {
//                    LOGGER.info("BroadcastReceiver: Update Queue Message Received");
                    String[] songs = intent.getStringArrayExtra(BUNDLE_UPDATE_QUEUE_SONGS);
                    final boolean wasEmpty = MusicPlayerService.this.isQueueEmpty();
                    MusicPlayerService.this.addSongs(songs);
                    if(wasEmpty) {
                        MusicPlayerService.this.resumeSong();
                    }
                }
                if(INTENT_CLEAR_QUEUE.equalsIgnoreCase(intent.getAction())) {
//                    LOGGER.info("BroadcastReceiver: Clear Queue Message Received");
                    MusicPlayerService.this.clearQueue();
                }
            }
        }
    };

    public boolean isQueueEmpty(){
        return this.queue.isEmpty();
    }

    public static MediaItem buildMediaItem(MusicFile musicFile) {
        return buildMediaItem(musicFile, null);
    }

    public static MediaItem buildMediaItem(MusicFile musicFile, Bundle extras){
        if(musicFile == null){
            return null;
        }
        MediaItem mediaItem =
                new MediaItem.Builder()
                        .setMediaId(musicFile.getId())
                        .setUri(musicFile.getUri())
                        .setMediaMetadata(
                                new MediaMetadata.Builder()
                                        .setArtworkUri(musicFile.getAlbumArtUri())
                                        .setTitle(musicFile.getName())
                                        .setArtist(musicFile.getArtist())
                                        .setExtras(extras)
                                        .build())
                        .build();
        return mediaItem;
    }

    public static MediaItem buildPlaylistMediaItem(MusicFile musicFile, int playlistSongId){
        if(musicFile == null){
            return null;
        }
        Bundle extras = new Bundle();
        extras.putInt(BUNDLE_PLAYLIST_SONG, playlistSongId);
        MediaItem mediaItem =
                new MediaItem.Builder()
                        .setMediaId(musicFile.getId())
                        .setUri(musicFile.getUri())
                        .setMediaMetadata(
                                new MediaMetadata.Builder()
                                        .setArtworkUri(musicFile.getAlbumArtUri())
                                        .setTitle(musicFile.getName())
                                        .setArtist(musicFile.getArtist())
                                        .setExtras(extras)
                                        .build())
                        .build();
        return mediaItem;
    }

    public static MediaItem buildBrowserMediaItem(MusicFile musicFile, boolean isAlbum, boolean isFavorite){
        if(musicFile == null){
            return null;
        }
        Bundle extraBundle = new Bundle();
        if(isAlbum){
            extraBundle.putString("album_id", musicFile.getAlbumId());
        }
        if(isFavorite){
            extraBundle.putBoolean("isFavorite", true);
        }
        MediaItem mediaItem =
                new MediaItem.Builder()
                        .setMediaId(musicFile.getId())
                        .setUri(musicFile.getUri())
                        .setMediaMetadata(
                                new MediaMetadata.Builder()
                                        .setArtworkUri(musicFile.getAlbumArtUri())
                                        .setTitle(musicFile.getName())
                                        .setArtist(musicFile.getArtist())
                                        .setIsBrowsable(false)
                                        .setIsPlayable(true)
                                        .setExtras(extraBundle)
                                        .build())
                        .setRequestMetadata(new MediaItem.RequestMetadata.Builder().setMediaUri(musicFile.getUri()).setExtras(extraBundle).build())
                        .build();
        return mediaItem;
    }

    public static MediaItem buildBrowsableMediaItemAlbum(MusicAlbum album){
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
        return albumMediaItem;
    }

    public static List<MediaItem> search(String query){
        List<MediaItem> mediaItems = new ArrayList<>();

        List<MediaItem> foundAlbums = MusicDatabase.ALBUMS.values().stream().filter((album) -> album.getName().toLowerCase().startsWith(query.toLowerCase())).map(MusicPlayerService::buildBrowsableMediaItemAlbum).toList();
        mediaItems.addAll(foundAlbums);
        return mediaItems;
    }

    public void addSong(String song){
        MusicFile musicFile = MusicDatabase.SONGS.get(song);
        if(musicFile !=null){
            player.addMediaItem(buildMediaItem(musicFile));
        }
    }

    public void addSongs(String[] songs) {
        for(String song : songs) {
            this.addSong(song);
        }
    }

    public void clearQueue(){
        player.clearMediaItems();
    }

    private void playAlbum(String albumId, boolean shuffle){
        MusicAlbum album = MusicDatabase.getAlbumById(albumId);
        if(album !=null) {
            musicPlayerSavedDataManager.addRecentAlbum(albumId);

            final List<MusicFile> allSongs = new ArrayList<>(album.getAllMusicFiles());
//            if(shuffle){
//                Collections.shuffle(allSongs);
//            }
            List<MediaItem> mediaItems = allSongs.stream().map(MusicPlayerService::buildMediaItem).toList();
            player.setMediaItems(mediaItems);
            player.setShuffleModeEnabled(shuffle);
            player.prepare();
            player.play();
        }
    }

    private void playFavoriteSongs(boolean shuffle){
        //Get Favorite Songs from Background Thread
        backgroundExecutor.execute(() -> {
            final List<String> allFavSongIds = appDatabaseRepository.getAllFavoriteSongIdsSortedSync();
            if(allFavSongIds !=null) {
                //Perform player actions on Main Thread
                backgroundHandler.post(() -> {
                    List<MediaItem> mediaItems = allFavSongIds.stream().map((songId) -> MusicDatabase.SONGS.get(songId)).map(MusicPlayerService::buildMediaItem).toList();
                    player.setMediaItems(mediaItems);
                    player.setShuffleModeEnabled(shuffle);
                    player.prepare();
                    player.play();
                });
            }
        });
    }

    private void playPlaylist(int playlistId, boolean shuffle){
        //Get Favorite Songs from Background Thread
        backgroundExecutor.execute(() -> {
            final List<PlaylistSong> playlistSongs = appDatabaseRepository.getPlaylistSongsSync(playlistId);
            if(playlistSongs !=null) {
                //Perform player actions on Main Thread
                backgroundHandler.post(() -> {
                    List<MediaItem> mediaItems = playlistSongs.stream().map((playlistSong) -> {
                        MusicFile musicFile = MusicDatabase.SONGS.get(playlistSong.songId);
                        return buildPlaylistMediaItem(musicFile, playlistSong.id);
                    }).toList();
                    player.setMediaItems(mediaItems);
                    player.setShuffleModeEnabled(shuffle);
                    player.prepare();
                    player.play();
                });
            }
        });
    }

    private void playPlaylistSong(int playlistId, int playlistSongId){
        //Get Favorite Songs from Background Thread
        backgroundExecutor.execute(() -> {
            final List<PlaylistSong> playlistSongs = appDatabaseRepository.getPlaylistSongsSync(playlistId);
            if(playlistSongs !=null) {
                final int songIndex = IntStream.range(0, playlistSongs.size())
                        .filter(i -> playlistSongs.get(i).id == playlistSongId)
                        .findFirst()
                        .orElse(-1);
                //Perform player actions on Main Thread
                backgroundHandler.post(() -> {
                    List<MediaItem> mediaItems = playlistSongs.stream().map((playlistSong) -> {
                        MusicFile musicFile = MusicDatabase.SONGS.get(playlistSong.songId);
                        return buildPlaylistMediaItem(musicFile, playlistSong.id);
                    }).toList();
                    player.setMediaItems(mediaItems);
                    if(songIndex > -1) {
                        player.seekTo(songIndex, 0);
                    }
                    player.prepare();
                    player.play();
                });
            }
        });
    }

    private void playSong(String songId, String albumId) {
        LOGGER.info("Playing Song...");
        MusicAlbum album = MusicDatabase.getAlbumById(albumId);
        if(album !=null){
            musicPlayerSavedDataManager.addRecentAlbum(albumId);

            final List<MusicFile> allSongs = album.getAllMusicFiles();
            final int songIndex = IntStream.range(0, allSongs.size())
                    .filter(i -> allSongs.get(i).getId().equals(songId))
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
    }

    private void playFavoriteSong(final String favSongId){
        //Get Favorite Songs from Background Thread
        backgroundExecutor.execute(() -> {
            final List<String> allFavSongIds = appDatabaseRepository.getAllFavoriteSongIdsSortedSync();
            if(allFavSongIds !=null) {
                final int songIndex = IntStream.range(0, allFavSongIds.size())
                        .filter(i -> allFavSongIds.get(i).equals(favSongId))
                        .findFirst()
                        .orElse(-1);
                //Perform player actions on Main Thread
                backgroundHandler.post(() -> {
                    List<MediaItem> mediaItems = allFavSongIds.stream().map((songId) -> MusicDatabase.SONGS.get(songId)).map(MusicPlayerService::buildMediaItem).toList();
                    player.setMediaItems(mediaItems);
                    if(songIndex > -1) {
                        player.seekTo(songIndex, 0);
                    }
                    player.prepare();
                    player.play();
                });
            }
        });
    }

    private void pauseSong() {
        this.player.pause();
    }

    private void resumeSong() {
        this.player.play();
    }

    @Override
    public void onTaskRemoved(@Nullable Intent rootIntent) {
        if (player.getPlayWhenReady()) {
            // Make sure the service is not in foreground.
            player.pause();
        }
        if(musicPlayerSavedDataManager !=null && player !=null){
            this.musicPlayerSavedDataManager.saveMusicSession(this.player);
        }
        stopSelf();
    }

    @Override
    public void onDestroy() {
        if(musicPlayerSavedDataManager !=null && player !=null){
            this.musicPlayerSavedDataManager.saveMusicSession(this.player);
        }
        mediaLibrarySession.getPlayer().release();
        mediaLibrarySession.release();
        mediaLibrarySession = null;
        LocalBroadcastManager.getInstance(this).unregisterReceiver(queueUpdateReceiver);
        backgroundExecutor.shutdown();
        super.onDestroy();
    }

    @Override
    public MediaLibrarySession onGetSession(MediaSession.ControllerInfo controllerInfo) {
        return mediaLibrarySession;
    }

    public class LibrarySessionCallback implements MediaLibraryService.MediaLibrarySession.Callback {

        private final ExecutorService backgroundExecutor;
        private final AppDatabaseRepository appDatabaseRepository;
        private final MusicPlayerSavedDataManager playerSavedDataManager;

        public LibrarySessionCallback(ExecutorService backgroundExecutor, AppDatabaseRepository appDatabaseRepository, MusicPlayerSavedDataManager sessionManager) {
            this.backgroundExecutor = backgroundExecutor;
            this.appDatabaseRepository = appDatabaseRepository;
            this.playerSavedDataManager = sessionManager;
        }

//        @OptIn(markerClass = UnstableApi.class)
//        @Override
//        public MediaSession.ConnectionResult onConnect(MediaSession session, MediaSession.ControllerInfo controller) {
////            SessionCommands sessionCommands = new SessionCommands.Builder()
////                    .addSessionCommands(LIBRARY_COMMANDS).addAllSessionCommands()
////                    .addCommand(SessionCommand.COMMAND_SEARCH) // Ensure the search command is available
////                    .build();
//            MediaSession.ConnectionResult result = new MediaSession.ConnectionResult.AcceptedResultBuilder(session).setAvailableSessionCommands().build();
//            return result;
//        }
//
//        @Override
//        public void onPostConnect(MediaSession session, MediaSession.ControllerInfo controller) {
//            session.setCustomLayout(controller)
//        }

        @NonNull
        @Override
        @OptIn(markerClass = UnstableApi.class)
        public ListenableFuture<MediaSession.MediaItemsWithStartPosition> onPlaybackResumption(
                MediaSession mediaSession,
                MediaSession.ControllerInfo controller
        ) {
            SettableFuture<MediaSession.MediaItemsWithStartPosition> settableFuture = SettableFuture.create();
            settableFuture.addListener(() -> {
                MediaSession.MediaItemsWithStartPosition resumptionPlaylist = playerSavedDataManager.getResumption();
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
            return Util.transformFutureAsync(
                    onAddMediaItems(mediaSession, controller, mediaItems),
                    (mediaItemList) -> {
                        if (mediaItemList.size() == 1) {
                            return buildAlbumMediaItemList(mediaItemList.get(0), startIndex, startPositionMs);
                        }
                        return Futures.immediateFuture(
                                new MediaSession.MediaItemsWithStartPosition(mediaItemList, startIndex, startPositionMs));
                    });
        }

        @OptIn(markerClass = UnstableApi.class)
        private ListenableFuture<MediaSession.MediaItemsWithStartPosition> buildAlbumMediaItemList(MediaItem mediaItem, int startIndex, long startPositionMs) {
            List<MediaItem> mediaItemList = new ArrayList<>();
            int indexInAlbum = startIndex;

            String mediaId = mediaItem.mediaId;
            boolean isFavoriteSong = false;
            Bundle extras = mediaItem.mediaMetadata.extras;
            if(extras.containsKey("isFavorite")){
                isFavoriteSong = extras.getBoolean("isFavorite", false);
            }

            if(isFavoriteSong){
                return Futures.submitAsync(() -> {
                    try {
                        List<MediaItem> favMediaItems = new ArrayList<>();
                        List<String> favSongIds = this.appDatabaseRepository.getAllFavoriteSongIdsSortedSync();
                        List<MusicFile> favSongs = favSongIds.stream().map((songId) -> MusicDatabase.SONGS.get(songId)).toList();
                        int favAlbumIndex = startIndex;
                        for (int i = 0; i < favSongs.size(); i++) {
                            MusicFile musicFile  = favSongs.get(i);
                            if (musicFile.getId().equals(mediaId)) {
                                favAlbumIndex = i;
                            }
                            favMediaItems.add(MusicPlayerService.buildMediaItem(musicFile));
                        }

                        return Futures.immediateFuture(new MediaSession.MediaItemsWithStartPosition(favMediaItems, favAlbumIndex, startPositionMs));
                    } catch (Exception e) {
                        e.printStackTrace();
                        List<MediaItem> errorMediaItems = new ArrayList<>();
                        errorMediaItems.add(mediaItem);
                        return Futures.immediateFuture(
                                new MediaSession.MediaItemsWithStartPosition(errorMediaItems, 0, startPositionMs));
                    }
                }, backgroundExecutor);
            }

            MusicFile musicFile = MusicDatabase.SONGS.get(mediaId);
            boolean addedAlbum = false;
            if (musicFile != null) {
                MusicAlbum album = MusicDatabase.getAlbumById(musicFile.getAlbumId());
                if (album != null) {
                    playerSavedDataManager.addRecentAlbum(album.getAlbumId());

                    List<MusicFile> albumMusicFiles = album.getAllMusicFiles();
                    for (int i = 0; i < albumMusicFiles.size(); i++) {
                        MusicFile albumMusicFile = albumMusicFiles.get(i);
                        if (albumMusicFile.getId().equals(mediaId)) {
                            indexInAlbum = i;
                        }
                        mediaItemList.add(MusicPlayerService.buildMediaItem(albumMusicFile));
                    }
                    addedAlbum = true;
                }
            }

            if (!addedAlbum) {
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
                    MusicFile musicFile = MusicDatabase.SONGS.get(mediaItem.mediaId);
                    if (musicFile != null) {
                        Bundle extras = mediaItem.requestMetadata !=null ? mediaItem.requestMetadata.extras : null;
                        MediaItem fixedMediaItem = MusicPlayerService.buildMediaItem(musicFile, extras);
                        fixedMediaItems.add(fixedMediaItem);
                    }
                }
            }
            return Futures.immediateFuture(fixedMediaItems);
        }

        @OptIn(markerClass = UnstableApi.class)
        @NonNull
        @Override
        public ListenableFuture<LibraryResult<MediaItem>> onGetLibraryRoot(
                @NonNull MediaLibraryService.MediaLibrarySession session,
                @NonNull MediaSession.ControllerInfo controller,
                @NonNull MediaLibraryService.LibraryParams params) {

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

        @OptIn(markerClass = UnstableApi.class)
        @NonNull
        @Override
        public ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> onGetChildren(
                MediaLibraryService.MediaLibrarySession session,
                MediaSession.ControllerInfo browser,
                String parentId,
                @IntRange(from = 0) int page,
                @IntRange(from = 1) int pageSize,
                @Nullable MediaLibraryService.LibraryParams params) {
            List<MediaItem> mediaItems = new ArrayList<>();

//            LOGGER.info("Children: " + parentId);
//            if(params !=null){
//                LOGGER.info("Children LibraryParams: " + parentId + " " + params.extras.toString());
//               if(params.isRecent) {
//                   LOGGER.info("Recent: " + parentId);
//               }
//                if(params.isSuggested) {
//                    LOGGER.info("Suggested: " + parentId);
//                }
//            }

            if (parentId.equalsIgnoreCase("root_library")) {
                // MediaItem for "Home"
                MediaItem homeCategory = new MediaItem.Builder()
                        .setMediaId("root_home")
                        .setMediaMetadata(new MediaMetadata.Builder()
                                .setTitle("Home")
                                .setIsBrowsable(true)
                                .setIsPlayable(false)
                                .build())
                        .build();

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

                // MediaItem for "Playlists"
                MediaItem playlistsCategory = new MediaItem.Builder()
                        .setMediaId("root_playlists")
                        .setMediaMetadata(new MediaMetadata.Builder()
                                .setTitle("Playlists")
                                .setIsBrowsable(true)
                                .setIsPlayable(false)
                                .build())
                        .build();

                // Add these root items to the list
                mediaItems.add(homeCategory);
                mediaItems.add(artistsCategory);
                mediaItems.add(albumsCategory);
                mediaItems.add(playlistsCategory);
            } else if (parentId.equalsIgnoreCase("root_home")) {
                // MediaItem for "Recent Albums"
                MediaItem recentHomeItem = new MediaItem.Builder()
                        .setMediaId("recent_albums")
                        .setMediaMetadata(new MediaMetadata.Builder()
                                .setTitle("Recent Albums")
                                .setIsBrowsable(true)
                                .setIsPlayable(false)
                                .build())
                        .build();

                // MediaItem for "Fav Albums"
                MediaItem favAlbumsHomeItem = new MediaItem.Builder()
                        .setMediaId("fav_albums")
                        .setMediaMetadata(new MediaMetadata.Builder()
                                .setTitle("Favorite Albums")
                                .setIsBrowsable(true)
                                .setIsPlayable(false)
                                .build())
                        .build();

                // MediaItem for "Fav Songs"
                MediaItem favSongsHomeItem = new MediaItem.Builder()
                        .setMediaId("fav_songs")
                        .setMediaMetadata(new MediaMetadata.Builder()
                                .setTitle("Favorite Songs")
                                .setIsBrowsable(true)
                                .setIsPlayable(false)
                                .build())
                        .build();

                // Add these root items to the list
                mediaItems.add(recentHomeItem);
                mediaItems.add(favAlbumsHomeItem);
                mediaItems.add(favSongsHomeItem);
            } else if (parentId.equalsIgnoreCase("recent_albums")) {
                List<String> recentAlbums = new ArrayList<>(this.playerSavedDataManager.getRecentAlbums());
                Collections.reverse(recentAlbums);
                for (String albumId : recentAlbums) {
                    MusicAlbum album = MusicDatabase.getAlbumById(albumId);
                    if (album == null) continue;
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
            } else if (parentId.equalsIgnoreCase("fav_albums")) {
                return Futures.submitAsync(() -> {
                    try {
                        List<MediaItem> favAlbumMediaItems = new ArrayList<>();
                        List<String> favAlbumIds = this.appDatabaseRepository.getAllFavoriteAlbumsSync();
                        List<MusicAlbum> favAlbums = favAlbumIds.stream().map((albumId) -> MusicDatabase.getAlbumById(albumId)).filter((album -> album !=null)).sorted(Comparator.comparing(a -> a.getName().toLowerCase())).toList();
                        for (MusicAlbum album : favAlbums) {
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
                            favAlbumMediaItems.add(albumMediaItem);
                        }

                        LibraryResult libraryRootResult = LibraryResult.ofItemList(favAlbumMediaItems, params);
                        return Futures.immediateFuture(libraryRootResult);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Futures.immediateFuture(LibraryResult.ofError(SessionError.ERROR_BAD_VALUE));
                    }
                }, backgroundExecutor);
            } else if (parentId.equalsIgnoreCase("fav_songs")) {
                //TODO Add Shuffle Media Item to top
                return Futures.submitAsync(() -> {
                    try {
                        List<MediaItem> favSongMediaItems = new ArrayList<>();
                        List<String> favSongIds = this.appDatabaseRepository.getAllFavoriteSongIdsSortedSync();
                        for (String songId : favSongIds) {
                            MusicFile musicFile = MusicDatabase.SONGS.get(songId);
                            if(musicFile !=null) {
                                MediaItem mediaItem = MusicPlayerService.buildBrowserMediaItem(musicFile, false, true);
                                favSongMediaItems.add(mediaItem);
                            }
                        }

                        LibraryResult libraryRootResult = LibraryResult.ofItemList(favSongMediaItems, params);
                        return Futures.immediateFuture(libraryRootResult);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Futures.immediateFuture(LibraryResult.ofError(SessionError.ERROR_BAD_VALUE));
                    }
                }, backgroundExecutor);
            } else if (parentId.equalsIgnoreCase("root_artists")) {
                List<MusicArtist> sortedArtists = new ArrayList<>(MusicDatabase.ARTISTS.values());
                sortedArtists.sort(Comparator.comparing(a -> a.getName().toLowerCase()));
                for (MusicArtist artist : sortedArtists) {
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
            } else if (parentId.startsWith("artist_")) {
                String artistId = parentId.substring("artist_".length());

                MusicArtist artist = MusicDatabase.ARTISTS.get(artistId);

                if (artist != null) {
                    for (String albumId : artist.getAlbumIds()) {
                        MusicAlbum album = MusicDatabase.ALBUMS.get(albumId);
                        if (album != null) {
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
            } else if (parentId.equalsIgnoreCase("root_albums")) {
                List<MusicAlbum> sortedAlbums = new ArrayList<>(MusicDatabase.ALBUMS.values());
                sortedAlbums.sort(Comparator.comparing(a -> a.getName().toLowerCase()));
                for (MusicAlbum album : sortedAlbums) {
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
            } else if (parentId.startsWith("album_")) {
                String albumId = parentId.substring("album_".length());

                MusicAlbum album = MusicDatabase.ALBUMS.get(albumId);

                if (album != null) {
                    for (MusicFile musicFile : album.getAllMusicFiles()) {
                        if (musicFile != null) {
//                            LOGGER.info("URI: " + musicFile.getUri());
                            MediaItem mediaItem = MusicPlayerService.buildBrowserMediaItem(musicFile, true, false);
//                            LOGGER.info("Has RequestMeta: " + (mediaItem.requestMetadata !=null));
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
                MediaLibraryService.MediaLibrarySession session, MediaSession.ControllerInfo browser, String mediaId) {
            MusicFile musicFile = MusicDatabase.SONGS.get(mediaId);
            if (musicFile != null) {
                MediaItem mediaItem = MusicPlayerService.buildBrowserMediaItem(musicFile, true, false);
                return Futures.immediateFuture(LibraryResult.ofItem(mediaItem, null));
            }
            return Futures.immediateFuture(LibraryResult.ofError(SessionError.ERROR_BAD_VALUE));
        }

        @OptIn(markerClass = UnstableApi.class)
        @NonNull
        @Override
        public ListenableFuture<LibraryResult<Void>> onSearch(
                MediaLibraryService.MediaLibrarySession session,
                MediaSession.ControllerInfo browser,
                String query,
                @Nullable MediaLibraryService.LibraryParams params) {
//            LOGGER.info("onSearch query: " + query);
//            if(params !=null){
//                LOGGER.info("onSearch LibraryParams: " + query + " " + params.extras.toString());
//                if(params.isRecent) {
//                    LOGGER.info("Recent: " + query);
//                }
//                if(params.isSuggested) {
//                    LOGGER.info("Suggested: " + query);
//                }
//            }
            return Futures.submitAsync(() -> {
                try {
                    List<MediaItem> searchResults = MusicPlayerService.search(query);
                    if (!searchResults.isEmpty()) {
                        session.notifySearchResultChanged(browser, query, searchResults.size(), params);
                    }
                    return Futures.immediateFuture(LibraryResult.ofVoid(params));
                } catch (Exception e) {
                    return Futures.immediateFuture(LibraryResult.ofError(SessionError.ERROR_BAD_VALUE));
                }
            }, MoreExecutors.directExecutor());
        }

        @OptIn(markerClass = UnstableApi.class)
        @NonNull
        @Override
        public ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> onGetSearchResult(
                MediaLibraryService.MediaLibrarySession session,
                MediaSession.ControllerInfo browser,
                String query,
                @IntRange(from = 0) int page,
                @IntRange(from = 1) int pageSize,
                @Nullable MediaLibraryService.LibraryParams params) {
//            LOGGER.info("onGetSearchResult query: " + query);
//            if(params !=null){
//                LOGGER.info("onGetSearchResult LibraryParams: " + query + " " + params.extras.toString());
//                if(params.isRecent) {
//                    LOGGER.info("Recent: " + query);
//                }
//                if(params.isSuggested) {
//                    LOGGER.info("Suggested: " + query);
//                }
//            }
            return Futures.submitAsync(() -> {
                try {
                    List<MediaItem> searchResults = MusicPlayerService.search(query);
                    return Futures.immediateFuture(LibraryResult.ofItemList(searchResults, params));
                } catch (Exception e) {
                    return Futures.immediateFuture(LibraryResult.ofError(SessionError.ERROR_BAD_VALUE));
                }
            }, MoreExecutors.directExecutor());
        }
    }

}