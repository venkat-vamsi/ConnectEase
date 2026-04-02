package com.cts.connectease.controller;



import com.cts.connectease.dto.CommunityPostDTO;
import com.cts.connectease.model.CommunityPost;
import com.cts.connectease.service.CommunityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/community")
public class CommunityController {

    @Autowired
    private CommunityService communityService;

    @Autowired
    private com.cts.connectease.repository.UserRepository userRepository;

    @GetMapping
    public List<CommunityPostDTO> getCommunityFeed() {
        return communityService.getAllPosts();
    }

    @PostMapping
    public CommunityPostDTO createPost(@RequestBody CommunityPost post, Authentication authentication) {
        post.setTime(LocalDateTime.now());

        String currentUserId = authentication != null && authentication.getCredentials() != null
                ? authentication.getCredentials().toString()
                : null;

        if (currentUserId != null) {
            com.cts.connectease.model.User user = userRepository.findById(currentUserId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            post.setUser(user);
        }

        CommunityPost saved = communityService.save(post);
        return communityService.getAllPosts().stream()
                .filter(p -> p.getPostId().equals(saved.getPostId()))
                .findFirst()
                .orElse(null);
    }
}
