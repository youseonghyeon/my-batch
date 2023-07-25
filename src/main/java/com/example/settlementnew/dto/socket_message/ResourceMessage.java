package com.example.settlementnew.dto.socket_message;

import lombok.Data;

public class ResourceMessage extends SocketMessage {

    private String cpu;
    private String memory;

    public ResourceMessage(String cpu, String memory) {
        super(MessageType.RESOURCE, null, null);
        this.cpu = cpu;
        this.memory = memory;
    }

    public String getCpu() {
        return cpu;
    }

    public void setCpu(String cpu) {
        this.cpu = cpu;
    }

    public String getMemory() {
        return memory;
    }

    public void setMemory(String memory) {
        this.memory = memory;
    }
}
