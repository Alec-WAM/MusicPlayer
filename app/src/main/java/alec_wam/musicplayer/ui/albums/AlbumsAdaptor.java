package alec_wam.musicplayer.ui.albums;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import alec_wam.musicplayer.database.MusicAlbum;
import alec_wam.musicplayer.R;
import alec_wam.musicplayer.utils.FragmentUtils;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

public class AlbumsAdaptor  extends RecyclerView.Adapter<AlbumsAdaptor.ViewHolder> {

    private Context context;
    private List<MusicAlbum> localDataSet;

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final View parentView;
        private final ImageView albumImage;
        private final TextView albumName;
        private final TextView albumArtist;

        public ViewHolder(View view) {
            super(view);
            parentView = view;
            albumImage = (ImageView) view.findViewById(R.id.album_cover_image);
            albumName = (TextView) view.findViewById(R.id.album_text_album);
            albumArtist = (TextView) view.findViewById(R.id.album_text_artist);
        }

        public ImageView getAlbumImage() {
            return albumImage;
        }

        public TextView getAlbumName() {
            return albumName;
        }

        public TextView getAlbumArtist() {
            return albumArtist;
        }
    }

    /**
     * Initialize the dataset of the Adapter
     *
     * @param dataSet String[] containing the data to populate views to be used
     * by RecyclerView
     */
    public AlbumsAdaptor(Context context, List<MusicAlbum> dataSet) {
        this.context = context;
        localDataSet = dataSet;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.list_item_album, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        MusicAlbum album = localDataSet.get(position);
//        viewHolder.getAlbumImage().setImageBitmap(bitmap);

        Glide.with(viewHolder.getAlbumImage().getContext())
                .load(album.getAlbumArtUri())  // URI for album art
                .placeholder(R.drawable.ic_unkown_album)  // Optional placeholder
                .error(R.drawable.ic_unkown_album)  // Optional error image
                .into(viewHolder.getAlbumImage());

        viewHolder.getAlbumName().setText(album.getName());
        viewHolder.getAlbumArtist().setText(album.getArtist());

        viewHolder.parentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(context instanceof FragmentActivity) {
                    FragmentUtils.openAlbumPage(view, album.getAlbumId());
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
