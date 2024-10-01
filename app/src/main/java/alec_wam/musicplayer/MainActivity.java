package alec_wam.musicplayer;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.navigation.NavigationBarView;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import alec_wam.musicplayer.data.database.AppDatabaseRepository;
import alec_wam.musicplayer.database.MusicDatabase;
import alec_wam.musicplayer.services.MusicPlayerService;
import alec_wam.musicplayer.ui.views.MusicPlayerOverlay;
import alec_wam.musicplayer.ui.views.SmallMusicPlayerControls;
import alec_wam.musicplayer.utils.PlaylistUtils;
import alec_wam.musicplayer.utils.ThemedDrawableUtils;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.media3.session.MediaController;
import androidx.media3.session.SessionToken;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import alec_wam.musicplayer.databinding.ActivityMainBinding;

import static alec_wam.musicplayer.services.MusicPlayerService.BUNDLE_PLAYLIST;

public class MainActivity extends AppCompatActivity {

    public static final Logger LOGGER = Logger.getLogger("MainActivity");

    private static final int REQUEST_CODE_PERMISSIONS = 1001;
    public static final int REQUEST_CODE_PICK_IMAGE = 1;

    private ActivityMainBinding binding;
    public static NavController navController;
    private SmallMusicPlayerControls smallMusicPlayerControls;
    private ListenableFuture<MediaController> mediaControllerFuture;
    private MediaController mediaController;

    private MusicPlayerOverlay playerView;
    private AppDatabaseRepository appDatabaseRepository;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private int imageSelectPlaylistId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        DynamicColors.applyToActivityIfAvailable(this);
        DynamicColors.applyToActivitiesIfAvailable(this.getApplication());
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar myToolbar = (Toolbar) findViewById(R.id.app_toolbar);
        setSupportActionBar(myToolbar);

        // Offset top padding with systemBars
        ViewCompat.setOnApplyWindowInsetsListener(myToolbar, (v, insets) -> {
            // Get the top inset (status bar height)
            int topInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top;

            // Apply padding to the top of the toolbar
            v.setPadding(v.getPaddingStart(), topInset, v.getPaddingEnd(), v.getPaddingBottom());

            // Return the insets so they can be applied to other views
            return insets;
        });

        final BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_album_list, R.id.navigation_playlists)
                .build();

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_activity_main);
        navController = navHostFragment.getNavController();

        NavigationUI.setupWithNavController(binding.navView, navController);
        navView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() != navView.getSelectedItemId()) {
                    navController.popBackStack(item.getItemId(), true, false);
                    navController.navigate(item.getItemId());
                    return true;
                }
                return false;
            }
        });
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        checkPermissions();

        appDatabaseRepository = new AppDatabaseRepository(getApplication());

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null && data.getData() != null) {
                            Uri selectedImageUri = data.getData();
                            if(this.imageSelectPlaylistId >= 0) {
                                PlaylistUtils.saveImageToInternalStorage(this, appDatabaseRepository, selectedImageUri, this.imageSelectPlaylistId);
                                this.imageSelectPlaylistId = -1;
                            }
                        }
                    }
                }
        );

        playerView = binding.playerView;
        playerView.setVisibility(View.GONE);
        ViewCompat.setOnApplyWindowInsetsListener(playerView, (v, insets) -> {
            // Get the top inset (status bar height)
            int topInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top;
            int bottomInset = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;

            // Apply padding to the top of the toolbar
            v.setPadding(v.getPaddingStart(), topInset, v.getPaddingEnd(), bottomInset);

            // Return the insets so they can be applied to other views
            return insets;
        });

        smallMusicPlayerControls = binding.mediaPlayerSmall;
        smallMusicPlayerControls.setPlayerView(this.playerView);
        smallMusicPlayerControls.setVisibility(View.GONE);
    }

    @Override
    public void onStart(){
        super.onStart();
        setupMediaController();
    }

    public void setupMediaController(){
        SessionToken sessionToken = new SessionToken(this, new ComponentName(this, MusicPlayerService.class));
        mediaControllerFuture = new MediaController.Builder(this, sessionToken).buildAsync();
        mediaControllerFuture.addListener(() -> {
            try {
                mediaController = mediaControllerFuture.get();
                playerView.setMediaController(mediaController);
                smallMusicPlayerControls.setMediaController(mediaController);
                mediaController.addListener(smallMusicPlayerControls);
                mediaController.addListener(playerView);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Clear cache and update icon on theme change
        ThemedDrawableUtils.clearCache();
    }

    @Override
    public void onStop(){
        super.onStop();
        if(mediaController !=null) {
            mediaController.removeListener(smallMusicPlayerControls);
            mediaController.removeListener(playerView);
        }
        MediaController.releaseFuture(mediaControllerFuture);
        this.playerView.cleanUpHandler();
    }

    @Override
    public boolean onSupportNavigateUp() {
        // Handle navigation when the "Up" button is pressed
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_activity_main);
        NavController navController = navHostFragment.getNavController();
        return navController.navigateUp() || super.onSupportNavigateUp();
    }

    private void startMusicPlayerService() {
        Intent serviceIntent = new Intent(this, MusicPlayerService.class);
        ContextCompat.startForegroundService(this, serviceIntent); // Use for foreground services
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{android.Manifest.permission.READ_MEDIA_AUDIO},
                    REQUEST_CODE_PERMISSIONS
            );
        }
        else {
            startMusicPlayerService();
        }
    }

    public void openPlaylistImagePicker(int playlistId){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        this.imageSelectPlaylistId = playlistId;
        imagePickerLauncher.launch(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startMusicPlayerService();
            } else {
                Toast.makeText(this, "Permission denied to read media files", Toast.LENGTH_SHORT).show();
            }
        }
    }

}