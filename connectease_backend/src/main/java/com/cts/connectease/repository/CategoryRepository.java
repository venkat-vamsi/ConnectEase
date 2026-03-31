package com.cts.connectease.repository;

import com.cts.connectease.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, String> {
    // You can add a custom finder if you need to search by name later
    // Category findByName(String name);
}