package alec_wam.musicplayer.ui.home;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import alec_wam.musicplayer.MainActivity;
import alec_wam.musicplayer.database.MusicAlbum;
import alec_wam.musicplayer.R;
import alec_wam.musicplayer.database.MusicDatabase;
import alec_wam.musicplayer.utils.ThemedDrawableUtils;
import androidx.annotation.IdRes;
import androidx.annotation.NavigationRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class SpecialItemAdaptor extends RecyclerView.Adapter<SpecialItemAdaptor.ViewHolder> {

    private Context context;
    private List<SpecialHomeItem> specialHomeItems;

    public static class SpecialHomeItem {
        public Drawable drawable;
        public String name;
        public String subText;
        public @IdRes int navId;
        public Bundle navBundle;

        public SpecialHomeItem(Drawable drawable, @NonNull String name, @Nullable String subText, @IdRes int navId, Bundle navBundle) {
            this.drawable = drawable;
            this.name = name;
            this.subText = subText;
            this.navId = navId;
            this.navBundle = navBundle;
        }
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
    public SpecialItemAdaptor(Context context, List<SpecialHomeItem> specialHomeItems) {
        this.context = context;
        this.specialHomeItems = specialHomeItems;
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
        final SpecialHomeItem specialHomeItem = specialHomeItems.get(position);
        viewHolder.getItemImage().setImageDrawable(specialHomeItem.drawable);
        viewHolder.getItemMainText().setText(specialHomeItem.name);
        viewHolder.getItemSubText().setText(specialHomeItem.subText);

        viewHolder.parentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(specialHomeItem.navId > 0){
                    MainActivity.navController.navigate(specialHomeItem.navId, specialHomeItem.navBundle);
                }
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return specialHomeItems.size();
    }

}
