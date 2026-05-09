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

    // Pass 1: Extract Intents and Features ONLY (Strict JSON)
    private static final String INTENT_SYSTEM_PROMPT = """
            You are the ConnectEase local guide AI. Your ONLY job right now is to extract search parameters.
            
            LOCATION RULES:
            - Platform ONLY covers Chennai.
            - Areas: Velachery, Seruseri, Anna Nagar, T.Nagar, Adyar, Tambaram, Mylapore, Nungambakkam, Perambur, Adambakkam, Porur, Vadapalani, Ashok Nagar, KK Nagar, Chromepet, Pallavaram, Guindy, Egmore, Royapettah, Triplicane, Kodambakkam, Mogappair, Avadi, Ambattur, Sholinganallur, OMR, ECR, Thoraipakkam, Perungudi, Besant Nagar, Kilpauk, Poonamallee, Thiruvanmiyur.
            - If user mentions an area, city="Chennai", area=locality.
            
            RESPONSE FORMAT — return STRICT JSON only:
            {
              "keyword": "Category name or null",
              "city": "Chennai or null",
              "area": "specific Chennai locality or null",
              "maxPrice": number or null,
              "minRating": number or null,
              "features": ["array", "of", "requested", "features", "like", "ac", "wifi"] // Empty array if none mentioned
            }
            """;

    // Pass 2: Generate the friendly summary response
    private static final String SUMMARY_SYSTEM_PROMPT = """
            You are the ConnectEase local guide. A user asked a query, and we have fetched the best matching services along with their customer reviews.
            
            Your tasks:
            1. Write a warm, conversational reply addressing their query.
            2. For EACH fetched service provided in the context, you MUST summarize the reviews.
            3. Highlight major POSITIVES and explicitly alert the user about any NEGATIVES (e.g., "KR PG - Most users love how near it is, but be aware the food is entirely Tamilian.").
            
            Keep the summaries concise and easy to scan so the user doesn't get a headache reading.
            Do NOT return JSON. Return plain text/markdown conversational response.
            """;

    @Transactional(readOnly = true)
    public AIChatResponse processUserQuery(String userQuery, List<ChatTurnDTO> history) {
        try {
            List<ChatTurnDTO> safeHistory = (history != null) ? history : new ArrayList<>();
            ArrayNode conversationHistory = buildConversationHistory(safeHistory);

            // -------------------------------------------------------------
            // PASS 1: Extract Intent & Features via JSON
            // -------------------------------------------------------------
            ArrayNode pass1Contents = conversationHistory.deepCopy();
            pass1Contents.add(createUserTurn(userQuery));

            String jsonOutput = callGemini(INTENT_SYSTEM_PROMPT, pass1Contents, true);
            JsonNode aiDecision = objectMapper.readTree(jsonOutput);

            String keyword = aiDecision.path("keyword").isNull() ? null : aiDecision.path("keyword").asText();
            String city    = aiDecision.path("city").isNull()    ? null : aiDecision.path("city").asText();
            String area    = aiDecision.path("area").isNull()    ? null : aiDecision.path("area").asText();
            BigDecimal maxPrice = aiDecision.path("maxPrice").isNull() ? null : new BigDecimal(aiDecision.path("maxPrice").asText());
            Double minRating    = aiDecision.path("minRating").isNull() ? null : aiDecision.path("minRating").asDouble();

            List<String> requestedFeatures = new ArrayList<>();
            if (aiDecision.has("features") && aiDecision.get("features").isArray()) {
                for (JsonNode f : aiDecision.get("features")) {
                    requestedFeatures.add(f.asText().toLowerCase());
                }
            }

            // -------------------------------------------------------------
            // DATABASE FETCH: Attempt Feature Match first, then Fallback
            // -------------------------------------------------------------
            List<ServiceEntity> entities = new ArrayList<>();

            if (!requestedFeatures.isEmpty()) {
                entities = serviceRepository.advancedSearchByFeaturesForAI(
                        keyword, city, area, maxPrice, minRating, requestedFeatures, PageRequest.of(0, 5));
            }

            // If feature search returned nothing (or no features requested), fallback to standard search
            if (entities.isEmpty()) {
                entities = serviceRepository.advancedSearchForAI(
                        keyword, city, area, maxPrice, minRating, PageRequest.of(0, 5));
            }

            List<ListingCardDTO> cards = entities.stream().map(this::mapToDTO).collect(Collectors.toList());

            if (cards.isEmpty()) {
                return new AIChatResponse("I understand what you need, but I couldn't find exact matches right now. Try broadening your search!", new ArrayList<>());
            }

            // -------------------------------------------------------------
            // PASS 2: Compile Reviews and Generate Summary Reply
            // -------------------------------------------------------------
            StringBuilder reviewContext = new StringBuilder("\n\n--- FETCHED SERVICES & REVIEWS ---\n");
            for (ServiceEntity entity : entities) {
                reviewContext.append("Service Name: ").append(entity.getName()).append("\nReviews: ");

                if (entity.getRatings() != null && !entity.getRatings().isEmpty()) {
                    int count = 0;
                    for (Rating r : entity.getRatings()) {
                        String reviewText = r.getReview(); // Using the exact field from your Rating model
                        if (reviewText != null && !reviewText.trim().isEmpty()) {
                            reviewContext.append("[").append(reviewText).append("] ");
                            if (++count >= 15) break; // Limit to 15 reviews per service to save context window and speed
                        }
                    }
                    if (count == 0) reviewContext.append("Only ratings, no written reviews yet.");
                } else {
                    reviewContext.append("No reviews available yet.");
                }
                reviewContext.append("\n\n");
            }

            ArrayNode pass2Contents = conversationHistory.deepCopy();
            String enrichedQuery = "User Query: " + userQuery + reviewContext.toString();
            pass2Contents.add(createUserTurn(enrichedQuery));

            String finalReplyMessage = callGemini(SUMMARY_SYSTEM_PROMPT, pass2Contents, false);

            return new AIChatResponse(finalReplyMessage, cards);

        } catch (Exception e) {
            e.printStackTrace();
            return new AIChatResponse("I'm having a little trouble reading the reviews right now. Please try again in a moment!", new ArrayList<>());
        }
    }

    // --- Helper Methods ---

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
        headers.set("User-Agent", "Mozilla/5.0");

        HttpEntity<String> httpRequest = new HttpEntity<>(objectMapper.writeValueAsString(requestNode), headers);
        ResponseEntity<String> response = null;

        int maxRetries = 3;
        for (int i = 0; i < maxRetries; i++) {
            try {
                response = restTemplate.postForEntity(apiUrl, httpRequest, String.class);
                break;
            } catch (org.springframework.web.client.HttpClientErrorException.TooManyRequests e) {
                System.out.println("Rate limit (429) hit. Retrying in 5s... (attempt " + (i + 1) + ")");
                Thread.sleep(5000);
                if (i == maxRetries - 1) throw e;
            }
        }

        JsonNode rootNode = objectMapper.readTree(response.getBody());
        String text = rootNode.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();

        if (forceJson) {
            return text.replace("```json", "").replace("```", "").trim();
        }
        return text;
    }

    private ArrayNode buildConversationHistory(List<ChatTurnDTO> safeHistory) {
        ArrayNode contentsArray = objectMapper.createArrayNode();
        int startIdx = 0;
        while (startIdx < safeHistory.size() && "model".equals(safeHistory.get(startIdx).getRole())) {
            startIdx++;
        }
        for (int i = startIdx; i < safeHistory.size(); i++) {
            ChatTurnDTO turn = safeHistory.get(i);
            ObjectNode turnNode = objectMapper.createObjectNode();
            turnNode.put("role", "user".equals(turn.getRole()) ? "user" : "model");
            turnNode.set("parts", objectMapper.createArrayNode().add(objectMapper.createObjectNode().put("text", turn.getText() != null ? turn.getText() : "")));
            contentsArray.add(turnNode);
        }
        return contentsArray;
    }

    private ObjectNode createUserTurn(String text) {
        ObjectNode turn = objectMapper.createObjectNode();
        turn.put("role", "user");
        turn.set("parts", objectMapper.createArrayNode().add(objectMapper.createObjectNode().put("text", text)));
        return turn;
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
            String primaryUrl = entity.getImages().stream()
                    .filter(img -> img.getIsPrimary() != null && img.getIsPrimary())
                    .map(img -> img.getUrl())
                    .findFirst()
                    .orElse(entity.getImages().get(0).getUrl());
            dto.setPrimaryImageUrl(primaryUrl);
        }
        return dto;
    }
}