package com.example.ascii_camera;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AsciiSettingsActivity extends AppCompatActivity {

    private Button btSettings;
    private Button btSaveImage;
    private EditText etCharset;
    private SeekBar sbFontSize;
    private SeekBar sbMinMag;
    private CheckBox chbMonochrome;
    private CheckBox chbEdges;
    private LinearLayout llSettingContainer;
    private TextView tvFontSize;
    private TextView tvMinMag;
    private Ascii ascii;
    private AsciiView avAscii;
    private MutableLiveData<Boolean> needsReset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_ascii);

        handleConnectionIndicatorColor();

        needsReset = new MutableLiveData<>(false);
        initVars();

        ascii = getIntent().getParcelableExtra("Ascii");
        assert ascii != null;
        ascii.initBmpIfNeeded(this);

        setupListeners();
        loadSettingsIntoViews();

        needsReset.observe(this, does -> {
            if (does) {
                ascii.setSettings(new AsciiSettings(
                        etCharset.getText().toString(),
                        sbFontSize.getProgress(),
                        chbMonochrome.isChecked(),
                        chbEdges.isChecked(),
                        sbMinMag.getProgress()
                ));
                presentAscii();
                needsReset.setValue(false);
            }
        });
    }

    private void initVars() {
        btSettings = findViewById(R.id.btSettings);
        btSaveImage = findViewById(R.id.btSaveImage);
        etCharset = findViewById(R.id.etCharset);
        sbFontSize = findViewById(R.id.sbFontSize);
        sbMinMag = findViewById(R.id.sbMinMag);
        chbMonochrome = findViewById(R.id.chbMonochrome);
        chbEdges = findViewById(R.id.chbEdges);
        llSettingContainer = findViewById(R.id.llSettingContainer);
        tvFontSize = findViewById(R.id.tvFontSize);
        tvMinMag = findViewById(R.id.tvMinMag);
        avAscii = findViewById(R.id.avAscii);
    }

    private void setupListeners() {
        btSettings.setOnClickListener(new OnSettingsButtonClick());
        btSaveImage.setOnClickListener(new OnSaveImageButtonClick());
        sbFontSize.setOnSeekBarChangeListener(new FontSizeTextAdjust());
        sbMinMag.setOnSeekBarChangeListener(new MinMagTextAdjust());
        chbEdges.setOnCheckedChangeListener(new OnCheckBoxCheckChange());
        etCharset.addTextChangedListener(new OnCharsetChange());
        chbMonochrome.setOnCheckedChangeListener(new OnCheckBoxCheckChange());
    }

    private void loadSettingsIntoViews() {
        etCharset.setText(ascii.getSettings().getCharset());
        sbFontSize.setProgress(ascii.getSettings().getFontSize());
        sbMinMag.setProgress(ascii.getSettings().getMinMag());
        chbEdges.setChecked(ascii.getSettings().isEdges());
        chbMonochrome.setChecked(ascii.getSettings().isMonochrome());
        tvFontSize.setText("Font size: " + sbFontSize.getProgress());
        tvMinMag.setText("Minimal magnitude: " + sbMinMag.getProgress());
    }

    private void presentAscii() {
        ascii.initBmpIfNeeded(this);
        ascii.generateColoredText();

        avAscii.setChcAscii(ascii.getChcArray());
        avAscii.redraw();
    }

    private void startMainActivity() {
        Intent intent = new Intent(AsciiSettingsActivity.this, MainActivity.class);
        startActivity(intent);
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

                // Repeat every 5 seconds
                handler.postDelayed(this, 500);
            }
        };

        handler.post(connectionCheckRunnable);
    }

    private class OnSaveImageButtonClick implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            Bitmap asciiAsBitmap = avAscii.getAsciiAsBitmap();
            CharactersColorsArray chcArray = avAscii.getChcAscii();

            AlertDialog.Builder alert = new AlertDialog.Builder(view.getContext())
                    .setCancelable(true)
                    .setMessage("Put the name of the file");

            EditText etFileName = new EditText(view.getContext());
            etFileName.setHint("File name");
            alert.setView(etFileName);

            alert.setNeutralButton("Save", null);
            alert.setPositiveButton("Send", null);
            alert.setNegativeButton("Send&Save", null);

            AlertDialog dialog = alert.create();

            dialog.setOnShowListener(dialogInterface -> {
                Button saveBtn = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
                Button sendBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                Button sendSaveBtn = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

                saveBtn.setOnClickListener(v -> {
                    String fileName = etFileName.getText().toString().trim();
                    if (fileName.isEmpty()) {
                        etFileName.setError("Can't be empty");
                        return;
                    }
                    saveLocaly(fileName, asciiAsBitmap);
                    startMainActivity();
                    dialog.dismiss();
                });

                sendBtn.setOnClickListener(v -> {
                    String artName = etFileName.getText().toString().trim();
                    if (artName.isEmpty()) {
                        etFileName.setError("Can't be empty");
                        return;
                    }

                    String author = Utils.getStringFromPrefs("name", AsciiSettingsActivity.this);
                    int width = chcArray.getWidth();
                    int height = chcArray.getHeight();
                    char[] rawLetters = chcArray.getCharacters();
                    int[] rawColors = chcArray.getColors();

                    sendToOnlineGallery(author, artName, width, height, rawLetters, rawColors);
                    dialog.dismiss();
                });

                sendSaveBtn.setOnClickListener(v -> {
                    String artName = etFileName.getText().toString().trim();
                    if (artName.isEmpty()) {
                        etFileName.setError("Can't be empty");
                        return;
                    }

                    String author = Utils.getStringFromPrefs("name", AsciiSettingsActivity.this);
                    int width = chcArray.getWidth();
                    int height = chcArray.getHeight();
                    char[] rawLetters = chcArray.getCharacters();
                    int[] rawColors = chcArray.getColors();

                    saveLocaly(artName, asciiAsBitmap);
                    sendToOnlineGallery(author, artName, width, height, rawLetters, rawColors);
                    dialog.dismiss();
                });
            });

            dialog.show();

        }

        private void sendToOnlineGallery(String author, String artName, int width, int heigth, char[] rawLetters, int[] rawColors) {

            JSONObject json = new JSONObject();

            JSONArray letters = new JSONArray();
            for (char letter : rawLetters) {
                letters.put(String.valueOf(letter));
            }

            JSONArray colors = new JSONArray();
            for (int color : rawColors) {
                colors.put(color);
            }


            try {
                json.put("author", author);
                json.put("artName", artName);
                json.put("width", width);
                json.put("height", heigth);
                json.put("letters", letters);
                json.put("colors", colors);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            ServerUtils.post(json.toString(), "add_image", new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    runOnUiThread(() -> {
                        Toast toast = new Toast(AsciiSettingsActivity.this);
                        toast.setText("Something went wrong ");
                        toast.show();
                    });
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        runOnUiThread(() -> {
                            Toast toast = new Toast(AsciiSettingsActivity.this);
                            toast.setText("Image send successfully");
                            toast.show();
                        });
                        startMainActivity();
                    }
                }
            });
        }

        private void saveLocaly(String fileName, Bitmap bmp) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, "ascii_" + fileName + ".png");
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
            ContentResolver resolver = getContentResolver();

            Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            try {
                OutputStream outStream = resolver.openOutputStream(uri);
                bmp.compress(Bitmap.CompressFormat.PNG, 75, outStream);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }
    }

    private class OnCharsetChange implements TextWatcher {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.toString().endsWith(" ") && s.length() != 1) {
                etCharset.setText(s.subSequence(0, s.length() - 1));
                etCharset.setSelection(s.length() - 1);
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.toString().isEmpty()) {
                etCharset.setText(" ");
            }
            needsReset.setValue(true);
        }
    }

    private class OnCheckBoxCheckChange implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            needsReset.setValue(true);
        }
    }

    private class OnSettingsButtonClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (llSettingContainer.getVisibility() == View.VISIBLE) {
                llSettingContainer.setVisibility(View.INVISIBLE);
            } else {
                llSettingContainer.setVisibility(View.VISIBLE);
            }
        }
    }

    private class FontSizeTextAdjust implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            tvFontSize.setText("Font size: " + progress);
            needsReset.setValue(true);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    }

    private class MinMagTextAdjust implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            tvMinMag.setText("Minimal magnitude: " + progress);
            needsReset.setValue(true);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    }
}
