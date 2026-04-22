package com.cts.connectease.controller;

import com.cts.connectease.dto.ReviewDTO;
import com.cts.connectease.service.RatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ratings")
public class RatingController {

    @Autowired
    private RatingService ratingService;

    @GetMapping("/service/{sid}")
    public ResponseEntity<Page<ReviewDTO>> getServiceReviews(
            @PathVariable String sid,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ratingService.getServiceReviews(sid, page, size));
    }

    @DeleteMapping("/{rid}")
    public ResponseEntity<Map<String, String>> deleteReview(
            @PathVariable String rid,
            Authentication authentication) {
        String currentUserId = authentication.getCredentials().toString();
        ratingService.deleteReview(rid, currentUserId);
        return ResponseEntity.ok(Map.of("status", "success", "message", "Review deleted successfully"));
    }
}
