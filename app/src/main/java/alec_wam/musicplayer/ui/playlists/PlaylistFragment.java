package alec_wam.musicplayer.ui.playlists;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import alec_wam.musicplayer.MainActivity;
import alec_wam.musicplayer.R;
import alec_wam.musicplayer.data.database.AppDatabaseViewModel;
import alec_wam.musicplayer.data.database.entities.PlaylistSong;
import alec_wam.musicplayer.database.MusicDatabase;
import alec_wam.musicplayer.database.MusicFile;
import alec_wam.musicplayer.databinding.FragmentPlaylistBinding;
import alec_wam.musicplayer.services.MusicPlayerService;
import alec_wam.musicplayer.ui.views.ModalMenuBottomSheet;
import alec_wam.musicplayer.utils.MusicPlayerUtils;
import alec_wam.musicplayer.utils.PlaylistUtils;
import alec_wam.musicplayer.utils.ThemedDrawableUtils;
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.media3.common.MediaItem;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static alec_wam.musicplayer.services.MusicPlayerService.BUNDLE_PLAYLIST_SONG;
import static alec_wam.musicplayer.utils.MusicPlayerUtils.BUNDLE_SONG_CHANGE_SONG;
import static alec_wam.musicplayer.utils.MusicPlayerUtils.BUNDLE_SONG_CHANGE_SONG_PLAYLIST_SONG_ID;
import static alec_wam.musicplayer.utils.MusicPlayerUtils.INTENT_SONG_CHANGE;

public class PlaylistFragment extends Fragment implements PlaylistSongAdapter.OnItemMovedListener, PlaylistSongAdapter.OnPlaylistSongClickListener, PlaylistSongAdapter.OnPlaylistSongMenuClickListener {

    public static final String ARG_PLAYLIST = "playlist";
    private FragmentPlaylistBinding binding;
    private int playlistId = -1;
    private List<PlaylistSong> playlistSongList;
    private PlaylistSongAdapter adaptor;

    private AppDatabaseViewModel databaseViewModel;

    //TODO Move playlist order, name, and cover image to and edit mode

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
        binding = FragmentPlaylistBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        if (getArguments() != null) {
            playlistId = getArguments().getInt(ARG_PLAYLIST);
        }

        int playingPlaylistSongId = -1;
        MediaItem currentSong = MusicPlayerService.currentSong;
        if(currentSong !=null){
            if(currentSong.mediaMetadata !=null && currentSong.mediaMetadata.extras !=null) {
                int playlistSongId = currentSong.mediaMetadata.extras.getInt(BUNDLE_PLAYLIST_SONG, -1);
                playingPlaylistSongId = playlistSongId;
            }
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(INTENT_SONG_CHANGE);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(
                this.broadcastReceiver, filter
        );

        final ImageView cover = (ImageView) binding.playlistInfoCover;
        final TextView titleView = (TextView) binding.playlistInfoTitle;
        final TextView subTitleView = (TextView) binding.playlistInfoSubTitle;

        databaseViewModel.getPlaylist(this.playlistId).observe(getViewLifecycleOwner(), playlist -> {
            if(playlist !=null){
                //TODO Set Playlist Image
                titleView.setText(playlist.name);
                subTitleView.setText(null);
                Drawable themed_default_playlist = ThemedDrawableUtils.getThemedIcon(PlaylistFragment.this.getContext(), R.drawable.ic_playlist_default, com.google.android.material.R.attr.colorPrimary, Color.BLACK);
                Glide.with(PlaylistFragment.this.getContext())
                        .load(playlist.coverImagePath !=null ? new File(playlist.coverImagePath) : null) // Load the image from the stored file path
                        .placeholder(themed_default_playlist) // Default image if none exists
                        .into(cover);
            }
        });

        cover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(playlistId >= 0) {
                    openImageSelector(playlistId);
                }
            }
        });

        Button playButton = binding.playlistInfoButtonPlay;
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MusicPlayerUtils.playPlaylist(PlaylistFragment.this.getContext(), playlistId, Optional.empty(), Optional.of(false));
            }
        });

        Button shuffleButton = binding.playlistInfoButtonShuffle;
        shuffleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MusicPlayerUtils.playPlaylist(PlaylistFragment.this.getContext(), playlistId, Optional.empty(), Optional.of(true));
            }
        });

        final RecyclerView recyclerView = binding.listPlaylistSongs;
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()) {
            @Override
            public boolean canScrollVertically(){
                return false;
            }
        });

        playlistSongList = new ArrayList<>();

        adaptor = new PlaylistSongAdapter(getContext(), this.databaseViewModel, playlistSongList, this, this, this);
//        ItemTouchHelper itemTouchHelper = getItemTouchHelper();
        CustomPlaylistItemTouchHelperCallback itemTouchHelperCallback = new CustomPlaylistItemTouchHelperCallback(adaptor);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchHelperCallback);
        adaptor.setItemTouchHelper(itemTouchHelper);
        adaptor.setPlayingPlaylistSongId(playingPlaylistSongId);
        recyclerView.setAdapter(adaptor);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        if(this.playlistId >= 0) {
            databaseViewModel.getPlaylistSongs(this.playlistId).observe(getViewLifecycleOwner(), playlistSongs -> {
                String songCount = playlistSongs.size() + " song" + (playlistSongs.size() > 1 ? "s" : "");
                subTitleView.setText(songCount);

                this.playlistSongList.clear();
                this.playlistSongList.addAll(playlistSongs);
                this.adaptor.notifyDataSetChanged();
            });

            databaseViewModel.getFavoriteSongIdsInPlaylist(this.playlistId).observe(getViewLifecycleOwner(), favoriteSongIds -> {
                this.adaptor.setFavoriteSongIds(favoriteSongIds);
                this.adaptor.notifyDataSetChanged();
            });
        }

        return root;
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                if(INTENT_SONG_CHANGE.equalsIgnoreCase(intent.getAction())){
                    int playlistSongId = intent.getIntExtra(BUNDLE_SONG_CHANGE_SONG_PLAYLIST_SONG_ID, -1);
                    if(playlistSongId >= 0) {
                        adaptor.setPlayingPlaylistSongId(playlistSongId);
                        adaptor.notifyDataSetChanged();
                    }
                }
            }
        }
    };

    private @NonNull ItemTouchHelper getItemTouchHelper() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                int fromPosition = viewHolder.getBindingAdapterPosition();
                int toPosition = target.getBindingAdapterPosition();
                adaptor.moveItem(fromPosition, toPosition, true);
                return true;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                // Do nothing here as we're only supporting drag-and-drop for reordering
            }

            @Override
            public boolean isLongPressDragEnabled() {
                // Disable long press drag; we will handle drag manually from the handle
                return false;
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        return itemTouchHelper;
    }

    public void buildSongModalMenu(final PlaylistSong playlistSong){
        final MusicFile musicFile = MusicDatabase.SONGS.get(playlistSong.songId);
        if(musicFile !=null) {
            String title = musicFile.getName();
            String subText = musicFile.getArtist();
            ModalMenuBottomSheet.ImageLoader imageLoader = new ModalMenuBottomSheet.ImageLoader() {
                @Override
                public void loadImage(ImageView imageView) {
                    Drawable themed_unknown_album = ThemedDrawableUtils.getThemedIcon(getContext(), R.drawable.ic_unkown_album, com.google.android.material.R.attr.colorSecondary, Color.BLACK);
                    Glide.with(PlaylistFragment.this.getContext())
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
                    PlaylistUtils.showAddToPlaylistDialog(PlaylistFragment.this.getContext(), playlistSong.songId, databaseViewModel);
                }
            }));
            menuOptionList.add(new ModalMenuBottomSheet.MenuOption(R.drawable.ic_delete, "Remove from Playlist", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    databaseViewModel.deletePlaylistSong(playlistSong.id);
                }
            }));
            ModalMenuBottomSheet modal = new ModalMenuBottomSheet(R.layout.layout_bottom_sheet_menu, imageLoader, title, subText, menuOptionList);
            if (getContext() instanceof FragmentActivity) {
                FragmentManager fragmentManager = ((FragmentActivity) getContext()).getSupportFragmentManager();
                modal.show(fragmentManager, ModalMenuBottomSheet.TAG);
            }
        }
    }

    private void openImageSelector(int playlistId) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.putExtra("playlistId", playlistId); // Pass the playlist ID
        if(getActivity() instanceof MainActivity) {
            ((MainActivity)getActivity()).openPlaylistImagePicker(playlistId);
        }
    }

    @Override
    public void onDestroyView() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(broadcastReceiver);
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onItemMoved(int fromPosition, int toPosition) {
        // Loop through the playlistSongs and update their position
        for (int i = 0; i < playlistSongList.size(); i++) {
            PlaylistSong playlistSong = playlistSongList.get(i);
            playlistSong.position = i;
            databaseViewModel.updatePlaylistSongPosition(playlistSong.id, i);  // Update position in database
        }
    }

    @Override
    public void onPlaylistSongClicked(int playlistSongId) {
        MusicPlayerUtils.playPlaylist(getContext(), this.playlistId, Optional.of(playlistSongId), Optional.empty());
    }

    @Override
    public void onMenuClicked(PlaylistSong playlistSong) {
        buildSongModalMenu(playlistSong);
    }
}