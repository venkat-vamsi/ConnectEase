package com.cts.connectease.service;

import com.cts.connectease.dto.ListingCardDTO;
import com.cts.connectease.model.ServiceEntity;
import com.cts.connectease.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class ListingService {

    @Autowired
    private ServiceRepository serviceRepository;

    // Use ONLY this method with BigDecimal
    @Transactional(readOnly = true)
    public Page<ListingCardDTO> getFilteredServices(
            String keyword, String categoryId, String city, String area,
            BigDecimal minPrice, BigDecimal maxPrice,Double minRating, Double maxRating, 
            String sortType, int page, int size,
            // Category-specific filters
            String genderType, String foodType, String occupancy, String amenities,
            String cuisineType, String mealType, String dietType, String delivery,
            String serviceType, String urgency, String experience,
            String cleaningType, String frequency, String ecoFriendly,
            String washType, String pickupService, String turnaround) {

        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");

        if ("price_asc".equalsIgnoreCase(sortType)) {
            sort = Sort.by(Sort.Direction.ASC, "price");
        } else if ("price_desc".equalsIgnoreCase(sortType)) {
            sort = Sort.by(Sort.Direction.DESC, "price");
        }

        Pageable pageable = PageRequest.of(page, size, sort);

        // Build Specification
        Specification<ServiceEntity> spec = ServiceSpecification.getFilteredServices(
                keyword, categoryId, city, area, minPrice, maxPrice, minRating, maxRating,
                genderType, foodType, occupancy, amenities,
                cuisineType, mealType, dietType, delivery,
                serviceType, urgency, experience,
                cleaningType, frequency, ecoFriendly,
                washType, pickupService, turnaround);

        return serviceRepository.findAll(spec, pageable).map(this::mapToDTO);
    }

    private ListingCardDTO mapToDTO(ServiceEntity entity) {
        ListingCardDTO dto = new ListingCardDTO();
        dto.setSid(entity.getSid());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());

        // This will now work because both are BigDecimal
        dto.setPrice(entity.getPrice());

        if (entity.getCategory() != null) {
            dto.setCategoryName(entity.getCategory().getName());
        }
        if (entity.getLocation() != null) {
            dto.setCity(entity.getLocation().getCity());
            dto.setArea(entity.getLocation().getArea());
        }
        // Determine primary image URL: prefer image with isPrimary==true, else first image, else null
        String primaryUrl = null;
        if (entity.getImages() != null && !entity.getImages().isEmpty()) {
            primaryUrl = entity.getImages().stream()
                    .filter(img -> img.getIsPrimary() != null && img.getIsPrimary())
                    .map(img -> img.getUrl())
                    .findFirst()
                    .orElse(entity.getImages().get(0).getUrl());
        }
        dto.setPrimaryImageUrl(primaryUrl);
        return dto;
    }
}