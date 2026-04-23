package com.cts.connectease.repository;

import com.cts.connectease.model.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, String> {

    @Query("SELECT c FROM ChatSession c WHERE (c.customer.uid = :user1 AND c.vendor.uid = :user2) OR (c.customer.uid = :user2 AND c.vendor.uid = :user1)")
    Optional<ChatSession> findExistingSession(@Param("user1") String user1, @Param("user2") String user2);

    @Query("SELECT c FROM ChatSession c WHERE c.customer.uid = :uid OR c.vendor.uid = :uid ORDER BY c.startedAt DESC")
    List<ChatSession> findAllSessionsForUser(@Param("uid") String uid);
}