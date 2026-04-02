package com.cts.connectease.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;
import com.cts.connectease.dto.ImageDTO;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceDetailsDTO {
    private String sid;
    private String name;
    private String description;
    private BigDecimal price;
    private Long totalViews;
    private String vendorName;
    private double averageRating; // Added
    private List<ReviewDTO> reviews; // Added
    private List<ImageDTO> images;
}