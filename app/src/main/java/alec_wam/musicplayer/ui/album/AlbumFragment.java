package alec_wam.musicplayer.ui.album;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import alec_wam.musicplayer.data.database.AppDatabaseViewModel;
import alec_wam.musicplayer.data.database.entities.FavoriteSong;
import alec_wam.musicplayer.data.database.entities.Playlist;
import alec_wam.musicplayer.database.MusicAlbum;
import alec_wam.musicplayer.database.MusicDatabase;
import alec_wam.musicplayer.database.MusicFile;
import alec_wam.musicplayer.R;
import alec_wam.musicplayer.databinding.FragmentAlbumBinding;
import alec_wam.musicplayer.services.MusicPlayerService;
import alec_wam.musicplayer.ui.views.ModalMenuBottomSheet;
import alec_wam.musicplayer.ui.views.MusicPlayerOverlay;
import alec_wam.musicplayer.utils.FragmentUtils;
import alec_wam.musicplayer.utils.MusicPlayerUtils;
import alec_wam.musicplayer.utils.ThemedDrawableUtils;
import alec_wam.musicplayer.utils.Utils;
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import static alec_wam.musicplayer.utils.MusicPlayerUtils.BUNDLE_SONG_CHANGE_SONG;
import static alec_wam.musicplayer.utils.MusicPlayerUtils.INTENT_SONG_CHANGE;

public class AlbumFragment extends Fragment {

    private static final Logger LOGGER = Logger.getLogger("AlbumFragment");

    public static final String ARG_ALBUM_ID = "album_id";
    public static final String ARG_IS_FAVORITES = "favorites";
    private FragmentAlbumBinding binding;

    private AppDatabaseViewModel databaseViewModel;
    private List<Playlist> playlists;

    private String albumId;
    private boolean isFavoriteSongs;
    private Map<String, View> songViews;
    private String playingSongId = null;

    public static AlbumFragment newInstance(String albumId) {
        AlbumFragment fragment = new AlbumFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ALBUM_ID, albumId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseViewModel = new ViewModelProvider(requireActivity()).get(AppDatabaseViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAlbumBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        if (getArguments() != null) {
            albumId = getArguments().getString(ARG_ALBUM_ID);
            isFavoriteSongs = getArguments().getBoolean(ARG_IS_FAVORITES, false);
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(INTENT_SONG_CHANGE);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(
                this.broadcastReceiver, filter
        );

        if(MusicPlayerService.currentSong !=null){
            LOGGER.info("Loading Current Song: " + MusicPlayerService.currentSong);
            this.playingSongId = MusicPlayerService.currentSong.mediaId;
        }

        ImageView cover = (ImageView) binding.albumInfoCover;
        TextView titleView = (TextView) binding.albumInfoTitle;
        TextView artistView = (TextView) binding.albumInfoArtist;
        TextView tracksInfoView = (TextView) binding.albumInfoTracks;
        LinearLayout song_container = (LinearLayout) binding.albumSongContainer;
        song_container.removeAllViews();

        Uri albumArt = null;
        String title = null;
        String artist = null;
        Map<Integer, List<MusicFile>> disks = null;

        if(isFavoriteSongs){
            Drawable favoriteDrawable = ThemedDrawableUtils.getThemedIcon(this.getContext(), R.drawable.ic_favorite_filled_24dp, com.google.android.material.R.attr.colorPrimary, Color.BLACK);
            cover.setImageDrawable(favoriteDrawable);
            title = "Favorite Songs";
        }
        else if(albumId !=null) {
            MusicAlbum album = MusicDatabase.getAlbumById(albumId);
            if (album != null) {
                albumArt = album.getAlbumArtUri();
                title = album.getName();
                artist = album.getArtist();
                disks = album.getAlbumMusic();
            }
        }

        if(albumArt !=null) {
            Glide.with(cover.getContext())
                    .load(albumArt)  // URI for album art
                    .placeholder(R.drawable.ic_unkown_album)  // Optional placeholder
                    .error(R.drawable.ic_unkown_album)  // Optional error image
                    .into(cover);
        }
        titleView.setText(title);
        artistView.setText(artist);

        Button playAlbumButton = binding.albumInfoButtonPlayAlbum;
        playAlbumButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isFavoriteSongs){
                    MusicPlayerUtils.playFavoriteSongs(getContext(), false);
                }
                else {
                    MusicPlayerUtils.playAlbum(getContext(), albumId, false);
                }
            }
        });

        Button shuffleAlbumButton = binding.albumInfoButtonShuffleAlbum;
        shuffleAlbumButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isFavoriteSongs){
                    MusicPlayerUtils.playFavoriteSongs(getContext(), true);
                }
                else {
                    MusicPlayerUtils.playAlbum(getContext(), albumId, true);
                }
            }
        });

        this.songViews = new HashMap<>();
        if(!isFavoriteSongs) {
            this.buildDefaultAlbum(disks, song_container, inflater);
        }

        databaseViewModel.getAllFavoriteSongsSorted().observe(getViewLifecycleOwner(), favoriteSongs -> {
            // Update UI with the favorite song items
            if(!isFavoriteSongs) {
                this.songViews.forEach((songId, songView) -> {
                    CheckBox favoriteCheckbox = (CheckBox) songView.findViewById(R.id.item_song_fav_button);
                    favoriteCheckbox.setTag("ignore");
                    favoriteCheckbox.setChecked(favoriteSongs.contains(songId));
                    favoriteCheckbox.setTag(null);
                });
            }
            else {
                this.buildFavoriteSongs(favoriteSongs, song_container, inflater);
            }
        });

        CheckBox favoriteAlbumCheckBox = binding.albumFavButton;
        if(isFavoriteSongs){
            favoriteAlbumCheckBox.setVisibility(View.GONE);
        }
        else {
            favoriteAlbumCheckBox.setVisibility(View.VISIBLE);
            favoriteAlbumCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                    if(favoriteAlbumCheckBox.getTag() == "ignore"){
                        return;
                    }
                    LOGGER.info("Check changed Album: " + albumId);
                    //TODO Fix this being called when Fragment is reloaded
//                    if(checked){
//                        AlbumFragment.this.databaseViewModel.insertFavoriteAlbum(albumId);
//                    }
//                    else {
//                        AlbumFragment.this.databaseViewModel.deleteFavoriteAlbum(albumId);
//                    }
                }
            });

            databaseViewModel.getAllFavoriteAlbums().observe(getViewLifecycleOwner(), favoriteAlbums -> {
                // Update UI with the favorite song items
                if (binding != null && albumId != null) {
                    CheckBox favoriteAlbumCheckbox = binding.albumFavButton;
                    favoriteAlbumCheckbox.setTag("ignore");
                    favoriteAlbumCheckbox.setChecked(favoriteAlbums.contains(albumId));
                    favoriteAlbumCheckbox.setTag(null);
                }
            });
        }

        databaseViewModel.getAllPlaylists().observe(getViewLifecycleOwner(), playlists -> {
            List<Playlist> playlistSorted = new ArrayList<>(playlists); // Retrieve playlists from your database
            playlistSorted.sort(new Comparator<Playlist>() {
                @Override
                public int compare(Playlist pl1, Playlist pl2) {
                    return pl1.name.compareTo(pl2.name);
                }
            });
            this.playlists = playlistSorted;
        });

        return root;
    }

    private void buildDefaultAlbum(Map<Integer, List<MusicFile>> disks, LinearLayout song_container, LayoutInflater inflater){
        if(disks !=null && disks.keySet().size() > 0){
            boolean showDisks = disks.keySet().size() > 1;
            List<Integer> diskKeys = new ArrayList<>(disks.keySet());
            diskKeys.sort((a, b) -> {
                return Integer.compare(a, b);
            });
            Drawable themed_disc = ThemedDrawableUtils.getThemedIcon(getContext(), R.drawable.ic_disk, com.google.android.material.R.attr.colorSecondary, Color.BLACK);
            for(int diskNum : diskKeys) {
                if(showDisks){
                    LinearLayout diskHeader = (LinearLayout) inflater.inflate(R.layout.list_item_disk_header, song_container, false);
                    ImageView diskImage = (ImageView) diskHeader.findViewById(R.id.list_item_disk_icon);
                    TextView diskNumberText = (TextView) diskHeader.findViewById(R.id.list_item_disk_number);
                    diskImage.setImageDrawable(themed_disc);
                    diskNumberText.setText("Disk " + diskNum);
                    song_container.addView(diskHeader);
                }
                List<MusicFile> tracks = new ArrayList<>(disks.get(diskNum));
                tracks.sort(Comparator.comparingInt(MusicFile::getTrack));

                for (int i = 0; i < tracks.size(); i++) {
                    final MusicFile track = tracks.get(i);
                    LinearLayout songView = (LinearLayout) inflater.inflate(R.layout.list_item_song, song_container, false);

                    TextView trackNumberView = (TextView) songView.findViewById(R.id.item_song_track);
                    trackNumberView.setText("" + Utils.getRealTrackNumber(track.getTrack()));

                    TextView trackTitleView = (TextView) songView.findViewById(R.id.item_song_title_text);
                    trackTitleView.setText(track.getName());

                    TextView trackSubtitleView = (TextView) songView.findViewById(R.id.item_song_subtitle);
                    String subTitle = track.getArtist();
                    if (track.getDuration() > 0) {
                        subTitle += " - " + Utils.convertMillisecondsToTimeString(track.getDuration());
                    }
                    trackSubtitleView.setText(subTitle);

                    CheckBox favoriteCheckBox = (CheckBox) songView.findViewById(R.id.item_song_fav_button);
                    favoriteCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                            if(favoriteCheckBox.getTag() == "ignore"){
                                return;
                            }
                            LOGGER.info("Check changed: " + track.getName());
                            if(checked){
                                AlbumFragment.this.databaseViewModel.insertFavoriteSong(track.getId());
                            }
                            else {
                                AlbumFragment.this.databaseViewModel.deleteFavoriteSong(track.getId());
                            }
                        }
                    });

                    Button menuButton = songView.findViewById(R.id.item_song_menu_button);
                    menuButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            buildSongModalMenu(track);
                        }
                    });

                    songView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            AlbumFragment.this.clickSong(track);
                        }
                    });

                    if(track.getId().equals(this.playingSongId)) {
                        int themeColor = ThemedDrawableUtils.getThemeColor(getContext(), com.google.android.material.R.attr.colorSecondaryContainer, R.color.colorCustomColor);
                        songView.setBackgroundColor(themeColor);
                    }

                    song_container.addView(songView);
                    this.songViews.put(track.getId(), songView);
                }
            }
        }
    }

    private void buildFavoriteSongs(List<String> songIds, LinearLayout song_container, LayoutInflater inflater){
        this.songViews.clear();
        song_container.removeAllViews();

        for (int i = 0; i < songIds.size(); i++) {
            final String songId = songIds.get(i);
            final MusicFile track = MusicDatabase.SONGS.get(songId);
            if(track == null){
                continue;
            }
            LinearLayout songView = (LinearLayout) inflater.inflate(R.layout.list_item_song, song_container, false);

            TextView trackNumberView = (TextView) songView.findViewById(R.id.item_song_track);
            trackNumberView.setVisibility(View.GONE);

            String trackAlbumId = track.getAlbumId();
            MusicAlbum musicAlbum = MusicDatabase.getAlbumById(trackAlbumId);
            Uri albumArtUri = musicAlbum !=null ? musicAlbum.getAlbumArtUri() : null;
            ImageView albumImage = (ImageView) songView.findViewById(R.id.item_song_album_image);
            albumImage.setVisibility(View.VISIBLE);
            Drawable themed_unknown_album = ThemedDrawableUtils.getThemedIcon(this.getContext(), R.drawable.ic_unkown_album, com.google.android.material.R.attr.colorPrimary, Color.BLACK);
            Glide.with(getContext())
                    .load(albumArtUri)  // URI for album art
                    .placeholder(themed_unknown_album)  // Optional placeholder
                    .error(themed_unknown_album)  // Optional error image
                    .into(albumImage);

            TextView trackTitleView = (TextView) songView.findViewById(R.id.item_song_title_text);
            trackTitleView.setText(track.getName());

            TextView trackSubtitleView = (TextView) songView.findViewById(R.id.item_song_subtitle);
            String subTitle = track.getArtist();
            if (track.getDuration() > 0) {
                subTitle += " - " + Utils.convertMillisecondsToTimeString(track.getDuration());
            }
            trackSubtitleView.setText(subTitle);

            CheckBox favoriteCheckBox = (CheckBox) songView.findViewById(R.id.item_song_fav_button);
            favoriteCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                    if(favoriteCheckBox.getTag() == "ignore"){
                        return;
                    }
                    LOGGER.info("Check changed: " + track.getName());
                    if(checked){
                        AlbumFragment.this.databaseViewModel.insertFavoriteSong(track.getId());
                    }
                    else {
                        AlbumFragment.this.databaseViewModel.deleteFavoriteSong(track.getId());
                    }
                }
            });
            favoriteCheckBox.setTag("ignore");
            favoriteCheckBox.setChecked(true);
            favoriteCheckBox.setTag(null);

            Button menuButton = songView.findViewById(R.id.item_song_menu_button);
            menuButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    buildSongModalMenu(track);
                }
            });

            songView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlbumFragment.this.clickSong(track);
                }
            });

            if(track.getId().equals(this.playingSongId)) {
                int themeColor = ThemedDrawableUtils.getThemeColor(getContext(), com.google.android.material.R.attr.colorSecondaryContainer, R.color.colorCustomColor);
                songView.setBackgroundColor(themeColor);
            }

            song_container.addView(songView);
            this.songViews.put(track.getId(), songView);
        }
    }

    public void buildSongModalMenu(final MusicFile musicFile){
        if(musicFile !=null) {
            String title = musicFile.getName();
            String subText = musicFile.getArtist();
            ModalMenuBottomSheet.ImageLoader imageLoader = new ModalMenuBottomSheet.ImageLoader() {
                @Override
                public void loadImage(ImageView imageView) {
                    Drawable themed_unknown_album = ThemedDrawableUtils.getThemedIcon(getContext(), R.drawable.ic_unkown_album, com.google.android.material.R.attr.colorSecondary, Color.BLACK);
                    Glide.with(AlbumFragment.this.getContext())
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
                    showAddToPlaylistDialog(musicFile.getId());
                }
            }));
            ModalMenuBottomSheet modal = new ModalMenuBottomSheet(R.layout.layout_bottom_sheet_menu, imageLoader, title, subText, menuOptionList);
            if (getContext() instanceof FragmentActivity) {
                FragmentManager fragmentManager = ((FragmentActivity) getContext()).getSupportFragmentManager();
                modal.show(fragmentManager, ModalMenuBottomSheet.TAG);
            }
        }
    }

    private void showAddToPlaylistDialog(final String sondId){
        // Inflate the dialog layout
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.layout_dialog_add_to_playlist, null);

        // Create the dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(dialogView);

        // Set up the RecyclerView (assuming you have an adapter)
        final List<Integer> selectedPlaylists = new ArrayList<>();
        final LinearLayout scrollContainer = dialogView.findViewById(R.id.dialog_add_to_playlist_scroll_container);
        for(final Playlist playlist : this.playlists){
            View playlistSelectionView = inflater.inflate(R.layout.list_item_playlist_selection, null);
            TextView playlistTextView = playlistSelectionView.findViewById(R.id.item_playlist_selection_text);
            playlistTextView.setText(playlist.name);
            CheckBox playlistCheckbox = playlistSelectionView.findViewById(R.id.item_playlist_selection_checkbox);
            playlistCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                    if(checked){
                        selectedPlaylists.add(playlist.id);
                    }
                    else {
                        selectedPlaylists.remove(playlist.id);
                    }
                }
            });
            scrollContainer.addView(playlistSelectionView);
        }

        // Handle clicking the "Create New Playlist" button
//        createPlaylistButton.setOnClickListener(v -> {
//            // Show a dialog to create a new playlist
//            showCreatePlaylistDialog();
//            dialog.dismiss();
//        });

        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                for(int playlistId : selectedPlaylists){
                    databaseViewModel.insertPlaylistSong(playlistId, sondId);
                }
            }
        });

        // Set negative button to cancel
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        // Create and show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                if(INTENT_SONG_CHANGE.equalsIgnoreCase(intent.getAction())){
                    final String lastPlayingSongId = playingSongId;
                    String songId = intent.getStringExtra(BUNDLE_SONG_CHANGE_SONG);
                    if(songId !=null) {
                        MusicFile musicFile = MusicDatabase.SONGS.get(songId);
                        if(musicFile !=null){
                            if(musicFile.getAlbumId().equals(albumId) || AlbumFragment.this.isFavoriteSongs){
                                final View oldSongView = AlbumFragment.this.songViews.get(lastPlayingSongId);
                                if(oldSongView !=null) {
                                    oldSongView.setBackground(null);
                                }

                                AlbumFragment.this.playingSongId = songId;
                                View newSongView = AlbumFragment.this.songViews.get(songId);
                                int themeColor = ThemedDrawableUtils.getThemeColor(getContext(), com.google.android.material.R.attr.colorSecondaryContainer, R.color.colorCustomColor);
                                newSongView.setBackgroundColor(themeColor);
                            }
                        }
                    }
                    else {
                        final View oldSongView = AlbumFragment.this.songViews.get(lastPlayingSongId);
                        if(oldSongView !=null) {
                            oldSongView.setBackground(null);
                        }
                    }
                }
            }
        }
    };

    @Override
    public void onDestroyView() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(broadcastReceiver);
        super.onDestroyView();
        binding = null;
    }

    public void clickSong(MusicFile musicFile){
        MusicPlayerUtils.playSong(this.getContext(), musicFile.getId(), albumId == null ? Optional.empty() : Optional.of(albumId), isFavoriteSongs);
    }
}