package com.cts.connectease.controller;

import com.cts.connectease.dto.AIChatRequest;
import com.cts.connectease.dto.AIChatResponse;
import com.cts.connectease.dto.ChatTurnDTO;
import com.cts.connectease.service.NativeAIChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/ai-chat")
@CrossOrigin(origins = "*")
public class AIChatController {

    @Autowired
    private NativeAIChatService nativeAIChatService;

    @PostMapping("/ask")
    public ResponseEntity<AIChatResponse> askChatbot(@RequestBody AIChatRequest request) {
        String query = request.getQuery();
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        List<ChatTurnDTO> history = request.getHistory() != null ? request.getHistory() : new ArrayList<>();
        AIChatResponse response = nativeAIChatService.processUserQuery(query, history);
        return ResponseEntity.ok(response);
    }
}