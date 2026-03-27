package com.cts.connectease.repository;



import com.cts.connectease.model.CommunityPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommunityRepository extends JpaRepository<CommunityPost, String> {
    // Custom query example: find all posts by a given user id
    List<CommunityPost> findByUserUid(String uid);
}

