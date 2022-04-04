package edu.temple.simpletunes;

import android.app.Service;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.documentfile.provider.DocumentFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * MediaPlayerService is a service created to run the MediaPlayer instance in the background
 * and bind to the controls of the media player.
 */

public class MediaPlayerService extends Service {

    private final ControlsBinder mControlsBinder = new ControlsBinder();
    private final MediaPlayer mMediaPlayer = new MediaPlayer();
    private final static String TAG = "MEDIAPLAYERSERVICE";
    private int mCurrentPosition;   // The current position of the paused track
    private boolean mIsPlayingFolder = false;   // Shows if folder is playing continuously
    private DocumentFile[] mFolder; // The folder that should be played
    private int mNextFolderIndex = 0;   // The index of the next song to be played in folder

    public MediaPlayerService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // OnCompletionListener used to play next track in order if mIsPlayingFolder set to true
        // Plays until last file is completed then resets variables.
        mMediaPlayer.setOnCompletionListener(mp -> {
            if(mIsPlayingFolder && mNextFolderIndex < mFolder.length) {
                play(mFolder[mNextFolderIndex].getUri());
                Log.d(TAG, "onCompleteListener: Playing track at index " + mNextFolderIndex + " of folder");
                mNextFolderIndex++;
            } else {
                // Set is playing folder back to false after last file is played.
                Log.d(TAG, "onCompleteListener: Reached end of tracks in folder");
                mIsPlayingFolder = false;
                mNextFolderIndex = 0;
            }
        });
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind: MediaPlayerService");
        return mControlsBinder;
    }

    /**
     * The play method will play the Uri passed in if it contains music content. It also
     * initialized the MediaPlayer instance, prepares to run asynchronously, and waits until it
     * is prepared to play the Uri.
     *
     * @param uri The Uri of the audio file.
     */
    private void play(Uri uri) {
        mMediaPlayer.reset();   // Reset to change data source.

        mMediaPlayer.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
        );
        try {
            mMediaPlayer.setDataSource(getApplicationContext(), uri);
        } catch (IOException e) {
            Log.d(TAG, "play: Could not play with current data source");
            e.printStackTrace();
        }
        mMediaPlayer.prepareAsync();
        mMediaPlayer.setOnPreparedListener(MediaPlayer::start);
    }

    /**
     * The play method will play the Uri passed in if it contains music content. It also
     * initialized the MediaPlayer instance, prepares to run asynchronously, and waits until it
     * is prepared to play the Uri.
     *
     * @param file The Uri of the audio file.
     */
    private void play(File file) {
        play(Uri.fromFile(file));
    }

    /**
     * The playFolder method will play the DocumentFile array one by one when each track is
     * completed using onCompletionListener in onCreate().
     *
     * @param folder The Uri of the audio file.
     */
    private void playFolder(DocumentFile[] folder) {
        mIsPlayingFolder = true;
        mNextFolderIndex = 1;
        mFolder = folder;
        play(folder[0].getUri());

    }

    /**
     * The pause method pauses the currently playing audio file and saves the current position
     * to resume the current audio file.
     */
    private void pause() {
        mMediaPlayer.pause();
        mCurrentPosition = mMediaPlayer.getCurrentPosition();
    }

    /**
     * The resume method seeks to the current position of audio file and starts playing.
     */
    private void resume() {
        mMediaPlayer.seekTo(mCurrentPosition);
        mMediaPlayer.start();
    }

    /**
     * The isPlaying method checks if the current audio file is playing.
     * @return True if playing or false if not.
     */
    public boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }

    /**
     * The stop method will stop the currently playing audio file.
     */
    public void stop() {
        mMediaPlayer.stop();
    }

    /**
     * Class to control media player instance.
     */
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
        
        public void resume() { MediaPlayerService.this.resume();}

        public void playFolder (DocumentFile[] folder){ MediaPlayerService.this.playFolder(folder);}
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

            if(mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }
            mMediaPlayer.release();

        Log.d(TAG, "onDestroy: MediaPlayerService");
    }
}