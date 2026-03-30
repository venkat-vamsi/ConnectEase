package com.cts.connectease.repository;

import com.cts.connectease.model.ServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceRepository extends JpaRepository<ServiceEntity, String>, JpaSpecificationExecutor<ServiceEntity> {
}