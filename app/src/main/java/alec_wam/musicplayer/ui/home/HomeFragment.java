package alec_wam.musicplayer.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import alec_wam.musicplayer.R;
import alec_wam.musicplayer.database.MusicAlbum;
import alec_wam.musicplayer.services.MusicPlayerSavedDataManager;
import alec_wam.musicplayer.ui.album_list.AlbumListAdaptor;
import alec_wam.musicplayer.utils.FragmentUtils;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import alec_wam.musicplayer.databinding.FragmentHomeBinding;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class HomeFragment extends Fragment {

    private static final Logger LOGGER = Logger.getLogger("HomeFragment");

    private FragmentHomeBinding binding;
    private MusicPlayerSavedDataManager musicPlayerSavedDataManager;

    private CardView recentAlbumsCard;
    private RecyclerView recentAlbumsRecyclerView;
    private RecentAlbumsAdaptor recentAlbumsAdaptor;
    private List<String> recentAlbums;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        musicPlayerSavedDataManager = new MusicPlayerSavedDataManager(getContext());
        //TODO Update this from the service
        recentAlbums = musicPlayerSavedDataManager.getRecentAlbums();
        Collections.reverse(recentAlbums);

        recentAlbumsRecyclerView = root.findViewById(R.id.home_card_recent_albums_recyclerview);
        recentAlbumsRecyclerView.setLayoutManager(new LinearLayoutManager(this.getContext(), RecyclerView.HORIZONTAL, false));

        recentAlbumsAdaptor = new RecentAlbumsAdaptor(this.getContext(), recentAlbums, new RecentAlbumsAdaptor.OnAlbumClickListener() {
            @Override
            public void onAlbumClick(MusicAlbum musicAlbum) {
                FragmentUtils.openAlbumPage(HomeFragment.this.getView(), musicAlbum.getAlbumId(), R.id.action_navigation_home_to_navigation_album);
            }
        });
        recentAlbumsRecyclerView.setAdapter(recentAlbumsAdaptor);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}