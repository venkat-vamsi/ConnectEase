package com.cts.connectease.dto;

import lombok.Data;

@Data
public class ChatMessageDto {
    private String messageId;
    private String senderId;
    private String senderName;
    private String senderImage;
    private String content;
    private String createdAt;
}
