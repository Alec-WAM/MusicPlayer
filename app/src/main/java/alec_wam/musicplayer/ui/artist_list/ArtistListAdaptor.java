package alec_wam.musicplayer.ui.artist_list;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import alec_wam.musicplayer.R;
import alec_wam.musicplayer.database.MusicAlbum;
import alec_wam.musicplayer.database.MusicArtist;
import androidx.recyclerview.widget.RecyclerView;

public class ArtistListAdaptor extends RecyclerView.Adapter<ArtistListAdaptor.ViewHolder> {

    private Context context;
    private List<MusicArtist> localDataSet;
    private OnAristClickListener onAristClickListener;

    public interface OnAristClickListener {
        void onArtistClick(MusicArtist musicArtist);
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final View parentView;
        private final ImageView artistImage;
        private final TextView artistName;
        private final TextView artistSubTitle;

        public ViewHolder(View view) {
            super(view);
            parentView = view;
            artistImage = (ImageView) view.findViewById(R.id.artist_image);
            artistName = (TextView) view.findViewById(R.id.artist_text_name);
            artistSubTitle = (TextView) view.findViewById(R.id.artist_text_sub_title);
        }

        public ImageView getArtistImage() {
            return artistImage;
        }

        public TextView getArtistName() {
            return artistName;
        }

        public TextView getArtistSubTitle() {
            return artistSubTitle;
        }
    }

    /**
     * Initialize the dataset of the Adapter
     *
     * @param dataSet String[] containing the data to populate views to be used
     * by RecyclerView
     */
    public ArtistListAdaptor(Context context, List<MusicArtist> dataSet, OnAristClickListener onAristClickListener) {
        this.context = context;
        localDataSet = dataSet;
        this.onAristClickListener = onAristClickListener;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.list_item_artist, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        MusicArtist artist = localDataSet.get(position);
//        viewHolder.getAlbumImage().setImageBitmap(bitmap);

//        Glide.with(viewHolder.getArtistImage().getContext())
//                .load(album.getAlbumArtUri())  // URI for album art
//                .placeholder(R.drawable.ic_unkown_album)  // Optional placeholder
//                .error(R.drawable.ic_unkown_album)  // Optional error image
//                .into(viewHolder.getArtistImage());

        viewHolder.getArtistName().setText(artist.getName());

        viewHolder.parentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(onAristClickListener !=null){
                    onAristClickListener.onArtistClick(artist);
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
