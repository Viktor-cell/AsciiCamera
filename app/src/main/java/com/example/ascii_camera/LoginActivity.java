package com.example.ascii_camera;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

        private MaterialButton btLogin;
        private MaterialButton btSignUp;
        private ImageButton btBack;
        private TextInputEditText etName;
        private TextInputEditText etPassword;
        private TextInputLayout tilName;
        private TextInputLayout tilPassword;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_login);

                initVars();

                btLogin.setOnClickListener(view -> {
                        String name = etName.getText().toString().trim();
                        String password = etPassword.getText().toString().trim();

                        tilName.setError(null);
                        tilPassword.setError(null);

                        if (name.isEmpty()) {
                                tilName.setError("Can't be empty");
                        }
                        if (password.isEmpty()) {
                                tilPassword.setError("Can't be empty");
                        }

                        if (name.isEmpty() || password.isEmpty()) {
                                return;
                        }

                        JSONObject json = new JSONObject();
                        try {
                                json.put("name", name);
                                json.put("password", password);
                        } catch (Exception e) {
                                throw new RuntimeException(e);
                        }

                        ServerUtils.post(json.toString(), "auth/login", new LoginCallback());
                });

                btSignUp.setOnClickListener(view -> {
                        startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
                });

                btBack.setOnClickListener(view -> {
                        startActivity(new Intent(LoginActivity.this, MainActivityGlobalGallery.class));
                });
        }

        private void initVars() {
                btLogin = findViewById(R.id.btLogin);
                btSignUp = findViewById(R.id.btSignUp);
                btBack = findViewById(R.id.btBack);
                etName = findViewById(R.id.etName);
                etPassword = findViewById(R.id.etPassword);
                tilName = findViewById(R.id.tilName);
                tilPassword = findViewById(R.id.tilPassword);
        }

        private class LoginCallback implements Callback {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                        int code = response.code();

                        if (code != 200) {
                                runOnUiThread(() -> {
                                        try {
                                                String errorMsg = new JSONObject(response.body().string()).getString("error");
                                                tilName.setError(errorMsg);
                                                tilPassword.setError(errorMsg);
                                        } catch (Exception e) {
                                                throw new RuntimeException(e);
                                        }
                                });
                                return;
                        }

                        String name = etName.getText().toString().trim();
                        Utils.addStringToPrefs("name", name, LoginActivity.this);


                        startActivity(new Intent(LoginActivity.this, MainActivityGlobalGallery.class));
                }
        }
}
