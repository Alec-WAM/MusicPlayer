package alec_wam.musicplayer.ui.views;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.List;
import java.util.logging.Logger;

import alec_wam.musicplayer.R;
import alec_wam.musicplayer.database.MusicDatabase;
import alec_wam.musicplayer.database.MusicFile;
import alec_wam.musicplayer.utils.FragmentUtils;
import androidx.annotation.DrawableRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ModalMenuBottomSheet extends BottomSheetDialogFragment {

    private static final Logger LOGGER = Logger.getLogger("ModalMenuBottomSheet");

    public static final String TAG = "ModalMenuBottomSheet";

    public static class MenuOption {
        private @DrawableRes int iconId;
        private String optionText;
        private View.OnClickListener clickListener;

        public MenuOption(int iconId, String optionText, View.OnClickListener clickListener) {
            this.iconId = iconId;
            this.optionText = optionText;
            this.clickListener = clickListener;
        }
    }

    public static interface ImageLoader {
        void loadImage(ImageView imageView);
    }

    private final ImageLoader menuIconLoader;
    private final String menuTitle;
    private final String menuSubtext;
    private final List<MenuOption> menuOptions;

    public ModalMenuBottomSheet(@LayoutRes int contentLayoutId, ImageLoader menuIconLoader, String menuTitle, String menuSubtext, List<MenuOption> menuOptions) {
        super(contentLayoutId);
        this.menuIconLoader = menuIconLoader;
        this.menuTitle = menuTitle;
        this.menuSubtext = menuSubtext;
        this.menuOptions = menuOptions;
    }

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        LinearLayout view = (LinearLayout) super.onCreateView(inflater, container, savedInstanceState);

        ImageView menuImageView = view.findViewById(R.id.bottom_sheet_menu_header_image);
        if(this.menuIconLoader !=null){
            this.menuIconLoader.loadImage(menuImageView);
        }

        TextView menuTitleView = view.findViewById(R.id.bottom_sheet_menu_header_title);
        menuTitleView.setText(menuTitle);

        TextView menuSubtextView = view.findViewById(R.id.bottom_sheet_menu_header_sub_title);
        menuSubtextView.setText(menuSubtext);

        for(final MenuOption option : this.menuOptions){
            View menuOptionView = inflater.inflate(R.layout.list_item_menu_option, container, false);

            ImageView menuOptionImageView = menuOptionView.findViewById(R.id.item_menu_option_icon);
            menuOptionImageView.setImageResource(option.iconId);

            TextView menuOptionTextView = menuOptionView.findViewById(R.id.item_menu_option_text);
            menuOptionTextView.setText(option.optionText);

            menuOptionView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(option.clickListener !=null){
                        option.clickListener.onClick(view);
                    }
                    ModalMenuBottomSheet.this.dismiss();
                }
            });
            view.addView(menuOptionView);
        }

//        MusicFile musicFile = MusicDatabase.SONGS.get(mediaId);
//        if(musicFile !=null){
//            ImageView albumImage = view.findViewById(R.id.music_player_song_menu_header_album_image);
//            albumImage.setImageURI(musicFile.getAlbumArtUri());
//
//            TextView songTitle = view.findViewById(R.id.music_player_song_menu_header_song_name);
//            songTitle.setText(musicFile.getName());
//
//            TextView songArtist = view.findViewById(R.id.music_player_song_menu_header_song_artist);
//            songArtist.setText(musicFile.getArtist());
//
//            LinearLayout viewAlbumLayout = view.findViewById(R.id.music_player_song_menu_view_album_container);
//            viewAlbumLayout.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    FragmentUtils.openAlbumPage(ModalMenuBottomSheet.this.getView(), musicFile.getAlbumId(), R.id.navigation_album);
//                    ModalMenuBottomSheet.this.dismiss();
//                    hidePlayerOverlay();
//                }
//            });
//
//            LinearLayout viewArtistLayout = view.findViewById(R.id.music_player_song_menu_view_artist_container);
//            viewArtistLayout.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    FragmentUtils.openArtistPage(ModalMenuBottomSheet.this.getView(), musicFile.getArtistId(), R.id.navigation_artist);
//                    ModalMenuBottomSheet.this.dismiss();
//                    hidePlayerOverlay();
//                }
//            });
//        }

        return view;
    }

}
