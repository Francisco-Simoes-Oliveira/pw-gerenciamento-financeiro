package com.financeiro.backend.features.realtime.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.financeiro.backend.features.realtime.websocket.RealtimeTicketHandshakeInterceptor;
import com.financeiro.backend.features.realtime.websocket.WalletRealtimeWebSocketHandler;

@Configuration
@EnableWebSocket
public class RealtimeWebSocketConfig implements WebSocketConfigurer {

    private final WalletRealtimeWebSocketHandler handler;
    private final RealtimeTicketHandshakeInterceptor handshakeInterceptor;

    public RealtimeWebSocketConfig(
            WalletRealtimeWebSocketHandler handler,
            RealtimeTicketHandshakeInterceptor handshakeInterceptor
    ) {
        this.handler = handler;
        this.handshakeInterceptor = handshakeInterceptor;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(handler, "/ws/realtime")
                .addInterceptors(handshakeInterceptor)
                .setAllowedOrigins("http://localhost:5173", "http://localhost:5174");
    }
}
