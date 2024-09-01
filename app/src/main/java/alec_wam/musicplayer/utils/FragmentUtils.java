package alec_wam.musicplayer.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import alec_wam.musicplayer.R;
import alec_wam.musicplayer.ui.album.AlbumFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

public class FragmentUtils {

    public static void openAlbumPage(final View view, final String albumId) {
//        AlbumFragment albumTracksFragment = AlbumFragment.newInstance(albumId);
//
//        // Replace the current fragment with the new one
//        FragmentManager fragmentManager = activity.getSupportFragmentManager();
//        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//        fragmentTransaction.replace(R.id.nav_host_fragment_activity_main, albumTracksFragment);
//        fragmentTransaction.setReorderingAllowed(true);
//        fragmentTransaction.commit();
        Bundle args = new Bundle();
        args.putString(AlbumFragment.ARG_ALBUM_ID, albumId);
        NavController navController = Navigation.findNavController(view);
        navController.navigate(R.id.action_navigation_albums_to_navigation_album, args);
    }
}
