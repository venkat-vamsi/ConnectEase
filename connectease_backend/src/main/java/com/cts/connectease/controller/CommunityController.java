package com.cts.connectease.controller;



import com.cts.connectease.dto.CommunityPostDTO;
import com.cts.connectease.model.CommunityPost;
import com.cts.connectease.service.CommunityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/community")
public class CommunityController {

    @Autowired
    private CommunityService communityService;

    @GetMapping
    public List<CommunityPostDTO> getCommunityFeed() {
        return communityService.getAllPosts();
    }

    @PostMapping
    public CommunityPostDTO createPost(@RequestBody CommunityPost post) {
        post.setTime(LocalDateTime.now());
        CommunityPost saved = communityService.save(post);
        return communityService.getAllPosts().stream()
                .filter(p -> p.getPostId().equals(saved.getPostId()))
                .findFirst()
                .orElse(null);
    }
}
