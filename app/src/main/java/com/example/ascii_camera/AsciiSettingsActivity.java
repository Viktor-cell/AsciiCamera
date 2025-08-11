package com.example.ascii_camera;

import android.os.AsyncTask;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_ascii);
        initVars();

        ascii = getIntent().getParcelableExtra("Ascii");
        assert ascii != null;
        Log.d("ascii_settings",ascii.toString());

        btSettings.setOnClickListener(new OnSettingsButtonClick());
        sbFontSize.setOnSeekBarChangeListener(new FontSizeTextAdjust());
        sbMinMag.setOnSeekBarChangeListener(new MinMagTextAdjust());
        btSaveSettings.setOnClickListener(new OnSaveSettingsButtonClick());

        etCharset.setText(ascii.getSettings().getCharset());
        sbFontSize.setProgress(ascii.getSettings().getFontSize());
        sbMinMag.setProgress(ascii.getSettings().getMinMag());
        chbEdges.setChecked(ascii.getSettings().isEdges());
        chbMonochrome.setChecked(ascii.getSettings().isMonochrome());

        // TODO: 11. 8. 2025 make rendering using canvas
    }

    private void initVars() {
        btSettings = findViewById(R.id.btSettings);
        etCharset = findViewById(R.id.etCharset);
        sbFontSize = findViewById(R.id.sbFontSize);
        chbMonochrome = findViewById(R.id.chbMonochrome);
        chbEdges = findViewById(R.id.chbEdges);
        llSettingContainer = findViewById(R.id.llSettingContainer);
        tvFontSize = findViewById(R.id.tvFontSize);
        sbMinMag = findViewById(R.id.sbMinMag);
        tvMinMag = findViewById(R.id.tvMinMag);
        btSaveSettings =  findViewById(R.id.btSaveSettings);
    }

    class OnSettingsButtonClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (llSettingContainer.getVisibility()) {
                case View.VISIBLE:
                    llSettingContainer.setVisibility(View.INVISIBLE);
                    break;
                case View.INVISIBLE:
                    llSettingContainer.setVisibility(View.VISIBLE);
                    break;
                default:

            }
        }
    }

    class OnSaveSettingsButtonClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            ascii.getSettings().setAll(etCharset.getText().toString(),
                    sbFontSize.getProgress(),
                    chbMonochrome.isChecked(),
                    chbEdges.isChecked(),
                    sbMinMag.getProgress());

            llSettingContainer.setVisibility(View.INVISIBLE);
            Log.d("ascii_settings",ascii.toString());
        }

    }

    class FontSizeTextAdjust implements SeekBar.OnSeekBarChangeListener{
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            tvFontSize.setText( "Font size: " + progress );
        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {}
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {}
    }
    class MinMagTextAdjust implements SeekBar.OnSeekBarChangeListener{
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            tvMinMag.setText( "Minimal magnitude: " + progress );
        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {}
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {}
    }
}