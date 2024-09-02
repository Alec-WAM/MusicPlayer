package alec_wam.musicplayer.ui.album_list;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import alec_wam.musicplayer.R;
import alec_wam.musicplayer.database.MusicAlbum;
import alec_wam.musicplayer.database.MusicDatabase;
import alec_wam.musicplayer.utils.FragmentUtils;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import alec_wam.musicplayer.databinding.FragmentAlbumsBinding;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AlbumListFragment extends Fragment implements AlbumListAdaptor.OnAlbumClickListener {

    private static final long DEBOUNCE_DELAY = 300; // Delay in milliseconds

    private FragmentAlbumsBinding binding;
    private List<MusicAlbum> albums;
    private List<MusicAlbum> filteredAlbums;
    private AlbumListAdaptor adaptor;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        AlbumListViewModel albumListViewModel =
                new ViewModelProvider(this).get(AlbumListViewModel.class);

        binding = FragmentAlbumsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

//        final TextView textView = binding.textAlbums;
//        albumsViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        albums = new ArrayList<>(MusicDatabase.ALBUMS.values());
        albums.sort(Comparator.comparing(a -> a.getName().toLowerCase()));
        filteredAlbums = new ArrayList<>();
        filteredAlbums.addAll(albums);

        SearchView searchBar = binding.albumSearchView;
        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterAlbums(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterAlbums(newText);
                return true;
            }
        });

        final RecyclerView recyclerView = binding.listAlbums;
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));

        adaptor = new AlbumListAdaptor(this.getContext(), filteredAlbums, this);
        recyclerView.setAdapter(adaptor);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);

        return root;
    }

    private void filterAlbums(final String searchValue){
        this.filteredAlbums.clear();
        if (searchValue.isEmpty()) {
            this.filteredAlbums.addAll(this.albums);
        } else {
            this.filteredAlbums.addAll(this.albums.stream().filter((a) -> a.getName().toLowerCase().contains(searchValue)).toList());
        }
        adaptor.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onAlbumClick(MusicAlbum musicAlbum) {
        FragmentUtils.openAlbumPage(this.getView(), musicAlbum.getAlbumId(), R.id.action_navigation_album_list_to_navigation_album);
    }
}