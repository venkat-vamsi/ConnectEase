package com.cts.connectease.controller;

import com.cts.connectease.dto.ChatSessionResponse;
import com.cts.connectease.dto.ChatMessageDto;
import com.cts.connectease.dto.SendMessageRequest;
import com.cts.connectease.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

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
            Principal principal) {

        // For testing without security enabled yet, you might need to hardcode a user ID here temporarily.
        // Once JWT is set up, principal.getName() will safely grab the logged-in user's ID.
        String currentUserId = principal != null ? principal.getName() : "user-123";

        ChatSessionResponse sessionData = chatService.getOrInitializeChat(currentUserId, vendorId);
        return ResponseEntity.ok(sessionData);
    }

    // STEP 2: Angular sends live messages here via WebSockets
    @MessageMapping("/chat.sendMessage")
    public void receiveAndBroadcastMessage(@Payload SendMessageRequest request, Principal principal) {
        String senderId = principal != null ? principal.getName() : "user-123";

        // Save to Database
        ChatMessageDto savedMessage = chatService.saveMessage(senderId, request);

        // Broadcast the message instantly to anyone subscribed to this specific session
        messagingTemplate.convertAndSend("/topic/session/" + request.getSessionId(), savedMessage);
    }
}