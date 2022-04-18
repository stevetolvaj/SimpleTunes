package edu.temple.simpletunes;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;

public class MusicTrack {
    private final DocumentFile df;
    private final String title;
    private final String track;
    private final String artist;
    private final String album;
    private final String TAG = "MusicTrack";
    public MusicTrack(Context ctx, @NonNull DocumentFile df){
        Log.d("MusicTrack", "MusicTrack: got DocumentFile " + df.getName() + " of type " + df.getType());
        String type = df.getType();
        if(type != null){
        type = type.substring(0, type.lastIndexOf('/'));
            if(type.equals("audio")){
                this.df = df;
                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                mmr.setDataSource(ctx, df.getUri());
                title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                String s = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER);
                if(s.lastIndexOf('/') == -1){
                    track = s;
                }else{
                    track = s.substring(0, s.lastIndexOf('/'));
                }
                artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                album = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
                mmr.close();
            }else{
                this.df = df;
                this.title = null;
                this.track = null;
                this.artist = null;
                this.album = null;
            }
        }else{
            this.df = df;
            this.title = null;
            this.track = null;
            this.artist = null;
            this.album = null;
        }

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
