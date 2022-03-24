package edu.temple.simpletunes;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MainActivity";
    private MediaPlayer player;
    private ActivityResultLauncher<Intent> mActivityResultLauncher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if(result.getResultCode() == RESULT_OK && result.getData() == null){
                Log.d(TAG, "onActivityResult: data was null");
            }else{
                assert result.getData() != null;
                Uri audioFile = result.getData().getData();
                Log.d(TAG, "onActivityResult: got URI " + audioFile.toString());

                mediaPlayerPlay(audioFile);
            }

        });

    }

    @Override
    protected void onResume() {
        Button browserButton = findViewById(R.id.browserButton);

        browserButton.setOnClickListener(view -> {
            Intent i = new Intent();
            i.setAction(Intent.ACTION_OPEN_DOCUMENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("audio/mpeg");
            mActivityResultLauncher.launch(i);
        });
        super.onResume();
    }


    /**
     * The mediaPlayerPlay method is used to initialize the mediaPlayer
     * play the Uri provided. Also checks if reset is needed to play new audio file.
     * Plays asynchronously
     * @param myUri The Uri to start playing.
     */
    private void mediaPlayerPlay(Uri myUri) {

        if (player == null) {
            player = new MediaPlayer();
        } else {
            player.reset();   // Reset to change data source.
        }

        player.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
        );
        try {
            player.setDataSource(getApplicationContext(), myUri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        player.prepareAsync();
        player.setOnPreparedListener(MediaPlayer::start);
    }


    @Override
    protected void onDestroy() {
        if (!isChangingConfigurations()) {
            if (player != null) {
                if (player.isPlaying()) {
                    player.stop();
                }
                player.release();
                player = null;
            }
        }
        super.onDestroy();
    }
}