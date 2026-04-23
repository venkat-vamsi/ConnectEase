package com.cts.connectease.dto;

import lombok.Data;
import java.util.List;

@Data
public class ChatSessionResponse {
    private String sessionId;
    private String currentUserId;
    private String participantName;
    private String participantImage;
    private List<ChatMessageDto> messages;
}

