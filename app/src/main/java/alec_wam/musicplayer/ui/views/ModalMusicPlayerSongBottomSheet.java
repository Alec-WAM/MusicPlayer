package alec_wam.musicplayer.ui.views;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.logging.Logger;

import alec_wam.musicplayer.R;
import alec_wam.musicplayer.database.MusicDatabase;
import alec_wam.musicplayer.database.MusicFile;
import alec_wam.musicplayer.utils.FragmentUtils;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ModalMusicPlayerSongBottomSheet extends BottomSheetDialogFragment {

    private static final Logger LOGGER = Logger.getLogger("ModalMusicPlayerSongBottomSheet");

    public static final String TAG = "ModalMusicPlayerSongBottomSheet";

    private final String mediaId;
    private final MusicPlayerOverlay musicPlayerOverlay;

    public ModalMusicPlayerSongBottomSheet(@LayoutRes int contentLayoutId, MusicPlayerOverlay musicPlayerOverlay, String mediaId) {
        super(contentLayoutId);
        this.musicPlayerOverlay = musicPlayerOverlay;
        this.mediaId = mediaId;
    }

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        MusicFile musicFile = MusicDatabase.SONGS.get(mediaId);
        if(musicFile !=null){
            ImageView albumImage = view.findViewById(R.id.music_player_song_menu_header_album_image);
            albumImage.setImageURI(musicFile.getAlbumArtUri());

            TextView songTitle = view.findViewById(R.id.music_player_song_menu_header_song_name);
            songTitle.setText(musicFile.getName());

            TextView songArtist = view.findViewById(R.id.music_player_song_menu_header_song_artist);
            songArtist.setText(musicFile.getArtist());

            LinearLayout viewAlbumLayout = view.findViewById(R.id.music_player_song_menu_view_album_container);
            viewAlbumLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FragmentUtils.openAlbumPage(ModalMusicPlayerSongBottomSheet.this.getView(), musicFile.getAlbumId(), R.id.navigation_album);
                    ModalMusicPlayerSongBottomSheet.this.dismiss();
                    hidePlayerOverlay();
                }
            });

            LinearLayout viewArtistLayout = view.findViewById(R.id.music_player_song_menu_view_artist_container);
            viewArtistLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FragmentUtils.openArtistPage(ModalMusicPlayerSongBottomSheet.this.getView(), musicFile.getArtistId(), R.id.navigation_artist);
                    ModalMusicPlayerSongBottomSheet.this.dismiss();
                    hidePlayerOverlay();
                }
            });
        }

        return view;
    }

    public void hidePlayerOverlay(){
        this.musicPlayerOverlay.hideOverlay();
    }

}
