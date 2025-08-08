package com.example.ascii_camera;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    //TODO implement camera
    private Button btPhotoSelection;
    private Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btPhotoSelection = findViewById(R.id.btPhotoSelection);
        btPhotoSelection.setOnClickListener(new SelectPhoto());
    }

    class SelectPhoto implements View.OnClickListener {
        ActivityResultLauncher<PickVisualMediaRequest> pickPhoto = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri == null) {
                Log.d("Photo", "empty uri, photo selection failed");
            } else {
                Log.d("Photo", "uri: " + uri);

                Intent i = new Intent(MainActivity.this, AsciiSettingsActivity.class);
                Ascii ascii = new Ascii(BitmapFactory.decodeFile(uri.getPath()));
                i.putExtra("Ascii", ascii);
                startActivity(i);
            }
        });
        @Override
        public void onClick(View v) {
            pickPhoto.launch(
                    new PickVisualMediaRequest
                            .Builder()
                            .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE )
                            .build()
            );
        }
    }

}