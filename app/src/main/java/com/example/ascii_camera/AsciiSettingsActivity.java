package com.example.ascii_camera;

import android.content.Intent;
import android.graphics.Bitmap;

import androidx.core.content.ContextCompat;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AsciiSettingsActivity extends AppCompatActivity {

        private FloatingActionButton btSettings;
        private MaterialButton btSaveImage;
        private TextInputEditText etCharset;
        private SeekBar sbFontSize;
        private SeekBar sbMinMag;
        private SwitchMaterial swMonochrome;
        private SwitchMaterial swInverted;
        private SwitchMaterial swJustLetters;
        private SwitchMaterial swbEdges;
        private MaterialCardView mcvSettingContainer;
        private TextView tvFontSize;
        private TextView tvMinMag;
        private AsciiCreator asciiCreator;
        private AsciiView avAscii;
        private MutableLiveData<Boolean> needsReset;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_settings_ascii);

                needsReset = new MutableLiveData<>(false);
                initVars();

                asciiCreator = getIntent().getParcelableExtra("Ascii");
                assert asciiCreator != null;
                asciiCreator.createBitmapsIfNeeded(this);

                setupListeners();
                loadSettingsIntoViews();

                findViewById(R.id.btSaveImage).setOnClickListener(new OnSaveImageButtonClick());

                MaterialButton btUploadImage = findViewById(R.id.btUploadImage);
                if (Utils.getStringFromPrefs("name", this).equals(Utils.LOGGED_OUT_USERNAME)) {
                        btUploadImage.setEnabled(false);
                        Toast.makeText(this, "To upload, you need to be logged in", Toast.LENGTH_LONG).show();
                } else {
                        findViewById(R.id.btUploadImage).setOnClickListener(new OnUploadButtonClick());
                }

                findViewById(R.id.btReturn).setOnClickListener(new OnReturnButtonClick());

                needsReset.observe(this, does -> {
                        if (does) {
                                asciiCreator.setSettings(new AsciiSettings(
                                        etCharset.getText().toString(),
                                        sbFontSize.getProgress(),
                                        swMonochrome.isChecked(),
                                        swInverted.isChecked(),
                                        swJustLetters.isChecked(),
                                        swbEdges.isChecked(),
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
                swMonochrome = findViewById(R.id.swMonochrome);
                swInverted = findViewById(R.id.swInverted);
                swJustLetters = findViewById(R.id.swJustLetters);
                swbEdges = findViewById(R.id.swEdges);
                mcvSettingContainer = findViewById(R.id.llSettingContainer);
                tvFontSize = findViewById(R.id.tvFontSize);
                tvMinMag = findViewById(R.id.tvMinMag);
                avAscii = findViewById(R.id.avAscii);
        }

        private void setupListeners() {
                btSettings.setOnClickListener(new OnSettingsButtonClick());
                btSaveImage.setOnClickListener(new OnSaveImageButtonClick());
                sbFontSize.setOnSeekBarChangeListener(new FontSizeTextAdjust());
                sbMinMag.setOnSeekBarChangeListener(new MinMagTextAdjust());
                swbEdges.setOnCheckedChangeListener(new OnCheckBoxCheckChange());
                etCharset.addTextChangedListener(new OnCharsetChange());
                swMonochrome.setOnCheckedChangeListener(new OnCheckBoxCheckChange());
                swInverted.setOnCheckedChangeListener(new OnCheckBoxCheckChange());
                swJustLetters.setOnCheckedChangeListener(new OnCheckBoxCheckChange());
        }

        private void loadSettingsIntoViews() {
                etCharset.setText(asciiCreator.getSettings().getCharset());
                sbFontSize.setProgress(asciiCreator.getSettings().getFontSize());
                sbMinMag.setProgress(asciiCreator.getSettings().getMinMag());
                swbEdges.setChecked(asciiCreator.getSettings().isEdges());
                swMonochrome.setChecked(asciiCreator.getSettings().isMonochrome());
                swInverted.setChecked(asciiCreator.getSettings().isInverted());
                swJustLetters.setChecked(asciiCreator.getSettings().isJustLetters());
                tvFontSize.setText("Font size: " + sbFontSize.getProgress());
                tvMinMag.setText("Minimal magnitude: " + sbMinMag.getProgress());
        }

        private void presentAscii() {
                asciiCreator.generateColoredText(this);
                avAscii.setChcAscii(asciiCreator.getChcArray());
                avAscii.redraw();
        }

        private class OnUploadButtonClick implements View.OnClickListener {
                @Override
                public void onClick(View view) {
                        CharactersColorsArray chcArray = avAscii.getChcAscii();
                        TextInputEditText etFileName = new TextInputEditText(AsciiSettingsActivity.this);
                        etFileName.setHint("Art name");
                        etFileName.setHintTextColor(ContextCompat.getColor(AsciiSettingsActivity.this, R.color.gray));

                        AlertDialog alert = new AlertDialog.Builder(AsciiSettingsActivity.this)
                                .setMessage("Put the name of an art")
                                .setCancelable(true)
                                .setView(etFileName)
                                .setPositiveButton("OK", (dialog, which) -> {

                                        String artName = etFileName.getText().toString().trim();

                                        if (artName.isEmpty()) {
                                                Toast.makeText(AsciiSettingsActivity.this, "Filename cant be empty", Toast.LENGTH_SHORT).show();
                                                return;
                                        }
                                        ServerUtils.isOnlineAsync(new Callback() {
                                                @Override
                                                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                                        runOnUiThread(() -> {
                                                                Toast.makeText(AsciiSettingsActivity.this, "No internet connection", Toast.LENGTH_SHORT).show();
                                                        });
                                                }

                                                @Override
                                                public void onResponse(@NonNull Call call, @NonNull Response response) {
                                                        runOnUiThread(() -> {

                                                                FullAscii fullAscii = new FullAscii(
                                                                        Utils.getStringFromPrefs("name", AsciiSettingsActivity.this),
                                                                        artName,
                                                                        chcArray.getWidth(),
                                                                        chcArray.getHeight(),
                                                                        chcArray.getColors(),
                                                                        chcArray.getCharacters()
                                                                );

                                                                Utils.sendAsciiToOnlineGallery(
                                                                        fullAscii,
                                                                        AsciiSettingsActivity.this,
                                                                        AsciiSettingsActivity.this
                                                                );

                                                                Toast.makeText(AsciiSettingsActivity.this, "Image send successfully", Toast.LENGTH_SHORT).show();
                                                        });
                                                }
                                        });

                                })
                                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                                .create();

                        alert.show();
                }
        }

        private class OnSaveImageButtonClick implements View.OnClickListener {
                @Override
                public void onClick(View view) {
                        Bitmap asciiAsBitmap = avAscii.getAsciiAsBitmap(true);
                        TextInputEditText etFileName = new TextInputEditText(AsciiSettingsActivity.this);
                        etFileName.setHint("Art name");
                        etFileName.setHintTextColor(ContextCompat.getColor(AsciiSettingsActivity.this, R.color.gray));

                        AlertDialog alert = new AlertDialog.Builder(AsciiSettingsActivity.this)
                                .setMessage("Put the name of an art")
                                .setCancelable(true)
                                .setView(etFileName)
                                .setPositiveButton("OK", (dialog, which) -> {

                                        String artName = etFileName.getText().toString().trim();

                                        if (artName.isEmpty()) {
                                                Toast.makeText(AsciiSettingsActivity.this, "Filename cant be empty", Toast.LENGTH_SHORT).show();
                                                return;
                                        }
                                        Utils.saveLocaly(artName, asciiAsBitmap, AsciiSettingsActivity.this);
                                        Toast.makeText(AsciiSettingsActivity.this, "File saved successfully", Toast.LENGTH_SHORT).show();

                                })
                                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                                .create();

                        alert.show();
                }

        }

        private class OnReturnButtonClick implements View.OnClickListener {
                @Override
                public void onClick(View view) {
                        startActivity(new Intent(AsciiSettingsActivity.this, MainActivityLocalGallery.class));
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
                        if (mcvSettingContainer.getVisibility() == View.VISIBLE) {
                                mcvSettingContainer.setVisibility(View.INVISIBLE);
                        } else {
                                mcvSettingContainer.setVisibility(View.VISIBLE);
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
