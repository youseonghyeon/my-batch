package com.example.settlementnew.dto.socket_message;

public class LogMessage extends SocketMessage {

    public LogMessage(String subject) {
        super(MessageType.LOG, subject, "");
    }
}
