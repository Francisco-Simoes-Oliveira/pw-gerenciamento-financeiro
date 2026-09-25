package com.financeiro.backend.features.realtime.websocket;

import java.net.URI;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import com.financeiro.backend.features.realtime.service.RealtimeTicketService;

@Component
public class RealtimeTicketHandshakeInterceptor implements HandshakeInterceptor {

    public static final String USER_ID_ATTRIBUTE = "realtimeUserId";

    private final RealtimeTicketService ticketService;

    public RealtimeTicketHandshakeInterceptor(RealtimeTicketService ticketService) {
        this.ticketService = ticketService;
    }

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {
        String ticket = extractTicket(request.getURI());
        UUID userId = ticketService.consume(ticket).orElse(null);
        if (userId == null) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        attributes.put(USER_ID_ATTRIBUTE, userId);
        return true;
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception
    ) {
        // Sem estado adicional: o ticket já foi consumido antes do handshake.
    }

    private String extractTicket(URI uri) {
        MultiValueMap<String, String> query = UriComponentsBuilder.fromUri(uri).build().getQueryParams();
        return query.getFirst("ticket");
    }
}
