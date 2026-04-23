package com.cts.connectease.dto;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String fullName;
    private String phoneNo;
    private String image;
}
