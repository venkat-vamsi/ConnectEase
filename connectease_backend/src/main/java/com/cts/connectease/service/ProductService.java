package com.cts.connectease.service;

import com.cts.connectease.dto.ImageDTO;
import com.cts.connectease.dto.ListingCardDTO;
import com.cts.connectease.dto.ReviewDTO;
import com.cts.connectease.dto.ReviewRequestDTO;
import com.cts.connectease.dto.ServiceDetailsDTO;
import com.cts.connectease.model.Rating;
import com.cts.connectease.model.ServiceEntity;
import com.cts.connectease.model.User;
import com.cts.connectease.repository.ProductRepository;
import com.cts.connectease.repository.RatingRepository;
import com.cts.connectease.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private UserRepository userRepository;

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
                            // If you added profileImage to ReviewDTO, map it here:
                            // .profileImage(r.getUser() != null ? r.getUser().getImage() : null)
                            .review(r.getReview())
                            .score(r.getScore() != null ? r.getScore() : 0)
                            .build())
                    .collect(Collectors.toList());
        }

        // Map service images to ImageDTO list for the details page
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
                .vendorName(service.getVendor() != null ? service.getVendor().getFullName() : "Unknown")
                .averageRating(Math.round(avgRating * 10.0) / 10.0)
                .reviews(reviewDTOs)
                .images(imageDTOs)
                .build();
    }

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

    @Transactional(readOnly = true)
    public Page<ListingCardDTO> getAllServices(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return productRepository.findAll(pageable).map(this::mapToListingCardDTO);
    }

    @Transactional(readOnly = true)
    public List<ListingCardDTO> getServicesByVendor(String vendorId) {
        return productRepository.findByVendorUid(vendorId)
                .stream()
                .map(this::mapToListingCardDTO)
                .collect(Collectors.toList());
    }

    private ListingCardDTO mapToListingCardDTO(ServiceEntity entity) {
        ListingCardDTO dto = new ListingCardDTO();
        dto.setSid(entity.getSid());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setPrice(entity.getPrice());
        if (entity.getCategory() != null) dto.setCategoryName(entity.getCategory().getName());
        if (entity.getLocation() != null) {
            dto.setCity(entity.getLocation().getCity());
            dto.setArea(entity.getLocation().getArea());
        }
        if (entity.getImages() != null && !entity.getImages().isEmpty()) {
            String primaryUrl = entity.getImages().stream()
                    .filter(img -> img.getIsPrimary() != null && img.getIsPrimary())
                    .map(img -> img.getUrl())
                    .findFirst()
                    .orElse(entity.getImages().get(0).getUrl());
            dto.setPrimaryImageUrl(primaryUrl);
        }
        return dto;
    }
}
