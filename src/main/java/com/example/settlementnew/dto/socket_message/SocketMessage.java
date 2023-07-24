package com.example.settlementnew.dto.socket_message;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SocketMessage {
    private MessageType type;
    private String subject;
    private String detail;

}
