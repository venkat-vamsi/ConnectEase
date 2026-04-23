package com.cts.connectease.service;

import com.cts.connectease.dto.*;
import com.cts.connectease.model.*;
import com.cts.connectease.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private final ChatSessionRepository sessionRepo;
    private final ChatMessageRepository messageRepo;
    private final UserRepository userRepo;

    public ChatService(ChatSessionRepository sessionRepo, ChatMessageRepository messageRepo, UserRepository userRepo) {
        this.sessionRepo = sessionRepo;
        this.messageRepo = messageRepo;
        this.userRepo = userRepo;
    }

    @Transactional
    public ChatSessionResponse getOrInitializeChat(String currentUserId, String targetUserId) {
        User currentUser = userRepo.findById(currentUserId).orElseThrow(() -> new RuntimeException("Current user not found"));
        User targetUser = userRepo.findById(targetUserId).orElseThrow(() -> new RuntimeException("Target user not found"));

        // 1. Find or create session
        ChatSession session = sessionRepo.findExistingSession(currentUserId, targetUserId)
                .orElseGet(() -> {
                    ChatSession newSession = new ChatSession();
                    newSession.setCustomer(currentUser);
                    newSession.setVendor(targetUser);
                    return sessionRepo.save(newSession);
                });

        // 2. Fetch history
        List<ChatMessage> history = messageRepo.findBySession_SessionIdOrderByCreatedAtAsc(session.getSessionId());

        // 3. Map to DTO
        ChatSessionResponse response = new ChatSessionResponse();
        response.setSessionId(session.getSessionId());
        response.setCurrentUserId(currentUserId);
        response.setParticipantName(targetUser.getFullName());
        response.setParticipantImage(targetUser.getImage());

        List<ChatMessageDto> messageDtos = history.stream().map(msg -> {
            ChatMessageDto dto = new ChatMessageDto();
            dto.setMessageId(msg.getMessageId());
            dto.setSessionId(session.getSessionId());
            dto.setSenderId(msg.getSender().getUid());
            dto.setSenderName(msg.getSender().getFullName());
            dto.setSenderImage(msg.getSender().getImage());
            dto.setContent(msg.getContent());
            dto.setCreatedAt(msg.getCreatedAt().toString());
            return dto;
        }).collect(Collectors.toList());

        response.setMessages(messageDtos);
        return response;
    }

    @Transactional
    public ChatMessageDto saveMessage(String senderId, SendMessageRequest request) {
        User sender = userRepo.findById(senderId).orElseThrow();
        ChatSession session = sessionRepo.findById(request.getSessionId()).orElseThrow();

        ChatMessage message = new ChatMessage();
        message.setSession(session);
        message.setSender(sender);
        message.setContent(request.getContent());

        //Manually stamp the time right now!
        message.setCreatedAt(LocalDateTime.now());

        ChatMessage savedMsg = messageRepo.save(message);

        // Map back to DTO to broadcast over WebSocket
        ChatMessageDto dto = new ChatMessageDto();
        dto.setMessageId(savedMsg.getMessageId());
        dto.setSessionId(request.getSessionId());
        dto.setSenderId(sender.getUid());
        dto.setSenderName(sender.getFullName());
        dto.setSenderImage(sender.getImage());
        dto.setContent(savedMsg.getContent());
        dto.setCreatedAt(savedMsg.getCreatedAt().toString());

        return dto;
    }

    @Transactional(readOnly = true)
    public List<ChatSessionSummaryDTO> getSessionsForUser(String userId) {
        return sessionRepo.findAllSessionsForUser(userId).stream().map(session -> {
            boolean isCustomer = session.getCustomer().getUid().equals(userId);
            User participant = isCustomer ? session.getVendor() : session.getCustomer();

            List<ChatMessage> messages = session.getMessages();
            String lastMsg = "";
            if (messages != null && !messages.isEmpty()) {
                lastMsg = messages.get(messages.size() - 1).getContent();
            }

            return ChatSessionSummaryDTO.builder()
                    .sessionId(session.getSessionId())
                    .participantName(participant.getFullName())
                    .participantImage(participant.getImage())
                    .participantId(participant.getUid())
                    .lastMessage(lastMsg)
                    .startedAt(session.getStartedAt() != null ? session.getStartedAt().toString() : "")
                    .messageCount(messages != null ? messages.size() : 0)
                    .build();
        }).collect(Collectors.toList());
    }
}