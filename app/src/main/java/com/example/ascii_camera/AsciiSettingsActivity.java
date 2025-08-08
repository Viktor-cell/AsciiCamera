package com.example.ascii_camera;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class AsciiSettingsActivity extends AppCompatActivity {

    // TODO: 8. 8. 2025 add minimum magnitude level 
    private Button btSettings;
    private EditText etCharset;
    private SeekBar sbFontSize;
    private CheckBox chbMonochrome;
    private CheckBox chbEdges;
    private LinearLayout llSettingContainer;
    private TextView tvFontSize;
    private Ascii ascii;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_ascii);
        initVars();

        ascii = (Ascii) getIntent().getExtras().get("Ascii");

        btSettings.setOnClickListener(new OnSettingButtonClick());
        sbFontSize.setOnSeekBarChangeListener(new FontSizeTextAdjust());
    }

    private void initVars() {
        btSettings = findViewById(R.id.btSettings);
        etCharset = findViewById(R.id.etCharset);
        sbFontSize = findViewById(R.id.sbFontSize);
        chbMonochrome = findViewById(R.id.chbMonochrome);
        chbEdges = findViewById(R.id.chbEdges);
        llSettingContainer = findViewById(R.id.llSettingContainer);
        tvFontSize = findViewById(R.id.tvFontSize);

    }

    class OnSettingButtonClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (llSettingContainer.getVisibility()) {
                case View.VISIBLE:
                    llSettingContainer.setVisibility(View.INVISIBLE);
                    break;
                case View.INVISIBLE:
                    llSettingContainer.setVisibility(View.VISIBLE);
                    break;
            }
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
}