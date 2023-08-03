package com.example.settlementnew.dto.socket_message;

import lombok.Data;
import lombok.Getter;

@Getter
public class ResourceMessage extends SocketMessage {

    private String cpu;
    private String memory;

    public ResourceMessage(String cpu, String memory) {
        super(MessageType.RESOURCE, null, null);
        this.cpu = cpu;
        this.memory = memory;
    }

    public void setCpu(String cpu) {
        this.cpu = cpu;
    }

    public void setMemory(String memory) {
        this.memory = memory;
    }
}
