package com.cts.connectease.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatSessionSummaryDTO {
    private String sessionId;
    private String participantName;
    private String participantImage;
    private String participantId;
    private String lastMessage;
    private String startedAt;
    private int messageCount;
}
