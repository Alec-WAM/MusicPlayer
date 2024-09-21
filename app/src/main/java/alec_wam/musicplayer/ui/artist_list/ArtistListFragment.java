package alec_wam.musicplayer.ui.artist_list;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import alec_wam.musicplayer.R;
import alec_wam.musicplayer.database.MusicArtist;
import alec_wam.musicplayer.database.MusicDatabase;
import alec_wam.musicplayer.utils.FragmentUtils;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import alec_wam.musicplayer.databinding.FragmentArtistListBinding;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ArtistListFragment extends Fragment implements ArtistListAdaptor.OnAristClickListener {

    private FragmentArtistListBinding binding;
    private List<MusicArtist> artists;
    private List<MusicArtist> filteredArtists;
    private ArtistListAdaptor adaptor;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentArtistListBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        artists = new ArrayList<>(MusicDatabase.ARTISTS.values());
        artists.sort(Comparator.comparing(a -> a.getName().toLowerCase()));
        filteredArtists = new ArrayList<>();
        filteredArtists.addAll(artists);

        SearchView searchView = binding.artistSearchView;
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterArtists(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterArtists(newText);
                return true;
            }
        });

        final RecyclerView recyclerView = binding.listArtists;
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));

        adaptor = new ArtistListAdaptor(this.getContext(), filteredArtists, this);
        recyclerView.setAdapter(adaptor);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);

        return root;
    }

    private void filterArtists(final String searchValue){
        this.filteredArtists.clear();
        if (searchValue.isEmpty()) {
            this.filteredArtists.addAll(this.artists);
        } else {
            this.filteredArtists.addAll(this.artists.stream().filter((a) -> a.getName().toLowerCase().contains(searchValue)).toList());
        }
        adaptor.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onArtistClick(MusicArtist musicArtist) {
        FragmentUtils.openArtistPage(this.getView(), musicArtist.getId(), R.id.action_navigation_artist_list_to_navigation_artist);
    }
}