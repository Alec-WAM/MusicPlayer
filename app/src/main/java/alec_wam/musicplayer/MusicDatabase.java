package alec_wam.musicplayer;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

class MusicFile {
    private final Uri uri;
    private final String name;
    private final int duration;
    private final String albumId;
    private final String album;
    private final String artist;
    private final int track;
    private Uri albumArtUri;

    public MusicFile(Uri uri, String name, int duration, String albumId, String album, String artist, int track){
        this.uri = uri;
        this.name = name;
        this.duration = duration;
        this.albumId = albumId;
        this.album = album;
        this.artist = artist;
        this.track = track;
    }

    public void setAlbumArtUri(Uri albumArtUri) {
        this.albumArtUri = albumArtUri;
    }

    public Uri getAlbumArtUri() {
        return this.albumArtUri;
    }
}

public class MusicDatabase {

    private static final Logger LOGGER = Logger.getLogger("MusicDatabase");

    public static final List<MusicFile> MUSIC_LIST = new ArrayList<>();
    public static final List<MusicAlbum> ALBUM_LIST = new ArrayList<>();

    public static void buildAlbumList(Context context) {
        ALBUM_LIST.clear();

        Uri collection;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            collection = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        } else {
            collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        }

        //https://developer.android.com/training/data-storage/shared/media
        String[] projection = new String[] {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.TRACK,
                MediaStore.Audio.Media.IS_MUSIC
        };
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        try (Cursor cursor = context.getContentResolver().query(
                collection,
                projection,
                selection,
                null,
                sortOrder
        )) {
            // Cache column indices.
            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
            int nameColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
            int durationColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
            int albumIdColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);
            int albumColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM);
            int artistColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
            int trackColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK);

            while (cursor.moveToNext()) {
                // Get values of columns for a given video.
                long id = cursor.getLong(idColumn);
                String name = cursor.getString(nameColumn);
                int duration = cursor.getInt(durationColumn);
                String artist = cursor.getString(artistColumn);
                String albumId = cursor.getString(albumIdColumn);
                String albumName = cursor.getString(albumColumn);
                int track = cursor.getInt(trackColumn);

                Uri contentUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);

//                Bitmap thumbnail = null;
//                try {
//                    thumbnail = context.getContentResolver().loadThumbnail(contentUri, new Size(200, 200), null);
//                }
//                catch (IOException e) {
//                    e.printStackTrace();
//                }
                // Stores column values and the contentUri in a local object
                // that represents the media file.
                MusicFile musicFile = new MusicFile(contentUri, name, duration, albumId, albumName, artist, track);

                Uri albumArtUri = getAlbumArtUri(albumId);
                musicFile.setAlbumArtUri(albumArtUri);

                MusicAlbum album = getOrCreateAlbum(albumName, artist);
                if(album.getAlbumArtUri() == null && albumArtUri != null){
                    LOGGER.info(albumArtUri.toString());
                    album.setAlbumArtUri(albumArtUri);
                }

                MUSIC_LIST.add(musicFile);
            }
        }
    }

    public static MusicAlbum getOrCreateAlbum(final String albumName, final String artist) {
        Optional<MusicAlbum> album = ALBUM_LIST.stream().filter((listAlbum) -> listAlbum.getName().equals(albumName)).findFirst();
        if(album.isPresent()){
            return album.get();
        }
        LOGGER.info("Creating album: " + albumName + " by " + artist);
        MusicAlbum album1 = new MusicAlbum(albumName, artist);
        ALBUM_LIST.add(album1);
        return album1;
    }

    private static String getAlbumArtPath(Context context, String albumId) {
        String[] projection = { MediaStore.Audio.Albums.ALBUM_ART };
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                projection,
                MediaStore.Audio.Albums._ID + "=?",
                new String[]{albumId},
                null
        );

        String albumArtPath = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                albumArtPath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ART));
            }
            cursor.close();
        }

        return albumArtPath;
    }

    public static Uri getAlbumArtUri(String albumId) {
        if(albumId == null){
            return null;
        }
        Uri albumArtUri = ContentUris.withAppendedId(
                Uri.parse("content://media/external/audio/albumart"), Long.parseLong(albumId));
        return albumArtUri;
    }

}
