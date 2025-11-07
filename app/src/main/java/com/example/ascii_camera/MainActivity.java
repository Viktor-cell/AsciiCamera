package com.example.ascii_camera;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;





public class MainActivity extends AppCompatActivity {
    private MutableLiveData<Uri> mldPhotoUri;
    private WebsocetClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUser();
        handleConnectionIndicatorColor();
        handleAccountButtonAndTextView();

        Utils.getPermissions(this, this);
        Utils.createTmpFolder(this, "tmpImageDir");
        Utils.cleanTmpFolder(this, "tmpImageDir");

        findViewById(R.id.btMenu).setOnClickListener(new onMenuClick());

        mldPhotoUri = new MutableLiveData<>();
        mldPhotoUri.observe(this, new isPhotoTakenObserver());

        View localGallery = createLocalGallery(Gallery.findAll(this, "ascii_"));
        FrameLayout layout = findViewById(R.id.flGallery);
        layout.addView(localGallery);

        client = new WebsocetClient();
        client.start();
        /*
        client.sendMessage("Ahojte", msg -> {
            Log.d("MAMAM", "runned the 1st callback, msg: " + msg);
        });

        client.sendMessage("Matky kladny", msg -> {
            Log.d("MAMAM", "runned the 2nd callback, msg: " + msg);
        });
        */
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
        


        if (!name.equals(Utils.LOGGED_OUT_USERNAME)) {
            btn.setText("Log out");
            btn.setOnClickListener(view -> {
                Utils.editStringInPrefs("name", Utils.LOGGED_OUT_USERNAME, this);
                recreate();
            });
        } else if (name.equals(Utils.LOGGED_OUT_USERNAME)) {
            btn.setText("Log in");
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
                        i.setColor(isOnline ? Color.GREEN : Color.RED);
                        indicator.setBackground(i);
                    });
                }).start();

                handler.postDelayed(this, 500);
            }
        };

        handler.post(connectionCheckRunnable);
    }

    View createLocalGallery(ArrayList<Uri> images) {
        if (images.isEmpty()) {
            TextView tv = new TextView(MainActivity.this);
            tv.setText("No image found");
            return tv;
        }
        RecyclerView rvGallery = new RecyclerView(this);
        rvGallery.setLayoutManager(new GridLayoutManager(this, 2));
        rvGallery.setAdapter(new LocalGalleryAdapter(images));

        return rvGallery;
    }

    private class isPhotoTakenObserver implements Observer<Uri> {
        @Override
        public void onChanged(Uri uri) {
            Intent intent = new Intent(MainActivity.this, AsciiSettingsActivity.class);
            Ascii ascii = new Ascii(uri, AsciiSettings.defaultValues());
            
            intent.putExtra("Ascii", ascii);
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