package com.cts.connectease.repository;

import com.cts.connectease.model.ServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<ServiceEntity, String> {
    List<ServiceEntity> findByVendorUid(String uid);
}