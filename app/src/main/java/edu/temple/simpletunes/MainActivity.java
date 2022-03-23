package edu.temple.simpletunes;

import androidx.annotation.Nullable;
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
    private static final int REQUEST_MP3 = 23;
    private MediaPlayer player;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        player = new MediaPlayer();
    }

    @Override
    protected void onResume() {
        Button browserButton = findViewById(R.id.browserButton);
        browserButton.setOnClickListener(view -> {
            Intent i = new Intent();
            i.setAction(Intent.ACTION_OPEN_DOCUMENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("audio/mpeg");
            startActivityForResult(i, REQUEST_MP3);
        });
        super.onResume();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == REQUEST_MP3 && resultCode == RESULT_OK){
            if(data == null){
                Log.d(TAG, "onActivityResult: data was null");
            }else{
                Uri audioFile = data.getData();
                Log.d(TAG, "onActivityResult: got URI " + audioFile.toString());

                mediaPlayerPlay(audioFile);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * The mediaPlayerPlay method is used to initialize the mediaPlayer
     * play the Uri provided. Also checks if reset is needed to play new audio file.
     * @param myUri The Uri to start playing.
     */
    private void mediaPlayerPlay(Uri myUri) {

        if (player.isPlaying()) {
            player.reset();
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
        try {
            player.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        player.start();
    }

}