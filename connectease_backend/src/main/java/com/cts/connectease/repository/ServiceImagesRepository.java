package com.cts.connectease.repository;

import com.cts.connectease.model.ServiceImages;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ServiceImagesRepository extends JpaRepository<ServiceImages, String> {
    List<ServiceImages> findByServiceIsNull();
}