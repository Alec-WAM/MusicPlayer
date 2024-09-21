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

import java.util.logging.Logger;

import alec_wam.musicplayer.R;
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

public class MusicPlayerOverlay extends ConstraintLayout implements Player.Listener {

    private static final Logger LOGGER = Logger.getLogger("MusicPlayerOverlay");

    public MediaController mediaController;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable seekBarRunnable;

    private long mediaId;

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
                ModalMusicPlayerSongBottomSheet modal = new ModalMusicPlayerSongBottomSheet(R.layout.layout_music_player_song_menu, MusicPlayerOverlay.this, mediaId);
                if(getContext() instanceof FragmentActivity){
                    FragmentManager fragmentManager = ((FragmentActivity) getContext()).getSupportFragmentManager();
                    modal.show(fragmentManager, ModalMusicPlayerSongBottomSheet.TAG);
                }
            }
        });

        // Initialize child views
        albumImageView = findViewById(R.id.music_player_album_image);
        songTitle = findViewById(R.id.music_player_song_name);
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

    public void setMediaController(MediaController controller) {
        this.mediaController = controller;
        updateFromMediaItem(controller.getCurrentMediaItem());
        updatePlayPauseButton(controller.isPlaying());
        updateSeekBarProgress();
        updatePrevNextButtons();
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
            this.mediaId = Long.parseLong(mediaItem.mediaId);
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
}
