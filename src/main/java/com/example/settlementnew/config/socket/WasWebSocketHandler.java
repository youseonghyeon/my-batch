package com.example.settlementnew.config.socket;

import com.example.settlementnew.dto.socket_message.SocketMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.HashSet;
import java.util.Set;


@Slf4j
@Component
@RequiredArgsConstructor
public class WasWebSocketHandler extends TextWebSocketHandler {

    private final Set<WebSocketSession> socketStore = new HashSet<>();

    private final ObjectMapper objectMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("Created new connection: SocketSession[id:{}]", session.getId());
        socketStore.add(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info("Closed connection: SocketSession[id:{}]", session.getId());
        socketStore.remove(session);
    }

    public void sendMessage(SocketMessage socketMessage) {
        socketStore.forEach(session -> {
            try {
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(socketMessage)));
            } catch (Exception e) {
                log.error("Failed to send message to session: {}", session.getId(), e);
            }
        });
    }


}
