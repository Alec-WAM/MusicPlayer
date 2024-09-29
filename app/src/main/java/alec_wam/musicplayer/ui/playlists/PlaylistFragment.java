package alec_wam.musicplayer.ui.playlists;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import alec_wam.musicplayer.data.database.AppDatabaseViewModel;
import alec_wam.musicplayer.data.database.entities.PlaylistSong;
import alec_wam.musicplayer.databinding.FragmentPlaylistBinding;
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class PlaylistFragment extends Fragment implements PlaylistSongAdapter.OnItemMovedListener {

    public static final String ARG_PLAYLIST = "playlist";
    private FragmentPlaylistBinding binding;
    private int playlistId = -1;
    private List<PlaylistSong> playlistSongList;
    private PlaylistSongAdapter adaptor;

    private AppDatabaseViewModel databaseViewModel;

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

        final ImageView cover = (ImageView) binding.playlistInfoCover;
        final TextView titleView = (TextView) binding.playlistInfoTitle;
        final TextView subTitleView = (TextView) binding.playlistInfoSubTitle;

        databaseViewModel.getPlaylist(this.playlistId).observe(getViewLifecycleOwner(), playlist -> {
            if(playlist !=null){
                //TODO Set Playlist Image
                titleView.setText(playlist.name);
                subTitleView.setText(null);
            }
        });

        final RecyclerView recyclerView = binding.listPlaylistSongs;
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));

        playlistSongList = new ArrayList<>();

        adaptor = new PlaylistSongAdapter(getContext(), playlistSongList, this);
//        ItemTouchHelper itemTouchHelper = getItemTouchHelper();
        CustomPlaylistItemTouchHelperCallback itemTouchHelperCallback = new CustomPlaylistItemTouchHelperCallback(adaptor);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchHelperCallback);
        adaptor.setItemTouchHelper(itemTouchHelper);
        recyclerView.setAdapter(adaptor);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        if(this.playlistId >= 0) {
            databaseViewModel.getPlaylistSongs(this.playlistId).observe(getViewLifecycleOwner(), playlistSongs -> {
                this.playlistSongList.clear();
                this.playlistSongList.addAll(playlistSongs);
                this.adaptor.notifyDataSetChanged();
            });
        }

        return root;
    }

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

    @Override
    public void onDestroyView() {
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
}