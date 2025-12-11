package com.example.ascii_camera;

import android.util.Log;

import org.json.JSONObject;

import java.util.HashMap;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class WebsocetClient {

        public static final String WEBSOCET_URL = "ws://" + ServerUtils.SERVER_SOCKET + "/art/stream";
        private static final String TAG = "WebSocketClient";
        private final OkHttpClient client = new OkHttpClient();
        private final HashMap<Integer, WebsocketCallback> pendingRequests = new HashMap<>();
        private Integer requestID = 0;
        private WebSocket ws;

        public WebsocetClient() {
                start();
        }

        public void sendMessage(String msg, WebsocketCallback onRecieve) {
                JSONObject json = new JSONObject();

                try {
                        json.put("id", requestID);
                } catch (Exception e) {
                        throw new RuntimeException(e);
                }

                String reqMsg = json.toString();
                pendingRequests.put(requestID, onRecieve);
                requestID += 1;
                ws.send(reqMsg);
        }

        public void sendMessage(JSONObject json, WebsocketCallback onRecieve) {
                try {
                        json.put("id", requestID);
                } catch (Exception e) {
                        throw new RuntimeException(e);
                }

                String reqMsg = json.toString();
                pendingRequests.put(requestID, onRecieve);
                requestID += 1;
                ws.send(reqMsg);
        }

        public void start() {
                Request req = new Request.Builder().url(WEBSOCET_URL).build();
                WebSocketListener wsl = new WebSocketListener() {
                        @Override
                        public void onOpen(WebSocket webSocket, Response response) {

                                Log.d(TAG, "WebSocket opened");
                        }

                        @Override
                        public void onMessage(WebSocket webSocket, String text) {
                                Log.d(TAG, "Received text: " + text);

                                try {
                                        JSONObject json = new JSONObject(text);

                                        int id = json.getInt("id");
                                        String msg = json.getString("msg");
                                        pendingRequests.get(id).run(msg);

                                } catch (Exception e) {
                                        throw new RuntimeException(e);
                                }
                        }

                        @Override
                        public void onMessage(WebSocket webSocket, ByteString bytes) {
                                Log.d(TAG, "Received bytes: " + bytes.hex());
                        }

                        @Override
                        public void onClosing(WebSocket webSocket, int code, String reason) {
                                Log.d(TAG, "Closing: " + reason);
                                webSocket.close(1000, null);
                        }

                        @Override
                        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                                Log.e(TAG, "WebSocket error", t);
                        }
                };

                this.ws = client.newWebSocket(req, wsl);
                //client.dispatcher().executorService().shutdown();
        }

        public interface WebsocketCallback {
                void run(String msg);
        }
}