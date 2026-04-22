package com.cts.connectease.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileDTO {
    private String uid;
    private String fullName;
    private String email;
    private String phoneNo;
    private String image;
    private String role;
    private LocalDateTime createdAt;
}
