package edu.temple.simpletunes;

import android.app.Service;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;

public class MediaPlayerService extends Service {

    private final ControlsBinder controlsBinder = new ControlsBinder();
    private MediaPlayer mMediaPlayer = null;
    private final static String TAG = "MEDIAPLAYERSERVICE";

    public MediaPlayerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind: MediaPlayerService");
        return controlsBinder;
    }

    private void play(Uri uri) {
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
        } else {
            mMediaPlayer.reset();   // Reset to change data source.
        }

        mMediaPlayer.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
        );
        try {
            mMediaPlayer.setDataSource(getApplicationContext(), uri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mMediaPlayer.prepareAsync();
        mMediaPlayer.setOnPreparedListener(MediaPlayer::start);
    }

    private void pause() {
        mMediaPlayer.pause();
    }

    public boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }

    public void stop() {
        mMediaPlayer.stop();
    }

    public class ControlsBinder extends Binder {
        public void play(Uri uri) {
            MediaPlayerService.this.play(uri);
        }

        public boolean isPlaying() {
            return MediaPlayerService.this.isPlaying();
        }

        public void stop() {
            MediaPlayerService.this.stop();
        }

        public void pause() {
            MediaPlayerService.this.pause();
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
        }
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }

    }
}