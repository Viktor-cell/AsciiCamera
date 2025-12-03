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


public class MainActivityGlobalGallery extends AppCompatActivity {
        private MutableLiveData<Uri> mldPhotoUri;
        private WebsocetClient client;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_main_global_gallery);
                client = new WebsocetClient();

                initUser();
                handleConnectionIndicatorColor();
                handleAccountButtonAndTextView();

                Utils.getPermissions(this, this);
                Utils.createTmpFolder(this, "tmpImageDir");
                Utils.cleanTmpFolder(this, "tmpImageDir");

                findViewById(R.id.btMenu).setOnClickListener(new onMenuClick());

                mldPhotoUri = new MutableLiveData<>();
                mldPhotoUri.observe(this, new isPhotoTakenObserver());

                MaterialButton btLocalGallery = findViewById(R.id.btLocalGallery);
                MaterialButton btGlobalGallery = findViewById(R.id.btGlobalGallery);
                FrameLayout layout = findViewById(R.id.flGallery);

                btGlobalGallery.setEnabled(false);
                btGlobalGallery.setAlpha(0.5f);

                btLocalGallery.setEnabled(true);
                btLocalGallery.setOnClickListener(view -> {
                        startActivity(new Intent(this, MainActivityLocalGallery.class));
                        overridePendingTransition(0, 0);
                });
                showGlobalGallery(layout);
        }



        private void showGlobalGallery(FrameLayout layout) {
                layout.removeAllViews();
                client = new WebsocetClient();

                JSONObject json = new JSONObject(Map.of(
                        "count", 15,
                        "author", "",
                        "artname", ""
                ));

                client.sendMessage(json, msg -> {
                        try {
                                Log.d("GOT_THIS", msg);

                                JSONArray jsonArray = new JSONArray(msg);
                                Log.d("GOT_THIS", jsonArray.toString());

                                ArrayList<FullAscii> fullAsciis = FullAscii.fromJSONArray(jsonArray);
                                Log.d("GOT_THIS", fullAsciis.toString());

                                runOnUiThread(() -> {
                                        View galleryView = Utils.createGlobalGallery(fullAsciis, MainActivityGlobalGallery.this, client);
                                        layout.addView(galleryView);
                                });
                        } catch (Exception e) {
                                throw new RuntimeException(e);
                        }
                });

        }


        private void initUser() {
                if (Utils.getStringFromPrefs("name", this).trim().isEmpty()) {
                        Utils.addStringToPrefs("name", Utils.LOGGED_OUT_USERNAME, this);
                }
        }

        private void handleAccountButtonAndTextView() {
                ImageButton btn = findViewById(R.id.btAccount);
                TextView tv = findViewById(R.id.tvLoginName);
                String name = Utils.getStringFromPrefs("name", this);

                tv.setText(name);


                if (!name.equals(Utils.LOGGED_OUT_USERNAME)) {
                        btn.setOnClickListener(view -> {
                                startActivity(new Intent(this, AccountActivity.class));
                        });
                } else {
                        btn.setOnClickListener(view -> startActivity(new Intent(this, LoginActivity.class)));
                }

        }

        private void handleConnectionIndicatorColor() {
                Handler handler = new Handler(Looper.getMainLooper());
                View indicator = findViewById(R.id.vConnectionIndicator);

                Runnable connectionCheckRunnable = new Runnable() {
                        @Override
                        public void run() {
                                new Thread(() -> {
                                        boolean isOnline = ServerUtils.isOnline();
                                        GradientDrawable i = new GradientDrawable();

                                        runOnUiThread(() -> {
                                                i.setColor(isOnline ? ContextCompat.getColor(MainActivityGlobalGallery.this, R.color.success) : ContextCompat.getColor(MainActivityGlobalGallery.this, R.color.error));
                                                indicator.setBackground(i);
                                        });
                                }).start();

                                handler.postDelayed(this, 5000);
                        }
                };

                handler.post(connectionCheckRunnable);
        }



        private class isPhotoTakenObserver implements Observer<Uri> {
                @Override
                public void onChanged(Uri uri) {
                        Intent intent = new Intent(MainActivityGlobalGallery.this, AsciiSettingsActivity.class);
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
                        PopupMenu menu = new PopupMenu(MainActivityGlobalGallery.this, view);

                        menu.setOnMenuItemClickListener(menuItem -> {
                                int id = menuItem.getItemId();

                                if (id == R.id.fromCameraMenuItem) {
                                        try {
                                                File tempFile = File.createTempFile("tmp_", ".jpg", new File(getCacheDir() + "/tmpImageDir"));
                                                uri = FileProvider.getUriForFile(
                                                        MainActivityGlobalGallery.this,
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