package com.cts.connectease.dto;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryFilterSchema {
    private String categoryId;
    private PriceRange priceRange;
    private List<FilterField> filters;
}
