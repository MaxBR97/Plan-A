package Acceptance;

import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class WebSocketRequestsManager {
    private final int port;
    private StompSession stompSession;
    private WebSocketStompClient stompClient;
    private final Map<String, StompSession.Subscription> subscriptions;
    private Consumer<String> defaultMessageHandler;
    private Consumer<Throwable> defaultErrorHandler;

    public WebSocketRequestsManager(int port) {
        this.port = port;
        this.subscriptions = new HashMap<>();
        setupWebSocketClient();
    }

    private void setupWebSocketClient() {
        StandardWebSocketClient webSocketClient = new StandardWebSocketClient();
        this.stompClient = new WebSocketStompClient(webSocketClient);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
    }

    public void setDefaultMessageHandler(Consumer<String> messageHandler) {
        this.defaultMessageHandler = messageHandler;
    }

    public void setDefaultErrorHandler(Consumer<Throwable> errorHandler) {
        this.defaultErrorHandler = errorHandler;
    }

    public void connect() {
        String wsUrl = String.format("ws://localhost:%d/scip-websocket", port);
        try {
            this.stompSession = stompClient.connect(wsUrl, new DefaultStompSessionHandler()).get();
        } catch (InterruptedException | ExecutionException e) {
            if (defaultErrorHandler != null) {
                defaultErrorHandler.accept(e);
            }
        }
    }

    public void subscribe(String destination) {
        subscribe(destination, defaultMessageHandler);
    }

    public void subscribe(String destination, Consumer<String> messageHandler) {
        if (stompSession != null && stompSession.isConnected()) {
            StompSession.Subscription subscription = stompSession.subscribe(destination, new StompFrameHandler() {
                @Override
                public Type getPayloadType(StompHeaders headers) {
                    return String.class;
                }

                @Override
                public void handleFrame(StompHeaders headers, Object payload) {
                    if (messageHandler != null) {
                        messageHandler.accept((String) payload);
                    }
                }
            });
            subscriptions.put(destination, subscription);
        }
    }

    public void unsubscribe(String destination) {
        StompSession.Subscription subscription = subscriptions.get(destination);
        if (subscription != null) {
            subscription.unsubscribe();
            subscriptions.remove(destination);
        }
    }

    public void send(String destination, Object payload) {
        if (stompSession != null && stompSession.isConnected()) {
            stompSession.send(destination, payload);
        }
    }

    public void disconnect() {
        subscriptions.clear();
        if (stompSession != null) {
            stompSession.disconnect();
            stompSession = null;
        }
        if (stompClient != null) {
            stompClient.stop();
        }
    }

    public boolean isConnected() {
        return stompSession != null && stompSession.isConnected();
    }

    private class DefaultStompSessionHandler extends StompSessionHandlerAdapter {
        @Override
        public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
            System.out.println("Connected to WebSocket server");
        }

        @Override
        public void handleException(StompSession session, StompCommand command, 
                                  StompHeaders headers, byte[] payload, Throwable exception) {
            if (defaultErrorHandler != null) {
                defaultErrorHandler.accept(exception);
            }
        }

        @Override
        public void handleTransportError(StompSession session, Throwable exception) {
            if (defaultErrorHandler != null) {
                defaultErrorHandler.accept(exception);
            }
        }
    }
} 