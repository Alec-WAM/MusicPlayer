package alec_wam.musicplayer.ui.album;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.Image;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import alec_wam.musicplayer.database.MusicAlbum;
import alec_wam.musicplayer.database.MusicDatabase;
import alec_wam.musicplayer.database.MusicFile;
import alec_wam.musicplayer.R;
import alec_wam.musicplayer.databinding.FragmentAlbumBinding;
import alec_wam.musicplayer.services.MusicPlayerService;
import alec_wam.musicplayer.utils.MusicPlayerUtils;
import alec_wam.musicplayer.utils.ThemedDrawableUtils;
import alec_wam.musicplayer.utils.Utils;
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DividerItemDecoration;

import static alec_wam.musicplayer.utils.MusicPlayerUtils.BUNDLE_SONG_CHANGE_ALBUM;
import static alec_wam.musicplayer.utils.MusicPlayerUtils.BUNDLE_SONG_CHANGE_SONG;
import static alec_wam.musicplayer.utils.MusicPlayerUtils.INTENT_SONG_CHANGE;

public class AlbumFragment extends Fragment {

    public static final String ARG_ALBUM_ID = "album_id";
    private FragmentAlbumBinding binding;
    private String albumId;
    private Map<Long, View> songViews;
    private long playingSongId = -1L;

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
        IntentFilter filter = new IntentFilter();
        filter.addAction(INTENT_SONG_CHANGE);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(
                this.broadcastReceiver, filter
        );

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

        this.songViews = new HashMap<>();
        if(disks !=null && disks.keySet().size() > 0){
            boolean showDisks = disks.keySet().size() > 1;
            List<Integer> diskKeys = new ArrayList<>(disks.keySet());
            diskKeys.sort((a, b) -> {
                return Integer.compare(a, b);
            });
            Drawable themed_disc = ThemedDrawableUtils.getThemedIcon(getContext(), R.drawable.ic_disk, com.google.android.material.R.attr.colorSecondary, Color.BLACK);
            for(int diskNum : diskKeys) {
                if(showDisks){
                    LinearLayout diskHeader = (LinearLayout) inflater.inflate(R.layout.list_item_disk_header, song_container, false);
                    ImageView diskImage = (ImageView) diskHeader.findViewById(R.id.list_item_disk_icon);
                    TextView diskNumberText = (TextView) diskHeader.findViewById(R.id.list_item_disk_number);
                    diskImage.setImageDrawable(themed_disc);
                    diskNumberText.setText("Disk " + diskNum);
                    song_container.addView(diskHeader);
                }
                List<MusicFile> tracks = new ArrayList<>(disks.get(diskNum));
                tracks.sort(Comparator.comparingInt(MusicFile::getTrack));

                for (int i = 0; i < tracks.size(); i++) {
                    final MusicFile track = tracks.get(i);
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

                    songView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            AlbumFragment.this.clickSong(track);
                        }
                    });

                    song_container.addView(songView);
                    this.songViews.put(track.getId(), songView);
                }
            }
        }

        return root;
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                if(INTENT_SONG_CHANGE.equalsIgnoreCase(intent.getAction())){
                    final long lastPlayingSongId = playingSongId;
                    long songId = intent.getLongExtra(BUNDLE_SONG_CHANGE_SONG, -1);
                    if(songId > -1L) {
                        MusicFile musicFile = MusicDatabase.SONGS.get(songId);
                        if(musicFile !=null){
                            if(musicFile.getAlbumId().equals(albumId)){
                                //TODO Update current playing song
                                final View oldSongView = AlbumFragment.this.songViews.get(lastPlayingSongId);

                                AlbumFragment.this.playingSongId = songId;
                                View newSongView = AlbumFragment.this.songViews.get(songId);
                            }
                        }
                    }
                }
            }
        }
    };

    @Override
    public void onDestroyView() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(broadcastReceiver);
        super.onDestroyView();
        binding = null;
    }

    public void clickSong(MusicFile musicFile){
        MusicPlayerUtils.playSong(this.getContext(), musicFile.getId(), Optional.of(albumId));
    }
}