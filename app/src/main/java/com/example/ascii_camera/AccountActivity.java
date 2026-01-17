package com.example.ascii_camera;

import static com.example.ascii_camera.Utils.showGlobalGallery;

import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import org.json.JSONObject;

import java.util.Map;

public class AccountActivity extends AppCompatActivity {

        private TextView tvName;
        private MaterialButton btLogout;
        private ImageButton btBack;
        private WebsocetClient client;
        private FrameLayout flGallery;

        @Override
        protected void onCreate(@Nullable Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_account);
                initVars();

                client = new WebsocetClient(ServerUtils.SERVER_SOCKET);

                tvName.setText(Utils.getStringFromPrefs("name", AccountActivity.this));

                btBack.setOnClickListener(view -> {
                        client.close();
                        startActivity(new Intent(AccountActivity.this, MainActivityGlobalGallery.class));
                });

                btLogout.setOnClickListener(view -> {
                        Utils.editStringInPrefs("name", Utils.LOGGED_OUT_USERNAME, AccountActivity.this);
                        startActivity(new Intent(AccountActivity.this, MainActivityGlobalGallery.class));
                });

                FrameLayout layout = findViewById(R.id.flGallery);

                Utils.showGlobalGallery(layout, client, this, new JSONObject(Map.of(
                        "count", 8,
                        "author", Utils.getStringFromPrefs("name", AccountActivity.this),
                        "artname", ""
                )));
        }

        private void initVars() {
                tvName = findViewById(R.id.tvName);
                btLogout = findViewById(R.id.btLogout);
                btBack = findViewById(R.id.btBack);
                flGallery = findViewById(R.id.flGallery);
        }
}
