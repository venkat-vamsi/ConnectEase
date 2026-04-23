package com.cts.connectease.service;

import com.cts.connectease.dto.ReviewDTO;
import com.cts.connectease.dto.ReviewRequestDTO;
import com.cts.connectease.dto.ServiceDetailsDTO;
import com.cts.connectease.dto.ImageDTO;
import com.cts.connectease.model.Rating;
import com.cts.connectease.model.ServiceEntity;
import com.cts.connectease.model.User;
import com.cts.connectease.repository.ProductRepository;
import com.cts.connectease.repository.RatingRepository;
import com.cts.connectease.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Transactional(readOnly = true)
    public ServiceDetailsDTO getServiceDetails(String sid) {
        ServiceEntity service = productRepository.findById(sid)
                .orElseThrow(() -> new RuntimeException("Service not found"));

        // Calculate Average Rating
        double avgRating = 0.0;
        if (service.getRatings() != null && !service.getRatings().isEmpty()) {
            avgRating = service.getRatings().stream()
                    .filter(r -> r.getScore() != null)
                    .mapToInt(Rating::getScore)
                    .average()
                    .orElse(0.0);
        }

        // Map Reviews to DTO
        List<ReviewDTO> reviewDTOs = new ArrayList<>();
        if (service.getRatings() != null) {
            reviewDTOs = service.getRatings().stream()
                    .map(r -> ReviewDTO.builder()
                            .userName(r.getUser() != null ? r.getUser().getFullName() : "Anonymous")
                            .review(r.getReview())
                            .score(r.getScore() != null ? r.getScore() : 0)
                            .build())
                    .collect(Collectors.toList());
        }

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

        // Map features to list of strings
        List<String> featureNames = new ArrayList<>();
        if (service.getFeatures() != null && !service.getFeatures().isEmpty()) {
            featureNames = service.getFeatures().stream()
                    .map(f -> f.getName())
                    .collect(Collectors.toList());
        }

        // Build location string
        String fullAddress = "";
        if (service.getLocation() != null) {
            String area = service.getLocation().getArea() != null ? service.getLocation().getArea() : "";
            String city = service.getLocation().getCity() != null ? service.getLocation().getCity() : "";
            fullAddress = area + ", " + city;
        }

        return ServiceDetailsDTO.builder()
                .sid(service.getSid())
                .name(service.getName())
                .description(service.getDescription())
                .price(service.getPrice())
                .totalViews(service.getTotalViews())
                .vendorName(service.getVendor() != null ? service.getVendor().getFullName() : "Unknown")
                .vendorEmail(service.getVendor() != null ? service.getVendor().getEmail() : null)
                .vendorPhone(service.getVendor() != null ? service.getVendor().getPhone() : null)
                .averageRating(Math.round(avgRating * 10.0) / 10.0)
                .reviews(reviewDTOs)
                .images(imageDTOs)
                .city(service.getLocation() != null ? service.getLocation().getCity() : null)
                .area(service.getLocation() != null ? service.getLocation().getArea() : null)
                .fullAddress(fullAddress)
                .categoryId(service.getCategory() != null ? service.getCategory().getCid() : null)
                .categoryName(service.getCategory() != null ? service.getCategory().getName() : null)
                .features(featureNames)
                .build();
    }

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public void addReview(String sid, ReviewRequestDTO request) {
        ServiceEntity service = productRepository.findById(sid)
                .orElseThrow(() -> new RuntimeException("Service not found"));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Rating newRating = Rating.builder()
                .service(service)
                .user(user)
                .review(request.getReview())
                .score(request.getScore())
                .build();

        ratingRepository.save(newRating);

        // CRITICAL: Update the bidirectional link so the DTO sees the new rating immediately
        if (service.getRatings() == null) {
            service.setRatings(new ArrayList<>());
        }
        service.getRatings().add(newRating);
    }
}