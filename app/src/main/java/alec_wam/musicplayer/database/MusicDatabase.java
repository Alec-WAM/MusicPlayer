package alec_wam.musicplayer.database;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class MusicDatabase {

    private static final Logger LOGGER = Logger.getLogger("MusicDatabase");

    public static final Map<Long, MusicFile> SONGS = new HashMap<>();
    public static final Map<String, MusicAlbum> ALBUMS = new HashMap<>();
    public static final Map<String, MusicArtist> ARTISTS = new HashMap<>();

    public static void buildAlbumList(Context context) {
        ALBUMS.clear();

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
                MusicFile musicFile = new MusicFile(id, contentUri, name, contentUri, duration, albumId, albumName, artist, track);

                Uri albumArtUri = getAlbumArtUri(albumId);
                musicFile.setAlbumArtUri(albumArtUri);

                MusicAlbum album = getOrCreateAlbum(albumId, albumName, artist);
                if(album.getAlbumArtUri() == null && albumArtUri != null){
                    LOGGER.info(albumArtUri.toString());
                    album.setAlbumArtUri(albumArtUri);
                }
                album.addMusic(musicFile);

                MusicArtist musicArtist = getOrCreateArtist(artist);
                musicArtist.addAlbum(albumId);

                SONGS.put(id, musicFile);
            }
        }
    }

    public static MusicAlbum getOrCreateAlbum(final String albumId, final String albumName, final String albumArtist) {
        MusicAlbum album = ALBUMS.get(albumId);
        if(album != null){
            return album;
        }
        LOGGER.info("Creating album: " + albumName + " by " + albumArtist);
        album = new MusicAlbum(albumId, albumName, albumArtist);
        ALBUMS.put(albumId, album);
        return album;
    }

    public static MusicAlbum getAlbumById(final String albumId){
        return ALBUMS.get(albumId);
    }

    public static Uri getAlbumArtUri(String albumId) {
        if(albumId == null){
            return null;
        }
        Uri albumArtUri = ContentUris.withAppendedId(
                Uri.parse("content://media/external/audio/albumart"), Long.parseLong(albumId));
        return albumArtUri;
    }

    public static MusicArtist getOrCreateArtist(final String artistName) {
        MusicArtist artist = ARTISTS.get(artistName);
        if(artist != null){
            return artist;
        }
        LOGGER.info("Creating artist: " + artistName);
        artist = new MusicArtist(artistName);
        ARTISTS.put(artistName, artist);
        return artist;
    }

}
