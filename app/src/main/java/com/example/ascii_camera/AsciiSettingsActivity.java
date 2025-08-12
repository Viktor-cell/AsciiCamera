package com.example.ascii_camera;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class AsciiSettingsActivity extends AppCompatActivity {

    private Button btSettings;
    private Button btSaveSettings;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_ascii);

        initVars();

        ascii = getIntent().getParcelableExtra("Ascii");
        Log.d("ascii_settings", ascii.toString());

        setupListeners();
        loadSettingsIntoViews();
        presentAscii();
    }

    private void initVars() {
        btSettings = findViewById(R.id.btSettings);
        btSaveSettings = findViewById(R.id.btSaveSettings);
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
        btSaveSettings.setOnClickListener(new OnSaveSettingsButtonClick());
        sbFontSize.setOnSeekBarChangeListener(new FontSizeTextAdjust());
        sbMinMag.setOnSeekBarChangeListener(new MinMagTextAdjust());
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
        ascii.initBmps(this);
        ascii.generateColoredText();
        avAscii.setChcAscii(ascii.getChcArray());
        avAscii.redraw();
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

    private class OnSaveSettingsButtonClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            llSettingContainer.setVisibility(View.INVISIBLE);

            ascii.setSettings(new AsciiSettings(
                    etCharset.getText().toString(),
                    sbFontSize.getProgress(),
                    chbMonochrome.isChecked(),
                    chbEdges.isChecked(),
                    sbMinMag.getProgress()
            ));
            Log.d("ascii_settings", ascii.toString());
            presentAscii();
        }
    }

    private class FontSizeTextAdjust implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            tvFontSize.setText("Font size: " + progress);
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
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    }
}
