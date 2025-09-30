package com.example.ascii_camera;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

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
        Utils.removeStringToPrefs("name", this);

        Log.d("USER_", Utils.getStringFromPrefs("name", this).trim());

        if (!Utils.getStringFromPrefs("name", this).trim().isEmpty()) {
            Intent it = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(it);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initVars();

        btLogin.setOnClickListener(new HandleLogin());
        btSignUp.setOnClickListener(new HandleSingIn());

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

        Log.d("USER_", "pswd hash: " + passwordHash);

        //TODO add server connection
        JSONObject json = new JSONObject();

        try {
            json.put("name", name);
            json.put("password_hash", passwordHash);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return json;
    }

    private class HandleSingIn implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            if (checkNameAndPassword()) {
                return;
            }

            JSONObject json = createJsonFromEditTexts();
            ServerUtils.post(json.toString(), "sign_in", new SignInCallback());

        }
    }

    private class HandleLogin implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            if (checkNameAndPassword()) {
                return;
            }

            JSONObject json = createJsonFromEditTexts();
            ServerUtils.post(json.toString(), "login", new LoginCallback());
        }
    }

    private class SignInCallback implements Callback {
        @Override
        public void onFailure(@NonNull Call call, @NonNull IOException e) {
            throw new RuntimeException(e);
        }

        @Override
        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
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
            throw new RuntimeException(e);
        }

        @Override
        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
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