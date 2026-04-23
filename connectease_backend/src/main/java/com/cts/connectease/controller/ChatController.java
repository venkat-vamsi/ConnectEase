package com.cts.connectease.controller;

import com.cts.connectease.dto.ChatSessionResponse;
import com.cts.connectease.dto.ChatSessionSummaryDTO;
import com.cts.connectease.dto.ChatMessageDto;
import com.cts.connectease.dto.SendMessageRequest;
import com.cts.connectease.model.ChatMessage;
import java.util.List;
import com.cts.connectease.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import org.springframework.security.core.Authentication;

@RestController
@CrossOrigin(origins = "*") // Allows Angular to call this API
//@RequestMapping(/api/) viji told to add this-> best pratice
//do no return direct entity , return response entity of that product with http status code (ex, return new ResponseEntity(productService.getById(id),HttpStatus.OK))
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatController(ChatService chatService, SimpMessagingTemplate messagingTemplate) {
        this.chatService = chatService;
        this.messagingTemplate = messagingTemplate;
    }

    // STEP 1: Angular calls this via HTTP GET when clicking "Send Message"
    @GetMapping("/api/chat/start/{vendorId}")
    public ResponseEntity<ChatSessionResponse> openChatWindow(
            @PathVariable String vendorId,
            Authentication authentication) {

        String currentUserId = authentication != null && authentication.getCredentials() != null
                ? authentication.getCredentials().toString()
                : null;

        ChatSessionResponse sessionData = chatService.getOrInitializeChat(currentUserId, vendorId);
        return ResponseEntity.ok(sessionData);
    }

    // REST endpoint for sending messages — uses HTTP cookie auth (reliable)
    @PostMapping("/api/chat/{sessionId}/messages")
    public ResponseEntity<ChatMessageDto> sendMessage(
            @PathVariable String sessionId,
            @RequestBody SendMessageRequest request,
            Authentication authentication) {
        String senderId = authentication != null && authentication.getCredentials() != null
                ? authentication.getCredentials().toString()
                : null;
        request.setSessionId(sessionId);
        ChatMessageDto saved = chatService.saveMessage(senderId, request);
        messagingTemplate.convertAndSend("/topic/session/" + sessionId, saved);
        return ResponseEntity.ok(saved);
    }

    // STOMP fallback (kept for compatibility, not used by frontend anymore)
    @MessageMapping("/chat.sendMessage")
    public void receiveAndBroadcastMessage(@Payload SendMessageRequest request, Authentication authentication) {
        String senderId = authentication != null && authentication.getCredentials() != null
                ? authentication.getCredentials().toString()
                : null;
        ChatMessageDto savedMessage = chatService.saveMessage(senderId, request);
        messagingTemplate.convertAndSend("/topic/session/" + request.getSessionId(), savedMessage);
    }

    @GetMapping("/api/chat/sessions")
    public ResponseEntity<List<ChatSessionSummaryDTO>> getMySessions(Authentication authentication) {
        String currentUserId = authentication != null && authentication.getCredentials() != null
                ? authentication.getCredentials().toString()
                : null;
        return ResponseEntity.ok(chatService.getSessionsForUser(currentUserId));
    }
}