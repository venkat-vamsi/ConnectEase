package com.cts.connectease.dto;

import lombok.Data;

@Data
public class ReviewRequestDTO {
    private String userId;
    private String review;
    private int score;
}