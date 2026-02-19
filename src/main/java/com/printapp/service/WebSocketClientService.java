package com.printapp.service;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.Timer;
import java.util.TimerTask;

/**
 * WebSocket client that connects to a Spring Boot STOMP WebSocket endpoint.
 * Subscribes to /topic/print-config and triggers a callback when
 * "NEW_PRINT_CONFIG" message is received.
 *
 * Features:
 * - Auto-reconnect on disconnect (every 5 seconds)
 * - STOMP protocol framing over raw WebSocket
 * - Thread-safe (caller must use Platform.runLater for UI updates)
 */
public class WebSocketClientService {

    private static final String WS_URL = "ws://print-test-env-env.eba-9gvrcrjp.us-east-1.elasticbeanstalk.com/ws/print-events/websocket";
    private static final String STOMP_TOPIC = "/topic/print-config";
    private static final long RECONNECT_DELAY_MS = 5000;

    private WebSocketClient client;
    private final Runnable onNewPrintConfig;
    private Timer reconnectTimer;
    private volatile boolean shouldReconnect = true;

    /**
     * @param onNewPrintConfig callback to invoke when "NEW_PRINT_CONFIG" is received.
     *                         The caller is responsible for wrapping UI calls in Platform.runLater().
     */
    public WebSocketClientService(Runnable onNewPrintConfig) {
        this.onNewPrintConfig = onNewPrintConfig;
    }

    /**
     * Initiates connection to the WebSocket server.
     */
    public void connect() {
        shouldReconnect = true;
        createAndConnect();
    }

    private void createAndConnect() {
        try {
            URI serverUri = URI.create(WS_URL);
            client = new WebSocketClient(serverUri) {

                @Override
                public void onOpen(ServerHandshake handshake) {
                    System.out.println("[WebSocket] Connected to server.");

                    // Send STOMP CONNECT frame
                    String connectFrame = "CONNECT\n"
                            + "accept-version:1.1,1.2\n"
                            + "heart-beat:10000,10000\n"
                            + "\n"
                            + "\u0000";
                    send(connectFrame);
                }

                @Override
                public void onMessage(String message) {
                    System.out.println("[WebSocket] Raw message: " + message);

                    // Handle STOMP CONNECTED frame → subscribe to topic
                    if (message.startsWith("CONNECTED")) {
                        System.out.println("[WebSocket] STOMP connected. Subscribing to " + STOMP_TOPIC);
                        String subscribeFrame = "SUBSCRIBE\n"
                                + "id:sub-0\n"
                                + "destination:" + STOMP_TOPIC + "\n"
                                + "\n"
                                + "\u0000";
                        send(subscribeFrame);
                        return;
                    }

                    // Handle STOMP MESSAGE frame
                    if (message.startsWith("MESSAGE")) {
                        // Extract the body after the blank line in STOMP frame
                        String body = extractStompBody(message);
                        System.out.println("[WebSocket] STOMP message body: " + body);

                        if ("NEW_PRINT_CONFIG".equals(body.trim())) {
                            System.out.println("[WebSocket] NEW_PRINT_CONFIG received → refreshing grid.");
                            if (onNewPrintConfig != null) {
                                onNewPrintConfig.run();
                            }
                        }
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    System.out.println("[WebSocket] Disconnected. Code: " + code + ", Reason: " + reason);
                    scheduleReconnect();
                }

                @Override
                public void onError(Exception ex) {
                    System.err.println("[WebSocket] Error: " + ex.getMessage());
                    // onClose will be called after this, which handles reconnect
                }
            };

            System.out.println("[WebSocket] Connecting to " + WS_URL + " ...");
            client.connect();

        } catch (Exception e) {
            System.err.println("[WebSocket] Failed to create client: " + e.getMessage());
            scheduleReconnect();
        }
    }

    /**
     * Extracts the body from a STOMP frame.
     * STOMP format: COMMAND\nheader1:value1\n...\n\nbody\u0000
     */
    private String extractStompBody(String frame) {
        // The body starts after the first blank line (\n\n) in the STOMP frame
        int bodyStart = frame.indexOf("\n\n");
        if (bodyStart == -1) {
            return "";
        }
        String body = frame.substring(bodyStart + 2);
        // Remove trailing null character
        if (body.endsWith("\u0000")) {
            body = body.substring(0, body.length() - 1);
        }
        return body;
    }

    /**
     * Schedules an auto-reconnect attempt after RECONNECT_DELAY_MS.
     */
    private void scheduleReconnect() {
        if (!shouldReconnect) {
            return;
        }

        cancelReconnectTimer();
        reconnectTimer = new Timer("ws-reconnect", true);
        reconnectTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (shouldReconnect) {
                    System.out.println("[WebSocket] Attempting reconnect...");
                    createAndConnect();
                }
            }
        }, RECONNECT_DELAY_MS);
    }

    private void cancelReconnectTimer() {
        if (reconnectTimer != null) {
            reconnectTimer.cancel();
            reconnectTimer = null;
        }
    }

    /**
     * Gracefully disconnects from the WebSocket server and stops auto-reconnect.
     */
    public void disconnect() {
        shouldReconnect = false;
        cancelReconnectTimer();
        if (client != null && !client.isClosed()) {
            try {
                // Send STOMP DISCONNECT frame
                String disconnectFrame = "DISCONNECT\n"
                        + "receipt:exit\n"
                        + "\n"
                        + "\u0000";
                client.send(disconnectFrame);
            } catch (Exception ignored) {
                // Connection may already be broken
            }
            client.close();
            System.out.println("[WebSocket] Disconnected gracefully.");
        }
    }
}
