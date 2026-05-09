package com.cts.connectease.service;

import com.cts.connectease.dto.AIChatResponse;
import com.cts.connectease.dto.ChatTurnDTO;
import com.cts.connectease.dto.ListingCardDTO;
import com.cts.connectease.model.Rating;
import com.cts.connectease.model.ServiceEntity;
import com.cts.connectease.repository.ServiceRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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

    // --- PASS 1: INTENT & FEATURE EXTRACTION ---
    private static final String INTENT_SYSTEM_PROMPT = """
            You are the ConnectEase local guide AI. Your ONLY job is to extract search parameters.
            
            LOCATION RULES:
            - Platform ONLY covers Chennai.
            - Areas: Velachery, Seruseri, Anna Nagar, T.Nagar, Adyar, Tambaram, Mylapore, Nungambakkam, Perambur, Adambakkam, Porur, Vadapalani, Ashok Nagar, KK Nagar, Chromepet, Pallavaram, Guindy, Egmore, Royapettah, Triplicane, Kodambakkam, Mogappair, Avadi, Ambattur, Sholinganallur, OMR, ECR, Thoraipakkam, Perungudi, Besant Nagar, Kilpauk, Poonamallee, Thiruvanmiyur.
            - If user mentions a locality, set city="Chennai" and area=locality name.
            
            CATEGORY MAPPING:
            - "Veg", "Vegetarian", "Hotel", "Restaurant", "Breakfast" -> keyword="Food"
            - "Room", "Stay", "Hostel" -> keyword="PGs"
            
            RESPONSE FORMAT — return STRICT JSON only:
            {
              "keyword": "Category name or null",
              "city": "Chennai or null",
              "area": "specific locality or null",
              "maxPrice": number or null,
              "minRating": number or null,
              "features": ["array", "of", "features", "like", "ac", "wifi", "veg"]
            }
            """;

    // --- PASS 2: REVIEW SUMMARIZATION & CONVERSATION ---
    private static final String SUMMARY_SYSTEM_PROMPT = """
            You are the ConnectEase local guide. A user asked a query, and we have fetched the best matching services and their reviews.
            
            TASKS:
            1. Write a warm, conversational reply. 
            2. If we didn't find the exact specific thing (like "veg") but found general top services in that area, honestly mention: "I couldn't find exact matches for [X], but here are the highest-rated options nearby."
            3. For EACH service, summarize the reviews. Highlight major POSITIVES and explicitly alert the user to NEGATIVES (e.g., "KR PG - Great location, but users mentioned the food is strictly Tamilian.").
            
            Format nicely with bold titles. Keep it concise. No raw JSON in output.
            """;

    @Transactional(readOnly = true)
    public AIChatResponse processUserQuery(String userQuery, List<ChatTurnDTO> history) {
        try {
            List<ChatTurnDTO> safeHistory = (history != null) ? history : new ArrayList<>();
            ArrayNode conversationHistory = buildConversationHistory(safeHistory);

            // 1. EXTRACT INTENT
            ArrayNode pass1Contents = conversationHistory.deepCopy();
            pass1Contents.add(createUserTurn(userQuery));
            String jsonOutput = callGemini(INTENT_SYSTEM_PROMPT, pass1Contents, true);
            JsonNode aiDecision = objectMapper.readTree(jsonOutput);

            String keyword = aiDecision.path("keyword").isNull() ? null : aiDecision.path("keyword").asText();
            String city    = aiDecision.path("city").isNull()    ? null : aiDecision.path("city").asText();
            String area    = aiDecision.path("area").isNull()    ? null : aiDecision.path("area").asText();
            BigDecimal maxPrice = aiDecision.path("maxPrice").isNull() ? null : new BigDecimal(aiDecision.path("maxPrice").asText());
            Double minRating    = aiDecision.path("minRating").isNull() ? null : aiDecision.path("minRating").asDouble();

            List<String> features = new ArrayList<>();
            if (aiDecision.has("features")) {
                aiDecision.get("features").forEach(f -> features.add(f.asText().toLowerCase()));
            }

            // 2. RESILIENT DATABASE SEARCH (3 STAGES)
            List<ServiceEntity> entities = new ArrayList<>();

            // Stage A: Match by Features (High Precision)
            if (!features.isEmpty()) {
                entities = serviceRepository.advancedSearchByFeaturesForAI(keyword, city, area, maxPrice, minRating, features, PageRequest.of(0, 5));
            }

            // Stage B: Match by Keyword/Category (Standard)
            if (entities.isEmpty()) {
                entities = serviceRepository.advancedSearchForAI(keyword, city, area, maxPrice, minRating, PageRequest.of(0, 5));
            }

            // Stage C: Broad Fallback (If keyword search fails, show top-rated in that area)
            boolean isFallbackUsed = false;
            if (entities.isEmpty() && (city != null || area != null)) {
                isFallbackUsed = true;
                entities = serviceRepository.advancedSearchForAI(null, city, area, null, null, PageRequest.of(0, 5));
            }

            List<ListingCardDTO> cards = entities.stream().map(this::mapToDTO).collect(Collectors.toList());

            if (cards.isEmpty()) {
                return new AIChatResponse("I'm sorry, I couldn't find any services matching your request in Chennai. Try searching for something broader!", new ArrayList<>());
            }

            // 3. COMPILE REVIEWS FOR SUMMARY
            StringBuilder reviewContext = new StringBuilder("\n\n--- SERVICE DATA & REVIEWS ---\n");
            for (ServiceEntity entity : entities) {
                reviewContext.append("Name: ").append(entity.getName()).append("\nReviews: ");
                List<Rating> ratings = entity.getRatings();
                if (ratings != null && !ratings.isEmpty()) {
                    ratings.stream().limit(10).forEach(r -> {
                        if (r.getReview() != null) reviewContext.append("[").append(r.getReview()).append("] ");
                    });
                } else {
                    reviewContext.append("No written reviews yet.");
                }
                reviewContext.append("\n\n");
            }

            // 4. GENERATE FINAL SUMMARY
            String finalPrompt = SUMMARY_SYSTEM_PROMPT;
            if (isFallbackUsed) {
                finalPrompt += "\nNOTE: No exact matches for '" + keyword + "' were found. These are top-rated alternatives in " + (area != null ? area : city) + ".";
            }

            ArrayNode pass2Contents = conversationHistory.deepCopy();
            pass2Contents.add(createUserTurn("User Query: " + userQuery + reviewContext.toString()));

            String finalReply = callGemini(finalPrompt, pass2Contents, false);

            return new AIChatResponse(finalReply, cards);

        } catch (Exception e) {
            e.printStackTrace();
            return new AIChatResponse("I hit a snag while searching. Please try again!", new ArrayList<>());
        }
    }

    // --- GEMINI API HELPER ---
    private String callGemini(String systemPrompt, ArrayNode contents, boolean forceJson) throws Exception {
        String cleanKey = apiKey.replace("\"", "").replace("'", "").trim();
        String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-lite:generateContent?key=" + cleanKey;

        ObjectNode requestNode = objectMapper.createObjectNode();
        ObjectNode sysInstr = objectMapper.createObjectNode();
        sysInstr.set("parts", objectMapper.createArrayNode().add(objectMapper.createObjectNode().put("text", systemPrompt)));
        requestNode.set("systemInstruction", sysInstr);
        requestNode.set("contents", contents);

        if (forceJson) {
            ObjectNode genConfig = objectMapper.createObjectNode();
            genConfig.put("response_mime_type", "application/json");
            requestNode.set("generationConfig", genConfig);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("User-Agent", "ConnectEase-Backend");

        HttpEntity<String> httpRequest = new HttpEntity<>(objectMapper.writeValueAsString(requestNode), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, httpRequest, String.class);

        JsonNode rootNode = objectMapper.readTree(response.getBody());
        String text = rootNode.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();

        return forceJson ? text.replace("```json", "").replace("```", "").trim() : text;
    }

    private ArrayNode buildConversationHistory(List<ChatTurnDTO> history) {
        ArrayNode nodes = objectMapper.createArrayNode();
        int start = 0;
        while (start < history.size() && "model".equals(history.get(start).getRole())) start++;
        for (int i = start; i < history.size(); i++) {
            ChatTurnDTO turn = history.get(i);
            ObjectNode turnNode = objectMapper.createObjectNode();
            turnNode.put("role", "user".equals(turn.getRole()) ? "user" : "model");
            turnNode.set("parts", objectMapper.createArrayNode().add(objectMapper.createObjectNode().put("text", turn.getText())));
            nodes.add(turnNode);
        }
        return nodes;
    }

    private ObjectNode createUserTurn(String text) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("role", "user");
        node.set("parts", objectMapper.createArrayNode().add(objectMapper.createObjectNode().put("text", text)));
        return node;
    }

    private ListingCardDTO mapToDTO(ServiceEntity entity) {
        ListingCardDTO dto = new ListingCardDTO();
        dto.setSid(entity.getSid());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setPrice(entity.getPrice());
        if (entity.getCategory() != null) dto.setCategoryName(entity.getCategory().getName());
        if (entity.getLocation() != null) {
            dto.setCity(entity.getLocation().getCity());
            dto.setArea(entity.getLocation().getArea());
        }
        if (entity.getImages() != null && !entity.getImages().isEmpty()) {
            dto.setPrimaryImageUrl(entity.getImages().get(0).getUrl());
        }
        return dto;
    }
}