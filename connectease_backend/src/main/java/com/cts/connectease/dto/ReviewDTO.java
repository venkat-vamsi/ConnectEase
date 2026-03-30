package com.cts.connectease.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewDTO {
    private String userName;
    private String profileImage;
    private String review;
    private int score;
}