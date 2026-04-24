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
            String categoryId, String city, String area,
            BigDecimal minPrice, BigDecimal maxPrice,Double minRating, Double maxRating, String sortType, int page, int size) {

        Sort sort = Sort.by(
                new Sort.Order(Sort.Direction.DESC, "averageRating").nullsLast(),
                Sort.Order.asc("name"));

        if ("price_asc".equalsIgnoreCase(sortType)) {
            sort = Sort.by(Sort.Direction.ASC, "price");
        } else if ("price_desc".equalsIgnoreCase(sortType)) {
            sort = Sort.by(Sort.Direction.DESC, "price");
        } else if ("newest".equalsIgnoreCase(sortType)) {
            sort = Sort.by(Sort.Direction.DESC, "createdAt");
        }

        Pageable pageable = PageRequest.of(page, size, sort);

        // Build Specification (Ensure ServiceSpecification also uses BigDecimal)
        // Inside ListingService.java
        Specification<ServiceEntity> spec = ServiceSpecification.getFilteredServices(
                null, categoryId, city, area, minPrice, maxPrice, minRating, maxRating);

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
        dto.setAverageRating(entity.getAverageRating());
        return dto;
    }
}