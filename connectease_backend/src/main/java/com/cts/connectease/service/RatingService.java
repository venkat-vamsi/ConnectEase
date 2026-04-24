package com.cts.connectease.service;

import com.cts.connectease.dto.ReviewDTO;
import com.cts.connectease.model.Rating;
import com.cts.connectease.model.ServiceEntity;
import com.cts.connectease.repository.ProductRepository;
import com.cts.connectease.repository.RatingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RatingService {

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private ProductRepository productRepository;

    @Transactional(readOnly = true)
    public Page<ReviewDTO> getServiceReviews(String sid, int page, int size) {
        productRepository.findById(sid)
                .orElseThrow(() -> new RuntimeException("Service not found"));
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "time"));
        return ratingRepository.findByServiceSid(sid, pageable)
                .map(r -> ReviewDTO.builder()
                        .userName(r.getUser() != null ? r.getUser().getFullName() : "Anonymous")
                        .profileImage(r.getUser() != null ? r.getUser().getImage() : null)
                        .review(r.getReview())
                        .score(r.getScore() != null ? r.getScore() : 0)
                        .build());
    }

    @Transactional
    public void deleteReview(String rid, String currentUserId) {
        Rating rating = ratingRepository.findById(rid)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        if (rating.getUser() == null || !rating.getUser().getUid().equals(currentUserId)) {
            throw new RuntimeException("Unauthorized: you can only delete your own reviews");
        }
        ServiceEntity service = rating.getService();
        ratingRepository.deleteById(rid);

        if (service != null) {
            List<Rating> remaining = ratingRepository.findByServiceSid(service.getSid());
            double avg = remaining.stream()
                    .filter(r -> r.getScore() != null)
                    .mapToInt(Rating::getScore)
                    .average().orElse(0.0);
            service.setAverageRating(remaining.isEmpty() ? null : Math.round(avg * 10.0) / 10.0);
            productRepository.save(service);
        }
    }
}
