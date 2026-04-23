package com.cts.connectease.repository;

import com.cts.connectease.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LocationRepository extends JpaRepository<Location, String> {
    
    @Query("SELECT DISTINCT l.city FROM Location l WHERE l.city IS NOT NULL ORDER BY l.city")
    List<String> findAllDistinctCities();
    
    @Query("SELECT DISTINCT l.area FROM Location l WHERE l.area IS NOT NULL AND l.city = :city ORDER BY l.area")
    List<String> findAreasByCity(String city);
}
