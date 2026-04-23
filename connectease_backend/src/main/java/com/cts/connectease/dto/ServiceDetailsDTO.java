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
    private double averageRating;
    private List<ReviewDTO> reviews;
    private List<ImageDTO> images;
    
    // Location details
    private String city;
    private String area;
    private String fullAddress;
    
    // Category details
    private String categoryId;
    private String categoryName;
    
    // Features
    private List<String> features;
    
    // Vendor contact info
    private String vendorEmail;
    private String vendorPhone;
}