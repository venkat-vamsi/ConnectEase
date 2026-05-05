package com.cts.connectease.service;

import com.cts.connectease.dto.AIChatResponse;
import com.cts.connectease.dto.ChatTurnDTO;
import com.cts.connectease.dto.ListingCardDTO;
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

    private static final String SYSTEM_PROMPT = """
            You are the ConnectEase local guide — a helpful, friendly assistant for people moving to or living in Chennai, India.
            ConnectEase connects customers with local service providers.

            OFFICIAL SERVICE CATEGORIES (use EXACT spelling only):
            'PGs', 'Food', 'Electrical service', 'plumbing service', 'cleaning service', 'laundry'

            LOCATION RULES — THIS IS CRITICAL:
            - Our platform ONLY covers Chennai, India.
            - The following are AREAS (localities/neighbourhoods) within Chennai — NEVER treat them as a city:
              Velachery, Seruseri, Anna Nagar, T.Nagar, Adyar, Tambaram, Mylapore, Nungambakkam, Perambur,
              Adambakkam, Porur, Vadapalani, Ashok Nagar, KK Nagar, Chromepet, Pallavaram, Guindy,
              Egmore, Royapettah, Triplicane, Kodambakkam, Mogappair, Avadi, Ambattur, Sholinganallur,
              OMR, ECR, Thoraipakkam, Perungudi, Besant Nagar, Kilpauk, Poonamallee, Thiruvanmiyur.
            - If the user mentions any Chennai locality → set city = "Chennai" and area = that locality name.
            - If the user mentions "Chennai" without a specific area → set city = "Chennai" and area = null.
            - If the user does not mention any location → set both city and area to null.
            - If the user mentions a city that is NOT Chennai → set keyword = null (we don't serve other cities).

            RESPONSE FORMAT — return STRICT JSON only, no markdown fences:
            {
              "reply": "warm, empathetic conversational response — reference previous conversation context naturally",
              "keyword": "EXACT category name from the list above, or null if unrelated",
              "city": "Chennai or null",
              "area": "specific Chennai locality or null",
              "maxPrice": number or null,
              "minRating": number or null
            }

            Additional rules:
            - Be warm, conversational, and refer to previous messages naturally (e.g., "Since you mentioned needing a PG earlier...").
            - If the user asks a follow-up like "cheaper ones" or "higher rated", infer from conversation history what they mean.
            - Never set a Chennai locality name in the 'city' field.
            """;

    @Transactional(readOnly = true)
    public AIChatResponse processUserQuery(String userQuery, List<ChatTurnDTO> history) {
        try {
            String cleanKey = apiKey.replace("\"", "").replace("'", "").trim();
            String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-lite:generateContent?key={key}";

            // Build request using Jackson ObjectNode to avoid any JSON injection
            ObjectNode requestNode = objectMapper.createObjectNode();

            // System instruction (separate from conversation turns)
            ObjectNode sysInstr = objectMapper.createObjectNode();
            ArrayNode sysParts = objectMapper.createArrayNode();
            sysParts.addObject().put("text", SYSTEM_PROMPT);
            sysInstr.set("parts", sysParts);
            requestNode.set("systemInstruction", sysInstr);

            // Build contents array from history + current query
            ArrayNode contentsArray = objectMapper.createArrayNode();

            // Ensure history starts with a user turn (Gemini requirement)
            List<ChatTurnDTO> safeHistory = (history != null) ? history : new ArrayList<>();
            int startIdx = 0;
            while (startIdx < safeHistory.size() && "model".equals(safeHistory.get(startIdx).getRole())) {
                startIdx++;
            }

            for (int i = startIdx; i < safeHistory.size(); i++) {
                ChatTurnDTO turn = safeHistory.get(i);
                String role = "user".equals(turn.getRole()) ? "user" : "model";
                String text = turn.getText() != null ? turn.getText() : "";
                ObjectNode turnNode = objectMapper.createObjectNode();
                turnNode.put("role", role);
                ArrayNode parts = objectMapper.createArrayNode();
                parts.addObject().put("text", text);
                turnNode.set("parts", parts);
                contentsArray.add(turnNode);
            }

            // Append current user query
            ObjectNode currentTurn = objectMapper.createObjectNode();
            currentTurn.put("role", "user");
            ArrayNode currentParts = objectMapper.createArrayNode();
            currentParts.addObject().put("text", userQuery);
            currentTurn.set("parts", currentParts);
            contentsArray.add(currentTurn);

            requestNode.set("contents", contentsArray);

            // Generation config — force JSON output
            ObjectNode genConfig = objectMapper.createObjectNode();
            genConfig.put("response_mime_type", "application/json");
            requestNode.set("generationConfig", genConfig);

            String requestBody = objectMapper.writeValueAsString(requestNode);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("User-Agent", "Mozilla/5.0");

            HttpEntity<String> httpRequest = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = null;

            // Rate limit retry loop
            int maxRetries = 3;
            for (int i = 0; i < maxRetries; i++) {
                try {
                    response = restTemplate.postForEntity(apiUrl, httpRequest, String.class, cleanKey);
                    break;
                } catch (org.springframework.web.client.HttpClientErrorException.TooManyRequests e) {
                    System.out.println("Rate limit (429) hit. Retrying in 10s... (attempt " + (i + 1) + ")");
                    try { Thread.sleep(10000); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                    if (i == maxRetries - 1) throw e;
                }
            }

            // Parse response
            JsonNode rootNode = objectMapper.readTree(response.getBody());
            String extractedJson = rootNode.path("candidates").get(0)
                    .path("content").path("parts").get(0).path("text").asText();
            extractedJson = extractedJson.replace("```json", "").replace("```", "").trim();
            JsonNode aiDecision = objectMapper.readTree(extractedJson);

            System.out.println("AI Intent: " + aiDecision.toPrettyString());

            String replyMessage = aiDecision.path("reply").asText();
            String keyword = aiDecision.path("keyword").isNull() ? null : aiDecision.path("keyword").asText();
            String city    = aiDecision.path("city").isNull()    ? null : aiDecision.path("city").asText();
            String area    = aiDecision.path("area").isNull()    ? null : aiDecision.path("area").asText();
            BigDecimal maxPrice = aiDecision.path("maxPrice").isNull() ? null : new BigDecimal(aiDecision.path("maxPrice").asText());
            Double minRating    = aiDecision.path("minRating").isNull() ? null : aiDecision.path("minRating").asDouble();

            List<ServiceEntity> entities = serviceRepository.advancedSearchForAI(
                    keyword, city, area, maxPrice, minRating, PageRequest.of(0, 5));

            List<ListingCardDTO> cards = entities.stream().map(this::mapToDTO).collect(Collectors.toList());

            if (cards.isEmpty()) {
                replyMessage = "I understand what you need, but I couldn't find exact matches right now. Try broadening your search!";
            }

            return new AIChatResponse(replyMessage, cards);

        } catch (Exception e) {
            e.printStackTrace();
            return new AIChatResponse("I'm having a little trouble right now. Please try again in a moment!", new ArrayList<>());
        }
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
