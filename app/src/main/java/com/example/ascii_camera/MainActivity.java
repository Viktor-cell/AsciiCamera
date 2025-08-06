package com.example.ascii_camera;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private Button btPhotoSelection;
    private Button btPhotoCapture;

    private Uri uriPhoto;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btPhotoSelection = findViewById(R.id.btPhotoSelection);
        btPhotoCapture = findViewById(R.id.btPhotoCapture);

        btPhotoSelection.setOnClickListener(new SelectPhoto());
        btPhotoCapture.setOnClickListener(new CapturPhoto());
    }

    class SelectPhoto implements View.OnClickListener {
        ActivityResultLauncher<PickVisualMediaRequest> pickPhoto = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            uriPhoto = uri;
            if (uri == null) {
                Log.d("Photo", "empty uri, photo selection failed");
            } else {
                Log.d("Photo", "uri: " + uriPhoto);
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

    class CapturPhoto implements View.OnClickListener {

        ActivityResultLauncher<Uri> capturePhoto = registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
            if (success) {
                Log.d("Photo", "uri: " + uriPhoto);
            } else {
                Log.d("Photo", "Photo capture failed");
            }
        });

        @Override
        public void onClick(View v) {
            String fileName = "IMG_" + System.currentTimeMillis() + ".jpg";
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

            File capturedPhotoFile = new File(storageDir, fileName);

            // TODO: 6. 8. 2025 create a uri where to save captured photo 


        }
    }
}