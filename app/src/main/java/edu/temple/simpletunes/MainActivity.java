package edu.temple.simpletunes;

import androidx.annotation.NonNull;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.ImageButton;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MainActivity";
    private ActivityResultLauncher<Intent> mActivityResultLauncher;
    private static final int STORAGE_PERMISSION_CODE = 101;


    // Variables and initialization of MediaPlayerService service connection.
    // TODO: use functions available through mAudioControlsBinder to control media.
    // mMediaControlsBinder.play, pause, resume, stop, isPlaying.
    // resume and pause does not check if track is playing or already paused.
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Bind the MediaPlayerService to the MainActivity.
        Intent intent = new Intent(this, MediaPlayerService.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);

        mActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if(result.getResultCode() == RESULT_OK && result.getData() == null){
                Log.d(TAG, "onActivityResult: data was null");
            }else{
                if (result.getData() != null) {
                    Uri audioFile = result.getData().getData();
                    Log.d(TAG, "onActivityResult: got URI " + audioFile.toString());

                    mediaPlayerPlay(audioFile);
                }
            }
        });
    }

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

    @Override
    protected void onResume() {
        ImageButton browserButton = findViewById(R.id.browserButton);

        browserButton.setOnClickListener(view -> {
            if(checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE)){
                Intent i = new Intent();
                i.setAction(Intent.ACTION_OPEN_DOCUMENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("audio/mpeg");
                mActivityResultLauncher.launch(i);
            }
        });
        super.onResume();
    }

    /**
     * The mediaPlayerPlay method is used to start the MediaPlayerService and
     * also play the associated Uri.
     * @param myUri The Uri to start playing.
     */
    private void mediaPlayerPlay(Uri myUri) {
        if (isConnected) // Start service if first time playing a track.
            startService(new Intent(this, MediaPlayerService.class));
        mAudioControlsBinder.play(myUri);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        if (!isChangingConfigurations())
            stopService(new Intent(this, MediaPlayerService.class));
    }
}