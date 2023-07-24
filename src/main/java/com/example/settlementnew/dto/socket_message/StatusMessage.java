package com.example.settlementnew.dto.socket_message;

public class StatusMessage extends SocketMessage {
    public StatusMessage(String subject, String detail) {
        super(MessageType.STATUS, subject, detail);
    }
}
