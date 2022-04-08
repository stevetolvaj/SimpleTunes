package edu.temple.simpletunes;

import android.app.Service;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.documentfile.provider.DocumentFile;

import java.io.IOException;

/**
 * MediaPlayerService is a service created to run the MediaPlayer instance in the background
 * and bind to the controls of the media player.
 */

public class MediaPlayerService extends Service {

    private final ControlsBinder mControlsBinder = new ControlsBinder();
    private final MediaPlayer mMediaPlayer = new MediaPlayer();
    private final static String TAG = "MEDIAPLAYERSERVICE";
    private boolean mIsPlayingFolder = false;   // Shows if folder is playing continuously
    private DocumentFile[] mFolder; // The folder that should be played
    private int mCurrentFolderIndex = 0;   // The index of the next song to be played in folder
    private int repeatStatus = 0; //0 = no repeat, 1 = folder repeat, 2 = file repeat
    public MediaPlayerService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // OnCompletionListener used to play next track in order if mIsPlayingFolder set to true
        // Plays until last file is completed then resets variables.
        mMediaPlayer.setOnCompletionListener(mp -> {

            if(mIsPlayingFolder && mCurrentFolderIndex < mFolder.length - 1) {
                mCurrentFolderIndex++;
                Log.d(TAG, "onCompleteListener: Playing track at index " + mCurrentFolderIndex + " of folder");
                playSingleTrack(mFolder[mCurrentFolderIndex].getUri());
            } else {
                mCurrentFolderIndex = 0;
                // Set is playing folder back to false after last file is played.
                if (mIsPlayingFolder) {
                    Log.d(TAG, "onCompleteListener: Reached end of tracks in folder");
                    if(repeatStatus == 1){
                        Log.d(TAG, "onCompleteListener: restarting from beginning of folder ");
                        playSingleTrack(mFolder[mCurrentFolderIndex].getUri());
                    }else{
                        mIsPlayingFolder = false;
                    }
                }
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
    private void playSingleTrack(Uri uri) {
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
     * The play method will play the Uri passed in if it contains music content. It also resets
     * the variables for playing a folder to prevent it.
     *
     * @param uri The Uri of the audio file.
     */
    private void play(Uri uri) {
        mIsPlayingFolder = false;
        mCurrentFolderIndex = 0;
        playSingleTrack(uri);
    }

    /**
     * The playFolder method will play the DocumentFile array one by one when each track is
     * completed using onCompletionListener in onCreate().
     *
     * @param folder The Uri of the audio file.
     */
    private void playFolder(DocumentFile[] folder) {
        mIsPlayingFolder = true;
        mFolder = folder;
        playSingleTrack(folder[0].getUri());

    }

    /**
     * The pause method pauses the currently playing audio file and saves the current position
     * to resume the current audio file.
     */
    private void pause() {
        mMediaPlayer.pause();
    }

    /**
     * The resume method seeks to the current position of audio file and starts playing
     * after pause is called.
     */
    private void resume() {
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
     * The playNext method checks if a track is being played from a folder. It then plays the
     * next song if any other are found in the folder.
     */
    public void playNext() {
        if(mIsPlayingFolder) {
            if(mCurrentFolderIndex < mFolder.length - 1) {
                mCurrentFolderIndex++;
                Log.d(TAG, "playNext: Next track playing at index " + mCurrentFolderIndex);
                playSingleTrack(mFolder[mCurrentFolderIndex].getUri());
            } else {
                Toast.makeText(getApplicationContext(), "End of folder reached", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Not playing a folder", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * The playPrev method checks if a track is being played from a folder. It then plays the
     * previous song if any other are found in the folder.
     */
    public void playPrev() {
        if(mIsPlayingFolder) {
            if(mCurrentFolderIndex > 0) {
                mCurrentFolderIndex--;
                Log.d(TAG, "playPrev: Prev track playing at index " + mCurrentFolderIndex);
                playSingleTrack(mFolder[mCurrentFolderIndex].getUri());
            } else {
                Toast.makeText(getApplicationContext(), "Start of folder reached", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Not playing a folder", Toast.LENGTH_LONG).show();
        }
    }
    public int repeat(){
        switch (repeatStatus){
            case 0:
                if(mIsPlayingFolder){
                    repeatStatus = 1;
                }else{
                    repeatStatus = 2;
                }
                break;
            case 1:
                repeatStatus = 2;
                break;
            case 2:
                repeatStatus = 0;
                break;
            default:
                Log.e(TAG, "repeat: illegal repeat status: " + repeatStatus);
                break;
        }
        return repeatStatus;
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

        public void playNext(){ MediaPlayerService.this.playNext();}

        public void playPrev(){ MediaPlayerService.this.playPrev();}
        public int repeat(){
            return MediaPlayerService.this.repeat();
        }
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