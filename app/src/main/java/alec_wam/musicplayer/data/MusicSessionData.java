package alec_wam.musicplayer.data;

import android.util.Log;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import alec_wam.musicplayer.database.MusicDatabase;
import alec_wam.musicplayer.database.MusicFile;
import alec_wam.musicplayer.services.MusicPlayerService;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;

public class MusicSessionData {

    public List<String> songIds;
    public int playingSongIndex = -1;
    public String playingSongId = null;
    public long playingSongProgress = 0;

    private MusicSessionData(List<String> songIds, int playingSongIndex, String playingSongId, long playingSongProgress){
        this.songIds = songIds;
        this.playingSongIndex = playingSongIndex;
        this.playingSongId = playingSongId;
        this.playingSongProgress = playingSongProgress;
    }

    public static MusicSessionData saveFromPlayer(ExoPlayer player) {
        List<String> songIds = new ArrayList<>();
        for(int i = 0; i < player.getMediaItemCount(); i++){
            MediaItem mediaItem = player.getMediaItemAt(i);
            songIds.add(mediaItem.mediaId);
        }
        int playingSongIndex = -1;
        String playingSongId = null;
        long playingSongProgress = 0;
        if(player.getCurrentMediaItem() !=null) {
            playingSongIndex = player.getCurrentMediaItemIndex();
            playingSongId = player.getCurrentMediaItem().mediaId;
            playingSongProgress = player.getCurrentPosition();
        }
        return new MusicSessionData(songIds, playingSongIndex, playingSongId, playingSongProgress);
    }

    public List<MediaItem> getMediaItems(){
        return this.songIds.stream().map((id) -> MusicDatabase.SONGS.get(id)).map(MusicPlayerService::buildMediaItem).toList();
    }

    public String loadToPlayer(ExoPlayer player) {
        List<MediaItem> mediaItems = this.getMediaItems();
        Log.i("MusicSessionData", "Found MediaItems: " + mediaItems.size());
        player.setMediaItems(mediaItems);
        if(playingSongIndex > -1){
            player.seekTo(playingSongIndex, Math.max(playingSongProgress, 0));
        }
        player.prepare();
        return playingSongId;
    }

    public static MusicSessionData loadFromJSON(Gson gson, String json){
        MusicSessionData sessionData = gson.fromJson(json, MusicSessionData.class);
        return sessionData;
    }
}
