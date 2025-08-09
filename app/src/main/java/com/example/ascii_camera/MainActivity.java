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

    //TODO implement camera
    private Button btPhotoSelection;
    private MutableLiveData<Uri> mldPhotoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btPhotoSelection = findViewById(R.id.btPhotoSelection);
        btPhotoSelection.setOnClickListener(new SelectPhoto());
        mldPhotoUri = new MutableLiveData<>();

        mldPhotoUri.observe(this, uri -> {
            Intent i = new Intent(MainActivity.this, AsciiSettingsActivity.class);
            Ascii ascii = new Ascii(this, uri, AciiSettings.defaultValues());
            i.putExtra("Ascii", ascii);
            startActivity(i);
        });

    }

    class SelectPhoto implements View.OnClickListener {
        ActivityResultLauncher<PickVisualMediaRequest> pickPhoto = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), mldPhotoUri -> {
            if (mldPhotoUri == null) {
                Log.d("Photo", "empty mldPhotoUri, photo selection failed");
            } else {
                Log.d("Photo", "mldPhotoUri: " + mldPhotoUri);
                MainActivity.this.mldPhotoUri.setValue(mldPhotoUri);

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