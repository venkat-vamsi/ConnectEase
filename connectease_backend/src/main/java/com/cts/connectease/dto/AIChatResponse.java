package com.cts.connectease.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AIChatResponse {
    private String aiMessage;           // The conversational reply ("Here are the top PGs...")
    private List<ListingCardDTO> cards; // The structured data to render the UI cards
}