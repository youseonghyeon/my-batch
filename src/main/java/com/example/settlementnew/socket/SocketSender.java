package com.example.settlementnew.socket;

import com.example.settlementnew.dto.socket_message.LogMessage;
import com.example.settlementnew.dto.socket_message.ResourceMessage;
import com.example.settlementnew.dto.socket_message.SocketMessage;
import com.example.settlementnew.dto.socket_message.StatusMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SocketSender {

    private final WasWebSocketHandler webSocketHandler;

    public void sendLogMessage(String logMessage) {
        if (webSocketHandler.sessionIsEmpty())
            return;
        send(new LogMessage(logMessage));
    }

    private void send(SocketMessage socketMessage) {
        webSocketHandler.sendMessage(socketMessage);
    }

    public void sendResourceMessage(String cpu, String memory) {
        if (webSocketHandler.sessionIsEmpty())
            return;
        send(new ResourceMessage(cpu, memory));
    }

    public void sendStatus(String subject, String detail) {
        this.sendStatus(subject, detail, null);
    }

    public void sendStatus(String subject, String detail, String img) {
        if (webSocketHandler.sessionIsEmpty())
            return;
        send(new StatusMessage(subject, detail, img));
    }
}
