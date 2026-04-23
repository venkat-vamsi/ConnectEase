package com.cts.connectease.repository;

import com.cts.connectease.model.Rating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RatingRepository extends JpaRepository<Rating, String> {
    Page<Rating> findByServiceSid(String sid, Pageable pageable);
}
