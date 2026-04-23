package com.cts.connectease.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceRange {
    private BigDecimal min;
    private BigDecimal max;
}
