package com.cts.connectease.service;

import com.cts.connectease.dto.ReviewDTO;
import com.cts.connectease.dto.ServiceDetailsDTO;
import com.cts.connectease.dto.VendorDashboardDTO;
import com.cts.connectease.dto.ImageDTO;
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

    @Autowired
    private ServiceImagesRepository serviceImagesRepository;

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
        System.out.println("Getting dashboard stats for vendorId: " + vendorId);
        User vendor = userRepository.findById(vendorId)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));

        List<ServiceEntity> services = productRepository.findByVendorUid(vendorId);
        System.out.println("Found " + services.size() + " services for vendor " + vendor.getEmail());

        long activeListings = services.size();
        long totalViews = services.stream()
                .mapToLong(s -> s.getTotalViews() != null ? s.getTotalViews() : 0L)
                .sum();

        long totalReviews = services.stream()
                .flatMap(s -> s.getRatings() != null ? s.getRatings().stream() : java.util.stream.Stream.empty())
                .count();

        double avgRating = services.stream()
                .flatMap(s -> s.getRatings() != null ? s.getRatings().stream() : java.util.stream.Stream.empty())
                .mapToInt(Rating::getScore)
                .average()
                .orElse(0.0);

        // Map services to DTOs for display
        List<ServiceDetailsDTO> serviceDTOs = services.stream()
                .map(this::mapToServiceDetailsDTO)
                .collect(Collectors.toList());

        return VendorDashboardDTO.builder()
                .vendorName(vendor.getFullName())
                .activeListings(activeListings)
                .totalViews(totalViews)
                .totalReviews(totalReviews)
                .averageRating(Math.round(avgRating * 10.0) / 10.0)
                .services(serviceDTOs)
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

        // Map service images to ImageDTO list
        List<ImageDTO> imageDTOs = new ArrayList<>();
        if (service.getImages() != null && !service.getImages().isEmpty()) {
            imageDTOs = service.getImages().stream()
                    .map(img -> ImageDTO.builder()
                            .url(img.getUrl())
                            .isPrimary(img.getIsPrimary() != null ? img.getIsPrimary() : Boolean.FALSE)
                            .build())
                    .collect(Collectors.toList());
        }

        return ServiceDetailsDTO.builder()
                .sid(service.getSid())
                .name(service.getName())
                .description(service.getDescription())
                .price(service.getPrice())
                .totalViews(service.getTotalViews())
                .vendorName(service.getVendor().getFullName())
                .categoryName(service.getCategory() != null ? service.getCategory().getName() : "")
                .averageRating(Math.round(avgRating * 10.0) / 10.0)
                .reviews(reviewDTOs)
                .images(imageDTOs)
                .build();
    }

    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public String getDefaultImageForCategory(String categoryId) {
        // Query default images from Service_Images table where service is null
        List<ServiceImages> defaultImages = serviceImagesRepository.findByServiceIsNull();
        // Map categoryId to image_id pattern
        String imageIdPrefix = switch (categoryId) {
            case "cat-01" -> "img-cleaning";
            case "cat-02" -> "img-plumbing";
            case "cat-03" -> "img-electrical";
            case "cat-04" -> "img-carpentry";
            case "cat-05" -> "img-painting";
            case "cat-06" -> "img-home-repair";
            default -> "img-cleaning";
        };
        return defaultImages.stream()
                .filter(img -> imageIdPrefix.equals(img.getImageId()))
                .findFirst()
                .map(ServiceImages::getUrl)
                .orElse("https://picsum.photos/id/100/400/300");
    }
}