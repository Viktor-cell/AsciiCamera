package com.example.ascii_camera;

import org.json.JSONObject;

import java.util.HashMap;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class WebsocetClient {

        public final String websocketUrl;
        private final OkHttpClient client = new OkHttpClient();
        private final HashMap<Integer, WebsocketCallback> pendingRequests = new HashMap<>();
        private Integer requestID = 0;
        private WebSocket ws;

        public WebsocetClient(String url) {
                websocketUrl = "ws://" + url + "/art/stream";
                start();
        }

        public void close() {
                if (ws != null) {
                        ws.close(1000, "Closing websocket");
                        ws = null;
                }
        }

        public void sendMessage(String msg, WebsocketCallback onReceive) {
                JSONObject json = new JSONObject();

                try {
                        json.put("id", requestID);
                } catch (Exception e) {
                        throw new RuntimeException(e);
                }

                String reqMsg = json.toString();
                pendingRequests.put(requestID, onReceive);
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
                Request req = new Request.Builder().url(websocketUrl).build();
                WebSocketListener wsl = new WebSocketListener() {
                        @Override
                        public void onOpen(WebSocket webSocket, Response response) {}

                        @Override
                        public void onMessage(WebSocket webSocket, String text) {

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
                        }

                        @Override
                        public void onClosing(WebSocket webSocket, int code, String reason) {
                                webSocket.close(1000, null);
                        }

                        @Override
                        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                        }
                };

                this.ws = client.newWebSocket(req, wsl);
        }

        public interface WebsocketCallback {
                void run(String msg);
        }
}