package com.cts.connectease.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Categories")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36, updatable = false, nullable = false)
    private String cid;

    @Column(nullable = false, length = 100)
    private String name;
}