package com.example.ascii_camera;

import android.util.Log;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ServerUtils {

        public static final String SERVER_SOCKET = "10.23.175.81:8080";
        public static final String SERVER_URL = "http://" + SERVER_SOCKET;
        private static final OkHttpClient client = new OkHttpClient.Builder()
                .callTimeout(10, TimeUnit.SECONDS)
                .build();

        public static void post(String json, String endpoint, Callback callback) {
                Request request = new Request.Builder()
                        .url(SERVER_URL + "/" + endpoint)
                        .post(RequestBody.create(json, MediaType.parse("application/json")))
                        .build();

                client.newCall(request).enqueue(callback);
        }


        public static boolean isOnline() {
                Request request = new Request.Builder()
                        .url(SERVER_URL)
                        .head() // just check headers, no body
                        .build();

                try (Response response = client.newCall(request).execute()) {
                        Log.d("ServerUtils", "Response code: " + response.code());
                        return response.isSuccessful();
                } catch (IOException e) {
                        Log.e("ServerUtils", "isOnline error", e);
                        return false;
                }
        }

        public static void isOnlineAsync(Callback callback) {
                Request request = new Request.Builder()
                        .url(SERVER_URL)
                        .head()
                        .build();

                client.newCall(request).enqueue(callback);
        }
}
