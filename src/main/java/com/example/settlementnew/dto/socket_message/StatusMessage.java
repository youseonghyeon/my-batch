package com.example.settlementnew.dto.socket_message;

import lombok.Getter;

@Getter
public class StatusMessage extends SocketMessage {

    private String img;

    public StatusMessage(String subject, String detail) {
        this(subject, detail, "");
    }

    public StatusMessage( String subject, String detail, String img) {
        super(MessageType.STATUS, subject, detail);
        this.img = img;
    }

    public void setImg(String img) {
        this.img = img;
    }
}
