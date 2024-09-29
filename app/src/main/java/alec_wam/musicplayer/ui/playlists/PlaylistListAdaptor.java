package alec_wam.musicplayer.ui.playlists;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import alec_wam.musicplayer.data.database.entities.Playlist;
import alec_wam.musicplayer.R;
import androidx.recyclerview.widget.RecyclerView;

public class PlaylistListAdaptor extends RecyclerView.Adapter<PlaylistListAdaptor.ViewHolder> {

    private Context context;
    private List<Playlist> localDataSet;
    private OnPlaylistClickListener onPlaylistClickListener;

    public interface OnPlaylistClickListener {
        void onPlaylistCicked(Playlist playlist);
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final View parentView;
        private final ImageView playlistImage;
        private final TextView playlistName;
        private final TextView playlistSubtext;

        public ViewHolder(View view) {
            super(view);
            parentView = view;
            playlistImage = (ImageView) view.findViewById(R.id.item_playlist_album_image);
            playlistName = (TextView) view.findViewById(R.id.item_playlist_title_text);
            playlistSubtext = (TextView) view.findViewById(R.id.item_playlist_subtitle);
        }

        public ImageView getPlaylistImage() {
            return playlistImage;
        }

        public TextView getPlaylistName() {
            return playlistName;
        }

        public TextView getPlaylistSubtext() {
            return playlistSubtext;
        }
    }

    /**
     * Initialize the dataset of the Adapter
     *
     * @param dataSet String[] containing the data to populate views to be used
     * by RecyclerView
     */
    public PlaylistListAdaptor(Context context, List<Playlist> dataSet, OnPlaylistClickListener onPlaylistClickListener) {
        this.context = context;
        localDataSet = dataSet;
        this.onPlaylistClickListener = onPlaylistClickListener;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.list_item_playlist, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        final Playlist playlist = localDataSet.get(position);
//        viewHolder.getAlbumImage().setImageBitmap(bitmap);

        //TODO Add playlist image
        viewHolder.getPlaylistImage().setVisibility(View.GONE);

        viewHolder.getPlaylistName().setText(playlist.name);
        //TODO Add song count
        viewHolder.getPlaylistSubtext().setText("");

        viewHolder.parentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(onPlaylistClickListener !=null){
                    onPlaylistClickListener.onPlaylistCicked(playlist);
                }
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return localDataSet.size();
    }

}
