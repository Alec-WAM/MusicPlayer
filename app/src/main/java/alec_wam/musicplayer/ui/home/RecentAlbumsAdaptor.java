package alec_wam.musicplayer.ui.home;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import alec_wam.musicplayer.database.MusicAlbum;
import alec_wam.musicplayer.R;
import alec_wam.musicplayer.database.MusicDatabase;
import alec_wam.musicplayer.utils.ThemedDrawableUtils;
import androidx.recyclerview.widget.RecyclerView;

public class RecentAlbumsAdaptor extends RecyclerView.Adapter<RecentAlbumsAdaptor.ViewHolder> {

    private Context context;
    private List<String> recentAlbums;
    private OnAlbumClickListener onAlbumClickListener;

    public interface OnAlbumClickListener {
        void onAlbumClick(MusicAlbum musicAlbum);
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final View parentView;
        private final ImageView itemImage;
        private final TextView itemMainText;
        private final TextView itemSubText;

        public ViewHolder(View view) {
            super(view);
            parentView = view;
            itemImage = (ImageView) view.findViewById(R.id.list_item_recent_image);
            itemMainText = (TextView) view.findViewById(R.id.list_item_recent_main_text);
            itemSubText = (TextView) view.findViewById(R.id.list_item_recent_sub_text);
        }

        public ImageView getItemImage() {
            return itemImage;
        }

        public TextView getItemMainText() {
            return itemMainText;
        }

        public TextView getItemSubText() {
            return itemSubText;
        }
    }

    /**
     * Initialize the dataset of the Adapter
     *
     * @param recentAlbums List<String> containing the data to populate views to be used
     * by RecyclerView
     */
    public RecentAlbumsAdaptor(Context context, List<String> recentAlbums, OnAlbumClickListener onAlbumClickListener) {
        this.context = context;
        this.recentAlbums = recentAlbums;
        this.onAlbumClickListener = onAlbumClickListener;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.list_item_recent, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        String albumId = recentAlbums.get(position);
        final MusicAlbum album = MusicDatabase.getAlbumById(albumId);
        if(album == null){
            return;
        }

        Drawable themed_unknown_album = ThemedDrawableUtils.getThemedIcon(this.context, R.drawable.ic_unkown_album, com.google.android.material.R.attr.colorPrimary, Color.BLACK);
        Glide.with(viewHolder.getItemImage().getContext())
                .load(album.getAlbumArtUri())  // URI for album art
                .placeholder(themed_unknown_album)  // Optional placeholder
                .error(themed_unknown_album)  // Optional error image
                .into(viewHolder.getItemImage());

        viewHolder.getItemMainText().setText(album.getName());
        viewHolder.getItemSubText().setText(album.getArtist());

        viewHolder.parentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(onAlbumClickListener !=null){
                    onAlbumClickListener.onAlbumClick(album);
                }
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return recentAlbums.size();
    }

}
