package com.example.ascii_camera;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

import java.io.IOException;

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
        private AsciiCreator asciiCreator;
        private AsciiView avAscii;
        private MutableLiveData<Boolean> needsReset;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_settings_ascii);

                handleConnectionIndicatorColor();

                needsReset = new MutableLiveData<>(false);
                initVars();

                asciiCreator = getIntent().getParcelableExtra("Ascii");
                assert asciiCreator != null;
                asciiCreator.initBmpIfNeeded(this);

                setupListeners();
                loadSettingsIntoViews();

                needsReset.observe(this, does -> {
                        if (does) {
                                asciiCreator.setSettings(new AsciiSettings(
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
                etCharset.setText(asciiCreator.getSettings().getCharset());
                sbFontSize.setProgress(asciiCreator.getSettings().getFontSize());
                sbMinMag.setProgress(asciiCreator.getSettings().getMinMag());
                chbEdges.setChecked(asciiCreator.getSettings().isEdges());
                chbMonochrome.setChecked(asciiCreator.getSettings().isMonochrome());
                tvFontSize.setText("Font size: " + sbFontSize.getProgress());
                tvMinMag.setText("Minimal magnitude: " + sbMinMag.getProgress());
        }

        private void presentAscii() {
                asciiCreator.initBmpIfNeeded(this);
                asciiCreator.generateColoredText();

                avAscii.setChcAscii(asciiCreator.getChcArray());
                avAscii.redraw();
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
                                handler.postDelayed(this, 5000);
                        }
                };

                handler.post(connectionCheckRunnable);
        }

        private class OnSaveImageButtonClick implements View.OnClickListener {
                @Override
                public void onClick(View view) {
                        Bitmap asciiAsBitmap = avAscii.getAsciiAsBitmap(true);
                        CharactersColorsArray chcArray = avAscii.getChcAscii();

                        View customDialog = getLayoutInflater().inflate(R.layout.asci_settings_dialog, null);
                        AlertDialog alert = new AlertDialog.Builder(AsciiSettingsActivity.this)
                                .setMessage("Put the name of a art")
                                .setCancelable(true)
                                .setView(customDialog)
                                .create();

                        EditText etFileName = customDialog.findViewById(R.id.etFileName);
                        Button save = customDialog.findViewById(R.id.btSave);
                        Button send = customDialog.findViewById(R.id.btSend);
                        Button sendAndSave = customDialog.findViewById(R.id.btSendAndSave);

                        save.setOnClickListener(v -> {
                                String fileName = etFileName.getText().toString().trim();
                                if (fileName.isEmpty()) {
                                        etFileName.setError("Can't be empty");
                                        return;
                                }
                                Utils.saveLocaly(fileName, asciiAsBitmap, AsciiSettingsActivity.this);

                                Intent intent = new Intent(AsciiSettingsActivity.this, MainActivity.class);
                                startActivity(intent);

                                alert.dismiss();
                        });

                        send.setOnClickListener(v -> {
                                ServerUtils.isOnlineAsync(new Callback() {
                                        @Override
                                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                                runOnUiThread(() -> {
                                                        Toast t = new Toast(customDialog.getContext());
                                                        t.setText("No internet connection");
                                                        t.show();
                                                });
                                        }

                                        @Override
                                        public void onResponse(@NonNull Call call, @NonNull Response response) {
                                                runOnUiThread(() -> {
                                                        String artName = etFileName.getText().toString().trim();
                                                        if (artName.isEmpty()) {
                                                                etFileName.setError("Can't be empty");
                                                                return;
                                                        }

                                                        FullAscii fullAscii = new FullAscii(
                                                                Utils.getStringFromPrefs("name", AsciiSettingsActivity.this),
                                                                artName,
                                                                chcArray.getWidth(),
                                                                chcArray.getHeight(),
                                                                chcArray.getColors(),
                                                                chcArray.getCharacters()
                                                        );

                                                        Utils.sendAsciiToOnlineGallery(fullAscii, AsciiSettingsActivity.this, AsciiSettingsActivity.this);
                                                        alert.dismiss();
                                                });
                                        }
                                });


                        });

                        sendAndSave.setOnClickListener(v -> {
                                ServerUtils.isOnlineAsync(new Callback() {
                                        @Override
                                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                                runOnUiThread(() -> {
                                                        Toast t = new Toast(customDialog.getContext());
                                                        t.setText("No internet connection");
                                                        t.show();
                                                });
                                        }

                                        @Override
                                        public void onResponse(@NonNull Call call, @NonNull Response response) {
                                                runOnUiThread(() -> {
                                                        String artName = etFileName.getText().toString().trim();
                                                        if (artName.isEmpty()) {
                                                                etFileName.setError("Can't be empty");
                                                                return;
                                                        }

                                                        FullAscii fullAscii = new FullAscii(
                                                                Utils.getStringFromPrefs("name", AsciiSettingsActivity.this),
                                                                artName,
                                                                chcArray.getWidth(),
                                                                chcArray.getHeight(),
                                                                chcArray.getColors(),
                                                                chcArray.getCharacters()
                                                        );

                                                        Utils.sendAsciiToOnlineGallery(fullAscii, AsciiSettingsActivity.this, AsciiSettingsActivity.this);
                                                        Utils.saveLocaly(artName, asciiAsBitmap, AsciiSettingsActivity.this);
                                                        alert.dismiss();
                                                });
                                        }
                                });

                        });

                        alert.show();
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
