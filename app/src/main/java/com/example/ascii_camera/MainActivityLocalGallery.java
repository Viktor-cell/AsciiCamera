package com.example.ascii_camera;

import android.content.Intent;
import androidx.core.content.ContextCompat;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;


public class MainActivityLocalGallery extends AppCompatActivity {
        private MutableLiveData<Uri> mldPhotoUri;
        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_main_local_gallery);

                Utils.getPermissions(this, this);
                Utils.createTmpFolder(this, "tmpImageDir");
                Utils.cleanTmpFolder(this, "tmpImageDir");

                findViewById(R.id.btMenu).setOnClickListener(new onMenuClick());

                mldPhotoUri = new MutableLiveData<>();
                mldPhotoUri.observe(this, new isPhotoTakenObserver());

                MaterialButton btLocalGallery = findViewById(R.id.btLocalGallery);
                MaterialButton btGlobalGallery = findViewById(R.id.btGlobalGallery);
                FrameLayout layout = findViewById(R.id.flGallery);

                btLocalGallery.setEnabled(false);
                btLocalGallery.setAlpha(0.5f);

                btGlobalGallery.setEnabled(true);
                btGlobalGallery.setOnClickListener(view -> {
                        startActivity(new Intent(this, MainActivityGlobalGallery.class));
                        overridePendingTransition(0, 0);
                });
                showLocalGallery(layout);
        }

        private void showLocalGallery(FrameLayout layout) {
                layout.removeAllViews();
                View galleryView = Utils.createLocalGallery(LocalGallery.findAll(this, "ascii_"), this);
                layout.addView(galleryView);
        }

        private class isPhotoTakenObserver implements Observer<Uri> {
                @Override
                public void onChanged(Uri uri) {
                        Intent intent = new Intent(MainActivityLocalGallery.this, AsciiSettingsActivity.class);
                        AsciiCreator asciiCreator = new AsciiCreator(uri, AsciiSettings.defaultValues());

                        intent.putExtra("Ascii", asciiCreator);
                        startActivity(intent);
                }
        }

        private class onMenuClick implements View.OnClickListener {

                private final ActivityResultLauncher<PickVisualMediaRequest> pickPhotoLauncher =
                        registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                                if (uri != null) {
                                        mldPhotoUri.setValue(uri);
                                }
                        });

                private Uri uri;
                private final ActivityResultLauncher<Uri> takePhoto =
                        registerForActivityResult(new ActivityResultContracts.TakePicture(), did -> {
                                if (did) {
                                        mldPhotoUri.setValue(uri);

                                }
                        });

                @Override
                public void onClick(View view) {
                        PopupMenu menu = new PopupMenu(MainActivityLocalGallery.this, view);

                        menu.setOnMenuItemClickListener(menuItem -> {
                                int id = menuItem.getItemId();

                                if (id == R.id.fromCameraMenuItem) {
                                        try {
                                                File tempFile = File.createTempFile("tmp_", ".jpg", new File(getCacheDir() + "/tmpImageDir"));
                                                uri = FileProvider.getUriForFile(
                                                        MainActivityLocalGallery.this,
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