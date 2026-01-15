package com.example.ascii_camera;

import static com.example.ascii_camera.Utils.createTmpFolder;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


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
                private ViewGroup currentMenuLayout = null;
                private ViewGroup currentEnterUrlLayout = null;
                private MaterialButton btMenu;
                private final LayoutInflater inflater = MainActivityLocalGallery.this.getLayoutInflater();

                private void showMenu() {
                        ViewGroup root = MainActivityLocalGallery.this.findViewById(R.id.rootLayout);
                        ViewGroup menuLayout = (ViewGroup) inflater.inflate(R.layout.src_options_for_ascii_popup, root, false);

                        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                                ConstraintLayout.LayoutParams.WRAP_CONTENT
                        );

                        params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
                        params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
                        params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
                        params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;

                        menuLayout.setLayoutParams(params);
                        root.addView(menuLayout);
                        currentMenuLayout = menuLayout;

                        setupMenuButtons(menuLayout);
                        setButtonStateClose();
                }

                private void hideMenu() {
                        ViewGroup root = MainActivityLocalGallery.this.findViewById(R.id.rootLayout);
                        root.removeView(currentMenuLayout);
                        currentMenuLayout = null;
                        setButtonStateAdd();
                }

                private void showEnterUrl() {

                        ViewGroup root = MainActivityLocalGallery.this.findViewById(R.id.rootLayout);
                        ViewGroup enterUrlLayout = (ViewGroup) inflater.inflate(R.layout.enter_url, root, false);

                        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                                ConstraintLayout.LayoutParams.MATCH_PARENT,
                                ConstraintLayout.LayoutParams.WRAP_CONTENT
                        );

                        params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
                        params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
                        params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
                        params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;

                        enterUrlLayout.setLayoutParams(params);
                        root.addView(enterUrlLayout);
                        currentEnterUrlLayout = enterUrlLayout;

                        setupEnterUrlButtons(enterUrlLayout);
                        setButtonStateClose();
                }

                private void hideEnterUrl() {
                        ViewGroup root = MainActivityLocalGallery.this.findViewById(R.id.rootLayout);
                        root.removeView(currentEnterUrlLayout);
                        currentEnterUrlLayout = null;
                        setButtonStateAdd();
                }

                private void setButtonStateClose() {
                        btMenu.setIcon(ContextCompat.getDrawable(MainActivityLocalGallery.this, R.drawable.ic_close));
                        btMenu.setIconTint(ContextCompat.getColorStateList(MainActivityLocalGallery.this, R.color.pure_white));
                        btMenu.setBackgroundTintList(ContextCompat.getColorStateList(MainActivityLocalGallery.this, R.color.system_red));
                }

                private void setButtonStateAdd() {
                        btMenu.setIcon(ContextCompat.getDrawable(MainActivityLocalGallery.this, R.drawable.ic_plus));
                        btMenu.setIconTint(ContextCompat.getColorStateList(MainActivityLocalGallery.this, R.color.pure_white));
                        btMenu.setBackgroundTintList(ContextCompat.getColorStateList(MainActivityLocalGallery.this, R.color.system_blue));
                }

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

                private void setupEnterUrlButtons(ViewGroup enterUrlLayout) {
                        TextInputEditText tielInputUrl = enterUrlLayout.findViewById(R.id.tielInputUrl);
                        MaterialButton mbtClose = enterUrlLayout.findViewById(R.id.mbtClose);
                        MaterialButton mbtAccept = enterUrlLayout.findViewById(R.id.mbtAccept);

                        mbtClose.setOnClickListener(view -> {
                                hideEnterUrl();
                                showMenu();
                        });

                        mbtAccept.setOnClickListener(view -> {
                                String url = tielInputUrl.getText().toString().trim();

                                if (url.isEmpty()) {
                                        Toast.makeText(MainActivityLocalGallery.this, "Please enter a URL", Toast.LENGTH_SHORT).show();
                                        return;
                                }

                                OkHttpClient client = new OkHttpClient();

                                Request req = new Request.Builder()
                                        .url(url)
                                        .build();

                                client.newCall(req).enqueue(new Callback() {
                                        @Override
                                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                                runOnUiThread(() -> {
                                                        Toast.makeText(MainActivityLocalGallery.this,
                                                                "URL doesn't exist or network error",
                                                                Toast.LENGTH_SHORT).show();
                                                });
                                        }

                                        @Override
                                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                                if (!response.isSuccessful()) {
                                                        runOnUiThread(() -> {
                                                                Toast.makeText(MainActivityLocalGallery.this,
                                                                        "URL doesn't exist (Error: " + response.code() + ")",
                                                                        Toast.LENGTH_SHORT).show();
                                                        });
                                                        response.close();
                                                        return;
                                                }

                                                String contentType = response.header("Content-Type");

                                                if (contentType == null || !contentType.startsWith("image/")) {
                                                        runOnUiThread(() -> {
                                                                Toast.makeText(MainActivityLocalGallery.this,
                                                                        "URL doesn't contain an image",
                                                                        Toast.LENGTH_SHORT).show();
                                                        });
                                                        response.close();
                                                        return;
                                                }

                                                try {
                                                        byte[] imageBytes = response.body().bytes();
                                                        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

                                                        if (bitmap == null) {
                                                                runOnUiThread(() -> {
                                                                        Toast.makeText(getApplicationContext(),
                                                                                "Failed to decode image",
                                                                                Toast.LENGTH_SHORT).show();
                                                                });
                                                                return;
                                                        }

                                                        runOnUiThread(() -> {
                                                                // Use your bitmap here - example:
                                                                // imageView.setImageBitmap(bitmap);
                                                                // or store it in a variable
                                                                handleImageBitmap(bitmap);

                                                                Toast.makeText(getApplicationContext(),
                                                                        "Image loaded successfully",
                                                                        Toast.LENGTH_SHORT).show();
                                                        });

                                                } catch (Exception e) {
                                                        runOnUiThread(() -> {
                                                                Toast.makeText(getApplicationContext(),
                                                                        "Error processing image",
                                                                        Toast.LENGTH_SHORT).show();
                                                        });
                                                } finally {
                                                        response.close();
                                                }
                                        }
                                });

                                hideEnterUrl();
                        });
                }

                // Add this method to handle the bitmap once it's loaded
                private void handleImageBitmap(Bitmap bitmap) {
                        try {
                                String path = createTmpFolder(MainActivityLocalGallery.this, "tmpImageDir");
                                File file = new File(path + "/web.png");
                                FileOutputStream fos = new FileOutputStream(file);
                                bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);

                                mldPhotoUri.setValue(Uri.fromFile(file));



                        } catch (Exception e) {
                                throw new RuntimeException(e);
                        }


                        // Do whatever you need with the bitmap here
                        // For example: imageView.setImageBitmap(bitmap);
                }
                private void setupMenuButtons(ViewGroup menuLayout) {
                        // Close menu when clicking the background
                        menuLayout.setOnClickListener(v -> hideMenu());

                        MaterialButton mbtSelectPhoto = menuLayout.findViewById(R.id.mbtSelectPhoto);
                        MaterialButton mbtTakePhoto = menuLayout.findViewById(R.id.mbtTakePhoto);
                        MaterialButton mbtAddUrl = menuLayout.findViewById(R.id.mbtAddUrl);

                        mbtSelectPhoto.setOnClickListener(view -> {
                                pickPhotoLauncher.launch(
                                        new PickVisualMediaRequest.Builder()
                                                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                                                .build()
                                );
                                hideMenu();
                        });

                        mbtTakePhoto.setOnClickListener(view -> {
                                try {
                                        File tempFile = File.createTempFile("tmp_", ".jpg", new File(getCacheDir() + "/tmpImageDir"));
                                        uri = FileProvider.getUriForFile(
                                                MainActivityLocalGallery.this,
                                                getPackageName() + ".provider",
                                                tempFile
                                        );
                                        takePhoto.launch(uri);
                                        hideMenu();
                                } catch (Exception e) {
                                        throw new RuntimeException(e);
                                }
                        });

                        mbtAddUrl.setOnClickListener(v -> {
                                hideMenu();
                                showEnterUrl();
                        });
                }

                @Override
                public void onClick(View view) {
                        btMenu = MainActivityLocalGallery.this.findViewById(R.id.btMenu);

                        if (currentMenuLayout != null) {
                                hideMenu();
                        } else if (currentEnterUrlLayout != null) {
                                hideEnterUrl();
                        } else {
                                showMenu();
                        }
                }
        }
}
