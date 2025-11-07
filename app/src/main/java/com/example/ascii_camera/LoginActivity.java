package com.example.ascii_camera;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {
    Button btLogin;
    Button btSignUp;
    EditText etName;
    EditText etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (!Utils.getStringFromPrefs("name", this).trim().isEmpty() && !Utils.getStringFromPrefs("name", this).equals(Utils.LOGGED_OUT_USERNAME)) {
            Intent it = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(it);
        }

        handleConnectionIndicatorColor();
        initVars();

        btLogin.setOnClickListener(new HandleLogin());
        btSignUp.setOnClickListener(new HandleSingUp());

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

                handler.postDelayed(this, 500);
            }
        };

        handler.post(connectionCheckRunnable);
    }

    private void initVars() {
        btLogin = findViewById(R.id.btLogin);
        btSignUp = findViewById(R.id.btSignUp);
        etName = findViewById(R.id.etName);
        etPassword = findViewById(R.id.etPassword);
    }

    private boolean checkNameAndPassword() {
        boolean isNameEmpty = etName.getText().toString().trim().isEmpty();
        boolean isPasswordEmpty = etPassword.getText().toString().trim().isEmpty();

        if (isNameEmpty) {
            etName.setError("Cant be empty");
        }
        if (isPasswordEmpty) {
            etPassword.setError("Cant be empty");
        }
        return isNameEmpty || isPasswordEmpty;
    }

    private JSONObject createJsonFromEditTexts() {
        String name = etName.getText().toString().trim();
        String passwordHash = Utils.hash(etPassword.getText().toString().trim());

        JSONObject json = new JSONObject();

        try {
            json.put("name", name);
            json.put("password_hash", passwordHash);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return json;
    }

    private class HandleSingUp implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            if (checkNameAndPassword()) {
                return;
            }

            JSONObject json = createJsonFromEditTexts();
            ServerUtils.post(json.toString(), "sign_up", new SignInCallback());

        }
    }

    private class HandleLogin implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            if (checkNameAndPassword()) {
                return;
            }

            JSONObject json = createJsonFromEditTexts();
            ServerUtils.post(json.toString(), "log_in", new LoginCallback());
        }
    }

    private class SignInCallback implements Callback {
        @Override
        public void onFailure(@NonNull Call call, @NonNull IOException e) {
            runOnUiThread(() -> {
                Toast toast = new Toast(LoginActivity.this);
                toast.setText("Something went wrong ");
                toast.show();
            });
        }

        @Override
        public void onResponse(@NonNull Call call, @NonNull Response response)  {
            int code = response.code();

            if (code != 200) {
                runOnUiThread(() -> {
                    try {
                        String body = response.body().string();
                        etName.setError(body);
                        etPassword.setError(body);
                    }catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                });

                return;
            }

            String name = etName.getText().toString().trim();
            Utils.addStringToPrefs("name", name,LoginActivity.this);

            Intent it = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(it);
        }

    }

    private class LoginCallback implements Callback {
        @Override
        public void onFailure(@NonNull Call call, @NonNull IOException e) {
            runOnUiThread(() -> {
                Toast toast = new Toast(LoginActivity.this);
                toast.setText("Something went wrong");
                toast.show();
            });
        }

        @Override
        public void onResponse(@NonNull Call call, @NonNull Response response) {
            int code = response.code();

            if (code != 200) {
                runOnUiThread(() -> {
                    try {
                        etName.setError(response.body().string());
                    }catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                });

                return;
            }

            String name = etName.getText().toString().trim();
            Utils.addStringToPrefs("name", name,LoginActivity.this);

            Intent it = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(it);
        }
    }
}