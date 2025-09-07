package com.example.ascii_camera;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;

import java.io.OutputStream;

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

    private class OnSaveImageButtonClick implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            Bitmap out = avAscii.getAsciiAsBitmap();

            AlertDialog.Builder alert = new AlertDialog.Builder(view.getContext());
            alert.setMessage("Put the name of the file");
            alert.setTitle("File name");

            EditText etFileName = new EditText(view.getContext());
            alert.setView(etFileName);

            alert.setPositiveButton("OK", (dialog, which) -> {
                String fileName = etFileName.getText().toString();

                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, "ascii_" + fileName + ".png");
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
                ContentResolver resolver = getContentResolver();

                Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                try {
                    OutputStream outStream = resolver.openOutputStream(uri);
                    out.compress(Bitmap.CompressFormat.PNG, 100, outStream);
                } catch (Exception e) {

                }

                Intent intent = new Intent(AsciiSettingsActivity.this, MainActivity.class);
                startActivity(intent);
            });

            alert.setNegativeButton("Cancel", null);

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
