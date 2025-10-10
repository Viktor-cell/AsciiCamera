package com.example.ascii_camera;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.net.URL;

import okhttp3.*;
public class ServerUtils {
    private static final String SERVER_URL = "http://10.213.213.81:8080";

    public static void post(String json, String endpoint, Callback callback) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(SERVER_URL + "/" + endpoint)
                .post(RequestBody.create(json, MediaType.parse("application/json")))
                .build();

        client.newCall(request).enqueue(callback);
    }

    public static boolean isOnline() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(SERVER_URL)
                .build();

        Response response;
        try {
            response = client.newCall(request).execute();
        } catch (Exception e) {
            return false;
        }

        return response.isSuccessful();
    }
}
