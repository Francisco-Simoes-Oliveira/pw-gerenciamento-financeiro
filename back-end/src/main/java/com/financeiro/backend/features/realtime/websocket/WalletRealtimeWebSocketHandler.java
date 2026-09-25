package com.financeiro.backend.features.realtime.websocket;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

@Component
public class WalletRealtimeWebSocketHandler extends TextWebSocketHandler {

    private final JsonMapper jsonMapper;
    private final ConcurrentMap<UUID, ConcurrentMap<String, WebSocketSession>> sessionsByUser =
            new ConcurrentHashMap<>();

    public WalletRealtimeWebSocketHandler(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        UUID userId = (UUID) session.getAttributes().get(RealtimeTicketHandshakeInterceptor.USER_ID_ATTRIBUTE);
        if (userId == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Ticket de tempo real inválido."));
            return;
        }

        sessionsByUser
                .computeIfAbsent(userId, ignored -> new ConcurrentHashMap<>())
                .put(session.getId(), session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        removeSession(session);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        removeSession(session);
        if (session.isOpen()) {
            session.close(CloseStatus.SERVER_ERROR);
        }
    }

    public void sendToUsers(Set<UUID> userIds, Object payload) {
        final String json;
        try {
            json = jsonMapper.writeValueAsString(payload);
        } catch (JacksonException exception) {
            throw new IllegalStateException("Não foi possível serializar evento em tempo real.", exception);
        }

        TextMessage message = new TextMessage(json);
        userIds.forEach(userId -> {
            Map<String, WebSocketSession> sessions = sessionsByUser.get(userId);
            if (sessions == null) {
                return;
            }

            sessions.values().forEach(session -> sendSafely(session, message));
        });
    }

    private void sendSafely(WebSocketSession session, TextMessage message) {
        if (!session.isOpen()) {
            removeSession(session);
            return;
        }

        try {
            // WebSocketSession não garante envios concorrentes; sincronizar evita frames sobrepostos.
            synchronized (session) {
                if (session.isOpen()) {
                    session.sendMessage(message);
                }
            }
        } catch (java.io.IOException exception) {
            removeSession(session);
        }
    }

    private void removeSession(WebSocketSession session) {
        UUID userId = (UUID) session.getAttributes().get(RealtimeTicketHandshakeInterceptor.USER_ID_ATTRIBUTE);
        if (userId == null) {
            return;
        }

        ConcurrentMap<String, WebSocketSession> sessions = sessionsByUser.get(userId);
        if (sessions == null) {
            return;
        }

        sessions.remove(session.getId());
        if (sessions.isEmpty()) {
            sessionsByUser.remove(userId, sessions);
        }
    }
}
