package edu.temple.simpletunes;

import androidx.annotation.NonNull;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The MainActivity class is used to control the UI in the application and start the
 * MediaPlayerService.
 */
public class MainActivity extends AppCompatActivity {
    /**
     * Tag used for PlayListAdapterData.
     */
    private static final String ADAPTER_DATA = "adapterData";
    /**
     * Tag used for MainActivity.
     */
    private final String TAG = "MainActivity";
    /**
     * Tag used for repeatState.
     */
    private final String REPEAT_STATE_KEY = "repeatState";
    /**
     * Tag used for ShuffleState.
     */
    private final String SHUFFLE_STATE_KEY = "shuffleState";
    /**
     * Tag used for playState.
     */
    private final String PLAY_STATE_KEY = "playState";
    /**
     * Tag used for trackPosition.
     */
    public static final String TRACK_POSITION = "trackPosition";
    /**
     * Used to launch the system file browser for single track play.
     */
    private ActivityResultLauncher<Intent> mActivityResultLauncher;
    /**
     * Used to launch the system file browser for folder play.
     */
    private ActivityResultLauncher<Intent> folderLauncher;
    /**
     * Tag used for trackFileName
     */
    public static final String TRACK_FILE_NAME = "trackFileName";
    /**
     * Used to store the storage permission code 101.
     */
    private static final int STORAGE_PERMISSION_CODE = 101;
    /**
     * The intent used to start the MediaPlayerService.
     */
    private Intent mServiceIntent;
    /**
     * The state of repeat for folder or tracks.
     */
    private int repeatState = 0;
    /**
     * The state of shuffle, 0 = no repeat, 1 = folder repeat, 2 = file repeat.
     */
    private boolean shuffleState = false;
    /**
     * The state showing if a track is playing.
     */
    private boolean playState = false;
    /**
     * The playListAdapter used for the RecyclerView.
     */
    private PlaylistAdapter playlistAdapter;
    /**
     * The list used to populate the RecyclerView.
     */
    private List<String> adapterData = new ArrayList<>();
    /**
     * The interface used to track which position is clicked within the RecyclerView.
     */
    private OnClickInterface onClickInterface;
    /**
     * The current state of the system night mode.
     */
    private boolean nightModeState = false;
    /**
     * The current track number being played.
     */
    private int currentTrackNum = 0;
    private MusicTrack[] currentFolder;
    private TextView artistTextview;
    private TextView trackNameTextView;
    /**
     * The connection state of the MediaPlayerService.
     */
    private boolean isConnected = false;
    private MediaPlayerService.ControlsBinder mAudioControlsBinder;
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            isConnected = true;
            mAudioControlsBinder = (MediaPlayerService.ControlsBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isConnected = false;
        }
    };

    /**
     * The onCreate method is called when the activity is started.
     * @param savedInstanceState The Bundle that was previously destroyed or null if first start.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        updateNightMode();
        setContentView(R.layout.activity_main);

        // Interface for playlistAdapter items being clicked. Play track at specific position.
        onClickInterface = position -> {
            if(isConnected) {
                mAudioControlsBinder.play(position);
            }
        };

        if(savedInstanceState == null) {
            // Show default message in RecyclerView on start of app.
            adapterData.add(getString(R.string.adapterDefaultMessage));
        }

        // Bind the MediaPlayerService to the MainActivity.
        mServiceIntent = new Intent(this, MediaPlayerService.class);
        bindService(mServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

        mActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if(result.getResultCode() == RESULT_OK && result.getData() == null){
                Log.d(TAG, "onActivityResult: data was null");
            }else{
                if (result.getData() != null) {
                    Uri audioFile = result.getData().getData();
                    Log.d(TAG, "onActivityResult: got URI " + audioFile.toString());
                    // Reset shuffle state after single track is selected.
                    if(isConnected && shuffleState){
                        mediaPlayerShuffle();
                        updateShuffleButton(false);
                    }
                    mediaPlayerPlay(audioFile);
                    updatePlayButton(true);
                    currentTrackNum = 0; // Reset returned save instance if selecting new track.
                }
            }
        });
        folderLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if(result.getResultCode() == RESULT_OK && result.getData() == null){
                Log.d(TAG, "onActivityResult: data was null");
            }else{
                if (result.getData() != null) {
                    Uri uri = result.getData().getData();
                    Log.d(TAG, "onActivityResult: got URI " + uri.toString());
                    DocumentFile directory = DocumentFile.fromTreeUri(MainActivity.this, uri);
                    if(directory == null){
                        Log.d(TAG, "onActivityResult: got empty directory");
                    }else{
                        DocumentFile[] contents = directory.listFiles();
                        ArrayList<MusicTrack> list = new ArrayList<>();
                        for(int i = 0; i < contents.length; i++){
                            MusicTrack m = new MusicTrack(MainActivity.this, contents[i]);
                            if(m.getIsAudio()){
                                list.add(m);
                            }
                        }
                        currentFolder = new MusicTrack[list.size()];
                        currentFolder = list.toArray(currentFolder);
                        Log.d(TAG, "onCreate: Folder passed to MediaPlayerService. Items in folder: " + contents.length);
                        // Reset shuffle state after new folder is selected.
                        if(isConnected && shuffleState){
                            mediaPlayerShuffle();
                            updateShuffleButton(false);
                        }
                        mediaPlayerPlayFolder(currentFolder);
                        updatePlayButton(true);
                        currentTrackNum = 0; // Reset returned save instance if selecting new track.
                    }
                }
            }
        });
    }

    /**
     * The onStart method is called after the activity is created and is used to register
     * the EventBus.
     */
    @Override
    protected void onStart() {
        super.onStart();
        // Event bus register for media player service data changes.
        EventBus.getDefault().register(this);
        artistTextview = findViewById(R.id.artistTextView);
        trackNameTextView = findViewById(R.id.trackNameTextView);
    }

    /**
     * The checkPermission function is used to check what permissions have been granted previously.
     * @param permission The permission to check.
     * @param requestCode The requestCode of the permission being checked.
     * @return True if permission is granted and false otherwise.
     */
    public boolean checkPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission) == PackageManager.PERMISSION_DENIED) {
            // Requesting the permission
            ActivityCompat.requestPermissions(MainActivity.this, new String[] { permission }, requestCode);
            return false;
        }else{
            Log.d(TAG, "checkPermission: permission granted");
            return true;
        }
    }

    /**
     * The onRequestPermissionResult method is called to verify permission was granted to
     * access storage on the device.
     * @param requestCode The code relating to permission storage.
     * @param permissions The requested permissions. Never null.
     * @param grantResults The results of the permission granted. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent i = new Intent();
                i.setAction(Intent.ACTION_OPEN_DOCUMENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("audio/mpeg");
                mActivityResultLauncher.launch(i);
            }
        }
    }

    /**
     * The onResume method is called after the activity is created and used to initialize
     * and control the UI elements.
     */
    @Override
    protected void onResume() {
        // Initialize the RecyclerView variables.
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        playlistAdapter = new PlaylistAdapter(this, adapterData, onClickInterface);
        recyclerView.setAdapter(playlistAdapter);
        playlistAdapter.setHighlightedPosition(currentTrackNum);

        ImageButton browserButton = findViewById(R.id.browserButton);
        browserButton.setOnClickListener(view -> {
            if(checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE)){
                Intent i = new Intent();
                i.setAction(Intent.ACTION_OPEN_DOCUMENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("*/*");
                i.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"audio/*"});
                mActivityResultLauncher.launch(i);
            }
        });
        ImageButton folderButton = findViewById(R.id.libraryButton);
        folderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // https://www.programcreek.com/java-api-examples/?class=android.content.Intent&method=ACTION_OPEN_DOCUMENT_TREE
                Intent i = new Intent();
                i.setAction(Intent.ACTION_OPEN_DOCUMENT_TREE);
                i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                        | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
                folderLauncher.launch(i);
            }
        });

        ImageButton playPauseButton = findViewById(R.id.playPauseButton);
        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean status = mediaPlayerPauseOrStart();
                updatePlayButton(status);
            }
        });
        ImageButton skipNextButton = findViewById(R.id.skipNextButton);
        skipNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayerNext();
            }
        });

        ImageButton skipPrevButton = findViewById(R.id.skipPrevButton);
        skipPrevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayerPrev();
            }
        });
        ImageButton repeatButton = findViewById(R.id.repeatButton);
        repeatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int status = mediaPlayerRepeat();
                updateRepeatButton(status);
            }
        });
        ImageButton shuffleButton = findViewById(R.id.shuffleButton);
        shuffleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean status = mediaPlayerShuffle();
                updateShuffleButton(status);
            }
        });
        super.onResume();
    }

    /**
     * The onRestoreInstanceState is called before the activity is resumed and sets instance
     * variables back to previous state.
     * @param savedInstanceState The bundle containing saved instance variables.
     */
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        repeatState = savedInstanceState.getInt(REPEAT_STATE_KEY, 0);
        updateRepeatButton(repeatState);
        shuffleState = savedInstanceState.getBoolean(SHUFFLE_STATE_KEY, false);
        updateShuffleButton(shuffleState);
        playState = savedInstanceState.getBoolean(PLAY_STATE_KEY, false);
        updatePlayButton(playState);
        adapterData = savedInstanceState.getStringArrayList(ADAPTER_DATA);
        currentTrackNum = savedInstanceState.getInt(TRACK_POSITION, 0);
        super.onRestoreInstanceState(savedInstanceState);
    }

    /**
     * The onSavedInstanceState method is called before the activity is destroyed to save any
     * variables that are needed after onCreate on restart.
     * @param outState The bundle containing instance variables.
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt(REPEAT_STATE_KEY, repeatState);
        outState.putBoolean(SHUFFLE_STATE_KEY, shuffleState);
        outState.putBoolean(PLAY_STATE_KEY, playState);
        outState.putStringArrayList(ADAPTER_DATA, (ArrayList<String>) adapterData);
        outState.putInt(TRACK_POSITION, currentTrackNum);
        super.onSaveInstanceState(outState);
    }

    /**
     * The updateNightMode method will set the nightModeState variable to true or false
     * if the system night mode is active.
     */
    private void updateNightMode(){
        // https://stackoverflow.com/questions/44170028/android-how-to-detect-if-night-mode-is-on-when-using-appcompatdelegate-mode-ni
        int nightModeFlags = getApplicationContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if(nightModeFlags == Configuration.UI_MODE_NIGHT_YES){
            nightModeState = true;
        }else if(nightModeFlags == Configuration.UI_MODE_NIGHT_NO){
            nightModeState = false;
        }
    }

    /**
     * The updateRepeatButton method sets the image resource according to night mode being on/off
     * and if the media player is currently in a repeat state.
     * @param status The current state of repeat.
     */
    private void updateRepeatButton(int status){
        updateNightMode();
        ImageButton repeatButton = findViewById(R.id.repeatButton);
        switch (status){
            case 0:
                if(nightModeState){
                    repeatButton.setImageResource(R.drawable.ic_baseline_repeat_48_night);
                }else{
                    repeatButton.setImageResource(R.drawable.ic_baseline_repeat_48);
                }
                break;
            case 1:
                if(nightModeState){
                    repeatButton.setImageResource(R.drawable.ic_baseline_repeat_on_48_night);
                }else{
                    repeatButton.setImageResource(R.drawable.ic_baseline_repeat_on_48);
                }
                break;
            case 2:
                if(nightModeState){
                    repeatButton.setImageResource(R.drawable.ic_baseline_repeat_one_48_night);
                }else{
                    repeatButton.setImageResource(R.drawable.ic_baseline_repeat_one_48);
                }
                break;
            default:
                break;
        }
    }

    /**
     * The updateShuffleButton method sets the image resource according to night mode being on/off
     * and if the playlist is currently in a shuffle state.
     * @param status The current state of the shuffle.
     */
    private void updateShuffleButton(boolean status){
        updateNightMode();
        ImageButton shuffleButton = findViewById(R.id.shuffleButton);
        if(status && nightModeState) {
            shuffleButton.setImageResource(R.drawable.ic_baseline_shuffle_on_48_night);
        }else if(status){
            shuffleButton.setImageResource(R.drawable.ic_baseline_shuffle_on_48);
        }else if(nightModeState){
            shuffleButton.setImageResource(R.drawable.ic_baseline_shuffle_48_night);
        }else{
            shuffleButton.setImageResource(R.drawable.ic_baseline_shuffle_48);
        }
    }

    /**
     * The updatePlayButton method sets the image resource according to night mode being on/off
     * and if the track is currently playing.
     * @param isPlaying The current state of the track.
     */
    private void updatePlayButton(boolean isPlaying){
        updateNightMode();
        ImageButton playPauseButton = findViewById(R.id.playPauseButton);
        if(isPlaying && nightModeState){
            playPauseButton.setImageResource(R.drawable.ic_baseline_pause_circle_outline_72_night);
        }else if(isPlaying){
            playPauseButton.setImageResource(R.drawable.ic_baseline_pause_circle_outline_72);
        } else if (nightModeState) {
            playPauseButton.setImageResource(R.drawable.ic_baseline_play_circle_outline_72_night);
        }else{
            playPauseButton.setImageResource(R.drawable.ic_baseline_play_circle_outline_72);
        }
    }
    private void updateTextViews(){
        artistTextview.setText(currentFolder[currentTrackNum].getArtist());
        trackNameTextView.setText(currentFolder[currentTrackNum].getTitle());
    }
    /**
     * The mediaPlayerPrev method is used to skip to the previously played track in the file.
     */
    private void mediaPlayerPrev() {
        if (isConnected) {
            mAudioControlsBinder.playPrev();
        }
    }
    /**
     * The mediaPlayerNext method is used to skip to the next track in the file.
     */
    private void mediaPlayerNext() {
        if (isConnected) {
            mAudioControlsBinder.playNext();
        }
    }

    /**
     * The mediaPlayerPauseOrStart method is used to pause the current track or start from the
     * paused position. Checks if service is bound first.
     *
     * @return True if the media is playing, false otherwise.
     */
    private boolean mediaPlayerPauseOrStart() {
        if (isConnected) {
            playState = mAudioControlsBinder.isPlaying();
            if(playState){
                mAudioControlsBinder.pause();
            }else{
                mAudioControlsBinder.resume();
            }
        }else{
            playState = false;
        }
        playState = mAudioControlsBinder.isPlaying();
        return playState;
    }

    /**
     * The mediaPlayerPlay method is used to start the MediaPlayerService and
     * also play the associated Uri.
     * @param myUri The Uri to start playing.
     */
    private void mediaPlayerPlay(Uri myUri) {
        String path = myUri.getPath();
        String fileName = path.substring(path.lastIndexOf("/") + 1);
        if (isConnected) { // Start service if first time playing a track.
            // Send file name through intent to service for first notification.
            mServiceIntent.putExtra(TRACK_FILE_NAME, fileName);
            startForegroundService(mServiceIntent);
            // Reset shuffle state after single track is selected.
            mAudioControlsBinder.play(myUri);
            playState = true;
        }
    }

    /**
     * The mediaPlayerPlayFolder plays the entire folder found in a DocumentFile array. Stops after
     * last file is completed playing.
     * @param folder The DocumentFile array to play all audio files from.
     */
    private void mediaPlayerPlayFolder(MusicTrack[] folder) {
        Arrays.sort(folder, new MusicTrackComparator());
        String name = folder[0].getName();
        if (isConnected) {
            // Send file name through intent to service for first notification.
            mServiceIntent.putExtra(TRACK_FILE_NAME, name);
            startForegroundService(mServiceIntent);
            // Reset shuffle state after new folder is selected.
            mAudioControlsBinder.playFolder(folder);
            playState = true;
        }
    }

    /**
     * The mediaPlayerRepeat method sets the MediaPlayerService into repeat mode.
     * @return The state of the repeat, 0 = no repeat, 1 = folder repeat, 2 = file repeat.
     */
    private int mediaPlayerRepeat(){
        if(isConnected){
            repeatState = mAudioControlsBinder.repeat();
        }else{
            repeatState = 0;
        }
        return repeatState;
    }

    /**
     * The mediaPlayerShuffle method is used to set the mediaPlayerService shuffle state.
     * @return True if shuffle is on and false otherwise.
     */
    private boolean mediaPlayerShuffle(){
        if(isConnected){
            shuffleState = mAudioControlsBinder.shuffle();
        }else{
            shuffleState = false;
        }
        return shuffleState;
    }


    /**
     * The handleTrackedDataChange method will be called when either the track position changes
     * or the playlist is updated in the MediaPlayerService.
     * @param event The TrackDataChangedEvent object that has changed.
     *              Contains playlist and current track number.
     */
    @Subscribe
    public void handleTrackDataChange(TrackDataChangedEvent event) {

        currentTrackNum = event.getTrackPosition();
        playlistAdapter.setHighlightedPosition(currentTrackNum);
        if (event.getSingleTrack() == null) { // Notify all adapter data changed for folder play.
            adapterData.clear();
            adapterData.addAll(event.getTrackList());
            playlistAdapter.notifyDataSetChanged();
        } else { // Notify first track played as single.
            adapterData.clear();
            adapterData.add(event.getSingleTrack());
            playlistAdapter.notifyItemChanged(0);
        }
        updateTextViews();
    }

    /**
     * The onStop method is called before the activity is stopped and used to unregister the
     * EventBus.
     */
    @Override
    protected void onStop() {
        super.onStop();
        // Event bus register for media player service data changes.
        EventBus.getDefault().unregister(this);
    }

    /**
     * The onDestroy method is called before the activity is destroyed and used to unbind
     * and stop the service.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        if (!isChangingConfigurations())
            stopService(new Intent(this, MediaPlayerService.class));
    }

    /**
     * The OnClickInterface is used to acquire the position of an item selected by passing into the
     * playlistAdapter.
     */
    public interface OnClickInterface {
        /**
         * The position method is used to determine position of the item clicked.
         * @param position The position in the list.
         */
        void itemClicked(int position);
    }
}