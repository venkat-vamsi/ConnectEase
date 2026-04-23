package com.cts.connectease.controller;

import com.cts.connectease.config.CategoryFilterConfig;
import com.cts.connectease.dto.CategoryFilterSchema;
import com.cts.connectease.dto.ListingCardDTO;
import com.cts.connectease.service.ListingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/listings")
@CrossOrigin(origins = "*") // Allow requests from your React frontend
public class ListingController {

    @Autowired
    private ListingService listingService;
    
    @Autowired
    private CategoryFilterConfig categoryFilterConfig;

    @GetMapping("/filter")
    public ResponseEntity<Page<ListingCardDTO>> filterServices(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String area,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) Double maxRating,
            // Category-specific filters
            @RequestParam(required = false) String genderType,
            @RequestParam(required = false) String foodType,
            @RequestParam(required = false) String occupancy,
            @RequestParam(required = false) String amenities,
            @RequestParam(required = false) String cuisineType,
            @RequestParam(required = false) String mealType,
            @RequestParam(required = false) String dietType,
            @RequestParam(required = false) String delivery,
            @RequestParam(required = false) String serviceType,
            @RequestParam(required = false) String urgency,
            @RequestParam(required = false) String experience,
            @RequestParam(required = false) String cleaningType,
            @RequestParam(required = false) String frequency,
            @RequestParam(required = false) String ecoFriendly,
            @RequestParam(required = false) String washType,
            @RequestParam(required = false) String pickupService,
            @RequestParam(required = false) String turnaround,
            @RequestParam(defaultValue = "newest") String sortType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<ListingCardDTO> results = listingService.getFilteredServices(
                keyword, categoryId, city, area, minPrice, maxPrice, minRating, maxRating, 
                sortType, page, size,
                genderType, foodType, occupancy, amenities,
                cuisineType, mealType, dietType, delivery,
                serviceType, urgency, experience,
                cleaningType, frequency, ecoFriendly,
                washType, pickupService, turnaround);

        return ResponseEntity.ok(results);
    }
    
    @GetMapping("/category-filters/{categoryId}")
    public ResponseEntity<CategoryFilterSchema> getCategoryFilters(@PathVariable String categoryId) {
        CategoryFilterSchema schema = categoryFilterConfig.getSchemaByCategoryId(categoryId);
        if (schema != null) {
            return ResponseEntity.ok(schema);
        }
        return ResponseEntity.notFound().build();
    }
    
    @GetMapping("/category-filters")
    public ResponseEntity<Map<String, CategoryFilterSchema>> getAllCategoryFilters() {
        return ResponseEntity.ok(categoryFilterConfig.getAllSchemas());
    }
}