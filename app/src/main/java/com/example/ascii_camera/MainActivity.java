package com.example.ascii_camera;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
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

        initUser();
        handleConnectionIndicatorColor();
        handleAccountButtonAndTextView();

        Utils.getPermissions(this, this);
        Utils.createTmpFolder(this, "tmpImageDir");
        Utils.showContentOfTmpFolder(this, "tmpImageDir");
        Utils.cleanTmpFolder(this, "tmpImageDir");

        findViewById(R.id.btMenu).setOnClickListener(new onMenuClick());


        mldPhotoUri = new MutableLiveData<>();
        mldPhotoUri.observe(this, new isPhotoTakenObserver());

        View localGallery = createLocalGallery(Gallery.findAll(this, "ascii_"));
        ConstraintLayout layout = findViewById(R.id.layout);
        layout.addView(localGallery);

        Log.d("USER_", Utils.getStringFromPrefs("name", this).trim());
    }

    private void initUser() {
        if (Utils.getStringFromPrefs("name", this).trim().isEmpty()) {
            Utils.addStringToPrefs("name", Utils.LOGGED_OUT_USERNAME, this);
        }
    }

    private void handleAccountButtonAndTextView() {
        Button btn = findViewById(R.id.btAccount);
        TextView tv = findViewById(R.id.tvLoginName);
        String name = Utils.getStringFromPrefs("name", this);

        tv.setText(name);
        Log.d("USER_LOGIN_", name);


        if (!name.equals(Utils.LOGGED_OUT_USERNAME)) {
            btn.setText("Log out");
            btn.setOnClickListener(view -> {
                Utils.editStringInPrefs("name", Utils.LOGGED_OUT_USERNAME, this);
                recreate();
            });
        } else if (name.equals(Utils.LOGGED_OUT_USERNAME)) {
            btn.setText("Log in");
            btn.setOnClickListener(view -> {
                startActivity(new Intent(this, LoginActivity.class));
            });
        }

    }

    private void handleConnectionIndicatorColor() {
        Button btn = findViewById(R.id.btMainConnection);
        Runnable rUpdateConnectionIndicator = new Runnable() {
            @Override
            public void run() {
                boolean isOnline = ServerUtils.isOnline();

                btn.setBackgroundTintList(isOnline ? ColorStateList.valueOf(Color.GREEN) : ColorStateList.valueOf(Color.RED));
            }
        };
        new Thread(rUpdateConnectionIndicator).start();
        btn.setOnClickListener(view -> {
            new Thread(rUpdateConnectionIndicator).start();
        });
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

    private class isPhotoTakenObserver implements Observer<Uri> {
        @Override
        public void onChanged(Uri uri) {
            Intent intent = new Intent(MainActivity.this, AsciiSettingsActivity.class);
            Ascii ascii = new Ascii(uri, AsciiSettings.defaultValues());
            Log.d("main_activity", ascii.toString());
            intent.putExtra("Ascii", ascii);
            startActivity(intent);
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
