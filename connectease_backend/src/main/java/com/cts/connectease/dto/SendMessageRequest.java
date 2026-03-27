package com.cts.connectease.dto;

import lombok.Data;

@Data
public class SendMessageRequest {
    private String sessionId;
    private String content;
}
