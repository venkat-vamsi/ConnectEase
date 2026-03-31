package com.cts.connectease.service;


import com.cts.connectease.dto.CommunityPostDTO;
import com.cts.connectease.model.CommunityPost;
import com.cts.connectease.repository.CommunityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommunityService {

    @Autowired
    private CommunityRepository communityRepository;

    public List<CommunityPostDTO> getAllPosts() {
        return communityRepository.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public CommunityPost save(CommunityPost post) {
        return communityRepository.save(post);
    }

    private CommunityPostDTO toDto(CommunityPost post) {
        return CommunityPostDTO.builder()
                .postId(post.getPostId())
                .title(post.getTitle())
                .description(post.getDescription())
                .image(post.getImage())
                .time(post.getTime())
                .authorFullName(post.getUser() != null ? post.getUser().getFullName() : "Unknown")
                .build();
    }
}
