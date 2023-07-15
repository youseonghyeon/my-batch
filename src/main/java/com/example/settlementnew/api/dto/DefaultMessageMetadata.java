package com.example.settlementnew.api.dto;

import java.io.File;
import java.util.List;

public class DefaultMessageMetadata extends MessageMetadata {

    private String from;
    private List<String> to;
    private List<String> cc;
    private String subject;
    private String body;
    private File attachment;

    public DefaultMessageMetadata(String from, List<String> to) {
        this.from = from;
        this.to = to;
    }
}
