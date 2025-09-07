package com.example.ascii_camera;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.MutableLiveData;

public class MainActivity extends AppCompatActivity {
    private Button btMenu;
    private MutableLiveData<Uri> mldPhotoUri;
    Gallery gallery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getPermisions();

        gallery = new Gallery("ascii_", this);
        btMenu = findViewById(R.id.btMenu);
        mldPhotoUri = new MutableLiveData<>();
        btMenu.setOnClickListener(new onMenuClick());

        // TODO: 7. 9. 2025 Programaticly implement generating image views from gallery of ascii images 

        mldPhotoUri.observe(this, uri -> {
            Intent intent = new Intent(MainActivity.this, AsciiSettingsActivity.class);
            Ascii ascii = new Ascii(uri, AsciiSettings.defaultValues());
            Log.d("main_activity", ascii.toString());
            intent.putExtra("Ascii", ascii);
            startActivity(intent);
        });
    }

    private void getPermisions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
    }
    private class onMenuClick implements View.OnClickListener {

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
        public void onClick(View view) {
            PopupMenu menu = new PopupMenu(MainActivity.this, view);

            menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    int id = menuItem.getItemId();

                    if (id == R.id.fromCameraMenuItem) {
                        // TODO: 7. 9. 2025 add camera support
                        return false;
                    }

                    if (id == R.id.fromGaleryMenuItem) {
                        pickPhotoLauncher.launch(
                                new PickVisualMediaRequest.Builder()
                                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                                        .build()
                        );
                        return true;
                    }

                    return false;
                }
            });

            menu.inflate(R.menu.create_ascii_menu_selection_menu);
            menu.show();
        }
    }
}
