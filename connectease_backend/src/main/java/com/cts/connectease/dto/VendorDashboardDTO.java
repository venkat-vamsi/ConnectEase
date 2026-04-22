package com.cts.connectease.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class VendorDashboardDTO {
    private String vendorName;
    private long activeListings;
    private long totalViews;
    private long totalReviews;
    private double averageRating;
    private List<ServiceDetailsDTO> services; // Added to include all vendor services
}
