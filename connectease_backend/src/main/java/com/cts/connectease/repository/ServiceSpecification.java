package com.cts.connectease.repository;

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
            Double minRating, Double maxRating) { // Added Rating parameters here

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

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}