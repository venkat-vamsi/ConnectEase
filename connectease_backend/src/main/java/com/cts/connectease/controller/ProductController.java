package com.cts.connectease.controller;

import com.cts.connectease.dto.ReviewRequestDTO;
import com.cts.connectease.dto.ServiceDetailsDTO; // Import the DTO
import com.cts.connectease.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/services")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/{sid}")
    public ServiceDetailsDTO getService(@PathVariable String sid) {
        // This now receives the clean DTO from the service
        return productService.getServiceDetails(sid);
    }

    @PostMapping("/{sid}/reviews")
    public String addReview(@PathVariable String sid, @RequestBody ReviewRequestDTO reviewRequest) {
        productService.addReview(sid, reviewRequest);
        return "Review added successfully!";
    }
}