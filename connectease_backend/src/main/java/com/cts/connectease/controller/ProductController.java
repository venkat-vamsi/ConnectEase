package com.cts.connectease.controller;

import com.cts.connectease.dto.ListingCardDTO;
import com.cts.connectease.dto.ReviewRequestDTO;
import com.cts.connectease.dto.ServiceDetailsDTO;
import com.cts.connectease.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.List;

@RestController
@RequestMapping("/api/services")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public ResponseEntity<Page<ListingCardDTO>> getAllServices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(productService.getAllServices(page, size));
    }

    @GetMapping("/{sid}")
    public ServiceDetailsDTO getService(@PathVariable String sid) {
        // This now receives the clean DTO from the service
        return productService.getServiceDetails(sid);
    }

    @GetMapping("/vendor/{vendorId}")
    public ResponseEntity<List<ListingCardDTO>> getServicesByVendor(@PathVariable String vendorId) {
        return ResponseEntity.ok(productService.getServicesByVendor(vendorId));
    }

    @PostMapping("/{sid}/reviews")
    public String addReview(@PathVariable String sid, @RequestBody ReviewRequestDTO reviewRequest, Authentication authentication) {
        String currentUserId = authentication != null && authentication.getCredentials() != null
                ? authentication.getCredentials().toString()
                : null;

        // Ensure the review uses the authenticated user id, ignoring any client-provided value
        reviewRequest.setUserId(currentUserId);

        productService.addReview(sid, reviewRequest);
        return "Review added successfully!";
    }
}
