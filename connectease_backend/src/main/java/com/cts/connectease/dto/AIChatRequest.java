package com.cts.connectease.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIChatRequest {
    private String query;
    private List<ChatTurnDTO> history;
}
