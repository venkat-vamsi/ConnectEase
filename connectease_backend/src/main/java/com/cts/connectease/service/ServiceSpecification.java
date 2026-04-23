package com.cts.connectease.service;

import com.cts.connectease.model.Rating;
import com.cts.connectease.model.ServiceEntity;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ServiceSpecification {

    public static Specification<ServiceEntity> getFilteredServices(
            String keyword, String categoryId, String city, String area,
            BigDecimal minPrice, BigDecimal maxPrice,
            Double minRating, Double maxRating,
            // Category-specific filters
            String genderType, String foodType, String occupancy, String amenities,
            String cuisineType, String mealType, String dietType, String delivery,
            String serviceType, String urgency, String experience,
            String cleaningType, String frequency, String ecoFriendly,
            String washType, String pickupService, String turnaround) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Keyword search (Name or Description)
            if (keyword != null && !keyword.trim().isEmpty()) {
                String searchPattern = "%" + keyword.toLowerCase() + "%";
                Predicate nameMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), searchPattern);
                Predicate descMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), searchPattern);
                predicates.add(criteriaBuilder.or(nameMatch, descMatch));
            }

            // 2. Category Filter
            if (categoryId != null && !categoryId.trim().isEmpty() && !categoryId.equals("null")) {
                predicates.add(criteriaBuilder.equal(root.get("category").get("cid"), categoryId));
            }

            // 3. Location Filtering
            if (city != null && !city.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("location").get("city"), city));
            }
            if (area != null && !area.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("location").get("area"), area));
            }

            // 4. Rating Subquery Filtering
            if (minRating != null) {
                Subquery<Double> avgRatingSubquery = query.subquery(Double.class);
                Root<Rating> ratingRoot = avgRatingSubquery.from(Rating.class);

                avgRatingSubquery.select(criteriaBuilder.avg(ratingRoot.get("score")))
                        .where(criteriaBuilder.equal(ratingRoot.get("service"), root));

                if (maxRating != null) {
                    predicates.add(criteriaBuilder.between(avgRatingSubquery, minRating, maxRating));
                } else {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(avgRatingSubquery, minRating));
                }
            }

            // 5. Price Filtering
            if (minPrice != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            // 6. Category-Specific Filters (search in features)
            // PG Filters
            addFeatureFilter(predicates, root, criteriaBuilder, genderType);
            addFeatureFilter(predicates, root, criteriaBuilder, foodType);
            addFeatureFilter(predicates, root, criteriaBuilder, occupancy);
            addFeatureFilter(predicates, root, criteriaBuilder, amenities);
            
            // Food Services Filters
            addFeatureFilter(predicates, root, criteriaBuilder, cuisineType);
            addFeatureFilter(predicates, root, criteriaBuilder, mealType);
            addFeatureFilter(predicates, root, criteriaBuilder, dietType);
            addFeatureFilter(predicates, root, criteriaBuilder, delivery);
            
            // Electrician/Plumber Filters
            addFeatureFilter(predicates, root, criteriaBuilder, serviceType);
            addFeatureFilter(predicates, root, criteriaBuilder, urgency);
            addFeatureFilter(predicates, root, criteriaBuilder, experience);
            
            // Cleaner Filters
            addFeatureFilter(predicates, root, criteriaBuilder, cleaningType);
            addFeatureFilter(predicates, root, criteriaBuilder, frequency);
            addFeatureFilter(predicates, root, criteriaBuilder, ecoFriendly);
            
            // Laundry Filters
            addFeatureFilter(predicates, root, criteriaBuilder, washType);
            addFeatureFilter(predicates, root, criteriaBuilder, pickupService);
            addFeatureFilter(predicates, root, criteriaBuilder, turnaround);

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
    
    /**
     * Helper method to add feature filter if the filter value is not null/empty
     * Searches for the filter value in the service's features collection
     */
    private static void addFeatureFilter(
            List<Predicate> predicates,
            Root<ServiceEntity> root,
            jakarta.persistence.criteria.CriteriaBuilder criteriaBuilder,
            String filterValue) {
        
        if (filterValue != null && !filterValue.trim().isEmpty()) {
            // Join with features table and search for matching feature name
            Predicate featureMatch = criteriaBuilder.isMember(
                filterValue,
                root.join("features").get("name")
            );
            predicates.add(featureMatch);
        }
    }
}