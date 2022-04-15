package edu.temple.simpletunes;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;

public class MusicTrack {
    private final DocumentFile df;
    private final String title;
    private final String track;
    private final String artist;
    private final String album;

    public MusicTrack(Context ctx, @NonNull DocumentFile df){
        this.df = df;
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(ctx, df.getUri());
        title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        track = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER);
        artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        album = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
        mmr.close();
    }
    public String getTitle(){
        return title;
    }
    public String getTrack(){
        return track;
    }
    public String getArtist(){
        return artist;
    }
    public String getAlbum(){
        return album;
    }
    public Uri getUri(){
        return df.getUri();
    }
    public String getName(){
        return df.getName();
    }
}
