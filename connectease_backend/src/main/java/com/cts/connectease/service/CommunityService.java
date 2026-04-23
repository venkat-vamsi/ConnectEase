package com.cts.connectease.service;

import com.cts.connectease.dto.CommunityPostDTO;
import com.cts.connectease.model.CommunityPost;
import com.cts.connectease.repository.CommunityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CommunityService {

    @Autowired
    private CommunityRepository communityRepository;

    @Transactional(readOnly = true)
    public List<CommunityPostDTO> getAllPosts() {
        return communityRepository.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public CommunityPost save(CommunityPost post) {
        return communityRepository.save(post);
    }

    @Transactional
    public CommunityPostDTO updatePost(String postId, CommunityPost updatePayload, String currentUserId) {
        CommunityPost existing = communityRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        if (existing.getUser() == null || !existing.getUser().getUid().equals(currentUserId)) {
            throw new RuntimeException("Unauthorized: you can only update your own posts");
        }
        if (updatePayload.getTitle() != null) existing.setTitle(updatePayload.getTitle());
        if (updatePayload.getDescription() != null) existing.setDescription(updatePayload.getDescription());
        if (updatePayload.getImage() != null) existing.setImage(updatePayload.getImage());
        if (updatePayload.getCategory() != null) existing.setCategory(updatePayload.getCategory());
        return toDto(communityRepository.save(existing));
    }

    @Transactional
    public void deletePost(String postId, String currentUserId) {
        CommunityPost existing = communityRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        if (existing.getUser() == null || !existing.getUser().getUid().equals(currentUserId)) {
            throw new RuntimeException("Unauthorized: you can only delete your own posts");
        }
        communityRepository.deleteById(postId);
    }

    @Transactional(readOnly = true)
    public List<CommunityPostDTO> getPostsByUser(String uid) {
        return communityRepository.findByUserUid(uid)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private CommunityPostDTO toDto(CommunityPost post) {
        return CommunityPostDTO.builder()
                .postId(post.getPostId())
                .title(post.getTitle())
                .description(post.getDescription())
                .image(post.getImage())
                .category(post.getCategory())
                .time(post.getTime())
                .authorFullName(post.getUser() != null ? post.getUser().getFullName() : "Unknown")
                .build();
    }
}
