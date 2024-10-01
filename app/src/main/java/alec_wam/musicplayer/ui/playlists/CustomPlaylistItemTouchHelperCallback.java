package alec_wam.musicplayer.ui.playlists;

import android.graphics.Canvas;

import java.util.Collections;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.media3.common.util.Log;
import androidx.media3.common.util.UnstableApi;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class CustomPlaylistItemTouchHelperCallback extends ItemTouchHelper.Callback {

    private final PlaylistSongAdapter adaptor;
    private int oldPosition = -1;

    public CustomPlaylistItemTouchHelperCallback(PlaylistSongAdapter adaptor){
        this.adaptor = adaptor;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        // Enable drag movement
        return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder source, @NonNull RecyclerView.ViewHolder target) {
        // Only visually move the item without sending database update
        // Get the positions of the source and target
        int fromPosition = source.getBindingAdapterPosition();
        int toPosition = target.getBindingAdapterPosition();

        adaptor.moveItem(fromPosition, toPosition, true);
        return true;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        // Handle swipe to delete if needed
    }

    @OptIn(markerClass = UnstableApi.class)
    @Override
    public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
        super.onSelectedChanged(viewHolder, actionState);
//        Log.i("CustomDrag", "onSelectionChanged: " + actionState);
        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
            // Item is being dragged; get the old position
            oldPosition = viewHolder.getAbsoluteAdapterPosition(); // Get the position when dragging starts
//            Log.i("CustomDrag", "onSelectionChanged: " + oldPosition);
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    @Override
    public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);
        // The drag operation has finished; now update the actual data based on final position
        int toPosition = viewHolder.getAbsoluteAdapterPosition();
//        Log.i("CustomDragCallback", "Clear View: " + "Old: " + oldPosition + " New: " + toPosition);
        if(oldPosition != -1 && oldPosition != toPosition) {
//            Log.i("CustomDragCallback", "Old: " + oldPosition + " New: " + toPosition);
            //Send database update
            this.adaptor.moveItem(oldPosition, toPosition, false);
            oldPosition = -1;
        }
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        // Customize the drawing of the item during drag if necessary
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

    @Override
    public boolean isLongPressDragEnabled() {
        // Disable long press drag; we will handle drag manually from the handle
        return false;
    }
}
