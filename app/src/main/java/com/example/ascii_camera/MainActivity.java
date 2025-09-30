package com.example.ascii_camera;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private MutableLiveData<Uri> mldPhotoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Utils.getPermissions(this, this);
        Utils.createTmpFolder(this, "tmpImageDir");
        Utils.showContentOfTmpFolder(this, "tmpImageDir");
        Utils.cleanTmpFolder(this, "tmpImageDir");

        for (Uri uri : Gallery.findAll(this, "ascii_")) {
            Log.d("GALLERY_", uri.toString());
        }

        findViewById(R.id.btMenu).setOnClickListener(new onMenuClick());

        mldPhotoUri = new MutableLiveData<>();
        mldPhotoUri.observe(this, uri -> {
            Intent intent = new Intent(MainActivity.this, AsciiSettingsActivity.class);
            Ascii ascii = new Ascii(uri, AsciiSettings.defaultValues());
            Log.d("main_activity", ascii.toString());
            intent.putExtra("Ascii", ascii);
            startActivity(intent);
        });

        View localGallery = createLocalGallery(Gallery.findAll(this, "ascii_"));
        ConstraintLayout layout = findViewById(R.id.layout);
        layout.addView(localGallery);

        Log.d("USER_", Utils.getStringFromPrefs("name", this).trim());
    }

    View createLocalGallery(ArrayList<Uri> images) {
        if (images.size() == 0) {
            TextView tv = new TextView(MainActivity.this);
            tv.setText("No image found");
            return tv;
        }
        RecyclerView rvGallery = new RecyclerView(this);
        rvGallery.setLayoutManager(new GridLayoutManager(this, 2));
        rvGallery.setAdapter(new GalleryAdapter(images));

        return rvGallery;
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
                        Log.d("DIR_", "created new temp file:" + uri.getPath());
                    }
                });

        @Override
        public void onClick(View view) {
            PopupMenu menu = new PopupMenu(MainActivity.this, view);

            menu.setOnMenuItemClickListener(menuItem -> {
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

                    return true;
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
            });

            menu.inflate(R.menu.create_ascii_menu_selection_menu);
            menu.show();
        }
    }
}
