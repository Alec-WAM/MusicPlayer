package alec_wam.musicplayer.utils;

import android.os.Bundle;
import android.view.View;

import alec_wam.musicplayer.MainActivity;
import alec_wam.musicplayer.R;
import alec_wam.musicplayer.ui.album.AlbumFragment;
import alec_wam.musicplayer.ui.artist.ArtistFragment;
import alec_wam.musicplayer.ui.playlists.PlaylistFragment;
import androidx.annotation.IdRes;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

public class FragmentUtils {

    public static void openAlbumPage(final View view, final String albumId, final @IdRes int navId) {
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
        MainActivity.navController.navigate(navId, args);
    }

    public static void openArtistPage(final View view, final String artistId, final @IdRes int navId) {
        Bundle args = new Bundle();
        args.putString(ArtistFragment.ARG_ARTIST, artistId);
        MainActivity.navController.navigate(navId, args);
    }

    public static void openPlaylist(final int playlistId, final @IdRes int navId) {
        Bundle args = new Bundle();
        args.putInt(PlaylistFragment.ARG_PLAYLIST, playlistId);
        MainActivity.navController.navigate(navId, args);
    }
}
