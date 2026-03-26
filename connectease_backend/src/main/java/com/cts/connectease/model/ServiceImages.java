package com.cts.connectease.model;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "Service_Images")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ServiceImages {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "image_id", length = 36, updatable = false, nullable = false)
    private String imageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sid", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE) // Enforces DB level ON DELETE CASCADE
    private ServiceEntity service;

    @Column(nullable = false, length = 500)
    private String url;

    @Column(name = "is_primary")
    private Boolean isPrimary = false;
}