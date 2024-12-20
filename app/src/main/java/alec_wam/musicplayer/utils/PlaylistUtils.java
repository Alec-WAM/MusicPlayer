package alec_wam.musicplayer.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import alec_wam.musicplayer.R;
import alec_wam.musicplayer.data.database.AppDatabaseRepository;
import alec_wam.musicplayer.data.database.AppDatabaseViewModel;
import alec_wam.musicplayer.data.database.entities.Playlist;
import alec_wam.musicplayer.ui.playlists.PlaylistFragment;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

public class PlaylistUtils {

    public static final String PLAYLIST_COVER_FOLDER = "playlist_covers";

    private static Executor ADD_TO_PLAYLIST_EXECUTOR;

    public static void showAddToPlaylistDialog(Context context, final String songId, final AppDatabaseViewModel databaseViewModel){
        if(ADD_TO_PLAYLIST_EXECUTOR == null){
            ADD_TO_PLAYLIST_EXECUTOR = Executors.newSingleThreadExecutor();
        }
        ADD_TO_PLAYLIST_EXECUTOR.execute(() -> {
            // Get the list of playlists from the database
            final List<Playlist> playlists = databaseViewModel.getAllPlaylistsSync();

            ContextCompat.getMainExecutor(context).execute(new Runnable() {
                @Override
                public void run() {
                    PlaylistUtils.showAddPlaylistDialogOnUIThread(context, playlists, songId, databaseViewModel);
                }
            });
        });
    }

    private static void showAddPlaylistDialogOnUIThread(Context context, List<Playlist> playlists, final String songId, final AppDatabaseViewModel databaseViewModel){

        // Inflate the dialog layout
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.layout_dialog_add_to_playlist, null);

        // Create the dialog builder
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setView(dialogView);

        // Set up the RecyclerView (assuming you have an adapter)
        final List<Integer> selectedPlaylists = new ArrayList<>();
        final LinearLayout scrollContainer = dialogView.findViewById(R.id.dialog_add_to_playlist_scroll_container);
        for(final Playlist playlist : playlists){
            View playlistSelectionView = inflater.inflate(R.layout.list_item_playlist_selection, null);
            ImageView playlistSelectionImageView = playlistSelectionView.findViewById(R.id.item_playlist_selection_image);
            TextView playlistTextView = playlistSelectionView.findViewById(R.id.item_playlist_selection_text);
            playlistTextView.setText(playlist.name);

            Drawable themed_default_playlist = ThemedDrawableUtils.getThemedIcon(context, R.drawable.ic_playlist_default, com.google.android.material.R.attr.colorPrimary, Color.BLACK);
            Glide.with(context)
                    .load(playlist.coverImagePath !=null ? new File(playlist.coverImagePath) : null) // Load the image from the stored file path
                    .placeholder(themed_default_playlist) // Default image if none exists
                    .into(playlistSelectionImageView);

            CheckBox playlistCheckbox = playlistSelectionView.findViewById(R.id.item_playlist_selection_checkbox);
            playlistCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                    if(checked){
                        selectedPlaylists.add(playlist.id);
                    }
                    else {
                        selectedPlaylists.remove(playlist.id);
                    }
                }
            });
            scrollContainer.addView(playlistSelectionView);
        }

        // Handle clicking the "Create New Playlist" button
        //        createPlaylistButton.setOnClickListener(v -> {
        //            // Show a dialog to create a new playlist
        //            showCreatePlaylistDialog();
        //            dialog.dismiss();
        //        });

        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                for(int playlistId : selectedPlaylists){
                    databaseViewModel.insertPlaylistSong(playlistId, songId);
                }
            }
        });

        // Set negative button to cancel
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        // Create and show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public static void saveImageToInternalStorage(Context context, AppDatabaseRepository appDatabaseRepository, Uri selectedImageUri, int playlistId) {
        try {
            // Open an InputStream from the selected image URI
            InputStream inputStream = context.getContentResolver().openInputStream(selectedImageUri);

            // Create a directory for the playlist covers inside internal storage
            File directory = new File(context.getFilesDir(), PLAYLIST_COVER_FOLDER);
            if (!directory.exists()) {
                directory.mkdir(); // Create the directory if it doesn't exist
            }

            // Create a file for the image using the playlistId as part of the filename
            File imageFile = new File(directory, "playlist_cover_" + playlistId + ".jpg");

            // Write the image to the file
            FileOutputStream outputStream = new FileOutputStream(imageFile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.close();
            inputStream.close();

            // Update the database with the file path
            appDatabaseRepository.updatePlaylistCoverImage(playlistId, imageFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
