package com.cts.connectease.repository;

import com.cts.connectease.model.ServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<ServiceEntity, String> {
    @Query("SELECT s FROM ServiceEntity s WHERE s.vendor.uid = :vendorId")
    List<ServiceEntity> findByVendorUid(@Param("vendorId") String vendorId);
}