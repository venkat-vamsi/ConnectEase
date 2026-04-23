package com.cts.connectease.controller;

import com.cts.connectease.dto.ListingCardDTO;
import com.cts.connectease.service.ListingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/listings")
@CrossOrigin(origins = "*") // Allow requests from your React frontend
public class ListingController {

    @Autowired
    private ListingService listingService;

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
            @RequestParam(defaultValue = "newest") String sortType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<ListingCardDTO> results = listingService.getFilteredServices(
                keyword, categoryId, city, area, minPrice, maxPrice, minRating, maxRating, sortType, page, size);

        return ResponseEntity.ok(results);
    }
}