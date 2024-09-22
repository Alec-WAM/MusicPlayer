package alec_wam.musicplayer.services;

import android.media.browse.MediaBrowser;
import android.os.Bundle;
import android.service.media.MediaBrowserService;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class MusicPlayerBrowserService extends MediaBrowserService {

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String s, int i, @Nullable Bundle bundle) {
        return new BrowserRoot("root_library", null);
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowser.MediaItem>> result) {

    }
}
