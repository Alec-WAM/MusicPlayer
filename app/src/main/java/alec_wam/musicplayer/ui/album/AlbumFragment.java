package alec_wam.musicplayer.ui.album;

import android.content.BroadcastReceiver;
import android.content.Context;
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
import alec_wam.musicplayer.database.MusicAlbum;
import alec_wam.musicplayer.database.MusicDatabase;
import alec_wam.musicplayer.database.MusicFile;
import alec_wam.musicplayer.R;
import alec_wam.musicplayer.databinding.FragmentAlbumBinding;
import alec_wam.musicplayer.services.MusicPlayerService;
import alec_wam.musicplayer.utils.MusicPlayerUtils;
import alec_wam.musicplayer.utils.ThemedDrawableUtils;
import alec_wam.musicplayer.utils.Utils;
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

    private String albumId;
    private boolean isFavoriteSongs;
    private Map<Long, View> songViews;
    private long playingSongId = -1L;

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
            this.playingSongId = Long.parseLong(MusicPlayerService.currentSong.mediaId);
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
                    if(checked){
                        AlbumFragment.this.databaseViewModel.insertFavoriteAlbum(albumId);
                    }
                    else {
                        AlbumFragment.this.databaseViewModel.deleteFavoriteAlbum(albumId);
                    }
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

                    // TODO Add action to button

                    songView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            AlbumFragment.this.clickSong(track);
                        }
                    });

                    if(track.getId() == this.playingSongId) {
                        int themeColor = ThemedDrawableUtils.getThemeColor(getContext(), com.google.android.material.R.attr.colorSecondaryContainer, R.color.colorCustomColor);
                        songView.setBackgroundColor(themeColor);
                    }

                    song_container.addView(songView);
                    this.songViews.put(track.getId(), songView);
                }
            }
        }
    }

    private void buildFavoriteSongs(List<Long> songIds, LinearLayout song_container, LayoutInflater inflater){
        this.songViews.clear();
        song_container.removeAllViews();

        for (int i = 0; i < songIds.size(); i++) {
            final long songId = songIds.get(i);
            final MusicFile track = MusicDatabase.SONGS.get(songId);
            if(track == null){
                continue;
            }
            LinearLayout songView = (LinearLayout) inflater.inflate(R.layout.list_item_song, song_container, false);

            TextView trackNumberView = (TextView) songView.findViewById(R.id.item_song_track);
            trackNumberView.setVisibility(View.GONE);

            String trackAlbumId = track.getAlbumId();
            Uri albumArtUri = MusicDatabase.getAlbumArtUri(trackAlbumId);
            if(albumArtUri !=null){
                ImageView albumImage = (ImageView) songView.findViewById(R.id.item_song_album_image);
                albumImage.setImageURI(albumArtUri);
                albumImage.setVisibility(View.VISIBLE);
            }

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

            // TODO Add action to button

            songView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlbumFragment.this.clickSong(track);
                }
            });

            if(track.getId() == this.playingSongId) {
                int themeColor = ThemedDrawableUtils.getThemeColor(getContext(), com.google.android.material.R.attr.colorSecondaryContainer, R.color.colorCustomColor);
                songView.setBackgroundColor(themeColor);
            }

            song_container.addView(songView);
            this.songViews.put(track.getId(), songView);
        }
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                if(INTENT_SONG_CHANGE.equalsIgnoreCase(intent.getAction())){
                    final long lastPlayingSongId = playingSongId;
                    long songId = intent.getLongExtra(BUNDLE_SONG_CHANGE_SONG, -1);
                    if(songId > -1L) {
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