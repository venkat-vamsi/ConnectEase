package com.cts.connectease.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "Features")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Feature {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36, updatable = false, nullable = false)
    private String fid;

    @Column(nullable = false, length = 100)
    private String name;
}