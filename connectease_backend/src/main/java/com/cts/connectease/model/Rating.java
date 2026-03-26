package com.cts.connectease.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import java.time.LocalDateTime;

@Entity
@Table(name = "Ratings")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36, updatable = false, nullable = false)
    private String rid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sid")
    @OnDelete(action = OnDeleteAction.CASCADE) // Matches DB: ON DELETE CASCADE
    private ServiceEntity service;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uid") // Implicitly behaves as ON DELETE SET NULL because no cascade=REMOVE is defined
    private User user;

    private Integer score;

    @Column(columnDefinition = "TEXT")
    private String review;

    @CreationTimestamp
    private LocalDateTime time;
}