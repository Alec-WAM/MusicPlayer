package alec_wam.musicplayer.ui.album;

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

import alec_wam.musicplayer.database.MusicAlbum;
import alec_wam.musicplayer.database.MusicDatabase;
import alec_wam.musicplayer.database.MusicFile;
import alec_wam.musicplayer.R;
import alec_wam.musicplayer.databinding.FragmentAlbumBinding;
import alec_wam.musicplayer.utils.Utils;
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class AlbumFragment extends Fragment {

    public static final String ARG_ALBUM_ID = "album_id";
    private FragmentAlbumBinding binding;
    private String albumId;

    public static AlbumFragment newInstance(String albumId) {
        AlbumFragment fragment = new AlbumFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ALBUM_ID, albumId);
        fragment.setArguments(args);
        return fragment;
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

        MusicAlbum album = MusicDatabase.getAlbumById(albumId);
        if(album !=null){
            albumArt = album.getAlbumArtUri();
            title = album.getName();
            artist = album.getArtist();
            disks = album.getAlbumMusic();
        }

        Glide.with(cover.getContext())
                .load(albumArt)  // URI for album art
                .placeholder(R.drawable.ic_unkown_album)  // Optional placeholder
                .error(R.drawable.ic_unkown_album)  // Optional error image
                .into(cover);
        titleView.setText(title);
        artistView.setText(artist);

        if(disks !=null && disks.keySet().size() > 0){
            boolean showDisks = disks.keySet().size() > 1;
            List<Integer> diskKeys = new ArrayList<>(disks.keySet());
            diskKeys.sort((a, b) -> {
                return Integer.compare(a, b);
            });
            for(int diskNum : diskKeys) {
                if(showDisks){
                    LinearLayout diskHeader = (LinearLayout) inflater.inflate(R.layout.list_item_disk_header, song_container, false);
                    TextView diskNumberText = (TextView) diskHeader.findViewById(R.id.list_item_disk_number);
                    diskNumberText.setText("Disk " + diskNum);
                    song_container.addView(diskHeader);
                }
                List<MusicFile> tracks = new ArrayList<>(disks.get(diskNum));
                tracks.sort(Comparator.comparingInt(MusicFile::getTrack));

                for (int i = 0; i < tracks.size(); i++) {
                    MusicFile track = tracks.get(i);
                    LinearLayout songView = (LinearLayout) inflater.inflate(R.layout.list_item_song, song_container, false);

                    TextView trackNumberView = (TextView) songView.findViewById(R.id.item_song_track);
                    trackNumberView.setText("" + Utils.getRealTrackNumber(track.getTrack()));

                    TextView trackTitleView = (TextView) songView.findViewById(R.id.item_song_title_text);
                    trackTitleView.setText(track.getName());

                    TextView trackSubtitleView = (TextView) songView.findViewById(R.id.item_song_subtitle);
                    String subTitle = track.getArtist();
                    // TODO Convert to mins
                    if (track.getDuration() > 0) {
                        subTitle += " - " + Utils.convertSecondsToTimeString((int) (track.getDuration() / 1000));
                    }
                    trackSubtitleView.setText(subTitle);

                    // TODO Add action to button

                    song_container.addView(songView);
                }
            }
        }

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}