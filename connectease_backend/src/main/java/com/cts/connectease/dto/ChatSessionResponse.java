package com.cts.connectease.dto;

import lombok.Data;
import java.util.List;

@Data
public class ChatSessionResponse {
    private String sessionId;
    private String participantName; // The person the user is talking to
    private String participantImage; // Their profile pic
    private List<ChatMessageDto> messages; // Chat history
}

