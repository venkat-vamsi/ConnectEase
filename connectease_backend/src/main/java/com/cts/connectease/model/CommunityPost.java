package com.cts.connectease.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import java.time.LocalDateTime;

@Entity
@Table(name = "Community")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CommunityPost {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "post_id", length = 36, updatable = false, nullable = false)
    private String postId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uid")
    @OnDelete(action = OnDeleteAction.CASCADE) // Matches DB: ON DELETE CASCADE
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 500)
    private String image;

    @Column(length = 100)
    private String category;

    @Column
    private Integer rating;

    @CreationTimestamp
    private LocalDateTime time;
}