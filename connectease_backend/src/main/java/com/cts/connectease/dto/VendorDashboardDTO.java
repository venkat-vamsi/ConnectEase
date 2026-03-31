package com.cts.connectease.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VendorDashboardDTO {
    private String vendorName;
    private long activeListings;
    private long totalViews;
    private long totalReviews;
    private double averageRating; // Added this
}
