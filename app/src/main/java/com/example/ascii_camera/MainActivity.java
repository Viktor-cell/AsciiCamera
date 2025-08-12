package com.example.ascii_camera;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;

public class MainActivity extends AppCompatActivity {

    private Button btPhotoSelection;
    private MutableLiveData<Uri> mldPhotoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btPhotoSelection = findViewById(R.id.btPhotoSelection);
        mldPhotoUri = new MutableLiveData<>();

        btPhotoSelection.setOnClickListener(new SelectPhoto());

        mldPhotoUri.observe(this, uri -> {
            Intent intent = new Intent(MainActivity.this, AsciiSettingsActivity.class);
            Ascii ascii = new Ascii(uri, AsciiSettings.defaultValues());
            Log.d("main_activity", ascii.toString());
            intent.putExtra("Ascii", ascii);
            startActivity(intent);
        });
    }

    private class SelectPhoto implements View.OnClickListener {
        private final ActivityResultLauncher<PickVisualMediaRequest> pickPhotoLauncher =
                registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                    if (uri == null) {
                        Log.d("Photo", "empty mldPhotoUri, photo selection failed");
                    } else {
                        Log.d("Photo", "mldPhotoUri: " + uri);
                        mldPhotoUri.setValue(uri);
                    }
                });

        @Override
        public void onClick(View v) {
            pickPhotoLauncher.launch(
                    new PickVisualMediaRequest.Builder()
                            .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                            .build()
            );
        }
    }
}
