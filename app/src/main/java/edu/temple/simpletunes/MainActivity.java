package edu.temple.simpletunes;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MainActivity";
    private static final int REQUEST_MP3 = 23;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {


            // OPCIONAL(explicaciones de poque pedimos los permisos)
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

            } else {
                //pedir permisos
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        permissionCheck);
            }
        }
    }



    @Override
    protected void onResume() {
        Button browserButton = findViewById(R.id.browserButton);
        browserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent();
                i.setAction(Intent.ACTION_OPEN_DOCUMENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("audio/mpeg");
                startActivityForResult(i, REQUEST_MP3);
            }
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
                //TODO: pass the URI to a function to open and play it
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}