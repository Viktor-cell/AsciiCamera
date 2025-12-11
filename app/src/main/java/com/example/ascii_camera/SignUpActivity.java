package com.example.ascii_camera;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class SignUpActivity extends AppCompatActivity {

        private MaterialButton btSignUp;
        private ImageButton btBack;
        private TextInputEditText etName;
        private TextInputEditText etPassword;
        private TextInputEditText etConfirmPassword;
        private TextInputLayout tilName;
        private TextInputLayout tilPassword;
        private TextInputLayout tilConfirmPassword;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_sign_up);
                initVars();

                btSignUp.setOnClickListener(view -> {
                        String name = etName.getText().toString().trim();
                        String password = etPassword.getText().toString().trim();
                        String confirmPassword = etConfirmPassword.getText().toString().trim();

                        tilName.setError(null);
                        tilPassword.setError(null);
                        tilConfirmPassword.setError(null);

                        if (name.isEmpty()) {
                                tilName.setError("Can't be empty");
                        }
                        if (password.isEmpty()) {
                                tilPassword.setError("Can't be empty");
                        }
                        if (confirmPassword.isEmpty()) {
                                tilConfirmPassword.setError("Can't be empty");
                        }
                        if (!password.equals(confirmPassword)) {
                                tilConfirmPassword.setError("Doesn't match password");
                        }

                        if (name.isEmpty() || password.isEmpty() || !password.equals(confirmPassword)) {
                                return;
                        }

                        JSONObject json = new JSONObject();
                        try {
                                json.put("name", name);
                                json.put("password", password);
                        } catch (Exception e) {
                                throw new RuntimeException(e);
                        }

                        ServerUtils.post(json.toString(), "auth/signup", new SignUpCallback());
                });

                btBack.setOnClickListener(view -> {
                        startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                });
        }

        private void initVars() {
                btSignUp = findViewById(R.id.btSignUp);
                btBack = findViewById(R.id.btBack);
                etName = findViewById(R.id.etName);
                etPassword = findViewById(R.id.etPassword);
                etConfirmPassword = findViewById(R.id.etConfirmPassword);
                tilName = findViewById(R.id.tilName);
                tilPassword = findViewById(R.id.tilPassword);
                tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        }

        private class SignUpCallback implements Callback {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        runOnUiThread(() -> Toast.makeText(SignUpActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                        int code = response.code();

                        if (code != 200) {
                                runOnUiThread(() -> {
                                        try {
                                                String errorMsg = new JSONObject(response.body().string()).getString("error");
                                                tilName.setError(errorMsg);
                                        } catch (Exception e) {
                                                throw new RuntimeException(e);
                                        }
                                });
                                return;
                        }

                        String name = etName.getText().toString().trim();
                        Utils.addStringToPrefs("name", name, SignUpActivity.this);
                        startActivity(new Intent(SignUpActivity.this, MainActivityGlobalGallery.class));
                }
        }
}
