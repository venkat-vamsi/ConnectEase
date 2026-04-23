package com.cts.connectease.dto;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FilterField {
    private String key;
    private String label;
    private String type; // dropdown, checkbox, range
    private List<String> options;
}
