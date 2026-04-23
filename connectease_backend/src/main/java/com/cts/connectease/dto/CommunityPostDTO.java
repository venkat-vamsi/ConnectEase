package com.cts.connectease.dto;


import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommunityPostDTO {
    private String postId;
    private String title;
    private String description;
    private String image;
    private String category;
    private LocalDateTime time;
    private String authorFullName;
}

