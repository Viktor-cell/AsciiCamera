package com.example.ascii_camera;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.*;
public class ServerUtils {
    private static String URL = "http://10.213.213.81:8080";

    public static void post(String json, String endpoint, Callback callback) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(URL + "/" + endpoint)
                .post(RequestBody.create(json, MediaType.parse("application/json")))
                .build();

        client.newCall(request).enqueue(callback);
    }
}
