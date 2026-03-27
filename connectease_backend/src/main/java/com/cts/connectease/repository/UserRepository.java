// src/main/java/com/cts/connectease/repository/UserRepository.java
package com.cts.connectease.repository;

import com.cts.connectease.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    // You will need this for your Login and Registration APIs!
    // Spring Boot will automatically write: SELECT * FROM Users WHERE email = ?
    Optional<User> findByEmail(String email);

    // Checks if an email is already taken during registration
    boolean existsByEmail(String email);
}