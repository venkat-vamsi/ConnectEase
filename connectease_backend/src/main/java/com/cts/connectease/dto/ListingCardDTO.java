package com.cts.connectease.dto;

import lombok.Data;
import java.math.BigDecimal; // Ensure this is imported

@Data
public class ListingCardDTO {
    private String sid;
    private String name;
    private String description;
    private BigDecimal price; // Change this from Double to BigDecimal
    private String categoryName;
    private String city;
    private String area;
    private String primaryImageUrl;
    private Double averageRating;
}