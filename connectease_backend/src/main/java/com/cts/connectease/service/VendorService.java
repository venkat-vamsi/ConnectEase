package com.cts.connectease.service;

import com.cts.connectease.dto.ReviewDTO;
import com.cts.connectease.dto.ServiceDetailsDTO;
import com.cts.connectease.dto.VendorDashboardDTO;
import com.cts.connectease.model.*;
import com.cts.connectease.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class VendorService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private FeatureRepository featureRepository;

    @Transactional
    public ServiceDetailsDTO createNewService(String vendorId, ServiceEntity servicePayload) {

        // 1. LINK VENDOR
        User vendor = userRepository.findById(vendorId)
                .orElseThrow(() -> new RuntimeException("Vendor not found: " + vendorId));
        servicePayload.setVendor(vendor);

        // 2. LINK CATEGORY (Existing - from Dropdown)
        if (servicePayload.getCategory() != null && servicePayload.getCategory().getCid() != null) {
            Category category = categoryRepository.findById(servicePayload.getCategory().getCid())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            servicePayload.setCategory(category);
        }

        // 3. CREATE LOCATION (New)
        if (servicePayload.getLocation() != null) {
            Location savedLocation = locationRepository.save(servicePayload.getLocation());
            servicePayload.setLocation(savedLocation);
        }

        // 4. HANDLE FEATURES (Find or Create by Name)
        if (servicePayload.getFeatures() != null && !servicePayload.getFeatures().isEmpty()) {
            List<Feature> processedFeatures = new ArrayList<>();
            for (Feature featureReq : servicePayload.getFeatures()) {
                Feature featureToLink = featureRepository.findByNameIgnoreCase(featureReq.getName())
                        .orElseGet(() -> {
                            Feature newFeature = Feature.builder()
                                    .name(featureReq.getName())
                                    .build();
                            return featureRepository.save(newFeature);
                        });
                processedFeatures.add(featureToLink);
            }
            servicePayload.setFeatures(processedFeatures);
        }

        // 5. LINK IMAGES (Bidirectional)
        if (servicePayload.getImages() != null) {
            for (ServiceImages image : servicePayload.getImages()) {
                image.setService(servicePayload);
            }
        }

        // 6. SAVE EVERYTHING
        servicePayload.setTotalViews(0L);
        ServiceEntity savedService = productRepository.save(servicePayload);

        // 7. RETURN DTO (Use the helper method to include all fields)
        return mapToServiceDetailsDTO(savedService);
    }

    @Transactional(readOnly = true)
    public VendorDashboardDTO getVendorDashboardStats(String vendorId) {
        User vendor = userRepository.findById(vendorId)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));

        List<ServiceEntity> services = productRepository.findByVendorUid(vendorId);

        long activeListings = services.size();
        long totalViews = services.stream()
                .mapToLong(s -> s.getTotalViews() != null ? s.getTotalViews() : 0L)
                .sum();

        double avgRating = services.stream()
                .flatMap(s -> s.getRatings().stream())
                .mapToInt(Rating::getScore)
                .average()
                .orElse(0.0);

        return VendorDashboardDTO.builder()
                .vendorName(vendor.getFullName())
                .activeListings(activeListings)
                .totalViews(totalViews)
                .averageRating(Math.round(avgRating * 10.0) / 10.0)
                .build();
    }

    // Helper method to map Entity to DTO with Ratings and Reviews
    private ServiceDetailsDTO mapToServiceDetailsDTO(ServiceEntity service) {
        // Calculate Average Rating
        double avgRating = service.getRatings() != null ?
                service.getRatings().stream()
                        .mapToInt(Rating::getScore)
                        .average()
                        .orElse(0.0) : 0.0;

        // Map Reviews (User info comes from the 'uid' relationship in the ratings table)
        List<ReviewDTO> reviewDTOs = service.getRatings() != null ?
                service.getRatings().stream()
                        .map(rating -> ReviewDTO.builder()
                                .userName(rating.getUser().getFullName()) // from uid column link
                                .review(rating.getReview())
                                .score(rating.getScore())
                                .build())
                        .collect(Collectors.toList()) : new ArrayList<>();

        return ServiceDetailsDTO.builder()
                .sid(service.getSid())
                .name(service.getName())
                .description(service.getDescription())
                .price(service.getPrice())
                .totalViews(service.getTotalViews())
                .vendorName(service.getVendor().getFullName())
                .averageRating(Math.round(avgRating * 10.0) / 10.0)
                .reviews(reviewDTOs)
                .build();
    }
}