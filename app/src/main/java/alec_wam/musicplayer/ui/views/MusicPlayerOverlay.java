package alec_wam.musicplayer.ui.views;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import alec_wam.musicplayer.R;
import alec_wam.musicplayer.data.database.AppDatabaseViewModel;
import alec_wam.musicplayer.database.MusicDatabase;
import alec_wam.musicplayer.database.MusicFile;
import alec_wam.musicplayer.ui.album.AlbumFragment;
import alec_wam.musicplayer.utils.FragmentUtils;
import alec_wam.musicplayer.utils.MusicPlayerUtils;
import alec_wam.musicplayer.utils.PlaylistUtils;
import alec_wam.musicplayer.utils.ThemedDrawableUtils;
import alec_wam.musicplayer.utils.Utils;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.Player;
import androidx.media3.session.MediaController;

import static androidx.media3.common.Player.REPEAT_MODE_ALL;
import static androidx.media3.common.Player.REPEAT_MODE_OFF;
import static androidx.media3.common.Player.REPEAT_MODE_ONE;

public class MusicPlayerOverlay extends ConstraintLayout implements Player.Listener {

    private static final Logger LOGGER = Logger.getLogger("MusicPlayerOverlay");

    public MediaController mediaController;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable seekBarRunnable;

    private AppDatabaseViewModel databaseViewModel;

    private String mediaId;

    public Button closeButton;
    public Button menuButton;

    public ImageView albumImageView;
    public TextView songTitle;
    public TextView songArtist;
    public SeekBar seekBar;
    public TextView songProgressText;
    public TextView songLengthText;
    public MaterialButton prevButton;
    public MaterialButton playPauseButton;
    public MaterialButton nextButton;
    public MaterialButton shuffleButton;
    public MaterialButton repeatButton;

    public FrameLayout songMenu;
    public BottomSheetBehavior songMenuBehavior;

    public MusicPlayerOverlay(Context context) {
        super(context);
        init(context);
    }

    public MusicPlayerOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MusicPlayerOverlay(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        // Inflate the layout
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.layout_music_player, this, true);

        closeButton = findViewById(R.id.music_player_minimize_button);
        closeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                hideOverlay();
            }
        });

        menuButton = findViewById(R.id.music_player_menu_button);
        menuButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                buildModalMenu();
            }
        });

        // Initialize child views
        albumImageView = findViewById(R.id.music_player_album_image);
        songTitle = findViewById(R.id.music_player_song_name);
        songTitle.setSelected(true);
        songArtist = findViewById(R.id.music_player_song_artist);

        seekBar = findViewById(R.id.music_player_control_seekbar);
        songProgressText = findViewById(R.id.music_player_controls_time_current);
        songLengthText = findViewById(R.id.music_player_controls_time_length);

        updateSeekBarProgress();
        seekBarRunnable = createSeekBarRunnable();

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaController.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Pause updating the SeekBar while dragging
                handler.removeCallbacks(seekBarRunnable);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Resume updating the SeekBar after user stops dragging
                handler.post(seekBarRunnable);
            }
        });

        prevButton = findViewById(R.id.music_player_control_prev_button);
        prevButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mediaController.hasPreviousMediaItem()){
                    mediaController.seekToPrevious();
                }
            }
        });

        playPauseButton = findViewById(R.id.music_player_control_play_button);
        playPauseButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mediaController.isPlaying()){
                    mediaController.pause();
                }
                else {
                    mediaController.play();
                }
            }
        });

        nextButton = findViewById(R.id.music_player_control_next_button);
        nextButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mediaController.hasNextMediaItem()){
                    mediaController.seekToNextMediaItem();
                }
            }
        });

        updatePrevNextButtons();

        shuffleButton = findViewById(R.id.music_player_control_shuffle);
        shuffleButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mediaController == null)return;
                final boolean oldShuffle = mediaController.getShuffleModeEnabled();
                mediaController.setShuffleModeEnabled(!oldShuffle);
            }
        });

        repeatButton = findViewById(R.id.music_player_control_repeat);
        repeatButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mediaController == null)return;
                int nextRepeat = -1;
                final int currentRepeat = mediaController.getRepeatMode();
                if(currentRepeat == REPEAT_MODE_OFF){
                    nextRepeat = REPEAT_MODE_ONE;
                }
                else if(currentRepeat == REPEAT_MODE_ONE){
                    nextRepeat = REPEAT_MODE_ALL;
                }
                else if(currentRepeat == REPEAT_MODE_ALL){
                    nextRepeat = REPEAT_MODE_OFF;
                }
                if(nextRepeat > -1) {
                    mediaController.setRepeatMode(nextRepeat);
                }
            }
        });

        if(mediaController !=null) {
            updateRepeatAndShuffleButtons(mediaController.getRepeatMode(), mediaController.getShuffleModeEnabled());
        }

//        songMenu = findViewById(R.id.music_player_song_options);
//        songMenuBehavior = BottomSheetBehavior.from(songMenu);
//        songMenuBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
//            @Override
//            public void onStateChanged(@NonNull View bottomSheet, int newState) {
//                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
//                    songMenuBehavior.setDraggable(false);
//                }
//                if (newState != BottomSheetBehavior.STATE_HIDDEN) {
//                    songMenuBehavior.setDraggable(true);
//                }
//            }
//
//            @Override
//            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
//            }
//        });
//        songMenuBehavior.setPeekHeight(0);
//        songMenuBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
//        songMenuBehavior.setDraggable(false);
    }

    public void setDatabaseViewModel(AppDatabaseViewModel databaseViewModel) {
        this.databaseViewModel = databaseViewModel;
    }

    public void setMediaController(MediaController controller) {
        this.mediaController = controller;
        updateFromMediaItem(controller.getCurrentMediaItem());
        updatePlayPauseButton(controller.isPlaying());
        updateSeekBarProgress();
        updatePrevNextButtons();
        updateRepeatAndShuffleButtons(controller.getRepeatMode(), controller.getShuffleModeEnabled());
    }

    public void buildModalMenu(){
        final MusicFile musicFile = MusicDatabase.SONGS.get(mediaId);
        if(musicFile !=null) {
            String title = musicFile.getName();
            String subText = musicFile.getArtist();
            ModalMenuBottomSheet.ImageLoader imageLoader = new ModalMenuBottomSheet.ImageLoader() {
                @Override
                public void loadImage(ImageView imageView) {
                    Drawable themed_unknown_album = ThemedDrawableUtils.getThemedIcon(getContext(), R.drawable.ic_unkown_album, com.google.android.material.R.attr.colorSecondary, Color.BLACK);
                    Glide.with(MusicPlayerOverlay.this)
                            .load(musicFile.getAlbumArtUri())  // URI for album art
                            .placeholder(themed_unknown_album)  // Optional placeholder
                            .error(themed_unknown_album)  // Optional error image
                            .into(imageView);
                }
            };
            List<ModalMenuBottomSheet.MenuOption> menuOptionList = new ArrayList<>();
            menuOptionList.add(new ModalMenuBottomSheet.MenuOption(R.drawable.ic_playlist_add, "Add to Playlist", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(MusicPlayerOverlay.this.databaseViewModel !=null) {
                        PlaylistUtils.showAddToPlaylistDialog(MusicPlayerOverlay.this.getContext(), musicFile.getId(), databaseViewModel);
                    }
                }
            }));
            menuOptionList.add(new ModalMenuBottomSheet.MenuOption(R.drawable.ic_unkown_album, "View Album", new OnClickListener() {
                @Override
                public void onClick(View view) {
                    FragmentUtils.openAlbumPage(view, musicFile.getAlbumId(), R.id.navigation_album);
                    MusicPlayerOverlay.this.hideOverlay();
                }
            }));
            menuOptionList.add(new ModalMenuBottomSheet.MenuOption(R.drawable.ic_unknown_artist, "View Artist", new OnClickListener() {
                @Override
                public void onClick(View view) {
                    FragmentUtils.openArtistPage(view, musicFile.getArtistId(), R.id.navigation_artist);
                    MusicPlayerOverlay.this.hideOverlay();
                }
            }));
            ModalMenuBottomSheet modal = new ModalMenuBottomSheet(R.layout.layout_bottom_sheet_menu, imageLoader, title, subText, menuOptionList);
            if (getContext() instanceof FragmentActivity) {
                FragmentManager fragmentManager = ((FragmentActivity) getContext()).getSupportFragmentManager();
                modal.show(fragmentManager, ModalMenuBottomSheet.TAG);
            }
        }
    }

    public void showOverlay(){
        this.setVisibility(VISIBLE);
        TranslateAnimation animate = new TranslateAnimation(0, 0, getHeight(), 0);
        animate.setDuration(250);
        animate.setFillAfter(true);
        startAnimation(animate);
    }

    public void hideOverlay(){
        this.setVisibility(GONE);
        TranslateAnimation animate = new TranslateAnimation(0, 0, 0, getHeight());
        animate.setDuration(250);
        startAnimation(animate);
    }

    public void cleanUpHandler(){
        handler.removeCallbacks(seekBarRunnable);
    }

    public Runnable createSeekBarRunnable(){
        return new Runnable() {
            @Override
            public void run() {
                if (mediaController != null && mediaController.isPlaying()) {
                    updateSeekBarProgress();
                    // Schedule the next update in 1000ms (1 second)
                    handler.postDelayed(this, 1000);
                }
            }
        };
    }

    public void updatePlayPauseButton(boolean isPlaying){
        if(!isPlaying){
            playPauseButton.setIcon(getResources().getDrawable(R.drawable.ic_play, getContext().getTheme()));
        }
        else {
            playPauseButton.setIcon(getResources().getDrawable(R.drawable.ic_pause, getContext().getTheme()));
        }
    }

    public void updatePrevNextButtons(){
        if(mediaController !=null){
            prevButton.setEnabled(mediaController.hasPreviousMediaItem());
            nextButton.setEnabled(mediaController.hasNextMediaItem());
        }
    }

    public void updateRepeatAndShuffleButtons(@Player.RepeatMode int repeatMode, boolean shuffleMode){
        LOGGER.info("Updating Repeat/Shuffle: " + repeatMode + " " + shuffleMode);
        int repeatIcon = androidx.media3.session.R.drawable.media3_icon_repeat_off;

        if(repeatMode == REPEAT_MODE_ONE){
            repeatIcon = androidx.media3.session.R.drawable.media3_icon_repeat_one;
        }
        else if(repeatMode == REPEAT_MODE_ALL){
            repeatIcon = androidx.media3.session.R.drawable.media3_icon_repeat_all;
        }

        int shuffleIcon = androidx.media3.session.R.drawable.media3_icon_shuffle_off;
        if(shuffleMode){
            shuffleIcon = androidx.media3.session.R.drawable.media3_icon_shuffle_on;
        }

        repeatButton.setIcon(getResources().getDrawable(repeatIcon, getContext().getTheme()));
        shuffleButton.setIcon(getResources().getDrawable(shuffleIcon, getContext().getTheme()));
    }

    public void updateSeekBarProgress(){
        if(mediaController !=null) {
            long currentPosition = mediaController.getCurrentPosition();
            seekBar.setProgress((int) currentPosition);
            songProgressText.setText(Utils.convertMillisecondsToTimeString(currentPosition));
        }
    }

    @Override
    public void onMediaItemTransition(
            @Nullable MediaItem mediaItem,
            @Player.MediaItemTransitionReason int reason
    ){
        updateFromMediaItem(mediaItem);
        updatePrevNextButtons();
    }

    @Override
    public void onMediaMetadataChanged(MediaMetadata mediaMetadata) {
        Drawable themed_unknown_album = ThemedDrawableUtils.getThemedIcon(getContext(), R.drawable.ic_unkown_album, com.google.android.material.R.attr.colorSecondary, Color.BLACK);
        Glide.with(this)
                .load(mediaMetadata.artworkUri)  // URI for album art
                .placeholder(themed_unknown_album)  // Optional placeholder
                .error(themed_unknown_album)  // Optional error image
                .into(albumImageView);
        songTitle.setText(mediaMetadata.title);
        songArtist.setText(mediaMetadata.artist);
        updatePrevNextButtons();
        if(mediaController !=null) {
            seekBar.setMax((int) mediaController.getDuration());
            songLengthText.setText(Utils.convertMillisecondsToTimeString(mediaController.getDuration()));
        }
    }

    public void updateFromMediaItem(MediaItem mediaItem){
        if(mediaItem !=null){
            this.mediaId = mediaItem.mediaId;
            Drawable themed_unknown_album = ThemedDrawableUtils.getThemedIcon(getContext(), R.drawable.ic_unkown_album, com.google.android.material.R.attr.colorSecondary, Color.BLACK);
            Glide.with(this)
                    .load(mediaItem.mediaMetadata.artworkUri)  // URI for album art
                    .placeholder(themed_unknown_album)  // Optional placeholder
                    .error(themed_unknown_album)  // Optional error image
                    .into(albumImageView);
            songTitle.setText(mediaItem.mediaMetadata.title);
            songArtist.setText(mediaItem.mediaMetadata.artist);
        }
        if(mediaController !=null) {
            seekBar.setMax((int) mediaController.getDuration());
            songLengthText.setText(Utils.convertMillisecondsToTimeString(mediaController.getDuration()));
            updateSeekBarProgress();
        }
    }

    @Override
    public void onIsPlayingChanged(boolean isPlaying) {
        updatePlayPauseButton(isPlaying);
        if(isPlaying){
            handler.post(seekBarRunnable);  // Start SeekBar update loop
        }
        else {
            handler.removeCallbacks(seekBarRunnable);  // Stop updating if paused
        }
    }

    @Override
    public void onPlaybackStateChanged(int playbackState) {
        // When player is ready, set the SeekBar max to media duration
        if (playbackState == Player.STATE_READY) {
            seekBar.setMax((int) mediaController.getDuration());
            songLengthText.setText(Utils.convertMillisecondsToTimeString(mediaController.getDuration()));
        }
    }

    @Override
    public void onRepeatModeChanged(@Player.RepeatMode int repeatMode) {
        this.updateRepeatAndShuffleButtons(repeatMode, mediaController.getShuffleModeEnabled());
    }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
        this.updateRepeatAndShuffleButtons(mediaController.getRepeatMode(), shuffleModeEnabled);
    }
}
