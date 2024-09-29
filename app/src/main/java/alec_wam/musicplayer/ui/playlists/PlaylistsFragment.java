package alec_wam.musicplayer.ui.playlists;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import alec_wam.musicplayer.R;
import alec_wam.musicplayer.data.database.AppDatabaseViewModel;
import alec_wam.musicplayer.data.database.entities.Playlist;
import alec_wam.musicplayer.utils.FragmentUtils;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import alec_wam.musicplayer.databinding.FragmentPlaylistsBinding;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class PlaylistsFragment extends Fragment implements PlaylistListAdaptor.OnPlaylistClickListener {

    private FragmentPlaylistsBinding binding;
    private PlaylistListAdaptor adaptor;
    private List<Playlist> playlistList;
    private FloatingActionButton createPlaylistFab;

    private AppDatabaseViewModel databaseViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseViewModel = new ViewModelProvider(requireActivity()).get(AppDatabaseViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPlaylistsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final RecyclerView recyclerView = binding.playlistRecyclerview;
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));

        playlistList = new ArrayList<>();

        adaptor = new PlaylistListAdaptor(this.getContext(), playlistList, this);
        recyclerView.setAdapter(adaptor);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);

        databaseViewModel.getAllPlaylists().observe(getViewLifecycleOwner(), playlists -> {
            this.playlistList.clear();
            this.playlistList.addAll(playlists);
            this.adaptor.notifyDataSetChanged();
        });

        createPlaylistFab = binding.playlistAddFab;
        createPlaylistFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCreatePlaylistDialog();
            }
        });

        return root;
    }

    private void showCreatePlaylistDialog(){
        final EditText playlistNameInput = new EditText(getContext());
        playlistNameInput.setHint("Enter playlist name");

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        dialogBuilder.setTitle("Create New Playlist");
        dialogBuilder.setView(playlistNameInput);

        dialogBuilder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String playlistName = playlistNameInput.getText().toString().trim();

                if (!playlistName.isEmpty()) {
                    // Handle playlist creation
                    createPlaylist(playlistName);
                } else {
                    // Optionally, show a message if the playlist name is empty
                    playlistNameInput.setError("Playlist name cannot be empty");
                }
            }
        });

        // Set negative button to cancel
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        // Show the dialog
        AlertDialog dialog = dialogBuilder.create();
        dialog.show();
    }

    private void createPlaylist(String playlistName) {
        databaseViewModel.insertPlaylist(playlistName);
        Toast.makeText(getContext(), "Playlist '" + playlistName + "' created", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onPlaylistCicked(Playlist playlist) {
        FragmentUtils.openPlaylist(playlist.id, R.id.action_navigation_playlists_to_navigation_playlist);
    }
}