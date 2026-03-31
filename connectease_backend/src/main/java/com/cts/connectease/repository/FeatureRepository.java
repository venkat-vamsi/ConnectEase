package com.cts.connectease.repository;

import com.cts.connectease.model.Feature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface FeatureRepository extends JpaRepository<Feature, String> {
    // Crucial for finding existing features by name
    Optional<Feature> findByNameIgnoreCase(String name);
}