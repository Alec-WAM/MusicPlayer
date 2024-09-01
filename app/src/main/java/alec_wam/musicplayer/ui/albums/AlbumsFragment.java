package alec_wam.musicplayer.ui.albums;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import alec_wam.musicplayer.database.MusicAlbum;
import alec_wam.musicplayer.database.MusicDatabase;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModelProvider;
import alec_wam.musicplayer.databinding.FragmentAlbumsBinding;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AlbumsFragment extends Fragment {

    private static final long DEBOUNCE_DELAY = 300; // Delay in milliseconds

    private FragmentAlbumsBinding binding;
    private List<MusicAlbum> albums;
    private List<MusicAlbum> filteredAlbums;
    private AlbumsAdaptor adaptor;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        AlbumsViewModel albumsViewModel =
                new ViewModelProvider(this).get(AlbumsViewModel.class);

        binding = FragmentAlbumsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

//        final TextView textView = binding.textAlbums;
//        albumsViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        albums = new ArrayList<>(MusicDatabase.ALBUMS.values());
        albums.sort(Comparator.comparing(a -> a.getName().toLowerCase()));
        filteredAlbums = new ArrayList<>();
        filteredAlbums.addAll(albums);

        SearchView searchView = binding.albumSearchView;
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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

        adaptor = new AlbumsAdaptor(this.getContext(), filteredAlbums);
        recyclerView.setAdapter(adaptor);
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
}