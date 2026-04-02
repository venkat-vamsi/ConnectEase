package com.cts.connectease.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageDTO {
    private String url;
    private Boolean isPrimary;
}
