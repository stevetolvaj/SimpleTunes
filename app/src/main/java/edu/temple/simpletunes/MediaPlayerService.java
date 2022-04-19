package edu.temple.simpletunes;

import static edu.temple.simpletunes.AppNotificationChannel.CHANNEL_ID;
import static edu.temple.simpletunes.MainActivity.TRACK_FILE_NAME;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.documentfile.provider.DocumentFile;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * MediaPlayerService is a service created to run the MediaPlayer instance in the background
 * and bind to the controls of the media player.
 */

public class MediaPlayerService extends Service {

    /**
     * The binder used to control the service.
     */
    private final ControlsBinder mControlsBinder = new ControlsBinder();
    /**
     * The MediaPlayer api used to play tracks within the service.
     */
    private final MediaPlayer mMediaPlayer = new MediaPlayer();
    /**
     * Tag for the service.
     */
    private final static String TAG = "MEDIAPLAYERSERVICE";
    /**
     * ID used for the notification.
     */
    public static final int NOTIFICATION_ID = 1;
    /**
     * The state of a folder being played.
     */
    private boolean mIsPlayingFolder = false;
    /**
     * The original folder used for the playlist.
     */
    private DocumentFile[] mFolder;
    /**
     * The shuffled folder for the playlist.
     */
    private DocumentFile[] shuffledFolder;
    /**
     * The current index of a track being played within a folder.
     */
    private int mCurrentFolderIndex = 0;   // The index of the next song to be played in folder
    /**
     * The notification manager for the foreground notification.
     */
    private NotificationManager mNotificationManager;
    /**
     * The current state of shuffle in a playlist.
     */
    private boolean shuffleOn = false;
    /**
     * The repeat status, 0 = no repeat, 1 = folder repeat, 2 = file repeat
     */
    private int repeatStatus = 0;
    /**
     * The current track's Uri that is being played.
     */
    private Uri currentTrack;

    /**
     * The onCreate method is called when the service is started and is used to control playback
     * when a track is completed.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        mNotificationManager = getSystemService(NotificationManager.class);

        // OnCompletionListener used to play next track in order if mIsPlayingFolder set to true
        // Plays until last file is completed then resets variables.
        mMediaPlayer.setOnCompletionListener(mp -> {
            if (mIsPlayingFolder && shuffleOn) {
                // shuffle functionality
                if (mCurrentFolderIndex < mFolder.length - 1) {
                    // we're not at the end of the shuffled folder yet
                    mCurrentFolderIndex++;
                    Log.d(TAG, "onCompleteListener: Playing track at index " + mCurrentFolderIndex + " of shuffled folder");
                    playSingleTrack(shuffledFolder[mCurrentFolderIndex].getUri());
                    // Update notification with filename
                    mNotificationManager.notify(NOTIFICATION_ID, getNotification(shuffledFolder[mCurrentFolderIndex].getName()));
                } else {
                    // this is the last track in the shuffled folder, so reset index
                    mCurrentFolderIndex = 0;
                    if (repeatStatus == 1) {
                        // repeat folder is on, so start playing from the beginning again
                        Log.d(TAG, "onCompleteListener: restarting from beginning of shuffled folder ");
                        playSingleTrack(shuffledFolder[mCurrentFolderIndex].getUri());
                    } else {
                        mIsPlayingFolder = false;
                    }
                }
            } else if (mIsPlayingFolder) {
                //folder playing functionality
                if (mCurrentFolderIndex < mFolder.length - 1) {
                    // we're not at the end of the folder yet
                    if (repeatStatus != 2) {
                        // repeat file isn't on, so increment the index
                        mCurrentFolderIndex++;
                    }
                    Log.d(TAG, "onCompleteListener: Playing track at index " + mCurrentFolderIndex + " of folder");
                    playSingleTrack(mFolder[mCurrentFolderIndex].getUri());
                    // Update notification with filename
                    mNotificationManager.notify(NOTIFICATION_ID, getNotification(mFolder[mCurrentFolderIndex].getName()));
                } else {
                    // we're at the end of the folder
                    if (repeatStatus == 2) {
                        // repeat file is on, so play the same track again
                        playSingleTrack(mFolder[mCurrentFolderIndex].getUri());
                    } else {
                        // we're at the end of the folder, so reset the index
                        Log.d(TAG, "onCompleteListener: Reached end of tracks in folder");
                        mCurrentFolderIndex = 0;
                        if (repeatStatus == 1) {
                            // repeat folder is on, so play from the beginning
                            Log.d(TAG, "onCompleteListener: restarting from beginning of folder ");
                            playSingleTrack(mFolder[mCurrentFolderIndex].getUri());
                        } else {
                            // we're at the end of the folder, and repeat is off, so we're no longer playing a folder
                            mIsPlayingFolder = false;
                        }
                    }
                }
            } else if (repeatStatus == 2) {
                // we're not playing a folder, but repeat file is on
                playSingleTrack(currentTrack);
            }
            // if none of the if statements are true, then we're only playing a single file, and repeat is off, so there's nothing to do
        });
    }

    /**
     * The onStartCommand is called after onCreate and is used to set the service as a foreground
     * service.
     * @param intent The intent passed from the MainActivity.
     * @param flags Specific flags used for starting the service.
     * @param startId The ID used to start the service.
     * @return That status of the service starting.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(NOTIFICATION_ID, getNotification(intent.getStringExtra(TRACK_FILE_NAME)));

        return START_NOT_STICKY;
    }

    /**
     * The getNotification method creates a Notification object using the builder and
     * a pending intent attached to the notification.
     *
     * @param description The track name or description to display
     * @return The Notification object
     */
    public Notification getNotification(String description) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentText("Current track: " + description)
                .setSmallIcon(R.drawable.ic_service)
                .setContentIntent(pendingIntent)
                .setOnlyAlertOnce(true)
                .build();
    }

    /**
     * The onBind method is used to bind the ControlBinder to the activity that started it.
     * @param intent The intent for specifying which activity it is bound to.
     * @return The binder for this service.
     */
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
            currentTrack = uri;
        } catch (IOException e) {
            Log.d(TAG, "play: Could not play with current data source");
            e.printStackTrace();
        }
        mMediaPlayer.prepareAsync();
        mMediaPlayer.setOnPreparedListener(MediaPlayer::start);

        // Event bus for MainActivity to receive new track data.
        if (mIsPlayingFolder) {
            if (shuffleOn) {
                EventBus.getDefault().post(new TrackDataChangedEvent(mCurrentFolderIndex, getFileNames(shuffledFolder)));
            } else {
                EventBus.getDefault().post(new TrackDataChangedEvent(mCurrentFolderIndex, getFileNames(mFolder)));
            }
        } else { // Send audio file name through event bus if not playing a folder.
            String path = currentTrack.getPath();
            String name = path.substring(path.lastIndexOf("/") + 1);
            EventBus.getDefault().post(new TrackDataChangedEvent(0, name));
        }
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
     *
     * @return True if playing or false if not.
     */
    private boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }

    /**
     * The stop method will stop the currently playing audio file.
     */
    private void stop() {
        mMediaPlayer.stop();
    }

    /**
     * The playNext method checks if a track is being played from a folder. It then plays the
     * next song if any other are found in the folder.
     */
    private void playNext() {
        if (mIsPlayingFolder) {
            if (mCurrentFolderIndex < mFolder.length - 1) {
                mCurrentFolderIndex++;
                Log.d(TAG, "playNext: Next track playing at index " + mCurrentFolderIndex);
                if (shuffleOn) {
                    // Update notification
                    mNotificationManager.notify(NOTIFICATION_ID, getNotification(shuffledFolder[mCurrentFolderIndex].getName()));
                    playSingleTrack(shuffledFolder[mCurrentFolderIndex].getUri());
                } else {
                    // Update notification
                    mNotificationManager.notify(NOTIFICATION_ID, getNotification(mFolder[mCurrentFolderIndex].getName()));
                    playSingleTrack(mFolder[mCurrentFolderIndex].getUri());
                }
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
    private void playPrev() {
        if (mIsPlayingFolder) {
            if (mCurrentFolderIndex > 0) {
                mCurrentFolderIndex--;
                if (shuffleOn) {
                    playSingleTrack(shuffledFolder[mCurrentFolderIndex].getUri());
                    // Update notification
                    mNotificationManager.notify(NOTIFICATION_ID, getNotification(shuffledFolder[mCurrentFolderIndex].getName()));
                } else {
                    playSingleTrack(mFolder[mCurrentFolderIndex].getUri());
                    // Update notification
                    mNotificationManager.notify(NOTIFICATION_ID, getNotification(mFolder[mCurrentFolderIndex].getName()));
                }
                Log.d(TAG, "playPrev: Prev track playing at index " + mCurrentFolderIndex);

            } else {
                Toast.makeText(getApplicationContext(), "Start of folder reached", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Not playing a folder", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * The repeat method will set the MediaPlayerService into a mode where each track or folder
     * is repeated upon completion.
     *
     * @return The current repeat status can be 0 = no repeat, 1 = folder repeat, 2 = file repeat.
     */
    private int repeat() {
        switch (repeatStatus) {
            case 0:
                if (mIsPlayingFolder) {
                    repeatStatus = 1;
                } else if (shuffleOn) {
                    Toast.makeText(this, "Can't repeat single file in shuffle mode", Toast.LENGTH_SHORT).show();
                } else {
                    repeatStatus = 2;
                }
                break;
            case 1:
                if (shuffleOn) {
                    Toast.makeText(this, "Can't repeat single file in shuffle mode", Toast.LENGTH_SHORT).show();
                    repeatStatus = 0;
                } else {
                    repeatStatus = 2;
                }
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
     * The shuffle method will re-order the folder that is being played into a random order.
     *
     * @return The state of shuffle being on or off.
     */
    private boolean shuffle() {
        if (shuffleOn) {
            shuffleOn = false;
            // Event bus for reordering playlist.
            EventBus.getDefault().post(new TrackDataChangedEvent(mCurrentFolderIndex, getFileNames(mFolder)));
            return false;
        } else if (repeatStatus == 2) {
            Toast.makeText(this, "Can't turn on shuffle when repeating a single file", Toast.LENGTH_SHORT).show();
            return false;
        } else if (mIsPlayingFolder) {
            shuffleOn = true;
            shuffledFolder = Arrays.copyOf(mFolder, mFolder.length);
            Collections.shuffle(Arrays.asList(shuffledFolder));
            // Event bus for reordering playlist.
            EventBus.getDefault().post(new TrackDataChangedEvent(mCurrentFolderIndex, getFileNames(shuffledFolder)));
            return true;
        } else {
            Toast.makeText(this, "Can't shuffle when not playing a folder", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    /**
     * The getFileNames method used to return a string array of the filenames.
     * @param folder The DocumentFile array
     * @return The String ArrayList of filenames.
     */
    private ArrayList<String> getFileNames(DocumentFile[] folder) {
        ArrayList<String> adapterData = new ArrayList<>();
        for (DocumentFile documentFile : folder) {
            adapterData.add(documentFile.getName());
        }
        return adapterData;
    }

    /**
     * The play method plays a track at a specific position if a Uri has been loaded into
     * the MediaPlayerService first.
     *
     * @param position The position or the single track to play if not a folder.
     */
    private void play(int position) {
        if (mIsPlayingFolder) {
            mCurrentFolderIndex = position;
            if (shuffleOn) {
                playSingleTrack(shuffledFolder[mCurrentFolderIndex].getUri());
            } else {
                playSingleTrack(mFolder[mCurrentFolderIndex].getUri());
            }

            // Update notification
            mNotificationManager.notify(NOTIFICATION_ID, getNotification(mFolder[mCurrentFolderIndex].getName()));
        } else {
            if (currentTrack == null) {
                Toast.makeText(getApplicationContext(), "Select a track or folder to play", Toast.LENGTH_LONG).show();
            } else {
                play(currentTrack);
            }
        }
    }

    /**
     * Class to control media player instance through a Binder.
     */
    public class ControlsBinder extends Binder {
        /**
         * {@link MediaPlayerService#play(Uri)}
         */
        public void play(Uri uri) {
            MediaPlayerService.this.play(uri);
        }

        /**
         * {@link MediaPlayerService#isPlaying()}
         */
        public boolean isPlaying() {
            return MediaPlayerService.this.isPlaying();
        }

        /**
         * {@link MediaPlayerService#stop()}
         */
        public void stop() {
            MediaPlayerService.this.stop();
        }

        /**
         * {@link MediaPlayerService#pause()}
         */
        public void pause() {
            MediaPlayerService.this.pause();
        }

        /**
         * {@link MediaPlayerService#resume()}
         */
        public void resume() {
            MediaPlayerService.this.resume();
        }
        /**
         * {@link MediaPlayerService#playFolder(DocumentFile[])}
         */
        public void playFolder(DocumentFile[] folder) {
            MediaPlayerService.this.playFolder(folder);
        }
        /**
         * {@link MediaPlayerService#playNext()}
         */
        public void playNext() {
            MediaPlayerService.this.playNext();
        }

        /**
         * {@link MediaPlayerService#playPrev()}
         */
        public void playPrev() {
            MediaPlayerService.this.playPrev();
        }

        /**
         * {@link MediaPlayerService#repeat()}
         */
        public int repeat() {
            return MediaPlayerService.this.repeat();
        }

        /**
         * {@link MediaPlayerService#shuffle()}
         */
        public boolean shuffle() {
            return MediaPlayerService.this.shuffle();
        }

        /**
         * The getService method returns the current service class.
         * @return The MediaPlayerService.
         */
        public MediaPlayerService getService() {
            return MediaPlayerService.this;
        }

        /**
         * {@link MediaPlayerService#play(int)}
         */
        public void play(int position) {
            MediaPlayerService.this.play(position);
        }
    }

    /**
     * The onDestroy method is called before the service is destroyed and releases
     * the MediaPlayer from running in the background.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
        }
        mMediaPlayer.release();

        Log.d(TAG, "onDestroy: MediaPlayerService");
    }
}