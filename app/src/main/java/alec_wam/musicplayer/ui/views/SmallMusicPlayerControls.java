package alec_wam.musicplayer.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.session.PlaybackState;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

import java.util.logging.Logger;

import alec_wam.musicplayer.R;
import alec_wam.musicplayer.database.MusicDatabase;
import alec_wam.musicplayer.database.MusicFile;
import alec_wam.musicplayer.utils.ThemedDrawableUtils;
import androidx.annotation.Nullable;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.Player;
import androidx.media3.session.MediaController;

import static androidx.media3.common.Player.STATE_READY;

public class SmallMusicPlayerControls extends LinearLayout implements Player.Listener {

    private static final Logger LOGGER = Logger.getLogger("SmallMusicPlayerControls");

    public MediaController mediaController;
    public MusicPlayerOverlay playerView;

    public ImageView albumImageView;
    public TextView songTitle;
    public TextView songArtist;
    public MaterialButton playPauseButton;

    public SmallMusicPlayerControls(Context context) {
        super(context);
        init(context);
    }

    public SmallMusicPlayerControls(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SmallMusicPlayerControls(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        // Inflate the layout
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.layout_music_player_small, this, true);

        // Initialize child views
        albumImageView = findViewById(R.id.small_music_player_album);
        songTitle = findViewById(R.id.small_music_player_song_title);
        songArtist = findViewById(R.id.small_music_player_song_artist);
        playPauseButton = findViewById(R.id.small_music_player_play_pause_button);
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

        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(playerView !=null){
                    playerView.showOverlay();
                }
            }
        });
    }

    public void setMediaController(MediaController controller) {
        this.mediaController = controller;
        updateFromMediaItem(controller.getCurrentMediaItem());
        updatePlayPauseButton(controller.isPlaying());
    }

    public void setPlayerView(MusicPlayerOverlay playerView) {
        this.playerView = playerView;
    }

    public void updatePlayPauseButton(boolean isPlaying){
        if(!isPlaying){
            playPauseButton.setIcon(getResources().getDrawable(R.drawable.ic_play, getContext().getTheme()));
        }
        else {
            playPauseButton.setIcon(getResources().getDrawable(R.drawable.ic_pause, getContext().getTheme()));
        }
    }

    @Override
    public void onMediaItemTransition(
            @Nullable MediaItem mediaItem,
            @Player.MediaItemTransitionReason int reason
    ){
        LOGGER.info("MediaItemTransition");
        updateFromMediaItem(mediaItem);
    }

    public void updateFromMediaItem(MediaItem mediaItem){
        if(mediaItem !=null){
            Drawable themed_unknown_album = ThemedDrawableUtils.getThemedIcon(getContext(), R.drawable.ic_unkown_album, com.google.android.material.R.attr.colorSecondary, Color.BLACK);
            Glide.with(this)
                    .load(mediaItem.mediaMetadata.artworkUri)  // URI for album art
                    .placeholder(themed_unknown_album)  // Optional placeholder
                    .error(themed_unknown_album)  // Optional error image
                    .into(albumImageView);
            songTitle.setText(mediaItem.mediaMetadata.title);
            songArtist.setText(mediaItem.mediaMetadata.artist);

            if(this.getVisibility() != View.VISIBLE) {
                setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onMediaMetadataChanged(MediaMetadata mediaMetadata) {
        LOGGER.info("onMediaMetadataChanged");
        Drawable themed_unknown_album = ThemedDrawableUtils.getThemedIcon(getContext(), R.drawable.ic_unkown_album, com.google.android.material.R.attr.colorSecondary, Color.BLACK);
        Glide.with(this)
                .load(mediaMetadata.artworkUri)  // URI for album art
                .placeholder(themed_unknown_album)  // Optional placeholder
                .error(themed_unknown_album)  // Optional error image
                .into(albumImageView);
        songTitle.setText(mediaMetadata.title);
        songArtist.setText(mediaMetadata.artist);
    }

    @Override
    public void onIsPlayingChanged(boolean isPlaying) {
        updatePlayPauseButton(isPlaying);
    }
}
