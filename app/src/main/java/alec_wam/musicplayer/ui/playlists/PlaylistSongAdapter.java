package alec_wam.musicplayer.ui.playlists;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;
import java.util.Collections;

import alec_wam.musicplayer.R;
import alec_wam.musicplayer.data.database.entities.PlaylistSong;
import alec_wam.musicplayer.database.MusicDatabase;
import alec_wam.musicplayer.database.MusicFile;
import alec_wam.musicplayer.utils.ThemedDrawableUtils;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class PlaylistSongAdapter extends RecyclerView.Adapter<PlaylistSongAdapter.ViewHolder> {

    private Context context;
    private List<PlaylistSong> playlistSongs;
    private ItemTouchHelper itemTouchHelper;
    private OnItemMovedListener onItemMovedListener;
    private OnPlaylistSongClickListener onItemClickedListener;

    private int playingPlaylistSongId = -1;

    public interface OnItemMovedListener {
        void onItemMoved(int fromPosition, int toPosition);
    }

    public interface OnPlaylistSongClickListener {
        void onClicked(int playlistSongId);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public View parentView;
        public ImageView songDragHandle;
        public ImageView songAlbumCover;
        public TextView songTitle;
        public TextView songArtist;

        public ViewHolder(View itemView) {
            super(itemView);
            parentView = itemView;
            songDragHandle = itemView.findViewById(R.id.item_song_drag_handle);
            songAlbumCover = itemView.findViewById(R.id.item_song_album_image);
            songTitle = itemView.findViewById(R.id.item_song_title_text);
            songArtist = itemView.findViewById(R.id.item_song_subtitle);
        }
    }

    public PlaylistSongAdapter(Context context, List<PlaylistSong> playlistSongs, OnItemMovedListener listener, OnPlaylistSongClickListener onItemClickedListener) {
        this.context = context;
        this.playlistSongs = playlistSongs;
        this.onItemMovedListener = listener;
        this.onItemClickedListener = onItemClickedListener;
    }

    public void setItemTouchHelper(ItemTouchHelper itemTouchHelper) {
        this.itemTouchHelper = itemTouchHelper;
    }

    public void setPlayingPlaylistSongId(int playingPlaylistSongId) {
        this.playingPlaylistSongId = playingPlaylistSongId;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_song, parent, false);
        View songDragHandle = view.findViewById(R.id.item_song_drag_handle);
        View songTrackNumber = view.findViewById(R.id.item_song_track);
        View songAlbumCover = view.findViewById(R.id.item_song_album_image);
        songDragHandle.setVisibility(View.VISIBLE);
        songTrackNumber.setVisibility(View.GONE);
        songAlbumCover.setVisibility(View.VISIBLE);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final PlaylistSong playlistSong = playlistSongs.get(position);

        MusicFile musicFile = MusicDatabase.SONGS.get(playlistSong.songId);
        Uri albumImageUri = null;
        String title = "Error";
        String artist = "Missing Song";
        if(musicFile !=null){
            albumImageUri = musicFile.getAlbumArtUri();
            title = musicFile.getName();
            artist = musicFile.getArtist();
        }

        if(itemTouchHelper !=null) {
            holder.songDragHandle.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    itemTouchHelper.startDrag(holder);
                }
                return false;
            });
        }

        Drawable themed_unknown_album = ThemedDrawableUtils.getThemedIcon(this.context, R.drawable.ic_unkown_album, com.google.android.material.R.attr.colorPrimary, Color.BLACK);
        Glide.with(this.context)
                .load(albumImageUri)  // URI for album art
                .placeholder(themed_unknown_album)  // Optional placeholder
                .error(themed_unknown_album)  // Optional error image
                .into(holder.songAlbumCover);

        holder.songTitle.setText(title);
        holder.songArtist.setText(artist);

        if(this.onItemClickedListener !=null){
            holder.parentView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PlaylistSongAdapter.this.onItemClickedListener.onClicked(playlistSong.id);
                }
            });
        }

        if(playlistSong.id == this.playingPlaylistSongId){
            int themeColor = ThemedDrawableUtils.getThemeColor(context, com.google.android.material.R.attr.colorSecondaryContainer, R.color.colorCustomColor);
            holder.parentView.setBackgroundColor(themeColor);
        }
        else {
            holder.parentView.setBackground(null);
        }
    }

    @Override
    public int getItemCount() {
        return playlistSongs.size();
    }

    public void moveItem(int fromPosition, int toPosition, boolean fake) {
        if(!fake) {
            if (onItemMovedListener != null) {
                onItemMovedListener.onItemMoved(fromPosition, toPosition);
            }
        }
        else {
            Collections.swap(playlistSongs, fromPosition, toPosition);
            notifyItemMoved(fromPosition, toPosition);
        }
    }
}

