package com.cts.connectease.service;

import com.cts.connectease.dto.AIChatResponse;
import com.cts.connectease.dto.ListingCardDTO;
import com.cts.connectease.model.ServiceEntity;
import com.cts.connectease.repository.ServiceRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NativeAIChatService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Autowired
    private ServiceRepository serviceRepository;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ADDED THIS to keep the database session open while mapping DTOs!
    @Transactional(readOnly = true)
    public AIChatResponse processUserQuery(String userQuery) {

        // 1. INJECT THE "MENU": Tell the AI exactly what categories exist in your DB
        String validCategories = "'PGs', 'Food', 'Electrical service', 'plumbing service', 'cleaning service', 'laundry'";

        String prompt = """
            You are the ConnectEase local guide. 
            A user is going to ask for a service. Your job is to map their request to ONE of our official categories.
            
            Our Official Categories: [%s]
            
            Rules:
            1. Be empathetic and conversational in your 'reply'.
            2. Map their intent to the EXACT official category name (e.g., if they say 'hungry', keyword is 'Food'. If they say 'leaking pipe', keyword is 'plumbing service').
            3. If their request is completely unrelated to our categories, set keyword to null.
            
            Return ONLY strict JSON matching this exact format:
            {
              "reply": "Hey! Let me find you some great options...",
              "keyword": "MUST MATCH AN OFFICIAL CATEGORY OR NULL",
              "city": "extract city if mentioned, else null",
              "area": "extract area if mentioned, else null",
              "maxPrice": extract number if mentioned, else null,
              "minRating": extract number if mentioned, else null
            }
            User said: "%s"
            """.formatted(validCategories, userQuery);

        try {
            // 2. SANITIZE URL & KEY
            String cleanKey = apiKey.replace("\"", "").replace("'", "").trim();
            String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key={key}";

            String requestBody = """
                {
                  "contents": [{
                    "parts": [{"text": %s}]
                  }],
                  "generationConfig": {
                    "response_mime_type": "application/json"
                  }
                }
                """.formatted(objectMapper.writeValueAsString(prompt));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("User-Agent", "Mozilla/5.0"); // Disguise as browser

            HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = null;

            // 3. THE SHOCK ABSORBER (Rate Limit Retry Loop)
            int maxRetries = 3;
            for (int i = 0; i < maxRetries; i++) {
                try {
                    response = restTemplate.postForEntity(apiUrl, request, String.class, cleanKey);
                    break; // Success! Break out of the retry loop.
                } catch (org.springframework.web.client.HttpClientErrorException.TooManyRequests e) {
                    System.out.println("🚨 Rate Limit (429) hit! Cooling down for 10 seconds... (Attempt " + (i+1) + ")");
                    try { Thread.sleep(10000); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                    if (i == maxRetries - 1) throw e;
                }
            }

            // 4. Parse the JSON response safely
            JsonNode rootNode = objectMapper.readTree(response.getBody());
            String extractedJsonText = rootNode.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();

            // Clean up any markdown blocks the AI might accidentally add
            extractedJsonText = extractedJsonText.replace("```json", "").replace("```", "").trim();
            JsonNode aiDecision = objectMapper.readTree(extractedJsonText);

            // LOG IT! This shows you exactly what the AI is thinking in your console
            System.out.println("🤖 AI Extracted Intent: " + aiDecision.toPrettyString());

            String replyMessage = aiDecision.path("reply").asText();

            // 5. Database Search Execution
            String keyword = aiDecision.path("keyword").isNull() ? null : aiDecision.path("keyword").asText();
            String city = aiDecision.path("city").isNull() ? null : aiDecision.path("city").asText();
            String area = aiDecision.path("area").isNull() ? null : aiDecision.path("area").asText();
            BigDecimal maxPrice = aiDecision.path("maxPrice").isNull() ? null : new BigDecimal(aiDecision.path("maxPrice").asText());
            Double minRating = aiDecision.path("minRating").isNull() ? null : aiDecision.path("minRating").asDouble();

            List<ServiceEntity> entities = serviceRepository.advancedSearchForAI(
                    keyword, city, area, maxPrice, minRating, PageRequest.of(0, 5)
            );

            // 6. Map to UI Cards
            List<ListingCardDTO> cards = entities.stream().map(this::mapToDTO).collect(Collectors.toList());

            if (cards.isEmpty()) {
                replyMessage = "I completely understand what you need, but my database doesn't have any exact matches for that right now. Try broadening your search!";
            }

            return new AIChatResponse(replyMessage, cards);

        } catch (Exception e) {
            e.printStackTrace();
            return new AIChatResponse("I'm having a little trouble connecting my thoughts right now. Please try again in a moment!", new ArrayList<>());
        }
    }

    // Isolated Mapper (Requires @Transactional on the parent method to fetch Category and Images)
    private ListingCardDTO mapToDTO(ServiceEntity entity) {
        ListingCardDTO dto = new ListingCardDTO();
        dto.setSid(entity.getSid());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setPrice(entity.getPrice());

        if (entity.getCategory() != null) {
            dto.setCategoryName(entity.getCategory().getName());
        }

        if (entity.getLocation() != null) {
            dto.setCity(entity.getLocation().getCity());
            dto.setArea(entity.getLocation().getArea());
        }

        String primaryUrl = null;
        if (entity.getImages() != null && !entity.getImages().isEmpty()) {
            primaryUrl = entity.getImages().stream()
                    .filter(img -> img.getIsPrimary() != null && img.getIsPrimary())
                    .map(img -> img.getUrl()).findFirst().orElse(entity.getImages().get(0).getUrl());
        }
        dto.setPrimaryImageUrl(primaryUrl);
        return dto;
    }
}