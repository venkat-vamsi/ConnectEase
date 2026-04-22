package com.cts.connectease.controller;

import com.cts.connectease.dto.AIChatResponse;
import com.cts.connectease.service.NativeAIChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai-chat")
@CrossOrigin(origins = "*")
public class AIChatController {

    @Autowired
    private NativeAIChatService nativeAIChatService;

    @PostMapping("/ask")
    public ResponseEntity<AIChatResponse> askChatbot(@RequestBody Map<String, String> request) {
        String query = request.get("query");
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        AIChatResponse response = nativeAIChatService.processUserQuery(query);
        return ResponseEntity.ok(response);
    }
}