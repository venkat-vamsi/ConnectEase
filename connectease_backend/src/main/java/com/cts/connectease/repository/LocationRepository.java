package com.cts.connectease.repository;

import com.cts.connectease.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocationRepository extends JpaRepository<Location, String> {

    @Query("SELECT DISTINCT l.city FROM Location l WHERE l.city IS NOT NULL ORDER BY l.city")
    List<String> findDistinctCities();

    @Query("SELECT DISTINCT l.area FROM Location l WHERE l.city = :city AND l.area IS NOT NULL ORDER BY l.area")
    List<String> findDistinctAreasByCity(@Param("city") String city);
}
