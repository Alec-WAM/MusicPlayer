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

import alec_wam.musicplayer.utils.Utils;

public class MusicDatabase {

    private static final Logger LOGGER = Logger.getLogger("MusicDatabase");

    public static final Map<String, MusicFile> SONGS = new HashMap<>();
    public static final Map<String, MusicAlbum> ALBUMS = new HashMap<>();
    public static final Map<String, MusicArtist> ARTISTS = new HashMap<>();

    //FIXME On initial load of song database the fragments do not get the update of the library

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
                MediaStore.Audio.Media.ALBUM_ARTIST,
//                MediaStore.Audio.Media.ARTIST_ID,
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
//            int artistIdColumn =
//                    cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID);
            int artistColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
            int albumArtistColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ARTIST);
            int trackColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK);

            while (cursor.moveToNext()) {
                // Get values of columns for a given video.
                long id = cursor.getLong(idColumn);
                String name = cursor.getString(nameColumn);
                int duration = cursor.getInt(durationColumn);
//                String artistId = cursor.getString(artistIdColumn);
                String artist = cursor.getString(artistColumn);
                String mediaAlbumId = cursor.getString(albumIdColumn);
                String albumName = cursor.getString(albumColumn).trim();
                String albumArtist = cursor.getString(albumArtistColumn);
                int track = cursor.getInt(trackColumn);

                String[] uniqueIdItems = {name, artist, albumName, ""+track};
                String uniqueSongId = buildUniqueId(uniqueIdItems);

                String artistToUse = albumArtist !=null ? albumArtist : artist;
                String[] uniqueAlbumIdItems = {albumName, artistToUse};
                String uniqueAlbumId = buildUniqueId(uniqueAlbumIdItems);

                String[] artistIdItems = {artistToUse};
                String uniqueArtistId = buildUniqueId(artistIdItems);

                Uri contentUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);

//                final String fixedArtistId = artistId.replace(" ", "_");
                // Stores column values and the contentUri in a local object
                // that represents the media file.
                MusicFile musicFile = new MusicFile(uniqueSongId, id, contentUri, name, contentUri, duration, uniqueAlbumId, albumName, uniqueArtistId, artistToUse, track);

                Uri albumArtUri = getAlbumArtUri(mediaAlbumId);
                musicFile.setAlbumArtUri(albumArtUri);

                MusicAlbum album = getOrCreateAlbum(uniqueAlbumId, mediaAlbumId, albumName, artistToUse);
                if(album.getAlbumArtUri() == null && albumArtUri != null){
                    album.setAlbumArtUri(albumArtUri);
                }
                album.addMusic(musicFile);

                MusicArtist musicArtist = getOrCreateArtist(uniqueArtistId, artistToUse);
                musicArtist.addAlbum(uniqueAlbumId);

                SONGS.put(uniqueSongId, musicFile);
            }
        }
    }

    public static String buildUniqueId(String[] items){
        String finalString = "";
        // Build the string with underscores
        for (int i = 0; i < items.length; i++) {
            String item = items[i];
            if(item == null || item.length() == 0){
                continue;
            }
            finalString += item;
            // Add an underscore only if it's not the last item
            if (i < items.length - 1) {
                finalString += "_";
            }
        }
        finalString = finalString.trim();
        if(finalString.length() == 0){
            return null;
        }
        String hashedFinalString = Utils.hashString(finalString);
        return hashedFinalString;
    }

    public static MusicAlbum getOrCreateAlbum(final String albumId, final String mediaAlbumId, final String albumName, final String albumArtist) {
        MusicAlbum album = ALBUMS.get(albumId);
        if(album != null){
            return album;
        }
//        LOGGER.info("Creating album: " + albumName + " by " + albumArtist);
        album = new MusicAlbum(albumId, mediaAlbumId, albumName, albumArtist);
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

    public static MusicArtist getOrCreateArtist(final String artistId, final String artistName) {
        MusicArtist artist = ARTISTS.get(artistId);
        if(artist != null){
            return artist;
        }
//        LOGGER.info("Creating artist: " + artistId + " " + artistName);
        artist = new MusicArtist(artistId, artistName);
        ARTISTS.put(artistId, artist);
        return artist;
    }

}
