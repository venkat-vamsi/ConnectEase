package com.cts.connectease.service;

import com.cts.connectease.dto.AIChatResponse;
import com.cts.connectease.dto.ChatTurnDTO;
import com.cts.connectease.dto.ListingCardDTO;
import com.cts.connectease.model.Feature;
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
            You are the ConnectEase Chennai discovery AI. Your ONLY task is to extract search parameters from the user's latest message. Do NOT answer, recommend, or chat.

            LOCATION RULES:
            - The platform covers ONLY Chennai. If any Chennai locality is mentioned, set city="Chennai" and area=that locality.
            - Known areas: Velachery, Seruseri, Anna Nagar, T.Nagar, Adyar, Tambaram, Mylapore, Nungambakkam, Perambur, Adambakkam, Porur, Vadapalani, Ashok Nagar, KK Nagar, Chromepet, Pallavaram, Guindy, Egmore, Royapettah, Triplicane, Kodambakkam, Mogappair, Avadi, Ambattur, Sholinganallur, OMR, ECR, Thoraipakkam, Perungudi, Besant Nagar, Kilpauk, Poonamallee, Thiruvanmiyur.

            CATEGORY MAPPING (set "keyword" to one of these canonical values):
            - "Veg" / "Vegetarian" / "Non-veg" / "Restaurant" / "Hotel" / "Food" / "Breakfast" / "Lunch" / "Dinner" -> keyword="Food"
            - "PG" / "PGs" / "Room" / "Stay" / "Hostel" / "Accommodation" -> keyword="PGs"
            - Otherwise pass the user's category through as a single word.

            PRICE PARSING (always integers):
            - "under 7k" / "below 7000" / "less than 7k" / "<7k" -> maxPrice=7000
            - "5k" -> 5000, "10k" -> 10000, "1.5k" -> 1500
            - Ignore stray numbers that aren't a budget.

            FEATURES (always lowercase, short tokens — only include features the user explicitly mentioned):
            - "AC" / "air conditioned" / "air conditioning" -> "ac"
            - "wifi" / "internet" / "wi-fi" -> "wifi"
            - "veg" / "vegetarian" / "pure veg" -> "veg"
            - "non-veg" / "nonveg" -> "nonveg"
            - "parking" -> "parking"
            - "laundry" -> "laundry"
            - "food included" / "meals" -> "food"

            RATING:
            - "highly rated" / "best" / "top rated" -> minRating=4.0
            - "decent" / "good" -> leave minRating null (subjective)

            RETURN STRICT JSON ONLY (no prose, no markdown fences, no commentary):
            {
              "keyword": "string or null",
              "city": "Chennai or null",
              "area": "string or null",
              "maxPrice": number or null,
              "minRating": number or null,
              "features": ["lowercase", "tokens"]
            }
            """;

    // --- PASS 2: REVIEW SUMMARIZATION, RANKING & TOP PICK ---
    private static final String SUMMARY_SYSTEM_PROMPT = """
            You are the ConnectEase Chennai local guide — warm, concise, brutally honest.

            INPUT: a JSON object with the user's original query and a list of services that ALREADY MATCHED the user's filters (price, area, features). The database has done the filtering. Every service in your input is a valid answer — DO NOT apologise or claim "no exact matches".

            Each service includes: sid, name, description, price, category, area, averageRating, reviewCount, full features list, and ALL reviews (with score and text).

            YOUR JOB — return STRICT JSON ONLY (no markdown fences, no prose around it):
            {
              "reply": "<conversational markdown reply, see rules below>",
              "orderedSids": ["sid1", "sid2", ...],
              "topPickSid": "the best sid"
            }

            "reply" RULES:
            1. Open with ONE short sentence answering the user's intent.
            2. Then a line introducing your TOP PICK in **bold**, with ONE crisp reason (e.g. "best balance of price and rating", or "reviewers consistently praise X"). Keep it specific.
            3. Then list each remaining service briefly: **Name** — one positive AND one honest negative drawn from the reviews. If reviews are empty/sparse, write "New listing, limited reviews yet." Do not fabricate positives or negatives.
            4. NO disclaimers like "I couldn't find...", "exact matches", "alternatives nearby". The matches ARE exact.
            5. Total reply under 180 words. Use **bold** for service names. Use line breaks between items. No headings, no JSON inside reply, no emoji.

            "orderedSids" RULES:
            - Rank by: (a) intent fit (features matched, price headroom), (b) averageRating, (c) review sentiment quality, (d) reviewCount as tiebreaker.
            - The topPickSid MUST be the first element.
            - Include every sid you received — no more, no less.
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

            String keyword     = textOrNull(aiDecision, "keyword");
            String city        = textOrNull(aiDecision, "city");
            String area        = textOrNull(aiDecision, "area");
            BigDecimal maxPrice = numberOrNull(aiDecision, "maxPrice");
            Double minRating    = doubleOrNull(aiDecision, "minRating");

            List<String> features = new ArrayList<>();
            if (aiDecision.has("features") && aiDecision.get("features").isArray()) {
                aiDecision.get("features").forEach(f -> {
                    String tok = f.asText();
                    if (tok != null && !tok.isBlank()) features.add(tok.toLowerCase().trim());
                });
            }

            // 2. STRICT SEARCH — no cross-category fallback. If filters yield nothing, we stay honest.
            List<ServiceEntity> entities;
            if (!features.isEmpty()) {
                entities = serviceRepository.advancedSearchByFeaturesForAI(
                        keyword, city, area, maxPrice, minRating, features, PageRequest.of(0, 10));
            } else {
                entities = serviceRepository.advancedSearchForAI(
                        keyword, city, area, maxPrice, minRating, PageRequest.of(0, 10));
            }

            if (entities.isEmpty()) {
                return new AIChatResponse(buildNoMatchHint(keyword, area, city, maxPrice), new ArrayList<>());
            }

            // 3. BUILD STRUCTURED CONTEXT FOR THE SUMMARY PASS (full data, MVP)
            ObjectNode contextNode = objectMapper.createObjectNode();
            contextNode.put("userQuery", userQuery);
            ArrayNode servicesArr = contextNode.putArray("services");
            for (ServiceEntity e : entities) {
                ObjectNode svc = servicesArr.addObject();
                svc.put("sid", e.getSid());
                svc.put("name", e.getName());
                svc.put("description", e.getDescription() == null ? "" : e.getDescription());
                svc.put("price", e.getPrice() == null ? "" : e.getPrice().toPlainString());
                svc.put("category", e.getCategory() == null ? "" : e.getCategory().getName());
                svc.put("city", e.getLocation() == null ? "" : e.getLocation().getCity());
                svc.put("area", e.getLocation() == null ? "" : e.getLocation().getArea());

                ArrayNode featsArr = svc.putArray("features");
                if (e.getFeatures() != null) {
                    for (Feature f : e.getFeatures()) featsArr.add(f.getName());
                }

                List<Rating> ratings = e.getRatings();
                svc.put("averageRating", computeAverage(ratings));
                svc.put("reviewCount", ratings == null ? 0 : ratings.size());

                ArrayNode reviewsArr = svc.putArray("reviews");
                if (ratings != null) {
                    for (Rating r : ratings) {
                        ObjectNode rn = reviewsArr.addObject();
                        rn.put("score", r.getScore() == null ? 0 : r.getScore());
                        rn.put("text", r.getReview() == null ? "" : r.getReview());
                    }
                }
            }

            // 4. SUMMARY + RANKING PASS
            ArrayNode pass2Contents = conversationHistory.deepCopy();
            pass2Contents.add(createUserTurn(objectMapper.writeValueAsString(contextNode)));
            String pass2Json = callGemini(SUMMARY_SYSTEM_PROMPT, pass2Contents, true);
            JsonNode pass2 = objectMapper.readTree(pass2Json);

            String reply = pass2.path("reply").asText("Here are your matches.");
            List<String> orderedSids = new ArrayList<>();
            if (pass2.has("orderedSids") && pass2.get("orderedSids").isArray()) {
                pass2.get("orderedSids").forEach(n -> orderedSids.add(n.asText()));
            }

            // 5. CARD LIST — reorder to match AI ranking
            List<ListingCardDTO> cards = entities.stream().map(this::mapToDTO).collect(Collectors.toList());
            if (!orderedSids.isEmpty()) {
                Map<String, Integer> rank = new HashMap<>();
                for (int i = 0; i < orderedSids.size(); i++) rank.put(orderedSids.get(i), i);
                cards.sort(Comparator.comparingInt(c -> rank.getOrDefault(c.getSid(), Integer.MAX_VALUE)));
            } else {
                // Fallback: rating desc, then review count desc
                cards.sort((a, b) -> {
                    int byRating = Double.compare(
                            b.getAverageRating() == null ? 0 : b.getAverageRating(),
                            a.getAverageRating() == null ? 0 : a.getAverageRating());
                    if (byRating != 0) return byRating;
                    return Integer.compare(
                            b.getRatingCount() == null ? 0 : b.getRatingCount(),
                            a.getRatingCount() == null ? 0 : a.getRatingCount());
                });
            }

            return new AIChatResponse(reply, cards);

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
        List<Rating> ratings = entity.getRatings();
        dto.setAverageRating(computeAverage(ratings));
        dto.setRatingCount(ratings == null ? 0 : ratings.size());
        return dto;
    }

    private double computeAverage(List<Rating> ratings) {
        if (ratings == null || ratings.isEmpty()) return 0.0;
        int sum = 0, cnt = 0;
        for (Rating r : ratings) {
            if (r.getScore() != null) { sum += r.getScore(); cnt++; }
        }
        if (cnt == 0) return 0.0;
        return Math.round(sum * 10.0 / cnt) / 10.0;
    }

    private String textOrNull(JsonNode node, String field) {
        JsonNode v = node.path(field);
        if (v.isNull() || v.isMissingNode()) return null;
        String s = v.asText().trim();
        return s.isEmpty() ? null : s;
    }

    private BigDecimal numberOrNull(JsonNode node, String field) {
        JsonNode v = node.path(field);
        if (v.isNull() || v.isMissingNode() || !v.isNumber()) return null;
        return new BigDecimal(v.asText());
    }

    private Double doubleOrNull(JsonNode node, String field) {
        JsonNode v = node.path(field);
        if (v.isNull() || v.isMissingNode() || !v.isNumber()) return null;
        return v.asDouble();
    }

    private String buildNoMatchHint(String keyword, String area, String city, BigDecimal maxPrice) {
        StringBuilder sb = new StringBuilder("Sorry, I couldn't find ");
        sb.append(keyword == null ? "any matching services" : keyword.toLowerCase());
        if (area != null) sb.append(" in ").append(area);
        else if (city != null) sb.append(" in ").append(city);
        if (maxPrice != null) sb.append(" under ₹").append(maxPrice.toPlainString());
        sb.append(". Try widening your budget or checking a nearby area.");
        return sb.toString();
    }
}
