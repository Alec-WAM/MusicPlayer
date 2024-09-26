package alec_wam.musicplayer.services;

import android.content.Context;

import com.google.common.collect.ImmutableList;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.session.CommandButton;
import androidx.media3.session.DefaultMediaNotificationProvider;
import androidx.media3.session.MediaNotification;
import androidx.media3.session.MediaSession;

@UnstableApi
public class CustomMediaNotificationProvider extends DefaultMediaNotificationProvider {
    public CustomMediaNotificationProvider(Context context) {
        super(context);
    }

    public CustomMediaNotificationProvider(Context context, NotificationIdProvider notificationIdProvider, String channelId, int channelNameResourceId) {
        super(context, notificationIdProvider, channelId, channelNameResourceId);
    }

    @NonNull
    @Override
    protected int[] addNotificationActions(
            MediaSession mediaSession,
            ImmutableList<CommandButton> mediaButtons,
            NotificationCompat.Builder builder,
            MediaNotification.ActionFactory actionFactory) {
        return super.addNotificationActions(
                mediaSession,
                mediaButtons,
                builder,
                actionFactory);
    }
}
