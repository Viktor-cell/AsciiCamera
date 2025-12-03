package com.example.ascii_camera;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class AccountActivity extends AppCompatActivity {

        private TextView tvName;
        private MaterialButton btLogout;
        private ImageButton btBack;

        @Override
        protected void onCreate(@Nullable Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_account);
                initVars();

                tvName.setText(Utils.getStringFromPrefs("name", AccountActivity.this));

                btBack.setOnClickListener(view -> {
                        startActivity(new Intent(AccountActivity.this, MainActivityLocalGallery.class));
                });

                btLogout.setOnClickListener(view -> {
                        Utils.editStringInPrefs("name", Utils.LOGGED_OUT_USERNAME, AccountActivity.this);
                        startActivity(new Intent(AccountActivity.this, MainActivityLocalGallery.class));
                });
        }

        private void initVars() {
                tvName = findViewById(R.id.tvName);
                btLogout = findViewById(R.id.btLogout);
                btBack = findViewById(R.id.btBack);
        }
}
