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
import androidx.core.content.FileProvider;
import androidx.lifecycle.MutableLiveData;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private Button btMenu;
    private MutableLiveData<Uri> mldPhotoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Helper.getPermisions(this, this);
        Helper.createTmpFolder(this, "tmpImageDir");
        Helper.showContentOfTmpFolder(this, "tmpImageDir");
        Helper.emptyTmpFolder(this, "tmpImageDir");

        for (Uri uri : Gallery.findAll(this, "ascii_")) {
            Log.d("GALLERY_", uri.toString());
        }

        btMenu = findViewById(R.id.btMenu);
        btMenu.setOnClickListener(new onMenuClick());

        mldPhotoUri = new MutableLiveData<>();
        mldPhotoUri.observe(this, uri -> {
            Intent intent = new Intent(MainActivity.this, AsciiSettingsActivity.class);
            Ascii ascii = new Ascii(uri, AsciiSettings.defaultValues());
            Log.d("main_activity", ascii.toString());
            intent.putExtra("Ascii", ascii);
            startActivity(intent);
        });
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

        private Uri uri;
        private final ActivityResultLauncher<Uri> takePhoto =
                registerForActivityResult(new ActivityResultContracts.TakePicture(), did -> {
                    if (did) {
                        mldPhotoUri.setValue(uri);
                        Log.d("DIR_", "created new temp file:" + uri.getPath() );
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
                        try {
                            File tempFile = File.createTempFile("tmp_", ".jpg", new File(getCacheDir() + "/tmpImageDir"));
                            uri = FileProvider.getUriForFile(
                                    MainActivity.this,
                                    getPackageName() + ".provider",
                                    tempFile
                            );

                            takePhoto.launch(uri);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }

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
