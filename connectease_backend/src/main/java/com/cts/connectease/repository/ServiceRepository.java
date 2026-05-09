package com.cts.connectease.repository;

import com.cts.connectease.model.ServiceEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<ServiceEntity, String>, JpaSpecificationExecutor<ServiceEntity> {

    // Your existing fallback query
    @Query("SELECT s FROM ServiceEntity s LEFT JOIN s.ratings r " +
            "WHERE (:keyword IS NULL OR :keyword = '' OR LOWER(s.category.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:city IS NULL OR :city = '' OR LOWER(s.location.city) LIKE LOWER(CONCAT('%', :city, '%'))) " +
            "AND (:area IS NULL OR :area = '' OR LOWER(s.location.area) LIKE LOWER(CONCAT('%', :area, '%'))) " +
            "AND (:maxPrice IS NULL OR s.price <= :maxPrice) " +
            "GROUP BY s.sid " +
            "HAVING (:minRating IS NULL OR AVG(r.score) >= :minRating) " +
            "ORDER BY AVG(r.score) DESC, s.totalViews DESC")
    List<ServiceEntity> advancedSearchForAI(
            @Param("keyword") String keyword,
            @Param("city") String city,
            @Param("area") String area,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("minRating") Double minRating,
            Pageable pageable);

    // NEW: Query that searches by specific features and ranks by feature match count
    @Query("SELECT s FROM ServiceEntity s LEFT JOIN s.ratings r JOIN s.features f " +
            "WHERE (:keyword IS NULL OR :keyword = '' OR LOWER(s.category.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:city IS NULL OR :city = '' OR LOWER(s.location.city) LIKE LOWER(CONCAT('%', :city, '%'))) " +
            "AND (:area IS NULL OR :area = '' OR LOWER(s.location.area) LIKE LOWER(CONCAT('%', :area, '%'))) " +
            "AND (:maxPrice IS NULL OR s.price <= :maxPrice) " +
            "AND LOWER(f.name) IN :features " +
            "GROUP BY s.sid " +
            "HAVING (:minRating IS NULL OR AVG(r.score) >= :minRating) " +
            "ORDER BY COUNT(f.fid) DESC, AVG(r.score) DESC, s.totalViews DESC")
    List<ServiceEntity> advancedSearchByFeaturesForAI(
            @Param("keyword") String keyword,
            @Param("city") String city,
            @Param("area") String area,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("minRating") Double minRating,
            @Param("features") List<String> features,
            Pageable pageable);
}