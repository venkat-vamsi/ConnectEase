package com.cts.connectease.repository;



import com.cts.connectease.model.CommunityPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommunityRepository extends JpaRepository<CommunityPost, String> {
    List<CommunityPost> findByUserUid(String uid);
    List<CommunityPost> findAllByOrderByTimeDesc();
}

