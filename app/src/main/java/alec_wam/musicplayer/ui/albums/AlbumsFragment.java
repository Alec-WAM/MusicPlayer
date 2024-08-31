package alec_wam.musicplayer.ui.albums;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import alec_wam.musicplayer.MusicDatabase;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import alec_wam.musicplayer.databinding.FragmentAlbumsBinding;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AlbumsFragment extends Fragment {

    private FragmentAlbumsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        AlbumsViewModel albumsViewModel =
                new ViewModelProvider(this).get(AlbumsViewModel.class);

        binding = FragmentAlbumsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

//        final TextView textView = binding.textAlbums;
//        albumsViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        final RecyclerView recyclerView = binding.listAlbums;
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        final AlbumsAdaptor adaptor = new AlbumsAdaptor(this.getContext(), MusicDatabase.ALBUM_LIST);
        recyclerView.setAdapter(adaptor);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}