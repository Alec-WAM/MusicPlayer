package alec_wam.musicplayer.ui.artist;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import alec_wam.musicplayer.database.MusicAlbum;
import alec_wam.musicplayer.database.MusicArtist;
import alec_wam.musicplayer.database.MusicDatabase;
import alec_wam.musicplayer.database.MusicFile;
import alec_wam.musicplayer.R;
import alec_wam.musicplayer.databinding.FragmentArtistBinding;
import alec_wam.musicplayer.ui.album_list.AlbumListAdaptor;
import alec_wam.musicplayer.utils.FragmentUtils;
import alec_wam.musicplayer.utils.Utils;
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ArtistFragment extends Fragment implements AlbumListAdaptor.OnAlbumClickListener {

    public static final String ARG_ARTIST = "artist";
    private FragmentArtistBinding binding;
    private String artistName;
    private List<MusicAlbum> artistAlbums;
    private AlbumListAdaptor adaptor;

    public static ArtistFragment newInstance(String albumId) {
        ArtistFragment fragment = new ArtistFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ARTIST, albumId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentArtistBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        if (getArguments() != null) {
            artistName = getArguments().getString(ARG_ARTIST);
        }

        ImageView cover = (ImageView) binding.artistInfoCover;
        TextView titleView = (TextView) binding.artistInfoTitle;
        TextView subTitleView = (TextView) binding.artistInfoSubTitle;

        Uri artistArt = null;
        Set<String> albumIds = null;

        MusicArtist artist = MusicDatabase.ARTISTS.get(artistName);
        if(artist !=null){
            albumIds = artist.getAlbumIds();
        }

//        Glide.with(cover.getContext())
//                .load(albumArt)  // URI for album art
//                .placeholder(R.drawable.ic_unkown_album)  // Optional placeholder
//                .error(R.drawable.ic_unkown_album)  // Optional error image
//                .into(cover);
//        titleView.setText(title);
        titleView.setText(artistName);

        artistAlbums = new ArrayList<>();
        if(albumIds !=null && albumIds.size() > 0){
            for(String id : albumIds){
                MusicAlbum album = MusicDatabase.getAlbumById(id);
                if(album !=null){
                    artistAlbums.add(album);
                }
            }
            artistAlbums.sort(Comparator.comparing(a -> a.getName().toLowerCase()));
        }

        final RecyclerView recyclerView = binding.listArtistAlbums;
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));

        adaptor = new AlbumListAdaptor(this.getContext(), artistAlbums, this);
        recyclerView.setAdapter(adaptor);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onAlbumClick(MusicAlbum musicAlbum) {
        FragmentUtils.openAlbumPage(this.getView(), musicAlbum.getAlbumId(), R.id.action_navigation_artist_to_navigation_album);
    }
}